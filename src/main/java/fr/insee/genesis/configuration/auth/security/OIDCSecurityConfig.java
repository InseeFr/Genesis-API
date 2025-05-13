package fr.insee.genesis.configuration.auth.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

    @Value("${fr.insee.genesis.security.resourceserver.jwt.issuer-uri}")
    String issuerUri;

    @Value("${fr.insee.genesis.security.resourceserver.dmz.jwt.issuer-uri}")
    String issuerUriDmz;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtIssuerAuthenticationManagerResolver authenticationManagerResolver) throws Exception {
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
             //   .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
                .oauth2ResourceServer(
                    oauth2 -> oauth2.authenticationManagerResolver(authenticationManagerResolver));
        return http.build();
    }


    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setPrincipalClaimName(inseeSecurityTokenProperties.getOidcClaimUsername());
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }



    @Bean
    public JwtIssuerAuthenticationManagerResolver authenticationManagerResolver() {
        final List<String> issuers = List.of(issuerUri,issuerUriDmz);
        Map<String, AuthenticationManager> authenticationManagers = new HashMap<>();

        for (String issuer : issuers) {
            NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                    .withJwkSetUri(issuer + "/protocol/openid-connect/certs")
                    .build();

            JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtDecoder);
            provider.setJwtAuthenticationConverter(jwtAuthenticationConverter());

            AuthenticationManager manager = new ProviderManager(provider);
            authenticationManagers.put(issuer, manager);
        }
        return new JwtIssuerAuthenticationManagerResolver(authenticationManagers::get);
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
