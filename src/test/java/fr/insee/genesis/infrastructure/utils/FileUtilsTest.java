package fr.insee.genesis.infrastructure.utils;

import cucumber.TestConstants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.stubs.ConfigStub;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilsTest {

	private final Config config = new ConfigStub();
	private final FileUtils fileUtils= new FileUtils(config);

	@Test
	void getDataFolderTest() {
		assertEquals(TestConstants.TEST_RESOURCES_DIRECTORY + "/IN/WEB/TEST",
				fileUtils.getDataFolder("TEST", "WEB"));
	}

	@Test
	void getDoneFolderTest() {
		assertEquals(TestConstants.TEST_RESOURCES_DIRECTORY + "/DONE/WEB/TEST",
				fileUtils.getDoneFolder("TEST", "WEB"));
	}

	@Test
	void getSpecFolderTest(){
		assertEquals(TestConstants.TEST_RESOURCES_DIRECTORY + "/IN/specs/TEST",
				fileUtils.getSpecFolder("TEST"));
	}


}
