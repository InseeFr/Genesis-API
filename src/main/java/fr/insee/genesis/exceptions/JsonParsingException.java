package fr.insee.genesis.exceptions;

import java.io.IOException;

public class JsonParsingException extends IOException {
    public JsonParsingException(String message) {
        super(message);
    }
}
