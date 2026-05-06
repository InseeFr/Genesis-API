package fr.insee.genesis.domain.model.surveyunit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class InterrogationIdTest {

    @Test
    void getInterrogationId_test() {
        //GIVEN
        String identifier = "test";
        InterrogationId interrogationId = new InterrogationId(identifier);

        //WHEN
        String result = interrogationId.getInterrogationId();

        //THEN
        Assertions.assertThat(result).isEqualTo(identifier);
    }

    @Test
    void setInterrogationId_test() {
        //GIVEN
        String identifier = "test";
        InterrogationId interrogationId = new InterrogationId();

        //WHEN
        interrogationId.setInterrogationId(identifier);

        //THEN
        Assertions.assertThat(interrogationId.getInterrogationId()).isEqualTo(identifier);
    }
}