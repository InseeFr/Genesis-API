package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SurveyUnitUpdatePersistencePortStub implements SurveyUnitUpdatePersistencePort {
    //TODO Maybe upgrade this stub to just interact with the mongoStub list
    List<SurveyUnitUpdateDto> mongoStub = new ArrayList<>();

    @Override
    public void saveAll(List<SurveyUnitUpdateDto> suList) {
        mongoStub.addAll(suList);
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIds(String idUE, String idQuest) {
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        surveyUnitUpdateDtoList.add(
                SurveyUnitUpdateDto.builder()
                        .idCampaign("TESTIDCAMPAIGN")
                        .mode(Mode.WEB)
                        .idUE(idUE)
                        .idQuest(idQuest)
                        .state(DataState.COLLECTED)
                        .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                        .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                        .externalVariables(externalVariableDtoList)
                        .collectedVariables(collectedVariableDtoList)
                        .build()
        );

        return surveyUnitUpdateDtoList;
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdUE(String idUE) {
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        surveyUnitUpdateDtoList.add(
                SurveyUnitUpdateDto.builder()
                        .idCampaign("TESTIDCAMPAIGN")
                        .mode(Mode.WEB)
                        .idUE(idUE)
                        .idQuest("TESTQUESTIONNAIRE")
                        .state(DataState.COLLECTED)
                        .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                        .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                        .externalVariables(externalVariableDtoList)
                        .collectedVariables(collectedVariableDtoList)
                        .build()
        );

        return surveyUnitUpdateDtoList;
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdUEsAndIdQuestionnaire(List<SurveyUnitDto> idUEs, String idQuestionnaire) {
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        surveyUnitUpdateDtoList.add(
                SurveyUnitUpdateDto.builder()
                        .idCampaign("TESTIDCAMPAIGN")
                        .mode(Mode.WEB)
                        .idUE(idUEs.get(0).getIdUE())
                        .idQuest(idQuestionnaire)
                        .state(DataState.COLLECTED)
                        .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                        .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                        .externalVariables(externalVariableDtoList)
                        .collectedVariables(collectedVariableDtoList)
                        .build()
        );

        return surveyUnitUpdateDtoList;
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        surveyUnitUpdateDtoList.add(
                SurveyUnitUpdateDto.builder()
                        .idCampaign("TESTIDCAMPAIGN")
                        .mode(Mode.WEB)
                        .idUE("TESTIDUE")
                        .idQuest(idQuestionnaire)
                        .state(DataState.COLLECTED)
                        .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                        .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                        .externalVariables(externalVariableDtoList)
                        .collectedVariables(collectedVariableDtoList)
                        .build()
        );

        return surveyUnitUpdateDtoList;
    }

    @Override
    public List<SurveyUnitDto> findIdUEsByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> surveyUnitDtoList = new ArrayList<>();
        surveyUnitDtoList.add(SurveyUnitDto.builder()
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .build()
        );

        return surveyUnitDtoList;
    }
}
