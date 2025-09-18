package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.exceptions.GenesisException;

import java.io.InputStream;

public interface EditedExternalResponseApiPort {
    boolean readEditedExternalFile(InputStream inputStream, String questionnaireId) throws GenesisException;
    }
