package fr.insee.genesis.configuration.auth.security;

import fr.insee.genesis.configuration.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@Slf4j
@ConditionalOnProperty(name = "fr.insee.genesis.authentication", havingValue = "OIDC")
public class OIDCSecurityConfig {

    Config config;
    @Autowired
    public OIDCSecurityConfig(Config config) {
        this.config = config;
    }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            for (var pattern : config.getWhiteList()) {
                http.authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(AntPathRequestMatcher.antMatcher(pattern)).permitAll()
                );
            }
            http
                    .authorizeHttpRequests(configurer -> configurer
                            .anyRequest().authenticated()
                    )
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
            return http.build();
        }

}
