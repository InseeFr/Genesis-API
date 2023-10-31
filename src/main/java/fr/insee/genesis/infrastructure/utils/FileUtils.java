package fr.insee.genesis.infrastructure.utils;

import fr.insee.genesis.configuration.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileUtils {

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
	 * @param filename Name of the file to move.
	 */
	public void moveFiles(String from, String destination ,String filename) throws IOException {
		if (!isFolderPresent(destination)) {
			Files.createDirectories(Path.of(destination));
		}
		Files.move(Path.of(from+"/"+filename),Path.of(destination+"/"+filename));
		log.info("File {} moved from {} to {}", filename, from, destination);
	}

	/**
	 * Move a data file to the folder done
	 * @param campaign Name of the campaign (also folder name)
	 * @param dataSource Application the data came from
	 * @param filename Data file to move
	 * @throws IOException
	 */
	public void moveDataFile(String campaign, String dataSource, String filename) throws IOException {
		String from = getDataFolder(campaign, dataSource);
		String destination = getDoneFolder(campaign, dataSource);
		moveFiles(from, destination, filename);
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
		try (Stream<Path> files = Files.find(Path.of(String.format("%s/%s",getSpecFolder(campaign),mode)), 1, (path, basicFileAttributes) -> path.toFile().getName().matches("ddi[\\w,\\s-]+\\.xml"))) {
			return files.findFirst()
					.orElseThrow(() -> new RuntimeException("No DDI file found in " + String.format("%s/%s",getSpecFolder(campaign),mode)));
		}
	}

	/**
	 * Get the path of the folder where the data files are stored
	 * @param campaign
	 * @param dataSource
	 * @return Path of the data folder
	 */
	public String getDataFolder(String campaign, String dataSource) {
		return  String.format("%s/%s/%s/%s", dataFolderSource, "IN", dataSource, campaign);
	}

	/**
	 * Get the path of the folder where the specifications files are stored
	 * @param campaign
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
}
