package fr.insee.genesis.configuration.auth.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
@ConditionalOnProperty(name = "fr.insee.genesis.authentication", havingValue = "OIDC")
@ConfigurationProperties(prefix = "fr.insee.genesis.security")
@RequiredArgsConstructor
public class OIDCSecurityConfig {

    @Getter
    @Setter
    private String[] whitelistMatchers;
    private static final String ROLE_PREFIX = "ROLE_";
    private final RoleConfiguration roleConfiguration;
    private final SecurityTokenProperties inseeSecurityTokenProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        for (var pattern : whitelistMatchers) {
            http.authorizeHttpRequests(authorize ->
                    authorize
                            .requestMatchers(AntPathRequestMatcher.antMatcher(pattern)).permitAll()
            );
        }
        http
                .authorizeHttpRequests(configure -> configure
                        .requestMatchers(HttpMethod.GET,"/questionnaires/**").hasRole(String.valueOf(ApplicationRole.READER))
                        .requestMatchers(HttpMethod.GET,"/modes/**").hasRole(String.valueOf(ApplicationRole.READER))
                        .requestMatchers(HttpMethod.GET,"/interrogations/**").hasRole(String.valueOf(ApplicationRole.READER))
                        .requestMatchers(HttpMethod.GET,"/campaigns/**").hasRole(String.valueOf(ApplicationRole.READER))
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setPrincipalClaimName(inseeSecurityTokenProperties.getOidcClaimUsername());
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }


    Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return new Converter<Jwt, Collection<GrantedAuthority>>() {
            @Override
            @SuppressWarnings({"unchecked"})
            public Collection<GrantedAuthority> convert(Jwt source) {

                String[] claimPath = inseeSecurityTokenProperties.getOidcClaimRole().split("\\.");
                Map<String, Object> claims = source.getClaims();
                try {
                    for (int i = 0; i < claimPath.length - 1; i++) {
                        claims = (Map<String, Object>) claims.get(claimPath[i]);
                    }
                    if (claims != null) {
                        List<String> tokenClaims = (List<String>) claims.getOrDefault(claimPath[claimPath.length - 1], List.of());
                        // Collect distinct values from mapping associated with input keys
                        List<String> claimedRoles = tokenClaims.stream()
                                .filter(roleConfiguration.getRolesByClaim()::containsKey) // Ensure the key exists in the mapping
                                .flatMap(key -> roleConfiguration.getRolesByClaim().get(key).stream()) // Get the list of values associated with the key
                                .distinct() // Remove duplicates
                                .toList();

                        return Collections.unmodifiableCollection(claimedRoles.stream().map(s -> new GrantedAuthority() {
                            @Override
                            public String getAuthority() {
                                return ROLE_PREFIX + s;
                            }

                            @Override
                            public String toString() {
                                return getAuthority();
                            }
                        }).toList());
                    }
                } catch (ClassCastException e) {
                    // role path not correctly found, assume that no role for this user
                    return List.of();
                }
                return List.of();
            }
        };
    }
}
