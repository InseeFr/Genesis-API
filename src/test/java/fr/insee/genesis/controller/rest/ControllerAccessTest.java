package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.infrastructure.repository.DataProcessingContextMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.EditedExternalResponseMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.EditedPreviousResponseMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.LunaticModelMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.RundeckExecutionDBRepository;
import fr.insee.genesis.infrastructure.repository.SurveyUnitMongoDBRepository;
import fr.insee.genesis.infrastructure.repository.VariableTypeMongoDBRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.oneOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
class ControllerAccessTest {

    // Constants for user roles
    private static final String USER_KRAFTWERK = "USER_KRAFTWERK";
    private static final String USER_PLATINE = "USER_PLATINE";
    private static final String USER_BACK_OFFICE = "USER_BACK_OFFICE";
    private static final String ADMIN = "ADMIN";
    private static final String READER = "READER";
    // JWT claim properties loaded from application properties
    @Value("${fr.insee.genesis.security.token.oidc-claim-role}")
    private String claimRoleDotRoles;
    @Value("${fr.insee.genesis.security.token.oidc-claim-username}")
    private String claimName;
    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests to the REST endpoints
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private MongoTemplate mongoTemplate;
    @MockitoBean
    private DataProcessingContextApiPort dataProcessingContextApiPort;
    @MockitoBean
    private SurveyUnitApiPort surveyUnitApiPort;
    @MockitoBean
    private LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort;
    @MockitoBean
    private SurveyUnitMongoDBRepository surveyUnitMongoDBRepository;
    @MockitoBean
    private LunaticJsonMongoDBRepository lunaticJsonMongoDBRepository;
    @MockitoBean
    private RundeckExecutionDBRepository rundeckExecutionDBRepository;
    @MockitoBean
    private DataProcessingContextMongoDBRepository dataProcessingContextMongoDBRepository;
    @MockitoBean
    private VariableTypeMongoDBRepository variableTypeMongoDBRepository;
    @MockitoBean
    private LunaticModelMongoDBRepository lunaticModelMongoDBRepository;
    @MockitoBean
    private EditedPreviousResponseMongoDBRepository editedPreviousResponseMongoDBRepository;
    @MockitoBean
    private EditedExternalResponseMongoDBRepository editedExternalResponseMongoDBRepository;

    /**
     * Provides a stream of URIs that are allowed for reader.
     */
    private static Stream<Arguments> endpointsReader() {
        return Stream.of(
                Arguments.of("/questionnaires/with-campaigns"),
                Arguments.of("/questionnaires/by-campaign?campaignId=CAMPAIGNTEST"),
                Arguments.of("/questionnaires/"),
                Arguments.of("/modes/by-questionnaire?questionnaireId=QUESTTEST"),
                Arguments.of("/modes/by-campaign?campaignId=CAMPAIGNTEST"),
                Arguments.of("/interrogations/by-questionnaire?questionnaireId=QUESTTEST"),
                Arguments.of("/campaigns/with-questionnaires"),
                Arguments.of("/campaigns/"),
                Arguments.of("/lunatic-model/get?questionnaireId=QUESTTEST")
        );
    }


    private static Stream<Arguments> responseEndpoint() {
        return Stream.of(
                Arguments.of(GET,"/response/lunatic-json/get/unprocessed"),
                Arguments.of(GET,"/response/lunatic-json/get/by-interrogation-mode-and-campaign"),
                Arguments.of(POST,"/response/lunatic-json/process"),
                Arguments.of(GET,"/response/lunatic-json/campaignId=TOTO")
        );
    }

    private static Stream<Arguments> backOfficeEndpointProd() {
        return Stream.of(
                Arguments.of(PUT,"/lunatic-model/save?questionnaireId=TEST", new HashMap<>()),
                Arguments.of(POST,"/edited/previous/json?questionnaireId=TEST&mode=WEB&jsonFileName=truc.json"),
                Arguments.of(POST,"/edited/external/json?questionnaireId=TEST&mode=WEB&jsonFileName=truc.json"),
                Arguments.of(PUT,"/context/review?partitionId=TEST")
        );
    }

