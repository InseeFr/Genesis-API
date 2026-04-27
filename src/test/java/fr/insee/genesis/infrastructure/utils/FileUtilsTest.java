package fr.insee.genesis.infrastructure.utils;

import fr.insee.genesis.Constants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.exceptions.GenesisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileUtils tests")
class FileUtilsTest {

	@TempDir
	Path tempDir;

	@Mock
	private Config config;

	private FileUtils fileUtils;

	@BeforeEach
	void setUp() {
		when(config.getDataFolderSource()).thenReturn(tempDir.toString());
		when(config.getSpecFolderSource()).thenReturn(tempDir.toString());
		when(config.getLogFolder()).thenReturn(tempDir.resolve("logs").toString());
		fileUtils = new FileUtils(config);
	}

	@Nested
	@DisplayName("moveFiles() tests")
	class MoveFilesTests {

		@Test
		@DisplayName("Should move a file to an existing destination")
		void moveFiles_existingDestination_shouldMoveFile() throws IOException {
			// GIVEN
			Path source = Files.createFile(tempDir.resolve("file.txt"));
			Path destination = Files.createDirectory(tempDir.resolve("dest"));

			// WHEN
			fileUtils.moveFiles(source, destination.toString());

			// THEN
			assertThat(destination.resolve("file.txt")).exists();
			assertThat(source).doesNotExist();
		}

		@Test
		@DisplayName("Should create destination directory if it does not exist")
		void moveFiles_missingDestination_shouldCreateDirectoryAndMoveFile() throws IOException {
			// GIVEN
			Path source = Files.createFile(tempDir.resolve("file.txt"));
			String destination = tempDir.resolve("new-dest").toString();

			// WHEN
			fileUtils.moveFiles(source, destination);

			// THEN
			assertThat(Path.of(destination, "file.txt")).exists();
		}

		@Test
		@DisplayName("Should replace existing file at destination")
		void moveFiles_existingFileAtDestination_shouldReplace() throws IOException {
			// GIVEN
			Path source = Files.writeString(tempDir.resolve("file.txt"), "new content");
			Path destination = Files.createDirectory(tempDir.resolve("dest"));
			Files.writeString(destination.resolve("file.txt"), "old content");

			// WHEN
			fileUtils.moveFiles(source, destination.toString());

			// THEN
			assertThat(Files.readString(destination.resolve("file.txt"))).isEqualTo("new content");
		}
	}

	@Nested
	@DisplayName("moveDataFile() tests")
	class MoveDataFileTests {

		@Test
		@DisplayName("Should move file to the DONE folder")
		void moveDataFile_shouldMoveFileToDoneFolder() throws IOException {
			// GIVEN
			Path source = Files.createFile(tempDir.resolve("data.xml"));
			String campaign = "campaign-2024";
			String dataSource = "WEB";

			// WHEN
			fileUtils.moveDataFile(campaign, dataSource, source);

			// THEN
			Path expectedDest = tempDir.resolve("DONE").resolve(dataSource).resolve(campaign).resolve("data.xml");
			assertThat(expectedDest).exists();
		}
	}

	@Nested
	@DisplayName("isFilePresent() tests")
	class IsFilePresentTests {

		@Test
		@DisplayName("Should return true when file exists")
		void isFilePresent_existingFile_shouldReturnTrue() throws IOException {
			// GIVEN
			Path file = Files.createFile(tempDir.resolve("existing.txt"));

			// WHEN / THEN
			assertThat(fileUtils.isFilePresent(file.toString())).isTrue();
		}

		@Test
		@DisplayName("Should return false when file does not exist")
		void isFilePresent_missingFile_shouldReturnFalse() {
			// GIVEN

			// WHEN / THEN
			assertThat(fileUtils.isFilePresent(tempDir.resolve("missing.txt").toString())).isFalse();
		}
	}

	@Nested
	@DisplayName("isFolderPresent() tests")
	class IsFolderPresentTests {

		@Test
		@DisplayName("Should return true when folder exists")
		void isFolderPresent_existingFolder_shouldReturnTrue() throws IOException {
			// GIVEN
			Path folder = Files.createDirectory(tempDir.resolve("my-folder"));

			// WHEN / THEN
			assertThat(fileUtils.isFolderPresent(folder.toString())).isTrue();
		}

		@Test
		@DisplayName("Should return false when folder does not exist")
		void isFolderPresent_missingFolder_shouldReturnFalse() {
			// GIVEN

			// WHEN / THEN
			assertThat(fileUtils.isFolderPresent(tempDir.resolve("missing").toString())).isFalse();
		}
	}

