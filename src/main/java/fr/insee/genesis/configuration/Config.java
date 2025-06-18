package fr.insee.genesis.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@Getter
@EnableCaching
public class Config {

	/******************************************************/
	/********** Configuration properties		***********/
	/******************************************************/

	@Value("${fr.insee.genesis.sourcefolder.data}")
	private String dataFolderSource;

	@Value("${fr.insee.genesis.sourcefolder.specifications}")
	private String specFolderSource;

	@Value("${fr.insee.genesis.authentication}")
	private String authType;

	@Value("${fr.insee.genesis.oidc.auth-server-url}")
	private String authServerUrl;

	@Value("${fr.insee.genesis.oidc.realm}")
	private String realm;

	@Value("${fr.insee.genesis.security.token.oidc-claim-role}")
	private String oidcClaimRole;

	@Value("${fr.insee.genesis.security.token.oidc-claim-username}")
	private String oidcClaimUsername;

	@Value("#{'${fr.insee.genesis.security.whitelist-matchers}'.split(',')}")
	private String[] whiteList;

	@Value("${fr.insee.genesis.survey-quality-tool.url}")
	private String surveyQualityToolUrl;

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
