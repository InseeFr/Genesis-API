package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.VariableDocument;
import fr.insee.genesis.infrastructure.repository.SurveyUnitMongoDBRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyUnitModelMongoAdapterTest {
	
    static SurveyUnitMongoDBRepository mongoRepository = Mockito.mock(SurveyUnitMongoDBRepository.class);

	@InjectMocks
	static SurveyUnitMongoAdapter surveyUnitMongoAdapter;

	static SurveyUnitDocument suDoc;
	static SurveyUnitDocument suDoc2;
	static SurveyUnitDocument suDoc3;

	@BeforeAll
	static void setUp() {
		surveyUnitMongoAdapter = new SurveyUnitMongoAdapter(mongoRepository, null);
		suDoc= new SurveyUnitDocument();
		suDoc.setCampaignId("campaignId");
		suDoc.setInterrogationId("UE1100000001");
		suDoc.setQuestionnaireId("TEST2023X01");
		suDoc.setState("COLLECTED");
		suDoc.setMode("WEB");
		suDoc.setRecordDate(LocalDateTime.now());
		suDoc.setCollectedVariables(List.of(new VariableDocument()));
		suDoc.setExternalVariables(List.of(new VariableDocument()));

		suDoc2= new SurveyUnitDocument();
		suDoc2.setCampaignId("campaignId");
		suDoc2.setInterrogationId("UE1100000001");
		suDoc2.setQuestionnaireId("TEST2023X01");
		suDoc2.setState("COLLECTED");
		suDoc2.setMode("TEL");
		suDoc2.setRecordDate(LocalDateTime.now());
		suDoc2.setCollectedVariables(List.of(new VariableDocument()));
		suDoc2.setExternalVariables(List.of(new VariableDocument()));

		suDoc3= new SurveyUnitDocument();
		suDoc3.setCampaignId("campaignId");
		suDoc3.setInterrogationId("UE1100000002");
		suDoc3.setQuestionnaireId("TEST2023X01");
		suDoc3.setState("COLLECTED");
		suDoc3.setMode("WEB");
		suDoc3.setRecordDate(LocalDateTime.now());
		suDoc3.setCollectedVariables(List.of(new VariableDocument()));
		suDoc3.setExternalVariables(List.of(new VariableDocument()));
	}

	@Test
	void shouldReturnListOfSurveyUnitModels_IfIdsFoundInDataBase() {
		//Given
		List<SurveyUnitDocument> responses = new ArrayList<>();
		responses.add(suDoc);
		responses.add(suDoc2);
		when(mongoRepository.findByInterrogationIdAndQuestionnaireId(any(String.class), any(String.class))).thenReturn(responses);
		// When
		List<SurveyUnitModel> updates = surveyUnitMongoAdapter.findByIds("UE1100000001", "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(2);
		Assertions.assertThat(updates.getFirst().getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnEmptyList_IfIdsNotFoundInDataBase() {
		//Given
		when(mongoRepository.findByInterrogationIdAndQuestionnaireId(any(String.class), any(String.class))).thenReturn(List.of());
		// When
		List<SurveyUnitModel> updates = surveyUnitMongoAdapter.findByIds("UE1100000001", "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isEmpty();
	}

	@Test
	void shouldReturnListOfSurveyUnitModels_IfInterrogationIdFoundInDataBase() {
		//Given
		List<SurveyUnitDocument> responses = new ArrayList<>();
		responses.add(suDoc);
		responses.add(suDoc2);
		when(mongoRepository.findByInterrogationId(any(String.class))).thenReturn(responses);
		// When
		List<SurveyUnitModel> updates = surveyUnitMongoAdapter.findByInterrogationId("UE1100000001");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(2);
		Assertions.assertThat(updates.getFirst().getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnEmptyList_IfInterrogationIdNotFoundInDataBase() {
		//Given
		when(mongoRepository.findByInterrogationId(any(String.class))).thenReturn(List.of());
		// When
		List<SurveyUnitModel> updates = surveyUnitMongoAdapter.findByInterrogationId("UE1100000001");
		// Then
		Assertions.assertThat(updates).isEmpty();
	}

	@Test
	void shouldReturnListOfSurveyUnitModels_IfQuestionnaireIdFoundInDataBase() {
		//Given
		List<SurveyUnitDocument> responses = new ArrayList<>();
		responses.add(suDoc);
		responses.add(suDoc2);
		responses.add(suDoc3);
		when(mongoRepository.findByQuestionnaireId(any(String.class))).thenReturn(responses.stream());
		// When
		Stream<SurveyUnitModel> updates = surveyUnitMongoAdapter.findByQuestionnaireId("TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(3);
	}

	@Test
	void shouldReturnEmptyList_IfQuestionnaireIdNotFoundInDataBase() {
		//Given
		when(mongoRepository.findByQuestionnaireId(any(String.class))).thenReturn(Stream.empty());
		// When
		List<SurveyUnitModel> updates = surveyUnitMongoAdapter.findByQuestionnaireId("TEST2023X01").toList();
		// Then
		Assertions.assertThat(updates).isEmpty();
	}

	@Test
	void shouldReturnListOfSurveyUnitModels_WhenGivenAListOfInterrogationIds() {
		//Given
		List<SurveyUnitDocument> responses1 = new ArrayList<>();
		responses1.add(suDoc);
		responses1.add(suDoc2);
		List<SurveyUnitDocument> responses2 = new ArrayList<>();
		responses2.add(suDoc3);
		when(mongoRepository.findByInterrogationIdAndQuestionnaireId("UE1100000001", "TEST2023X01")).thenReturn(responses1);
		when(mongoRepository.findByInterrogationIdAndQuestionnaireId("UE1100000002", "TEST2023X01")).thenReturn(responses2);
		SurveyUnitModel id1 = SurveyUnitModel.builder().interrogationId("UE1100000001").build();
		SurveyUnitModel id2 = SurveyUnitModel.builder().interrogationId("UE1100000002").build();
		List<SurveyUnitModel> ids = List.of(id1, id2);
		// When
		List<SurveyUnitModel> updates = surveyUnitMongoAdapter.findByInterrogationIdsAndQuestionnaireId(ids, "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isNotNull().hasSize(3);
		Assertions.assertThat(updates.getFirst().getMode()).isEqualTo(Mode.WEB);
	}

	@Test
	void shouldReturnEmptyList_IfInterrogationIdsNotFoundInDataBase() {
		//Given
		when(mongoRepository.findByInterrogationIdAndQuestionnaireId(any(String.class),any(String.class))).thenReturn(List.of());
		SurveyUnitModel id1 = SurveyUnitModel.builder().interrogationId("UE1100000001").build();
		SurveyUnitModel id2 = SurveyUnitModel.builder().interrogationId("UE1100000002").build();
		List<SurveyUnitModel> ids = List.of(id1, id2);
		// When
		List<SurveyUnitModel> updates = surveyUnitMongoAdapter.findByInterrogationIdsAndQuestionnaireId(ids, "TEST2023X01");
		// Then
		Assertions.assertThat(updates).isEmpty();
	}

}
