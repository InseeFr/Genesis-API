package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.configuration.MinioConfig;
import fr.insee.genesis.infrastructure.client.MinioClientBean;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import okhttp3.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.BiPredicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioFileSystemAdapterTest {

    private static final String BUCKET = "test-bucket";

    @Mock
    private MinioConfig minioConfig;
    @Mock
    private MinioClientBean minioClientBean;
    @Mock
    private StatObjectResponse statObjectResponse;

    @InjectMocks
    private MinioFileSystemAdapter adapter;

    @BeforeEach
    void setUp() {
        lenient().when(minioConfig.getBucketName()).thenReturn(BUCKET);
    }

    private GetObjectResponse objectResponse(String content) {
        return new GetObjectResponse(
                Headers.of(),
                BUCKET,
                "region",
                "object",
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))
        );
    }

    @SuppressWarnings("unchecked")
    private Result<Item> resultOf(String objectName) throws Exception {
        Item item = mock(Item.class);
        when(item.objectName()).thenReturn(objectName);
        Result<Item> result = mock(Result.class);
        when(result.get()).thenReturn(item);
        return result;
    }

    //createDirectories
    @Test
    void createDirectories_shouldDoNothing() {
        //GIVEN
        //Any path

        //WHEN
        //Creating directories

        //THEN
        //Nothing happens and Minio is never called
        assertThatCode(() -> adapter.createDirectories("some/path")).doesNotThrowAnyException();
        org.mockito.Mockito.verifyNoInteractions(minioClientBean);
    }

    //write
    @Test
    void write_shouldReplaceFile_whenTruncateOptionGiven() throws Exception {
        //GIVEN
        //A path with backslashes and the TRUNCATE_EXISTING option
        byte[] content = "content".getBytes(StandardCharsets.UTF_8);

        //WHEN
        //Writing the file
        adapter.write("folder\\file.txt", content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        //THEN
        //The file is put on Minio with a normalized path without checking its existence
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClientBean).putObject(captor.capture());
        verify(minioClientBean, never()).statObject(any(StatObjectArgs.class));
        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(captor.getValue().object()).isEqualTo("folder/file.txt");
    }

    @Test
    void write_shouldCreateFile_whenFileDoesNotExist() throws Exception {
        //GIVEN
        //A file that does not exist on Minio and no truncate option
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenThrow(new MinioException("Not exists"));

        //WHEN
        //Writing the file with an option that is not TRUNCATE_EXISTING
        adapter.write("folder/file.txt", "content".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

        //THEN
        //The file is created
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClientBean).putObject(captor.capture());
        assertThat(captor.getValue().object()).isEqualTo("folder/file.txt");
        verify(minioClientBean, never()).getObject(any(GetObjectArgs.class));
    }

    @Test
    void write_shouldAppendContent_whenFileAlreadyExists() throws Exception {
        //GIVEN
        //An existing file on Minio containing "abc"
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);
        when(minioClientBean.getObject(any(GetObjectArgs.class))).thenReturn(objectResponse("abc"));

        //WHEN
        //Writing "def" without the truncate option
        adapter.write("folder/file.txt", "def".getBytes(StandardCharsets.UTF_8));

        //THEN
        //The file is overwritten with the concatenated content (6 bytes)
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClientBean).putObject(captor.capture());
        assertThat(captor.getValue().object()).isEqualTo("folder/file.txt");
        assertThat(captor.getValue().objectSize()).isEqualTo(6L);
    }

    @Test
    void write_shouldNotThrow_whenPutFailsThenAppendSucceeds() throws Exception {
        //GIVEN
        //A put that fails the first time and succeeds the second time, with an existing file
        when(minioClientBean.putObject(any(PutObjectArgs.class)))
                .thenThrow(new MinioException("put failed"))
                .thenReturn(null);
        when(minioClientBean.getObject(any(GetObjectArgs.class))).thenReturn(objectResponse("abc"));

        //WHEN
        //Writing with the truncate option
        assertThatCode(() -> adapter.write(
                "folder/file.txt",
                "def".getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.TRUNCATE_EXISTING
        )).doesNotThrowAnyException();

        //THEN
        //The fallback append path is executed and put is called twice
        verify(minioClientBean, times(2)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void write_shouldNotThrow_whenAppendFails() throws Exception {
        //GIVEN
        //An existing file that cannot be read from Minio
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);
        when(minioClientBean.getObject(any(GetObjectArgs.class))).thenThrow(new MinioException("read failed"));

        //WHEN
        //Writing without the truncate option
        assertThatCode(() -> adapter.write("folder/file.txt", "def".getBytes(StandardCharsets.UTF_8)))
                .doesNotThrowAnyException();

        //THEN
        //The error is logged and nothing is put
        verify(minioClientBean, never()).putObject(any(PutObjectArgs.class));
    }

    //readAsString
    @Test
    void readAsString_shouldReturnFileContent() throws Exception {
        //GIVEN
        //A file on Minio containing "hello"
        when(minioClientBean.getObject(any(GetObjectArgs.class))).thenReturn(objectResponse("hello"));

        //WHEN
        //Reading the file as a string
        String result = adapter.readAsString("folder\\file.txt");

        //THEN
        //The content is returned and the path is normalized
        assertThat(result).isEqualTo("hello");
        ArgumentCaptor<GetObjectArgs> captor = ArgumentCaptor.forClass(GetObjectArgs.class);
        verify(minioClientBean).getObject(captor.capture());
        assertThat(captor.getValue().object()).isEqualTo("folder/file.txt");
    }

    //readAsStream
    @Test
    void readAsStream_shouldReturnStream() throws Exception {
        //GIVEN
        //A file on Minio containing "hello"
        when(minioClientBean.getObject(any(GetObjectArgs.class))).thenReturn(objectResponse("hello"));

        //WHEN
        //Reading the file as a stream
        InputStream result = adapter.readAsStream("folder/file.txt");

        //THEN
        //The stream contains the file content
        assertThat(result).isNotNull();
        assertThat(new String(result.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("hello");
    }

    @Test
    void readAsStream_shouldReturnNull_whenMinioFails() throws Exception {
        //GIVEN
        //Minio throwing an exception on getObject
        when(minioClientBean.getObject(any(GetObjectArgs.class))).thenThrow(new MinioException("error"));

        //WHEN
        //Reading the file as a stream
        InputStream result = adapter.readAsStream("folder/file.txt");

        //THEN
        //Null is returned
        assertThat(result).isNull();
    }

    //readAttributes
    @Test
    void readAttributes_shouldReturnAttributes() throws Exception {
        //GIVEN
        //A file on Minio with a last modified date
        ZonedDateTime lastModified = ZonedDateTime.of(2024, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);
        when(statObjectResponse.lastModified()).thenReturn(lastModified);
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);

        //WHEN
        //Reading the attributes
        BasicFileAttributes attributes = adapter.readAttributes("folder\\file.txt", BasicFileAttributes.class);

        //THEN
        //The attributes reflect a regular file with the last modified date
        assertThat(attributes.isRegularFile()).isTrue();
        assertThat(attributes.isDirectory()).isFalse();
        assertThat(attributes.isOther()).isFalse();
        assertThat(attributes.lastModifiedTime().toInstant()).isEqualTo(lastModified.toInstant());
        assertThat(attributes.creationTime().toInstant()).isEqualTo(lastModified.toInstant());
    }

    @Test
    void readAttributes_shouldThrowIOException_whenMinioFails() throws Exception {
        //GIVEN
        //Minio throwing a MinioException on statObject
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenThrow(new MinioException("error"));

        //WHEN
        //Reading the attributes
        //THEN
        //An IOException is thrown
        assertThatThrownBy(() -> adapter.readAttributes("folder/file.txt", BasicFileAttributes.class))
                .isInstanceOf(IOException.class);
    }

    //listFiles
    @Test
    void listFiles_shouldReturnObjectNames() throws Exception {
        //GIVEN
        //Two objects listed by Minio
        List<Result<Item>> results = List.of(resultOf("folder/a.txt"), resultOf("folder/b.txt"));
        when(minioClientBean.listObjects(any(ListObjectsArgs.class))).thenReturn(results);

        //WHEN
        //Listing files of the folder
        List<String> files = adapter.listFiles("folder\\").toList();

        //THEN
        //Both names are returned
        assertThat(files).containsExactly("folder/a.txt", "folder/b.txt");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listFiles_shouldReturnEmptyStream_whenResultFails() throws Exception {
        //GIVEN
        //A listed result throwing an exception when read
        Result<Item> failingResult = mock(Result.class);
        when(failingResult.get()).thenThrow(new MinioException("error"));
        when(minioClientBean.listObjects(any(ListObjectsArgs.class))).thenReturn(List.of(failingResult));

        //WHEN
        //Listing files of the folder
        List<String> files = adapter.listFiles("folder").toList();

        //THEN
        //The stream is empty
        assertThat(files).isEmpty();
    }

    //exists
    @Test
    void exists_shouldReturnTrue_whenObjectFound() throws Exception {
        //GIVEN
        //An object found on Minio
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);

        //WHEN
        //Checking existence
        boolean result = adapter.exists("folder\\file.txt");

        //THEN
        //True is returned
        assertThat(result).isTrue();
    }

    @Test
    void exists_shouldReturnFalse_whenException() throws Exception {
        //GIVEN
        //Minio throwing an unexpected exception
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenThrow(new MinioException("error"));

        //WHEN
        //Checking existence
        boolean result = adapter.exists("folder/file.txt");

        //THEN
        //False is returned
        assertThat(result).isFalse();
    }

    //writeString
    @Test
    void writeString_shouldWriteBytesOnMinio() throws Exception {
        //GIVEN
        //A string to write
        //WHEN
        //Writing the string with truncate option
        adapter.writeString("folder/file.txt", "line", StandardOpenOption.TRUNCATE_EXISTING);

        //THEN
        //The file is put on Minio
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClientBean).putObject(captor.capture());
        assertThat(captor.getValue().object()).isEqualTo("folder/file.txt");
    }

    //walk
    @Test
    void walk_shouldReturnEmptyStream() throws Exception {
        //GIVEN
        //Any root folder
        //WHEN
        //Walking the folder
        List<String> result = adapter.walk("folder").toList();

        //THEN
        //The stream is empty
        assertThat(result).isEmpty();
    }

    //move
    @Test
    void move_shouldCopyThenDeleteSource() throws Exception {
        //GIVEN
        //An existing source file on Minio
        when(minioClientBean.getObject(any(GetObjectArgs.class))).thenReturn(objectResponse("content"));
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);

        //WHEN
        //Moving the file
        adapter.move("from/file.txt", "to/file.txt");

        //THEN
        //The content is written to the destination and the source is removed
        ArgumentCaptor<PutObjectArgs> putCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClientBean).putObject(putCaptor.capture());
        assertThat(putCaptor.getValue().object()).isEqualTo("to/file.txt");
        ArgumentCaptor<RemoveObjectArgs> removeCaptor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClientBean).removeObject(removeCaptor.capture());
        assertThat(removeCaptor.getValue().object()).isEqualTo("from/file.txt");
    }

    //find
    @Test
    void find_shouldReturnMatchingFiles() throws Exception {
        //GIVEN
        //Two files in a folder, only one matching the predicate
        List<Result<Item>> results = List.of(resultOf("folder/a.xml"), resultOf("folder/b.txt"));
        when(minioClientBean.listObjects(any(ListObjectsArgs.class))).thenReturn(results);
        when(statObjectResponse.lastModified()).thenReturn(ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);
        BiPredicate<Path, BasicFileAttributes> matcher =
                (path, attributes) -> path.toString().endsWith(".xml");

        //WHEN
        //Finding files with max depth 0
        List<String> files = adapter.find("folder", 0, matcher).toList();

        //THEN
        //Only the matching file is returned
        assertThat(files).containsExactly("folder/a.xml");
    }

    @Test
    void find_shouldReturnEmpty_whenMaxDepthIsNegative() throws Exception {
        //GIVEN
        //A negative max depth
        BiPredicate<Path, BasicFileAttributes> matcher = (path, attributes) -> true;

        //WHEN
        //Finding files
        List<String> files = adapter.find("folder", -1, matcher).toList();

        //THEN
        //Nothing is returned and Minio is never listed
        assertThat(files).isEmpty();
        verify(minioClientBean, never()).listObjects(any(ListObjectsArgs.class));
    }

    //deleteIfExists
    @Test
    void deleteIfExists_shouldDoNothing_whenFileDoesNotExist() throws Exception {
        //GIVEN
        //A file that does not exist
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenThrow(new MinioException("error"));

        //WHEN
        //Deleting the file
        adapter.deleteIfExists("folder/file.txt");

        //THEN
        //Nothing is removed
        verify(minioClientBean, never()).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteIfExists_shouldRemoveFile_whenFileExists() throws Exception {
        //GIVEN
        //An existing file
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);

        //WHEN
        //Deleting the file
        adapter.deleteIfExists("folder\\file.txt");

        //THEN
        //The file is removed with a normalized path
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClientBean).removeObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(captor.getValue().object()).isEqualTo("folder/file.txt");
    }

    @Test
    void deleteIfExists_shouldNotThrow_whenRemoveFails() throws Exception {
        //GIVEN
        //An existing file whose removal fails
        when(minioClientBean.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);
        doThrow(new MinioException("error"))
                .when(minioClientBean).removeObject(any(RemoveObjectArgs.class));

        //WHEN
        //Deleting the file
        //THEN
        //No exception is thrown
        assertThatCode(() -> adapter.deleteIfExists("folder/file.txt")).doesNotThrowAnyException();
        verify(minioClientBean).removeObject(any(RemoveObjectArgs.class));
    }
}