package fr.insee.genesis.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class GenesisException extends Exception{

    @Getter
    private final int status;

    @Getter
    private final String message;

}
