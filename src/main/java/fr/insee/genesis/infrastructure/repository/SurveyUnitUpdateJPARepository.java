package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.model.entity.SurveyUnitUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyUnitUpdateJPARepository extends JpaRepository<SurveyUnitUpdate, Long> {

	List<SurveyUnitUpdate> findByIdUE(String idUE);

	List<SurveyUnitUpdate> findByIdQuestionnaire(String idQuestionnaire);

	List<SurveyUnitUpdate> findByIdUEAndIdQuestionnaire(String idUE, String idQuestionnaire);


}
