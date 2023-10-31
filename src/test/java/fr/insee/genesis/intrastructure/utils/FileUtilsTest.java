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
		assertEquals("/test/genesis/IN/WEB/TEST",
				fileUtils.getDataFolder("TEST", "WEB"));
	}

	@Test
	void getDoneFolderTest() {
		assertEquals("/test/genesis/DONE/WEB/TEST",
				fileUtils.getDoneFolder("TEST", "WEB"));
	}

	@Test
	void getSpecFolderTest(){
		assertEquals("/test/kraftwerk/specs/TEST",
				fileUtils.getSpecFolder("TEST"));
	}


}
