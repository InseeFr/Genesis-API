package fr.insee.genesis.domain.model.surveyunit.rawdata;

/** Format of raw data to be imported into the data storage. */
public enum RawDataModelType {

    /** Legacy format of raw data ('Lunatic'). */
    LEGACY,

    /** 'Filière' raw response model. */
    FILIERE;

    @Override
    public String toString() {
        return switch (this) {
            case LEGACY -> "LEGACY (Lunatic)";
            case FILIERE -> "FILIERE raw responses";
        };
    }

}
