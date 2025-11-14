package fr.insee.genesis.domain.model.surveyunit.rawdata;

import fr.insee.genesis.exceptions.GenesisError;

import java.util.List;

public record DataProcessResult(int dataCount, int formattedDataCount, List<GenesisError> errors) {
}
