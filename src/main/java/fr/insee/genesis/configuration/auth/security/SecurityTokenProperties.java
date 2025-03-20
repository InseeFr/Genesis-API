package fr.insee.genesis.configuration.auth.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(
        prefix = "fr.insee.genesis.security.token"
)
@Data
public class SecurityTokenProperties {

    //Chemin pour récupérer la liste des rôles dans le jwt (token)
    private String oidcClaimRole;
    //Chemin pour récupérer le username dans le jwt (token)
    private String oidcClaimUsername;

}
