package fr.insee.genesis.infrastructure.adapter;

import lombok.Builder;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

@Builder
public record MinioFileAttributes(
        FileTime lastModifiedTime,
        FileTime lastAccessTime,
        FileTime creationTime,
        boolean isRegularFile,
        boolean isDirectory,
        boolean isSymbolicLink,
        boolean isOther,
        long size,
        Object fileKey
) implements BasicFileAttributes {
}
