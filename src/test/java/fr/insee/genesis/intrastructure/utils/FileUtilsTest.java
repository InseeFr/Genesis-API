package fr.insee.genesis.intrastructure.utils;

import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilsTest {

	private final Config config = new ConfigStub();
	private final FileUtils fileUtils= new FileUtils(config);

	@Test
	void getDataFolderTest() {
		assertEquals("/test/genesis/data/coleman/TEST",
				fileUtils.getDataFolder("TEST", "coleman"));
	}

	@Test
	void getDoneFolderTest() {
		assertEquals("/test/genesis/done/TEST/WEB",
				fileUtils.getDoneFolder("TEST", "WEB"));
	}

	@Test
	void getSpecFolderTest(){
		assertEquals("/test/genesis/in/TEST/WEB",
				fileUtils.getSpecFolder("TEST", "WEB"));
	}


}
