package fr.insee.genesis.infrastructure.utils;


import fr.insee.genesis.TestConstants;
import fr.insee.genesis.configuration.Config;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class FileUtilsTest {

	private final Config config = TestConstants.getConfigStub();
	private final FileUtils fileUtils= new FileUtils(config);

	@Test
	void getDataFolderTest() {
		assertEquals(TestConstants.TEST_RESOURCES_DIRECTORY + "/IN/WEB/TEST",
				fileUtils.getDataFolder("TEST", "WEB", null));
	}

	@Test
	void getDoneFolderTest() {
		assertEquals(TestConstants.TEST_RESOURCES_DIRECTORY + "/DONE/WEB/TEST",
				fileUtils.getDoneFolder("TEST", "WEB"));
	}

	@Test
	void getSpecFolderTest(){
		assertEquals(TestConstants.TEST_RESOURCES_DIRECTORY + "/specs",
				fileUtils.getSpecFolder());
		assertEquals(TestConstants.TEST_RESOURCES_DIRECTORY + "/specs/TEST",
				fileUtils.getSpecFolder("TEST"));
	}

	@Test
	void writeFileTest() throws IOException {
		Path testFilePath = Path.of("test.txt");

		fileUtils.writeFile(testFilePath,"test");

		testFilePath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"test.txt");

		Assertions.assertThat(testFilePath.toFile()).exists();
		Assertions.assertThat(testFilePath.toFile()).hasContent("test");

		Files.deleteIfExists(testFilePath);
	}

}