package fr.insee.genesis.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

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

	@Value("${fr.insee.genesis.logfolder}")
	private String logFolder;

}
