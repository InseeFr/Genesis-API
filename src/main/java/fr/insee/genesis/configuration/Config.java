package fr.insee.genesis.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@Getter
public class Config {

	/******************************************************/
	/********** Configuration properties		***********/
	/******************************************************/

	@Value("${fr.insee.genesis.sourcefolder.data}")
	private String dataFolderSource;

	@Value("${fr.insee.genesis.sourcefolder.specifications}")
	private String specFolderSource;

	private final String logFolder;

	//Extract log folder from log filename property
	public Config(@Value("${logging.file.name}") String logFileName) {
		Path logFileNamePath = Path.of(logFileName);
		if(logFileNamePath.getFileName().toString().contains(".")){
			this.logFolder = logFileNamePath.getParent().toString();
		}else{
			this.logFolder = logFileName;
		}
	}
}
