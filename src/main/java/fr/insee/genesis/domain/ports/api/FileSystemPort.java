package fr.insee.genesis.domain.ports.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

//TODO paths en strings
public interface FileSystemPort {

    void createDirectories(String path) throws IOException;

    void write(String path, byte[] bytes, OpenOption... options) throws IOException;

    String readAsString(String filePath) throws IOException;

    InputStream readAsStream(String filePath) throws FileNotFoundException;

    BasicFileAttributes readAttributes(String filePath, Class<BasicFileAttributes> basicFileAttributesClass) throws IOException;

    Stream<String> listFiles(String folderPath) throws IOException;

    boolean exists(String filePath);

    void writeString(String filePath, String line, OpenOption... options) throws IOException;

    Stream<String> walk(String rootFolderPath) throws IOException;

    void move(String from, String to, CopyOption... options) throws IOException;

    Stream<String> find(String start,
                          int maxDepth,
                          BiPredicate<Path, BasicFileAttributes> matcher,
                          FileVisitOption... options
    ) throws IOException;

    void deleteIfExists(String filepath) throws IOException;
}
