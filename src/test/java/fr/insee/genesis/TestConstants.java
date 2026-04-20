package fr.insee.genesis;

import fr.insee.genesis.configuration.Config;
import lombok.experimental.UtilityClass;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Class to create static variables giving path for test resources.
 */
@UtilityClass
public class TestConstants {

	public static final String TEST_RESOURCES_DIRECTORY = "src/test/resources";
	public static final String UNIT_TESTS_DDI_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/specs";

    public static final String DEFAULT_INTERROGATION_ID = "TESTINTERROGATIONID";
    public static final String DEFAULT_COLLECTION_INSTRUMENT_ID = "RAWDATATESTCAMPAIGN";
    public static final String DEFAULT_SURVEY_UNIT_ID = "TESTIDUE";

    public static Config getConfigStub(){
        Config configStub = mock(Config.class);

        doReturn(TEST_RESOURCES_DIRECTORY).when(configStub).getDataFolderSource();
        doReturn(TEST_RESOURCES_DIRECTORY).when(configStub).getSpecFolderSource();
        doReturn(TEST_RESOURCES_DIRECTORY).when(configStub).getLogFolder();
        doReturn("NONE").when(configStub).getAuthType();
        doReturn(1000).when(configStub).getRawDataProcessingBatchSize();

        return configStub;
    }
}
