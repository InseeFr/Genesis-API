package fr.insee.genesis.configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Slf4j
@Getter
public class Config {

	/******************************************************/
	/********** Configuration properties		***********/
	/******************************************************/

	@Value("${fr.insee.genesis.files.source}")
	private String genesisFolder;

	@Value("${fr.insee.genesis.persistence.implementation}")
	private String persistenceImplementation;


}
