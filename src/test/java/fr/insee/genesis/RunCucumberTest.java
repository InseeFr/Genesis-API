package fr.insee.genesis;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

@RunWith(Cucumber.class)
@SpringBootTest(classes = GenesisApi.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CucumberOptions(
        features = "src/test/resources/fr/insee/genesis/features",
        glue = {"cucumber.functional_tests", "cucumber.config"},
        plugin = {"pretty"}
)
public class RunCucumberTest {
}