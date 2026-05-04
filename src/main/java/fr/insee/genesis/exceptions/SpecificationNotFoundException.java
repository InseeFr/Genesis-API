package fr.insee.genesis.exceptions;

import lombok.Getter;

@Getter
public class SpecificationNotFoundException extends RuntimeException {
     final String collectionInstrumentId;

    public SpecificationNotFoundException(String collectionInstrumentId) {
        super("No specification folder found for collectionInstrumentId: " + collectionInstrumentId);
        this.collectionInstrumentId = collectionInstrumentId;
    }
}
