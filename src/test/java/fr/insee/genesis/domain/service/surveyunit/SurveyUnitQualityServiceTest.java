package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.VariableInputDto;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.utils.DataVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyUnitQualityServiceUnitTest {

    private SurveyUnitQualityService surveyUnitQualityService;

    @BeforeEach
    void init() {
        surveyUnitQualityService = new SurveyUnitQualityService();
    }

    @Nested
    @DisplayName("verifySurveyUnits tests")
    class VerifySurveyUnitsTests {

        @Test
        @DisplayName("Should call dataVerifier")
        void verifySurveyUnits_shouldDelegateToDataVerifier() {
            // GIVEN
            List<SurveyUnitModel> surveyUnitModels = List.of(new SurveyUnitModel());
            VariablesMap variablesMap = new VariablesMap();

            try (MockedStatic<DataVerifier> dataVerifierMock = mockStatic(DataVerifier.class)) {
                // WHEN
                surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);

                // THEN
                dataVerifierMock.verify(() ->
                        DataVerifier.verifySurveyUnits(surveyUnitModels, variablesMap)
                );
            }
        }

        @Test
        @DisplayName("Should handle empty lists of surveyUnitModels")
        void verifySurveyUnits_shouldHandleEmptyList() {
            // GIVEN
            VariablesMap variablesMap = new VariablesMap();

            try (MockedStatic<DataVerifier> dataVerifierMock = mockStatic(DataVerifier.class)) {
                // WHEN
                surveyUnitQualityService.verifySurveyUnits(Collections.emptyList(), variablesMap);

                // THEN
                dataVerifierMock.verify(() ->
                        DataVerifier.verifySurveyUnits(Collections.emptyList(), variablesMap)
                );
            }
        }
    }

    @Nested
    @DisplayName("checkVariablesPresentInMetadata tests")
    class CheckVariablesPresentInMetadataTests {

        @Test
        @DisplayName("Should return empty list if all variables present")
        void checkVariablesPresentInMetadata_shouldReturnEmpty_whenAllPresent() {
            // GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);
            VariableInputDto var1 = VariableInputDto.builder()
                    .variableName("VAR1")
                    .build();
            VariableInputDto var2 = VariableInputDto.builder()
                    .variableName("VAR2")
                    .build();
            when(variablesMap.hasVariable("VAR1")).thenReturn(true);
            when(variablesMap.hasVariable("VAR2")).thenReturn(true);

            // WHEN
            List<String> result = surveyUnitQualityService.checkVariablesPresentInMetadata(
                    List.of(var1, var2), variablesMap
            );

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return absent variables")
        void checkVariablesPresentInMetadata_shouldReturnAbsentVariables() {
            // GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);
            VariableInputDto var1 = VariableInputDto.builder()
                    .variableName("VAR_PRESENT")
                    .build();
            VariableInputDto var2 = VariableInputDto.builder()
                    .variableName("VAR_ABSENT")
                    .build();
            when(variablesMap.hasVariable("VAR_PRESENT")).thenReturn(true);
            when(variablesMap.hasVariable("VAR_ABSENT")).thenReturn(false);

            // WHEN
            List<String> result = surveyUnitQualityService.checkVariablesPresentInMetadata(
                    List.of(var1, var2), variablesMap
            );

            // THEN
            assertThat(result).containsExactly("VAR_ABSENT");
        }

        @Test
        @DisplayName("Should return all variables if all variables are absent")
        void checkVariablesPresentInMetadata_shouldReturnAll_whenNonePresent() {
            // GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);
            VariableInputDto var1 = VariableInputDto.builder()
                    .variableName("VAR1")
                    .build();
            VariableInputDto var2 = VariableInputDto.builder()
                    .variableName("VAR2")
                    .build();
            when(variablesMap.hasVariable("VAR1")).thenReturn(false);
            when(variablesMap.hasVariable("VAR2")).thenReturn(false);

            // WHEN
            List<String> result = surveyUnitQualityService.checkVariablesPresentInMetadata(
                    List.of(var1, var2), variablesMap
            );

            // THEN
            assertThat(result).containsExactlyInAnyOrder("VAR1", "VAR2");
        }

        @Test
        @DisplayName("Should return empty list if called with empty list")
        void checkVariablesPresentInMetadata_shouldReturnEmpty_whenInputIsEmpty() {
            // GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);

            // WHEN
            List<String> result = surveyUnitQualityService.checkVariablesPresentInMetadata(
                    Collections.emptyList(), variablesMap
            );

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should preserve order of present variables")
        void checkVariablesPresentInMetadata_shouldPreserveOrder() {
            // GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);
            List<VariableInputDto> inputs = List.of(
                    VariableInputDto.builder()
                            .variableName("C")
                            .build(),
                    VariableInputDto.builder()
                            .variableName("A")
                            .build(),
                    VariableInputDto.builder()
                            .variableName("B")
                            .build()
            );
            inputs.forEach(v -> when(variablesMap.hasVariable(v.getVariableName())).thenReturn(false));

            // WHEN
            List<String> result = surveyUnitQualityService.checkVariablesPresentInMetadata(inputs, variablesMap);

            // THEN
            assertThat(result).containsExactly("C", "A", "B");
        }
    }
}