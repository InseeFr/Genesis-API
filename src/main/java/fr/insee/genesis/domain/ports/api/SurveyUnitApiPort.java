package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.CampaignWithQuestionnaire;
import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.SurveyUnitSimplifiedDto;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


public interface SurveyUnitApiPort {

    void saveSurveyUnits(List<SurveyUnitModel> suList);

    List<SurveyUnitModel> findByIdsInterrogationAndCollectionInstrument(String interrogationId, String collectionInstrumentId);

    List<SurveyUnitModel> findByIdsUsualSurveyUnitAndCollectionInstrument(String usualSurveyUnitId, String collectionInstrumentId);

    List<SurveyUnitModel> findByInterrogationId(String interrogationId);

    List<SurveyUnitModel> findLatestByIdAndByCollectionInstrumentId(String interrogationId, String collectionInstrumentId);

    SurveyUnitSimplifiedDto findSimplifiedByCollectionInstrumentIdAndInterrogationId(
            String collectionInstrumentId,
            String interrogationId,
            Mode mode
    ) throws NoDataException;

    List<SurveyUnitSimplifiedDto> findSimplifiedByCollectionInstrumentIdAndInterrogationIdList(
            String collectionInstrumentId,
            List<InterrogationId> interrogationIds
    );


    //========= OPTIMISATIONS PERFS (START) ==========
    List<List<SurveyUnitModel>> findLatestByIdAndByQuestionnaireIdAndModeOrdered(String questionnaireId, String mode, List<InterrogationId> interrogationIds);
    //========= OPTIMISATIONS PERFS (END) ==========

    SurveyUnitDto findLatestValuesByStateByIdAndByCollectionInstrumentId(String interrogationId, String collectionInstrumentId) throws GenesisException;

    List<SurveyUnitModel> findInterrogationIdsAndModesByQuestionnaireId(String questionnaireId);

    List<InterrogationId> findDistinctInterrogationIdsByQuestionnaireId(String questionnaireId);

    List<InterrogationId> findDistinctInterrogationIdsByQuestionnaireIdAndDateAfter(String questionnaireId, LocalDateTime since);

    List<InterrogationId> findDistinctInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(String collectionInstrumentId, LocalDateTime start, LocalDateTime end);

    //========= OPTIMISATIONS PERFS (START) ==========
    long countResponsesByCollectionInstrumentId(String questionnaireId);

    List<InterrogationId> findDistinctPageableInterrogationIdsByQuestionnaireId(String questionnaireId,
                                                                                long totalSize, long blockSize, long page);

    List<Mode> findModesByQuestionnaireIdV2(String questionnaireId);
    //========= OPTIMISATIONS PERFS (END) ==========

    List<Mode> findModesByCollectionInstrumentId(String collectionInstrumentId);

    Long deleteByCollectionInstrumentId(String collectionInstrumentId);

    long countResponses();

    Set<String> findDistinctQuestionnairesAndCollectionInstrumentIds();

    List<SurveyUnitModel> parseEditedVariables(SurveyUnitInputDto surveyUnitInputDto,
                                         String userIdentifier,
                                         VariablesMap variablesMap) throws GenesisException;

    String findQuestionnaireIdByInterrogationId(String interrogationId) throws GenesisException;

    long countResponsesByQuestionnaireId(String questionnaireId);

    long countDistinctInterrogationIdsByQuestionnaireAndCollectionInstrumentId(String id);
}