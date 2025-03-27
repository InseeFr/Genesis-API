package fr.insee.genesis;

public class Constants {

    public static final String XSLT_STRUCTURED_VARIABLES = "xslt/structured-variables.xsl";
    public static final String ROOT_GROUP_NAME = "RACINE";
    public static final String ROOT_IDENTIFIER_NAME = "interrogationId";
    public static final String METADATA_SEPARATOR = ".";
    public static final String DATE_REGEX = "(^([0-9]{4})[\\-\\/]([0-9]|1[0-2]|0[1-9])[\\-\\/]([0-9]|[0-2][0-9]|3[0-1])$)|" +
            "(^([0-9]|[0-2][0-9]|3[0-1])[\\-\\/]([0-9]|1[0-2]|0[1-9])[\\-\\/]([0-9]{4})$)";
    public static final String FILTER_RESULT_PREFIX = "FILTER_RESULT_";
    public static final String MISSING_SUFFIX = "_MISSING";
    public static final String MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME = "lunaticjsondata";
    private static final String[] ENO_VARIABLES = {"COMMENT_QE","COMMENT_UE","HEURE_REMPL","MIN_REMPL"};

    public static final String MONGODB_SCHEDULE_COLLECTION_NAME = "schedules";
    public static final String LOOP_NAME_PREFIX = "BOUCLE";
    public static final String MONGODB_RESPONSE_COLLECTION_NAME = "responses";
    public static final String MONGODB_RESPONSE_RAW_COLLECTION_NAME = "lunaticjsondata";
    public static final String MONGODB_VARIABLETYPE_COLLECTION_NAME = "variabletypes";
    public static final String VOLUMETRY_FOLDER_NAME = "genesis_volumetries";
    public static final String VOLUMETRY_FILE_SUFFIX = "_VOLUMETRY";
    public static final String VOLUMETRY_RAW_FILE_SUFFIX = "_RAW_VOLUMETRY";
    public static final String VOLUMETRY_FILE_DATE_FORMAT = "yyyy_MM_dd";
    public static final int VOLUMETRY_FILE_EXPIRATION_DAYS = 30;

    public static final String SCHEDULE_ARCHIVE_FOLDER_NAME = "genesis_deleted_schedules";


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
    public static final int BATCH_SIZE = 100; //Adapt to avoid OutOfMemoryException
    public static final String DIFFERENTIAL_DATA_FOLDER_NAME = "differential/data";

    // Kraftwerk service path parameters
    public static final String KRAFTWERK_MAIN_ENDPOINT = "";

    private Constants() {
        throw new IllegalStateException("Constants class");
    }


    public static String[] getEnoVariables() {
        return ENO_VARIABLES;
    }
}
