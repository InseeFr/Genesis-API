package fr.insee.genesis.controller.utils;

import lombok.Getter;

@Getter
public enum SchemaType {

 //   PROCESS_MESSAGE(Names.PROCESS_MESSAGE),
    INTERROGATION(Names.INTERROGATION),
    RAW_RESPONSE(Names.RAW_RESPONSE);

    public static class Names {
//        public static final String PROCESS_MESSAGE = "/modele-filiere-spec/Command.json";
        public static final String INTERROGATION = "/modele-filiere-spec/Interrogation.json";
        public static final String RAW_RESPONSE = "/modele-filiere-spec/RawResponse.json";

        private Names() {

        }
    }
    private final String schemaFileName;

    SchemaType(String schemaFileName) {
        this.schemaFileName = schemaFileName;
    }
}
