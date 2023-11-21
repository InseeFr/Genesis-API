package fr.insee.genesis;

public class Constants {

    public static final String XSLT_STRUCTURED_VARIABLES = "xslt/structured-variables.xsl";
    public static final String ROOT_GROUP_NAME = "RACINE";
    public static final String ROOT_IDENTIFIER_NAME = "IdUE";
    public static final String METADATA_SEPARATOR = ".";
    public static final String DATE_REGEX = "(^([0-9]{4})[\\-\\/]([0-9]|1[0-2]|0[1-9])[\\-\\/]([0-9]|[0-2][0-9]|3[0-1])$)|" +
            "(^([0-9]|[0-2][0-9]|3[0-1])[\\-\\/]([0-9]|1[0-2]|0[1-9])[\\-\\/]([0-9]{4})$)";

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

}
