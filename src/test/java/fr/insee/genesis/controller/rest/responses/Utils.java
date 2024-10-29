package fr.insee.genesis.controller.rest.responses;

import cucumber.TestConstants;
import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.Variable;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


class Utils {

    //Constants
    static final String defaultIdUE = "TESTIDUE";
    static final String defaultIdQuest = "TESTIDQUESTIONNAIRE";

    static void reset(SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) throws IOException {
        //MongoDB stub management
        surveyUnitPersistencePortStub.getMongoStub().clear();

        addAdditionalDtoToMongoStub("TESTIDCAMPAIGN", defaultIdQuest ,
                LocalDateTime.of(2023, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                surveyUnitPersistencePortStub);


        //Test file management
        //Clean DONE folder
        Path testResourcesPath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY);
        if (testResourcesPath.resolve("DONE").toFile().exists())
            Files.walk(testResourcesPath.resolve("DONE"))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

        //Recreate data files
        //SAMPLETEST-PARADATA-v1
        //Root
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                .toFile().exists()
        ){
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
            );
        }
        //Differential data
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("differential")
                .resolve("data")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                .toFile().exists()
        ) {
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("differential")
                            .resolve("data")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("differential")
                            .resolve("data")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
            );
        }
        //SAMPLETEST-PARADATA-v2
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v2")
                .resolve("data.complete.validated.STPDv2.20231122164209.xml")
                .toFile().exists()
        ){
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv2.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("data.complete.partial.STPDv2.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv2.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("data.complete.validated.STPDv2.20231122164209.xml")
            );
        }
        //SAMPLETEST-NO-COLLECTED
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-NO-COLLECTED")
                .resolve("differential")
                .resolve("data")
                .resolve("data_diff_no_collected.xml")
                .toFile().exists()
        ){
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-NO-COLLECTED")
                            .resolve("data_diff_no_collected.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-NO-COLLECTED")
                            .resolve("differential")
                            .resolve("data")
                            .resolve("data_diff_no_collected.xml")
            );
        }
    }

    static void addAdditionalDtoToMongoStub(SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) {
        addAdditionalDtoToMongoStub(defaultIdQuest, surveyUnitPersistencePortStub);
    }

    static void addAdditionalDtoToMongoStub(String idQuestionnaire, SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) {
        addAdditionalDtoToMongoStub("TESTIDCAMPAIGN",idQuestionnaire, surveyUnitPersistencePortStub);
    }

    static void addAdditionalDtoToMongoStub(String idCampaign, String idQuestionnaire, SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) {
        addAdditionalDtoToMongoStub(idCampaign,idQuestionnaire,
                LocalDateTime.of(2023, 2, 2, 0, 0, 0),
                LocalDateTime.of(2024, 2, 2, 0, 0, 0),
                surveyUnitPersistencePortStub);
    }

    static void addAdditionalDtoToMongoStub(String idCampaign, String idQuestionnaire,
                                             LocalDateTime fileDate, LocalDateTime recordDate,
                                             SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .idCampaign(idCampaign)
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(idQuestionnaire)
                .state(DataState.COLLECTED)
                .fileDate(fileDate)
                .recordDate(recordDate)
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }



    static void addAdditionalDtoToMongoStub(DataState state,
                                            String collectedVariableValue,
                                            String externalVariableValue,
                                            LocalDateTime fileDate,
                                            LocalDateTime recordDate,
                                            SurveyUnitPersistencePortStub surveyUnitPersistencePortStub) {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{externalVariableValue})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{collectedVariableValue}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(defaultIdQuest)
                .state(state)
                .fileDate(fileDate)
                .recordDate(recordDate)
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

}
