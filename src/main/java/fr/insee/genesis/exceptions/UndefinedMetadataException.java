package fr.insee.genesis.exceptions;

public class UndefinedMetadataException extends RuntimeException {

    public UndefinedMetadataException(String message) {
        super(message);
    }

    public UndefinedMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
