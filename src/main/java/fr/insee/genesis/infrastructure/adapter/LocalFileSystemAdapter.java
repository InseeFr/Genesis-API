package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.ports.api.FileSystemPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

@ConditionalOnProperty(name = "fr.insee.genesis.filesystem", havingValue = "local", matchIfMissing = true)
@Service
@RequiredArgsConstructor
public class LocalFileSystemAdapter implements FileSystemPort {

    @Override
    public void createDirectories(String path) throws IOException {
        Files.createDirectories(Path.of(path));
    }

    @Override
    public void write(String path, byte[] bytes, OpenOption... options) throws IOException {
        Files.write(Path.of(path), bytes, options);
    }

    @Override
    public String readAsString(String filePath) throws IOException {
        return Files.readString(Path.of(filePath));
    }

    @Override
    public InputStream readAsStream(String filePath) throws FileNotFoundException {
        return new FileInputStream(filePath);
    }

    @Override
    public BasicFileAttributes readAttributes(String filePath, Class<BasicFileAttributes> basicFileAttributesClass) throws IOException {
        return Files.readAttributes(Path.of(filePath), basicFileAttributesClass);
    }

    @Override
    public Stream<String> listFiles(String folderPath) throws IOException {
        return Files.list(Path.of(folderPath)).map(Path::toString);
    }

    @Override
    public boolean exists(String filePath) {
        return Files.exists(Path.of(filePath));
    }

    @Override
    public void writeString(String filePath, String line, OpenOption... options) throws IOException {
        Files.writeString(Path.of(filePath), line, options);
    }

    @Override
    public Stream<String> walk(String rootFolderPath) throws IOException {
        return Files.walk(Path.of(rootFolderPath)).map(Path::toString);
    }

    @Override
    public void move(String from, String to, CopyOption... options) throws IOException {
        Files.move(Path.of(from), Path.of(to), options);
    }

    @Override
    public Stream<String> find(String start, int maxDepth, BiPredicate<Path, BasicFileAttributes> matcher,
                          FileVisitOption... options) throws IOException {
        return Files.find(Path.of(start), maxDepth, matcher, options).map(Path::toString);
    }

    @Override
    public void deleteIfExists(String filepath) throws IOException {
        Files.deleteIfExists(Path.of(filepath));
    }
}
