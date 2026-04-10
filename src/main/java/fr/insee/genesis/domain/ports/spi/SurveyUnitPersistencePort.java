package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface SurveyUnitPersistencePort {

    void saveAll(List<SurveyUnitModel> suList);

    List<SurveyUnitModel> findByIds(String interrogationId, String collectionInstrumentId);

    List<SurveyUnitModel> findByUsualSurveyUnitAndCollectionInstrumentIds(String usualSurveyUnitId, String collectionInstrumentId);

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    List<SurveyUnitModel> findBySetOfIdsAndQuestionnaireIdAndMode(String questionnaireId, String mode, List<String> interrogationIdSet);
    //========= OPTIMISATIONS PERFS (START) ==========

    List<SurveyUnitModel> findByInterrogationId(String interrogationId);

    List<SurveyUnitModel> findByInterrogationIdsAndQuestionnaireId(List<SurveyUnitModel> interrogationIds, String questionnaireId);

    Stream<SurveyUnitModel> findByQuestionnaireId(String questionnaireId);

    List<SurveyUnitModel> findInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId);

    List<SurveyUnitModel> findInterrogationIdsByQuestionnaireIdAndDateAfter(String questionnaireId, LocalDateTime since);

    List<SurveyUnitModel> findInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(
            String collectionInstrumentId,
            LocalDateTime start,
            LocalDateTime end
    );

    //======== OPTIMISATIONS PERFS (START) ========
    long countByCollectionInstrumentId(String collectionInstrumentId);

    List<SurveyUnitModel> findPageableInterrogationIdsByQuestionnaireId(String questionnaireId, Long skip, Long limit);

    List<SurveyUnitModel> findModesByQuestionnaireIdV2(String questionnaireId);
    //======= OPTIMISATIONS PERFS (END) =========


    Long deleteByCollectionInstrumentId(String collectionInstrumentId);

    long count();

    Set<String> findDistinctQuestionnairesAndCollectionInstrumentIds();

    long countByQuestionnaireId(String questionnaireId);

    long countDistinctInterrogationIdsByQuestionnaireAndCollectionInstrumentId(String id);
}