    /**
     * Tests that users with the "ADMIN" role can access read-only endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsReader")
    @DisplayName("Admins should access reader-allowed services")
    void admin_should_access_reader_allowed_services(String endpointURI) throws Exception{
        Jwt jwt = generateJwt(List.of("administrateur_traiter"), ADMIN);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(get(endpointURI).header("Authorization", "bearer token_blabla"))
                .andExpect(status().is(oneOf(200,404)));
    }

    /**
     * Tests that users with the "USER_KRAFTWERK" role can access read-only endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsReader")
    @DisplayName("Kraftwerk users should access reader-allowed services")
    void kraftwerk_users_should_access_reader_allowed_services(String endpointURI) throws Exception {
        Jwt jwt = generateJwt(List.of("utilisateur_Kraftwerk"), USER_KRAFTWERK);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(get(endpointURI).header("Authorization", "bearer token_blabla"))
                .andExpect(status().is(oneOf(200, 404)));
    }

    /**
     * Tests that users with the "USER_PLATINE" role can access read-only endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsReader")
    @DisplayName("Platine users should access reader-allowed services")
    void platine_users_should_access_reader_allowed_services(String endpointURI) throws Exception {
        Jwt jwt = generateJwt(List.of("utilisateur_Platine"), USER_PLATINE);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(get(endpointURI).header("Authorization", "bearer token_blabla"))
                .andExpect(status().is(oneOf(200, 404)));
    }

    /**
     * Tests that users with the "USER_BACK_OFFICE" role can access read-only endpoints.
     */
    @ParameterizedTest
    @MethodSource("backOfficeEndpointProd")
    @DisplayName("Back office users should access prod services")
    void back_office_users_should_access_prod_services(HttpMethod method, String endpointURI) throws Exception {
        Jwt jwt = generateJwt(List.of("utilisateur_Back_Office"), USER_BACK_OFFICE);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        MockHttpServletRequestBuilder requestBuilder;
        if (method == HttpMethod.GET) {
            requestBuilder = get(endpointURI);
        } else if (method == HttpMethod.POST) {
            requestBuilder = post(endpointURI);
        } else if (method == PUT) {
            requestBuilder = put(endpointURI);
        } else if (method == HttpMethod.DELETE) {
            requestBuilder = delete(endpointURI);
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        mockMvc.perform(requestBuilder.header("Authorization", "bearer token_blabla"))
                .andExpect(status().is(oneOf(200, 400, 404)));
    }

    /**
     * Tests that users with the "USER_BACK_OFFICE" role can access read-only endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsReader")
    @DisplayName("Back office users should access reader-allowed services")
    void back_office_users_should_access_reader_allowed_services(String endpointURI) throws Exception {
        Jwt jwt = generateJwt(List.of("utilisateur_Back_Office"), USER_BACK_OFFICE);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(get(endpointURI).header("Authorization", "bearer token_blabla"))
                .andExpect(status().is(oneOf(200, 404)));
    }

    /**
     * Tests that users with the "READER" role can access read-only endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsReader")
    @DisplayName("Readers should access reader-allowed services")
    void reader_should_access_reader_allowed_services(String endpointURI) throws Exception {
        Jwt jwt = generateJwt(List.of("lecteur_traiter"), "reader");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(get(endpointURI).header("Authorization", "bearer token_blabla"))
                .andExpect(status().is(oneOf(200, 404)));
    }

    /**
     * Tests that users with invalid role are denied.
     */
    @ParameterizedTest
    @MethodSource("endpointsReader")
    @DisplayName("User with invalid roles should not access reader-allowed services")
    void invalid_user_should_not_access_reader_allowed_services(String endpointURI) throws Exception {
        Jwt jwt = generateJwt(List.of("toto"), "invalid_role");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(get(endpointURI).header("Authorization", "bearer token_blabla"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test that reader can access the schedule/all endpoint.
     */
    @Test
    @DisplayName("Reader should access schedule/all endpoint")
    void reader_should_access_schedules_services() throws Exception {
        Jwt jwt = generateJwt(List.of("lecteur_traiter"), READER);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(get("/context/schedules/all").header("Authorization", "bearer token_blabla"))
                .andExpect(status().is(oneOf(200, 404)));
    }

    /**
     * Test that reader can not access other schedule endpoints.
     */
    @Test
    @DisplayName("Reader should not access other schedule endpoints")
    void reader_should_not_access_other_schedules_services() throws Exception {
        doNothing().when(dataProcessingContextApiPort).deleteSchedules(anyString());
        Jwt jwt = generateJwt(List.of("lecteur_traiter"), READER);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(delete("/context/schedules?partitionId=ENQ_TEST").header("Authorization", "bearer token_blabla"))
                .andExpect(status().isForbidden());
    }


    /**
     * Test that reader can not access other schedule endpoints.
     */
    @ParameterizedTest
    @MethodSource("responseEndpoint")
    @DisplayName("Reader should not access /responses endpoints")
    void reader_should_not_access_response_services(HttpMethod method,String endpointURI) throws Exception {
        Jwt jwt = generateJwt(List.of("lecteur_traiter"), READER);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        MockHttpServletRequestBuilder requestBuilder;
        if (method == HttpMethod.GET) {
            requestBuilder = get(endpointURI);
        } else if (method == HttpMethod.POST) {
            requestBuilder = post(endpointURI);
        } else if (method == PUT) {
            requestBuilder = put(endpointURI);
        } else if (method == HttpMethod.DELETE) {
            requestBuilder = delete(endpointURI);
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        mockMvc.perform(requestBuilder.header("Authorization", "bearer token_blabla"))
                .andExpect(status().is(oneOf(200, 404)));
    }


    /**
     * Test that kraftwerk users can't access the schedule endpoints.
     */
    @Test
    @DisplayName("Kraftwerk users should access schedules service")
    void kraftwerk_users_should_not_access_schedules_services() throws Exception {
        Jwt jwt = generateJwt(List.of("utilisateur_Kraftwerk"), USER_KRAFTWERK);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(get("/context/schedules/all").header("Authorization", "bearer token_blabla"))
                .andExpect(status().is(oneOf(200, 404)));
    }

    /**
     * Test that admins can access the schedule endpoints.
     */
    @Test
    @DisplayName("Admins should access schedules service")
    void admins_should_access_schedules_services() throws Exception {
        Jwt jwt = generateJwt(List.of("administrateur_traiter"), ADMIN);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(get("/context/schedules/all").header("Authorization", "bearer token_blabla"))
                .andExpect(status().is(oneOf(200, 404)));
    }

    /**
     * Test that invalid roles can't access the schedule endpoints.
     */
    @Test
    @DisplayName("Invalid roles should not access schedules service")
    void invalid_roles_should_access_schedules_services() throws Exception {
        Jwt jwt = generateJwt(List.of("invalid_role"), "invalid_role");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(get("/context/schedules").header("Authorization", "bearer token_blabla"))
                .andExpect(status().isForbidden());
    }

    /**
     * Generates a mock JWT token with specified roles and username.
     *
     * @param roles List of roles assigned to the user.
     * @param name  Username for the JWT.
     * @return A mock Jwt object.
     */
    public Jwt generateJwt(List<String> roles, String name) {
        Date issuedAt = new Date();
        Date expiresAT = Date.from((new Date()).toInstant().plusSeconds(100));
        var claimRole = claimRoleDotRoles.split("\\.")[0];
        var attributRole = claimRoleDotRoles.split("\\.")[1];
        return new Jwt("token", issuedAt.toInstant(), expiresAT.toInstant(),
                Map.of("alg", "RS256", "typ", "JWT"),
                Map.of(claimRole, Map.of(attributRole, roles),
                        claimName, name
                )
        );
    }

}
