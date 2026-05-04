package fr.insee.genesis.domain.model.surveyunit.rawdata;

import fr.insee.genesis.exceptions.GenesisError;

import java.util.List;

public record DataProcessResult(
        int dataCount,
        int formattedDataCount,
        List<GenesisError> errors) {

    public String message(String collectionInstrumentId) {
        return String.format("%s%s%s.",
                interrogationCountMessage(dataCount),
                formattedCountMessage(formattedDataCount),
                collectionIdMessage(collectionInstrumentId));
    }

    private static String interrogationCountMessage(int processedInterrogationsCount) {
        boolean plural = processedInterrogationsCount > 1;
        return "%d interrogation%s processed".formatted(processedInterrogationsCount,  plural ? "s" : "");
    }

    private static String collectionIdMessage(String collectionInstrumentId) {
        return " for collectionInstrumentId '%s'".formatted(collectionInstrumentId);
    }

    private static String formattedCountMessage(int formattedCount) {
        if (formattedCount == 0)
            return "";
        return " (including %d FORMATTED after data verification)".formatted(formattedCount);
    }

}