	@Nested
	@DisplayName("listFiles() tests")
	class ListFilesTests {

		@Test
		@DisplayName("Should return names of files in the directory, excluding subdirectories")
		void listFiles_shouldReturnFileNamesOnly() throws IOException {
			// GIVEN
			Path dir = Files.createDirectory(tempDir.resolve("dir"));
			Files.createFile(dir.resolve("a.txt"));
			Files.createFile(dir.resolve("b.xml"));
			Files.createDirectory(dir.resolve("subdir"));

			// WHEN
			List<String> result = fileUtils.listFiles(dir.toString());

			// THEN
			assertThat(result).containsExactlyInAnyOrder("a.txt", "b.xml");
		}

		@Test
		@DisplayName("Should return empty list when directory does not exist")
		void listFiles_missingDirectory_shouldReturnEmptyList() {
			// GIVEN

			// WHEN
			List<String> result = fileUtils.listFiles(tempDir.resolve("nonexistent").toString());

			// THEN
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Should return empty list when directory is empty")
		void listFiles_emptyDirectory_shouldReturnEmptyList() throws IOException {
			// GIVEN
			Path dir = Files.createDirectory(tempDir.resolve("empty"));

			// WHEN
			List<String> result = fileUtils.listFiles(dir.toString());

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("listFolders() tests")
	class ListFoldersTests {

		@Test
		@DisplayName("Should return names of subdirectories only, excluding files")
		void listFolders_shouldReturnSubdirectoryNamesOnly() throws IOException {
			// GIVEN
			Path dir = Files.createDirectory(tempDir.resolve("parent"));
			Files.createDirectory(dir.resolve("sub1"));
			Files.createDirectory(dir.resolve("sub2"));
			Files.createFile(dir.resolve("file.txt"));

			// WHEN
			List<String> result = fileUtils.listFolders(dir.toString());

			// THEN
			assertThat(result).containsExactlyInAnyOrder("sub1", "sub2");
		}

		@Test
		@DisplayName("Should return empty list when directory does not exist")
		void listFolders_missingDirectory_shouldReturnEmptyList() {
			// GIVEN

			// WHEN
			List<String> result = fileUtils.listFolders(tempDir.resolve("nonexistent").toString());

			// THEN
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Should return empty list when directory contains no subdirectories")
		void listFolders_noSubdirectories_shouldReturnEmptyList() throws IOException {
			// GIVEN
			Path dir = Files.createDirectory(tempDir.resolve("nosubdirs"));
			Files.createFile(dir.resolve("file.txt"));

			// WHEN
			List<String> result = fileUtils.listFolders(dir.toString());

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("findFile() tests")
	class FindFileTests {

		@Test
		@DisplayName("Should return the path of a file matching the regex")
		void findFile_matchingFile_shouldReturnPath() throws IOException {
			// GIVEN
			Path dir = Files.createDirectory(tempDir.resolve("search"));
			Path expected = Files.createFile(dir.resolve("data-2024.xml"));

			// WHEN
			Path result = fileUtils.findFile(dir.toString(), "data-.*\\.xml");

			// THEN
			assertThat(result).isEqualTo(expected);
		}

		@Test
		@DisplayName("Should throw RuntimeException when no file matches the regex")
		void findFile_noMatch_shouldThrowRuntimeException() throws IOException {
			// GIVEN
			Path dir = Files.createDirectory(tempDir.resolve("empty-search"));
			String dirString = dir.toString();

			// WHEN / THEN
			assertThatThrownBy(() -> fileUtils.findFile(dirString, ".*\\.xml"))
					.isInstanceOf(NoSuchFileException.class)
					.hasMessageContaining("No file");
		}

		@Test
		@DisplayName("Should match file name case-insensitively")
		void findFile_shouldMatchCaseInsensitively() throws IOException {
			// GIVEN
			Path dir = Files.createDirectory(tempDir.resolve("case-search"));
			Files.createFile(dir.resolve("DATA.XML"));

			// WHEN
			Path result = fileUtils.findFile(dir.toString(), "data\\.xml");

			// THEN
			assertThat(result.getFileName().toString()).isEqualToIgnoringCase("data.xml");
		}
	}

	@Nested
	@DisplayName("getDataFolder() tests")
	class GetDataFolderTests {

		@Test
		@DisplayName("Should return IN/dataSource/campaign path when rootDataFolder is null")
		void getDataFolder_nullRoot_shouldReturnInPath() {
			// GIVEN

			// WHEN
			String result = fileUtils.getDataFolder("campaign", "WEB", null);

			// THEN
			assertThat(result).isEqualTo(tempDir + "/IN/WEB/campaign");
		}

		@Test
		@DisplayName("Should append rootDataFolder to path when provided")
		void getDataFolder_withRoot_shouldAppendRoot() {
			// GIVEN

			// WHEN
			String result = fileUtils.getDataFolder("campaign", "WEB", "differential");

			// THEN
			assertThat(result).isEqualTo(tempDir + "/IN/WEB/campaign/differential");
		}
	}

	@Nested
	@DisplayName("getSpecFolder() tests")
	class GetSpecFolderTests {

		@Test
		@DisplayName("Should return specFolderSource/specs")
		void getSpecFolder_noArg_shouldReturnSpecsPath() {
			// GIVEN

			// WHEN
			String result = fileUtils.getSpecFolder();

			// THEN
			assertThat(result).isEqualTo(tempDir + "/specs");
		}

		@Test
		@DisplayName("Should return specFolderSource/specs/campaign when campaign is provided")
		void getSpecFolder_withCampaign_shouldReturnCampaignSpecsPath() {
			// GIVEN

			// WHEN
			String result = fileUtils.getSpecFolder("my-campaign");

			// THEN
			assertThat(result).isEqualTo(tempDir + "/specs/my-campaign");
		}
	}

	@Nested
	@DisplayName("getDoneFolder() tests")
	class GetDoneFolderTests {

		@Test
		@DisplayName("Should return DONE/dataSource/campaign path")
		void getDoneFolder_shouldReturnDonePath() {
			// GIVEN

			// WHEN
			String result = fileUtils.getDoneFolder("campaign", "WEB");

			// THEN
			assertThat(result).isEqualTo(tempDir + "/DONE/WEB/campaign");
		}
	}

	@Nested
	@DisplayName("getKraftwerkOutFolder() tests")
	class GetKraftwerkOutFolderTests {

		@Test
		@DisplayName("Should return out/campaign path")
		void getKraftwerkOutFolder_shouldReturnOutPath() {
			// GIVEN

			// WHEN
			String result = fileUtils.getKraftwerkOutFolder("campaign");

			// THEN
			assertThat(result).isEqualTo(tempDir + "/out/campaign");
		}
	}

	@Nested
	@DisplayName("getLogFolder() tests")
	class GetLogFolderTests {

		@Test
		@DisplayName("Should return the configured log folder")
		void getLogFolder_shouldReturnConfiguredValue() {
			// GIVEN

			// WHEN
			String result = fileUtils.getLogFolder();

			// THEN
			assertThat(result).isEqualTo(tempDir.resolve("logs").toString());
		}
	}

	@Nested
	@DisplayName("writeFile() tests")
	class WriteFileTests {

		@Test
		@DisplayName("Should create the file with the given content")
		void writeFile_shouldCreateFileWithContent() {
			// GIVEN
			Path relativePath = Path.of("output", "result.txt");

			// WHEN
			fileUtils.writeFile(relativePath, "hello world");

			// THEN
			Path expectedPath = tempDir.resolve("output").resolve("result.txt");
			assertThat(expectedPath).exists().hasContent("hello world");
		}

		@Test
		@DisplayName("Should create parent directories if they do not exist")
		void writeFile_shouldCreateParentDirectories() {
			// GIVEN
			Path relativePath = Path.of("deep", "nested", "dir", "file.txt");

			// WHEN
			fileUtils.writeFile(relativePath, "content");

			// THEN
			assertThat(tempDir.resolve("deep").resolve("nested").resolve("dir")).isDirectory();
		}

		@Test
		@DisplayName("Should not overwrite an existing file")
		void writeFile_existingFile_shouldNotOverwrite() throws IOException {
			// GIVEN
			Path relativePath = Path.of("existing.txt");
			Path fullPath = tempDir.resolve("existing.txt");
			Files.writeString(fullPath, "original");

			// WHEN
			fileUtils.writeFile(relativePath, "new content");

			// THEN — file already existed, createNewFile returns false, so nothing is written
			assertThat(fullPath).hasContent("original");
		}
	}

	@Nested
	@DisplayName("writeSuUpdatesInFile() tests")
	class WriteSuUpdatesInFileTests {

		@Test
		@DisplayName("Should create the file and write a valid JSON array")
		void writeSuUpdates_shouldWriteJsonArray() throws IOException {
			// GIVEN
			Path filePath = tempDir.resolve("updates").resolve("output.json");
			Stream<SurveyUnitModel> stream = Stream.of(
					SurveyUnitModel.builder().interrogationId("i1").build()
			);

			// WHEN
			fileUtils.writeSuUpdatesInFile(filePath, stream);

			// THEN
			assertThat(filePath).exists();
			String content = Files.readString(filePath);
			assertThat(content).startsWith("[").endsWith("{}]");
		}

		@Test
		@DisplayName("Should write an empty array when stream is empty")
		void writeSuUpdates_emptyStream_shouldWriteEmptyArray() throws IOException {
			// GIVEN
			Path filePath = tempDir.resolve("updates").resolve("empty.json");

			// WHEN
			fileUtils.writeSuUpdatesInFile(filePath, Stream.empty());

			// THEN
			assertThat(Files.readString(filePath)).isEqualTo("[{}]");
		}

		@Test
		@DisplayName("Should create parent directories if they do not exist")
		void writeSuUpdates_shouldCreateParentDirectories() throws IOException {
			// GIVEN
			Path filePath = tempDir.resolve("deep").resolve("nested").resolve("out.json");

			// WHEN
			fileUtils.writeSuUpdatesInFile(filePath, Stream.empty());

			// THEN
			assertThat(filePath.getParent()).isDirectory();
		}

		@Test
		@DisplayName("Should append to an existing file")
		void writeSuUpdates_existingFile_shouldAppend() throws IOException {
			// GIVEN
			Path filePath = tempDir.resolve("append.json");
			Files.writeString(filePath, "previous content\n");

			// WHEN
			fileUtils.writeSuUpdatesInFile(filePath, Stream.empty());

			// THEN
			String content = Files.readString(filePath);
			assertThat(content).startsWith("previous content").endsWith("[{}]");
		}
	}

	@Nested
	@DisplayName("listAllSpecsFolders() tests")
	class ListAllSpecsFoldersTests {

		@Test
		@DisplayName("Should return only subdirectories from the specs folder")
		void listAllSpecsFolders_shouldReturnDirectoriesOnly() throws IOException {
			// GIVEN
			Path specsDir = Files.createDirectories(tempDir.resolve("specs"));
			Files.createDirectory(specsDir.resolve("campaign-A"));
			Files.createDirectory(specsDir.resolve("campaign-B"));
			Files.createFile(specsDir.resolve("readme.txt"));

			// WHEN
			List<File> result = fileUtils.listAllSpecsFolders();

			// THEN
			assertThat(result)
					.hasSize(2)
					.allMatch(File::isDirectory)
					.extracting(File::getName)
					.containsExactlyInAnyOrder("campaign-A", "campaign-B");
		}

		@Test
		@DisplayName("Should return empty list when specs folder is empty")
		void listAllSpecsFolders_emptyFolder_shouldReturnEmptyList() throws IOException {
			// GIVEN
			Files.createDirectories(tempDir.resolve("specs"));

			// WHEN
			List<File> result = fileUtils.listAllSpecsFolders();

			// THEN
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Should return empty list when specs folder does not exist")
		void listAllSpecsFolders_missingFolder_shouldReturnEmptyList() {
			// GIVEN — specs folder not created

			// WHEN
			List<File> result = fileUtils.listAllSpecsFolders();

			// THEN
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("ensureContextualFolderExists() tests")
	class EnsureContextualFolderExistsTests {

		@Test
		@DisplayName("Should create the contextual folder when it does not exist")
		void ensureContextualFolder_missing_shouldCreateFolder() throws IOException, GenesisException {
			// GIVEN
			String questionnaireId = "questionnaire-123";
			Mode mode = Mode.WEB;

			// WHEN
			fileUtils.ensureContextualFolderExists(questionnaireId, mode);

			// THEN
			String expectedPath = fileUtils.getDataFolder(questionnaireId, mode.getFolder(), null)
					+ Constants.CONTEXTUAL_FOLDER;
			assertThat(Path.of(expectedPath)).isDirectory();
		}

		@Test
		@DisplayName("Should not throw when the contextual folder already exists")
		void ensureContextualFolder_alreadyExists_shouldNotThrow() throws IOException {
			// GIVEN
			String questionnaireId = "questionnaire-123";
			Mode mode = Mode.WEB;
			String path = fileUtils.getDataFolder(questionnaireId, mode.getFolder(), null)
					+ Constants.CONTEXTUAL_FOLDER;
			Files.createDirectories(Path.of(path));

			// WHEN / THEN
			org.assertj.core.api.Assertions.assertThatCode(
							() -> fileUtils.ensureContextualFolderExists(questionnaireId, mode))
					.doesNotThrowAnyException();
		}
	}
}