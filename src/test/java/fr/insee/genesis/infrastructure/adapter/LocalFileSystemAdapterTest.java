package fr.insee.genesis.infrastructure.adapter;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalFileSystemAdapterTest {

    @TempDir
    Path tempDir;

    @Mock
    private BiPredicate<Path, BasicFileAttributes> matcher;

    private LocalFileSystemAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LocalFileSystemAdapter();
    }

    @Test
    void createDirectories_shouldCreateNestedDirectories() throws IOException {
        //GIVEN
        //A path made of nested directories that do not exist yet
        Path nested = tempDir.resolve("a").resolve("b").resolve("c");

        //WHEN
        //Creating the directories
        adapter.createDirectories(nested.toString());

        //THEN
        //The nested directories exist
        assertThat(nested).isDirectory();
    }

    @Test
    void write_shouldWriteBytesToFile() throws IOException {
        //GIVEN
        //A file path and some bytes
        Path file = tempDir.resolve("file.bin");
        byte[] content = {1, 2, 3, 4};

        //WHEN
        //Writing the bytes
        adapter.write(file.toString(), content);

        //THEN
        //The file contains the same bytes
        assertThat(Files.readAllBytes(file)).isEqualTo(content);
    }

    @Test
    void write_shouldAppendWhenAppendOptionIsGiven() throws IOException {
        //GIVEN
        //An existing file containing two bytes
        Path file = tempDir.resolve("append.bin");
        Files.write(file, new byte[]{1, 2});

        //WHEN
        //Writing two more bytes with the APPEND option
        adapter.write(file.toString(), new byte[]{3, 4}, StandardOpenOption.APPEND);

        //THEN
        //The file contains the four bytes
        assertThat(Files.readAllBytes(file)).containsExactly(1, 2, 3, 4);
    }

    @Test
    void readAsString_shouldReturnFileContent() throws IOException {
        //GIVEN
        //A file containing text
        Path file = tempDir.resolve("text.txt");
        Files.writeString(file, "hello world");

        //WHEN
        //Reading the file as a string
        String result = adapter.readAsString(file.toString());

        //THEN
        //The content is returned
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void readAsString_shouldThrowWhenFileDoesNotExist() {
        //GIVEN
        //A path to a missing file
        Path file = tempDir.resolve("missing.txt");

        //WHEN
        //Reading the file as a string
        //THEN
        //A NoSuchFileException is thrown
        assertThatThrownBy(() -> adapter.readAsString(file.toString()))
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    void readAsStream_shouldReturnStreamOnFileContent() throws IOException {
        //GIVEN
        //A file containing text
        Path file = tempDir.resolve("stream.txt");
        Files.writeString(file, "stream content");

        //WHEN
        //Reading the file as a stream
        try (InputStream inputStream = adapter.readAsStream(file.toString())) {
            //THEN
            //The stream provides the file content
            assertThat(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8))
                    .isEqualTo("stream content");
        }
    }

    @Test
    void readAsStream_shouldThrowWhenFileDoesNotExist() {
        //GIVEN
        //A path to a missing file
        Path file = tempDir.resolve("missing.txt");

        //WHEN
        //Reading the file as a stream
        //THEN
        //A FileNotFoundException is thrown
        assertThatThrownBy(() -> adapter.readAsStream(file.toString()))
                .isInstanceOf(java.io.FileNotFoundException.class);
    }

    @Test
    void readAttributes_shouldReturnFileAttributes() throws IOException {
        //GIVEN
        //A file with 5 characters
        Path file = tempDir.resolve("attributes.txt");
        Files.writeString(file, "12345");

        //WHEN
        //Reading the file attributes
        BasicFileAttributes attributes = adapter.readAttributes(file.toString(), BasicFileAttributes.class);

        //THEN
        //The attributes describe a regular file of 5 bytes
        assertThat(attributes.isRegularFile()).isTrue();
        assertThat(attributes.size()).isEqualTo(5L);
    }

    @Test
    void listFiles_shouldReturnDirectChildren() throws IOException {
        //GIVEN
        //A directory containing a file and a sub directory
        Path file = Files.createFile(tempDir.resolve("file.txt"));
        Path subDirectory = Files.createDirectory(tempDir.resolve("sub"));

        //WHEN
        //Listing the files of the directory
        try (Stream<String> result = adapter.listFiles(tempDir.toString())) {
            //THEN
            //Both children are returned as strings
            assertThat(result.toList()).containsExactlyInAnyOrder(file.toString(), subDirectory.toString());
        }
    }

    @Test
    void exists_shouldReturnTrueWhenFileExists() throws IOException {
        //GIVEN
        //An existing file
        Path file = Files.createFile(tempDir.resolve("exists.txt"));

        //WHEN
        //Checking its existence
        boolean result = adapter.exists(file.toString());

        //THEN
        //True is returned
        assertThat(result).isTrue();
    }

    @Test
    void exists_shouldReturnFalseWhenFileDoesNotExist() {
        //GIVEN
        //A path to a missing file
        Path file = tempDir.resolve("missing.txt");

        //WHEN
        //Checking its existence
        boolean result = adapter.exists(file.toString());

        //THEN
        //False is returned
        assertThat(result).isFalse();
    }

    @Test
    void writeString_shouldWriteTextToFile() throws IOException {
        //GIVEN
        //A file path and a line of text
        Path file = tempDir.resolve("line.txt");

        //WHEN
        //Writing the text
        adapter.writeString(file.toString(), "first line");

        //THEN
        //The file contains the text
        assertThat(Files.readString(file)).isEqualTo("first line");
    }

    @Test
    void writeString_shouldAppendWhenAppendOptionIsGiven() throws IOException {
        //GIVEN
        //An existing file containing text
        Path file = tempDir.resolve("append.txt");
        Files.writeString(file, "first");

        //WHEN
        //Writing more text with the APPEND option
        adapter.writeString(file.toString(), "-second", StandardOpenOption.APPEND);

        //THEN
        //The file contains both texts
        assertThat(Files.readString(file)).isEqualTo("first-second");
    }

    @Test
    void walk_shouldReturnRootAndAllDescendants() throws IOException {
        //GIVEN
        //A root directory with a nested file
        Path subDirectory = Files.createDirectory(tempDir.resolve("sub"));
        Path nestedFile = Files.createFile(subDirectory.resolve("nested.txt"));

        //WHEN
        //Walking the root directory
        try (Stream<String> result = adapter.walk(tempDir.toString())) {
            //THEN
            //The root, the sub directory and the nested file are returned
            assertThat(result.toList()).containsExactlyInAnyOrder(
                    tempDir.toString(),
                    subDirectory.toString(),
                    nestedFile.toString()
            );
        }
    }

    @Test
    void move_shouldMoveFileToDestination() throws IOException {
        //GIVEN
        //An existing source file and a destination path
        Path source = tempDir.resolve("source.txt");
        Path destination = tempDir.resolve("destination.txt");
        Files.writeString(source, "moved content");

        //WHEN
        //Moving the file
        adapter.move(source.toString(), destination.toString());

        //THEN
        //The source no longer exists and the destination has the content
        assertThat(source).doesNotExist();
        assertThat(Files.readString(destination)).isEqualTo("moved content");
    }

    @Test
    void move_shouldReplaceExistingDestinationWhenOptionIsGiven() throws IOException {
        //GIVEN
        //A source file and an already existing destination file
        Path source = tempDir.resolve("source.txt");
        Path destination = tempDir.resolve("destination.txt");
        Files.writeString(source, "new content");
        Files.writeString(destination, "old content");

        //WHEN
        //Moving the file with the REPLACE_EXISTING option
        adapter.move(source.toString(), destination.toString(), StandardCopyOption.REPLACE_EXISTING);

        //THEN
        //The destination contains the new content
        assertThat(Files.readString(destination)).isEqualTo("new content");
    }

    @Test
    void find_shouldReturnPathsAcceptedByMatcher() throws IOException {
        //GIVEN
        //A directory containing two files and a matcher accepting every path
        Path first = Files.createFile(tempDir.resolve("first.txt"));
        Path second = Files.createFile(tempDir.resolve("second.txt"));
        when(matcher.test(any(), any())).thenReturn(true);

        //WHEN
        //Finding files with a max depth of 1
        try (Stream<String> result = adapter.find(tempDir.toString(), 1, matcher)) {
            //THEN
            //The root and both files are returned
            assertThat(result.toList()).containsExactlyInAnyOrder(
                    tempDir.toString(),
                    first.toString(),
                    second.toString()
            );
        }
        verify(matcher, times(3)).test(any(), any());
    }

    @Test
    void find_shouldReturnNothingWhenMatcherRejectsEverything() throws IOException {
        //GIVEN
        //A directory containing a file and a matcher rejecting every path
        Files.createFile(tempDir.resolve("file.txt"));
        when(matcher.test(any(), any())).thenReturn(false);

        //WHEN
        //Finding files with a max depth of 1
        try (Stream<String> result = adapter.find(tempDir.toString(), 1, matcher)) {
            List<String> paths = result.toList();

            //THEN
            //No path is returned
            assertThat(paths).isEmpty();
        }
    }

    @Test
    void deleteIfExists_shouldDeleteExistingFile() throws IOException {
        //GIVEN
        //An existing file
        Path file = Files.createFile(tempDir.resolve("to-delete.txt"));

        //WHEN
        //Deleting the file
        adapter.deleteIfExists(file.toString());

        //THEN
        //The file no longer exists
        assertThat(file).doesNotExist();
    }

    @Test
    @SneakyThrows
    void deleteIfExists_shouldNotFailWhenFileDoesNotExist() {
        //GIVEN
        //A path to a missing file
        Path file = tempDir.resolve("missing.txt");

        //WHEN
        //Deleting the file
        adapter.deleteIfExists(file.toString());

        //THEN
        //Nothing happens and the file still does not exist
        assertThat(file).doesNotExist();
    }
}