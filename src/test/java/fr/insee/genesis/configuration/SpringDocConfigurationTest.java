package fr.insee.genesis.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = {SpringDocConfiguration.class, Config.class})
class SpringDocConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testNoAuthOpenAPIBeanCreation() {
        System.setProperty("fr.insee.genesis.authentication", "NONE");

        OpenAPI openAPI = applicationContext.getBean(OpenAPI.class);
        assertNotNull(openAPI);
        assertEquals("Genesis API", openAPI.getInfo().getTitle());
    }

    @Test
    void testOidcOpenAPIBeanCreation() {
        System.setProperty("fr.insee.genesis.authentication", "OIDC");

        OpenAPI openAPI = applicationContext.getBean(OpenAPI.class);
        assertNotNull(openAPI);
        assertEquals("Genesis API", openAPI.getInfo().getTitle());

        // Check that security schemes are added
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey(SpringDocConfiguration.BEARERSCHEME));
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey(SpringDocConfiguration.OAUTH2SCHEME));
    }
}
