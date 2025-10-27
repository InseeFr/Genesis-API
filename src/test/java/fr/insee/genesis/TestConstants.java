package fr.insee.genesis;

/**
 * Class to create static variables giving path for test resources.
 */
public class TestConstants {

	public static final String TEST_RESOURCES_DIRECTORY = "src/test/resources";
	public static final String UNIT_TESTS_DDI_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/specs";

	public static final String DEFAULT_INTERROGATION_ID = "TESTINTERROGATIONID";
	public static final String DEFAULT_QUESTIONNAIRE_ID = "TESTQUESTIONNAIREID";

    //Functional tests
    public static final String FUNCTIONAL_TESTS_INPUT_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/IN";
    public static final String FUNCTIONAL_TESTS_WEB_DIRECTORY = FUNCTIONAL_TESTS_INPUT_DIRECTORY + "/WEB";
    public static final String FUNCTIONAL_TESTS_DDI_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/specs";
    public static final String FUNCTIONAL_TESTS_TEMP_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/temp";
    public static final String FUNCTIONAL_TESTS_OUTPUT_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/out";
    public static final String FUNCTIONAL_TESTS_API_URL = "http://localhost:8080";
}
