package fr.insee.genesis;

public class Constants {

    public static final String XSLT_STRUCTURED_VARIABLES = "xslt/structured-variables.xsl";
    public static final String ROOT_GROUP_NAME = "RACINE";
    public static final String ROOT_IDENTIFIER_NAME = "IdUE";
    public static final String METADATA_SEPARATOR = ".";
    public static final String DATE_REGEX = "(^([0-9]{4})[\\-\\/]([0-9]|1[0-2]|0[1-9])[\\-\\/]([0-9]|[0-2][0-9]|3[0-1])$)|" +
            "(^([0-9]|[0-2][0-9]|3[0-1])[\\-\\/]([0-9]|1[0-2]|0[1-9])[\\-\\/]([0-9]{4})$)";


   // XML sequential reading parameters
    public static final int MAX_FILE_SIZE_UNTIL_SEQUENTIAL = 200; // In megabytes

    public static final String CAMPAIGN_ID_ELEMENT_NAME = "Id";

    public static final String CAMPAIGN_LABEL_ELEMENT_NAME = "Label";

    public static final String SURVEY_UNITS_NODE_NAME = "SurveyUnits";
    public static final String SURVEY_UNIT_ELEMENT_NAME = "SurveyUnit";

    public static final String SURVEYUNIT_ID_ELEMENT_NAME = "Id";
    public static final String SURVEYUNIT_SURVEYMODELID_ELEMENT_NAME = "QuestionnaireModelId";
    public static final String SURVEYUNIT_DATA_COLLECTED_NODE_NAME = "COLLECTED";
    public static final String SURVEYUNIT_DATA_CALCULATED_NODE_NAME = "CALCULATED";
    public static final String SURVEYUNIT_DATA_EXTERNAL_NODE_NAME = "EXTERNAL";



    public static final String COLLECTED_NODE_NAME = "COLLECTED";
    public static final String CALCULATED_NODE_NAME = "CALCULATED";
    public static final String EXTERNAL_NODE_NAME = "EXTERNAL";


    // Data extraction parameters
    public static final int BATCH_SIZE = 100; //TODO Adapt to avoid OutOfMemoryException

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

}
