package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.exceptions.GenesisException;

import java.io.InputStream;

public interface ContextualPreviousVariableApiPort {
    boolean readContextualPreviousFile(InputStream inputStream, String questionnaireId, String sourceState) throws GenesisException;
    }
