package fr.insee.genesis.exceptions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class NoDataExceptionTest {
    @Test
    void constructorTest(){
        //GIVEN
        String message = "test";
        NoDataException noDataException = new NoDataException(message);

        //WHEN + THEN
        Assertions.assertThat(noDataException.getMessage()).isEqualTo(message);
    }
}