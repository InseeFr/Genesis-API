package fr.insee.genesis.controller.utils;

import fr.insee.genesis.configuration.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    public String getIDEP() {
        if ("OIDC".equals(config.getAuthType())) {
            return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                    .map(Authentication::getName)
                    .map(name -> name.substring(name.lastIndexOf(":") + 1))
                    .orElse(null);
        }
        return null;
    }
}
