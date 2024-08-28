package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnit;
import fr.insee.genesis.infrastructure.model.document.surveyunit.ExternalVariable;
import fr.insee.genesis.infrastructure.model.document.surveyunit.VariableState;
import fr.insee.genesis.infrastructure.model.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.repository.SurveyUnitMongoDBRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyUnitMongoAdapterTest {

	@Mock
	SurveyUnitMongoDBRepository mongoRepository;

	@InjectMocks
	static SurveyUnitMongoAdapter surveyUnitMongoAdapter;

	static SurveyUnitDocument suDoc;
	static SurveyUnitDocument suDoc2;
	static SurveyUnitDocument suDoc3;

	@BeforeAll
	static void setUp() {
		surveyUnitMongoAdapter = new SurveyUnitMongoAdapter();
		suDoc= new SurveyUnitDocument();
		suDoc.setIdCampaign("idCampaign");
		suDoc.setIdUE("UE1100000001");
		suDoc.setIdQuestionnaire("TEST2023X01");
		suDoc.setState("COLLECTED");
		suDoc.setMode("WEB");
		suDoc.setRecordDate(LocalDateTime.now());
		suDoc.setCollectedVariables(List.of(new VariableState()));
		suDoc.setExternalVariables(List.of(new ExternalVariable()));

		suDoc2= new SurveyUnitDocument();
		suDoc2.setIdCampaign("idCampaign");
		suDoc2.setIdUE("UE1100000001");
		suDoc2.setIdQuestionnaire("TEST2023X01");
		suDoc2.setState("COLLECTED");
		suDoc2.setMode("TEL");
		suDoc2.setRecordDate(LocalDateTime.now());
		suDoc2.setCollectedVariables(List.of(new VariableState()));
		suDoc2.setExternalVariables(List.of(new ExternalVariable()));

		suDoc3= new SurveyUnitDocument();
		suDoc3.setIdCampaign("idCampaign");
		suDoc3.setIdUE("UE1100000002");
		suDoc3.setIdQuestionnaire("TEST2023X01");
		suDoc3.setState("COLLECTED");
		suDoc3.setMode("WEB");
		suDoc3.setRecordDate(LocalDateTime.now());
		suDoc3.setCollectedVariables(List.of(new VariableState()));
		suDoc3.setExternalVariables(List.of(new ExternalVariable()));
	}

	@Test
	void shouldReturnListOfSurveyUnitDto_IfIdsFoundInDataBase() {
		//Given
		List<SurveyUnitDocument> responses = new ArrayList<>();
		responses.add(suDoc);
		responses.add(suDoc2);
		when(mongoRepository.findByIdUEAndIdQuestionnaire(any(String.class), any(String.class))).thenReturn(responses);
		// When
		List<SurveyUnit> updates = surveyUnitMongoAdapter.findByIds("UE1100000001", "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(2);
		Assertions.assertThat(updates.getFirst().getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnEmptyList_IfIdsNotFoundInDataBase() {
		//Given
		when(mongoRepository.findByIdUEAndIdQuestionnaire(any(String.class), any(String.class))).thenReturn(List.of());
		// When
		List<SurveyUnit> updates = surveyUnitMongoAdapter.findByIds("UE1100000001", "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isEmpty();
	}

	@Test
	void shouldReturnListOfSurveyUnitDto_IfIdUEFoundInDataBase() {
		//Given
		List<SurveyUnitDocument> responses = new ArrayList<>();
		responses.add(suDoc);
		responses.add(suDoc2);
		when(mongoRepository.findByIdUE(any(String.class))).thenReturn(responses);
		// When
		List<SurveyUnit> updates = surveyUnitMongoAdapter.findByIdUE("UE1100000001");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(2);
		Assertions.assertThat(updates.getFirst().getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnEmptyList_IfIdUENotFoundInDataBase() {
		//Given
		when(mongoRepository.findByIdUE(any(String.class))).thenReturn(List.of());
		// When
		List<SurveyUnit> updates = surveyUnitMongoAdapter.findByIdUE("UE1100000001");
		// Then
		Assertions.assertThat(updates).isEmpty();
	}

	@Test
	void shouldReturnListOfSurveyUnitDto_IfIdQuestionnaireFoundInDataBase() {
		//Given
		List<SurveyUnitDocument> responses = new ArrayList<>();
		responses.add(suDoc);
		responses.add(suDoc2);
		responses.add(suDoc3);
		when(mongoRepository.findByIdQuestionnaire(any(String.class))).thenReturn(responses.stream());
		// When
		Stream<SurveyUnit> updates = surveyUnitMongoAdapter.findByIdQuestionnaire("TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(3);
		//Assertions.assertThat(updates.get(2).getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnEmptyList_IfIdQuestionnaireNotFoundInDataBase() {
		//Given
		when(mongoRepository.findByIdQuestionnaire(any(String.class))).thenReturn(Stream.empty());
		// When
		List<SurveyUnit> updates = surveyUnitMongoAdapter.findByIdQuestionnaire("TEST2023X01").toList();
		// Then
		Assertions.assertThat(updates).isEmpty();
	}

	@Test
	void shouldReturnListOfSurveyUnitDto_WhenGivenAListOfIdUEs() {
		//Given
		List<SurveyUnitDocument> responses1 = new ArrayList<>();
		responses1.add(suDoc);
		responses1.add(suDoc2);
		List<SurveyUnitDocument> responses2 = new ArrayList<>();
		responses2.add(suDoc3);
		when(mongoRepository.findByIdUEAndIdQuestionnaire("UE1100000001", "TEST2023X01")).thenReturn(responses1);
		when(mongoRepository.findByIdUEAndIdQuestionnaire("UE1100000002", "TEST2023X01")).thenReturn(responses2);
		SurveyUnit id1 = SurveyUnit.builder().idUE("UE1100000001").build();
		SurveyUnit id2 = SurveyUnit.builder().idUE("UE1100000002").build();
		List<SurveyUnit> ids = List.of(id1, id2);
		// When
		List<SurveyUnit> updates = surveyUnitMongoAdapter.findByIdUEsAndIdQuestionnaire(ids, "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(3);
		Assertions.assertThat(updates.getFirst().getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnEmptyList_IfIdUEsNotFoundInDataBase() {
		//Given
		when(mongoRepository.findByIdUEAndIdQuestionnaire(any(String.class),any(String.class))).thenReturn(List.of());
		SurveyUnit id1 = SurveyUnit.builder().idUE("UE1100000001").build();
		SurveyUnit id2 = SurveyUnit.builder().idUE("UE1100000002").build();
		List<SurveyUnit> ids = List.of(id1, id2);
		// When
		List<SurveyUnit> updates = surveyUnitMongoAdapter.findByIdUEsAndIdQuestionnaire(ids, "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isEmpty();
	}

}
