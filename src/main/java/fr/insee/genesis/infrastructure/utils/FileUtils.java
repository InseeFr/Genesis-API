package fr.insee.genesis.infrastructure.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileUtils {

	@Getter
	private final String dataFolderSource;

	private final String specFolderSource;

	public FileUtils(Config config) {
		this.dataFolderSource = config.getDataFolderSource();
		this.specFolderSource = config.getSpecFolderSource();
	}

	/**
	 * Move a file from a directory to another. It creates the destination directory if it does not exist.
	 * @param from Path of the source directory.
	 *                Example: /home/genesis/data/in/2021/2021-01-01
	 * @param destination Path of the destination directory.
	 *                Example: /home/genesis/data/done/2021/2021-01-01
	 */
	public void moveFiles(Path from, String destination) throws IOException {
		if (!isFolderPresent(destination)) {
			Files.createDirectories(Path.of(destination));
		}
		Files.move(from,Path.of(destination+"/"+ from.getFileName().toString()));
		log.info("File {} moved from {} to {}", from.getFileName().toString(), from, destination);
	}

	/**
	 * Move a data file to the folder done
	 * @param campaign Name of the campaign (also folder name)
	 * @param dataSource Application the data came from
	 * @param dataFilePath Data file to move
	 * @throws IOException
	 */
	public void moveDataFile(String campaign, String dataSource, Path dataFilePath) throws IOException {
		String destination = getDoneFolder(campaign, dataSource);
		moveFiles(dataFilePath, destination);
	}

	/**
	 * Check if a file exists
	 * @param path
	 * @return true if the file exists, false otherwise
	 */
	public boolean isFilePresent(String path) {
		return Files.exists(Path.of(path));
	}

	/**
	 * Checks if a folder exists
	 * @param path
	 * @return true if the folder exists, false otherwise
	 */
	public boolean isFolderPresent(String path) {
		return Files.exists(Path.of(path));
	}

	/**
	 * List all files in a folder
	 * @param dir
	 * @return List of files, empty list if the folder does not exist
	 */
	public List<String> listFiles(String dir) {
		if (isFolderPresent(dir)) {
			return Stream.of(Objects.requireNonNull(new File(dir).listFiles()))
					.filter(file -> !file.isDirectory())
					.map(File::getName)
					.toList();
		}
		return List.of();
	}

	/**
	 * List all folders in a folder
	 * @param dir
	 * @return List of folders, empty list if the folder does not exist
	 */
	public List<String> listFolders(String dir) {
		if (isFolderPresent(dir)) {
			List<String> folders =new ArrayList<>();
			File[] objs = new File(dir).listFiles();
			if (objs == null) {
				return List.of();
			}
			for (File file : objs) {
				if (file.isDirectory()) {
					folders.add(file.getName());
				}
			}
			return folders;
		}
		return List.of();
	}

	/**
	 * Find the DDI file in the folder of a campaign
	 * @param campaign
	 * @param mode
	 * @return Path of the DDI file
	 * @throws IOException
	 */
	public Path findDDIFile(String campaign, String mode) throws IOException {
		String ddiPath = String.format("%s/%s", getSpecFolder(campaign), mode);
		try (Stream<Path> files = Files.find(Path.of(ddiPath), 1,
				(path, basicFileAttributes) -> path.toFile().getName().toLowerCase().matches("ddi[\\w,\\s-]+\\.xml"))) {
			return files.findFirst()
					.orElseThrow(() -> new RuntimeException("No DDI file found in " + ddiPath));
		}
	}

	/**
	 * Get the path of the folder where the data files are stored
	 * @param campaign name of campaign
	 * @param dataSource folder of the mode (ENQ, WEB...)
	 * @return Path of the data folder
	 */
	public String getDataFolder(String campaign, String dataSource) {
		return  String.format("%s/%s/%s/%s", dataFolderSource, "IN", dataSource, campaign);
	}

	/**
	 * Get the path of the folder where the data files are stored
	 * @param campaign name of campaign
	 * @param dataSource folder of the mode (ENQ, WEB...)
	 * @param rootDataFolder folder of data (ex: differential/complete)
	 * @return Path of the data folder
	 */
	public String getDataFolder(String campaign, String dataSource, String rootDataFolder) {
		return  String.format("%s/%s/%s/%s/%s", dataFolderSource, "IN", dataSource, campaign, rootDataFolder);
	}


	/**
	 * Get the path of the folder where the specifications files are stored
	 * @return Path of the specifications folder
	 */
	public String getSpecFolder() {
		return  String.format("%s/%s", specFolderSource, "specs");
	}

	/**
	 * Get the path of the folder where the specifications files are stored for specific campaign
	 * @param campaign name of campaign
	 * @return Path of the specifications folder
	 */
	public String getSpecFolder(String campaign) {
		return  String.format("%s/%s/%s", specFolderSource, "specs", campaign);
	}

	/**
	 * Get the path of the folder where the files are stored after processing
	 * @param campaign
	 * @param dataSource
	 * @return Path of the done folder
	 */
	public String getDoneFolder(String campaign, String dataSource) {
		return  String.format("%s/%s/%s/%s", dataFolderSource, "DONE", dataSource, campaign);
	}

	/**
	 * Write a text file.
	 * @param filePath Path to the file.
	 * @param fileContent Content of the text file.
	 */
	public void writeFile(Path filePath, String fileContent) {
		Path path = Path.of(dataFolderSource, filePath.toString());
		boolean fileCreated = false;
		File myFile = null;
		try {
			Files.createDirectories(path.getParent());
			myFile = path.toFile();
			fileCreated = myFile.createNewFile();
		} catch (IOException e) {
			log.error("Permission refused to create folder: " + path.getParent(), e);
		}
		if (!fileCreated){return ;}
		try (FileWriter myWriter = new FileWriter(myFile)) {
			myWriter.write(fileContent);
			log.info(String.format("Text file: %s successfully written", filePath));
		} catch (IOException e) {
			log.warn(String.format("Error occurred when trying to write file: %s", filePath), e);
		}
	}

	/**
	 * Appends a JSON object array into file.
	 * Creates the files if it doesn't exist
	 * @param filePath Path to the file.
	 * @param responsesStream Stream of SurveyUnitUpdateDto to write
	 */
	public void writeSuUpdatesInFile(Path filePath, Stream<SurveyUnitUpdateDto> responsesStream) throws IOException {
		Files.createDirectories(filePath.getParent());
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), true))) {
			writer.write("[");
			responsesStream.forEach(response -> {
				try {
					writer.write(objectMapper.writeValueAsString(response));
					writer.write(",");
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			writer.write("{}]");
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

	/**
	 * List all folders in the specs folder
	 * @return List of specs folders
	 */
	public List<File> listAllSpecsFolders() {
		File[] objs = new File(getSpecFolder()).listFiles();
		if (objs == null) {
			return List.of();
		}
		return Arrays.stream(objs)
			.filter(File::isDirectory)
			.toList();
	}
}
