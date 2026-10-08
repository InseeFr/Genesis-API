package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.configuration.MinioConfig;
import fr.insee.genesis.domain.ports.api.FileSystemPort;
import fr.insee.genesis.infrastructure.client.MinioClientBean;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

@ConditionalOnProperty(name = "fr.insee.genesis.minio.enable", havingValue = "true")
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioFileSystemAdapter implements FileSystemPort {

    private final MinioConfig minioConfig;
    private final MinioClientBean minioClientBean;

    @Override
    public void createDirectories(String path) throws IOException {
        //MinIO doesn't work with directories, only files
    }

    @Override
    public void write(String path, byte[] bytes, OpenOption... options) throws IOException {
        boolean replace = false;
        for(OpenOption openOption : options){
            if(openOption.equals(StandardOpenOption.TRUNCATE_EXISTING)){
                replace = true;
                break;
            }
        }

        writeFileOnMinio(
                path.replace("\\","/"),
                new ByteArrayInputStream(bytes),
                replace
        );
    }

    @Override
    public String readAsString(String filePath) throws IOException {
        try (InputStream inputStream = readAsStream(filePath)){
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public InputStream readAsStream(String filePath) throws FileNotFoundException {
        try {
            return minioClientBean.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath.replace("\\", "/"))
                    .build());
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    @Override
    public BasicFileAttributes readAttributes(String filePath, Class<BasicFileAttributes> basicFileAttributesClass) throws IOException {
        try {
            StatObjectResponse statObjectResponse = minioClientBean.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath.replace("\\","/")).build());
            return MinioFileAttributes
                    .builder()
                    .creationTime(FileTime.from(statObjectResponse.lastModified().toInstant()))
                    .lastModifiedTime(FileTime.from(statObjectResponse.lastModified().toInstant()))
                    .isRegularFile(true)
                    .isOther(false)
                    .isDirectory(false)
                    .build();
        } catch (MinioException me){
            log.error(me.toString());
            throw new IOException(me);
        }
    }

    @Override
    public Stream<String> listFiles(String folderPath) throws IOException {
        try {
            ArrayList<String> filePaths = new ArrayList<>();
            Iterable<Result<Item>> results = minioClientBean.listObjects(
                    ListObjectsArgs.builder().bucket(minioConfig.getBucketName())
                            .prefix(folderPath.replace("\\","/"))
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                filePaths.add(result.get().objectName());
            }
            return filePaths.stream();
        } catch (Exception e) {
            log.error(e.toString());
            return Stream.empty();
        }
    }

    @Override
    public boolean exists(String filePath) {
        try {
            minioClientBean.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath.replace("\\","/")).build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    @Override
    public void writeString(String filePath, String line, OpenOption... options) throws IOException {
        write(filePath, line.getBytes(StandardCharsets.UTF_8), options);
    }

    @Override
    public Stream<String> walk(String rootFolderPath) throws IOException {
        return Stream.empty();
    }

    @Override
    public void move(String from, String to, CopyOption... options) throws IOException {
        try (InputStream inputStream = readAsStream(from)){
            write(to, inputStream.readAllBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        }
        deleteIfExists(from);
    }

    @Override
    public Stream<String> find(String rootDir,
                               int maxDepth,
                               BiPredicate<Path, BasicFileAttributes> matcher,
                               FileVisitOption... options) throws IOException {
        ArrayList<String> strings = new ArrayList<>();

        recursiveFind(rootDir, -1, maxDepth, matcher, strings);

        return strings.stream();
    }

    /**
     * Recursive find method
     */
    private void recursiveFind(String dir,
                               int currentDepth,
                               int maxDepth,
                               BiPredicate<Path, BasicFileAttributes> matcher,
                               ArrayList<String> strings
                                ) throws IOException {
        currentDepth++;
        if(currentDepth > maxDepth){
            return;
        }
        for(String filePath : listFiles(dir).toList()){
            if(matcher.test(Path.of(filePath), readAttributes(filePath, BasicFileAttributes.class))){
                strings.add(filePath);
            }
        }
        recursiveFind(dir, currentDepth, maxDepth, matcher, strings);
    }

    @Override
    public void deleteIfExists(String filepath) throws IOException {
        if(!exists(filepath)){
            return;
        }
        try {
            minioClientBean.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filepath.replace("\\","/")).build());
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    //UTILS
    private void writeFileOnMinio(String minioPath, InputStream inputStream, boolean replace) {
        try{
            if(replace || !exists(minioPath)){
                minioClientBean.putObject(
                        PutObjectArgs.builder().bucket(minioConfig.getBucketName()).stream(
                                inputStream,
                                (long) -1,
                                10485760L
                        ).object(minioPath.replace("\\","/")).build());
                return;
            }
        }catch (Exception e) {
            log.error(e.toString());
        }
        try (InputStream alreadyExistingInputStream = readAsStream(minioPath)){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            alreadyExistingInputStream.transferTo(baos);
            inputStream.transferTo(baos);
            inputStream.close();
            InputStream appendedInputStream = new ByteArrayInputStream(baos.toByteArray());
            int size = baos.size();
            baos.close();
            minioClientBean.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .stream(appendedInputStream, (long) size, (long) -1)
                    .object(minioPath.replace("\\","/"))
                    .build());
            appendedInputStream.close();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }
}
