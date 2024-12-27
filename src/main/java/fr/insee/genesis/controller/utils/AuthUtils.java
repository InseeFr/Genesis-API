package fr.insee.genesis.controller.utils;

import fr.insee.genesis.configuration.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {
    private final Config config;

    @Autowired
    public AuthUtils(Config config){
        this.config = config;
    }

    /**
     * Extract IDEP from OIDC token
     * @return the IDEP from the OIDC if the app uses OIDC auth, null otherwise
     */
    public String getIDEP(){
        if(config.getAuthType().equals("OIDC")){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return  authentication
                    .getName()
                    .substring(authentication.getName().lastIndexOf(":") + 1);
        }
        return null;
    }
}
