package fr.insee.genesis.controller.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.ddi.Group;
import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariableType;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.*;
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;

class LunaticXmlAdapterTest {

	LunaticXmlSurveyUnit lunaticXmlSurveyUnit1 = new LunaticXmlSurveyUnit();
	LunaticXmlSurveyUnit lunaticXmlSurveyUnit2 = new LunaticXmlSurveyUnit();
	LunaticXmlSurveyUnit lunaticXmlSurveyUnit3 = new LunaticXmlSurveyUnit();
	LunaticXmlSurveyUnit lunaticXmlSurveyUnit4 = new LunaticXmlSurveyUnit();
	LunaticXmlSurveyUnit lunaticXmlSurveyUnit5 = new LunaticXmlSurveyUnit();

	VariablesMap variablesMap = new VariablesMap();

	private static final String LOOP_NAME = "BOUCLE1";

	private static final String ID_CAMPAIGN = "ID_CAMPAIGN";

	@BeforeEach
	void setUp(){
		//Given
		//SurveyUnit 1 : Only collected data
		LunaticXmlData lunaticXmlData = new LunaticXmlData();
		LunaticXmlCollectedData lunaticXmlCollectedData = new LunaticXmlCollectedData();
		lunaticXmlCollectedData.setVariableName("var1");
		lunaticXmlCollectedData.setCollected(List.of(new ValueType("1","string"),new ValueType("2","string")));
		LunaticXmlCollectedData lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
		lunaticXmlCollectedData2.setVariableName("var2");
		lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3","string"),new ValueType("4","string")));
		List<LunaticXmlCollectedData> collected = List.of(lunaticXmlCollectedData,lunaticXmlCollectedData2);
		lunaticXmlData.setCollected(collected);
		List<LunaticXmlOtherData> external = List.of();
		lunaticXmlData.setExternal(external);
		lunaticXmlSurveyUnit1.setId("idUE1");
		lunaticXmlSurveyUnit1.setQuestionnaireModelId("idQuest1");
		lunaticXmlSurveyUnit1.setData(lunaticXmlData);

		//SurveyUnit 2 : COLLECTED + EDITED
		lunaticXmlData = new LunaticXmlData();

		lunaticXmlCollectedData = new LunaticXmlCollectedData();
		lunaticXmlCollectedData.setVariableName("var1");
		lunaticXmlCollectedData.setCollected(List.of(new ValueType("1","string"),new ValueType("2","string")));
		lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e","string"),new ValueType("2e","string")));

		lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
		lunaticXmlCollectedData2.setVariableName("var2");
		lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3","string"),new ValueType("4","string")));
		collected = List.of(lunaticXmlCollectedData,lunaticXmlCollectedData2);
		lunaticXmlData.setCollected(collected);

		lunaticXmlData.setExternal(external);
		lunaticXmlSurveyUnit2.setId("idUE1");
		lunaticXmlSurveyUnit2.setQuestionnaireModelId("idQuest1");
		lunaticXmlSurveyUnit2.setData(lunaticXmlData);

		//SurveyUnit 3 : COLLECTED + EDITED + FORCED
		lunaticXmlData = new LunaticXmlData();

		lunaticXmlCollectedData = new LunaticXmlCollectedData();
		lunaticXmlCollectedData.setVariableName("var1");
		lunaticXmlCollectedData.setCollected(List.of(new ValueType("1","string"),new ValueType("2","string")));
		lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e","string"),new ValueType("2e","string")));

		lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
		lunaticXmlCollectedData2.setVariableName("var2");
		lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3","string"),new ValueType("4","string")));
		lunaticXmlCollectedData2.setForced(List.of(new ValueType("3f","string"),new ValueType("4f","string")));
		collected = List.of(lunaticXmlCollectedData,lunaticXmlCollectedData2);
		lunaticXmlData.setCollected(collected);

		lunaticXmlData.setExternal(external);
		lunaticXmlSurveyUnit3.setId("idUE1");
		lunaticXmlSurveyUnit3.setQuestionnaireModelId("idQuest1");
		lunaticXmlSurveyUnit3.setData(lunaticXmlData);

		//SurveyUnit 4 : COLLECTED + EDITED + PREVIOUS
		lunaticXmlData = new LunaticXmlData();

		lunaticXmlCollectedData = new LunaticXmlCollectedData();
		lunaticXmlCollectedData.setVariableName("var1");
		lunaticXmlCollectedData.setCollected(List.of(new ValueType("1","string"),new ValueType("2","string")));
		lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e","string"),new ValueType("2e","string")));

		lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
		lunaticXmlCollectedData2.setVariableName("var2");
		lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3","string"),new ValueType("4","string")));
		lunaticXmlCollectedData2.setPrevious(List.of(new ValueType("3p","string"),new ValueType("4p","string")));
		collected = List.of(lunaticXmlCollectedData,lunaticXmlCollectedData2);
		lunaticXmlData.setCollected(collected);

		lunaticXmlData.setExternal(external);
		lunaticXmlSurveyUnit4.setId("idUE1");
		lunaticXmlSurveyUnit4.setQuestionnaireModelId("idQuest1");
		lunaticXmlSurveyUnit4.setData(lunaticXmlData);

		//SurveyUnit 5 : COLLECTED + EDITED + PREVIOUS + INPUTED
		lunaticXmlData = new LunaticXmlData();

		lunaticXmlCollectedData = new LunaticXmlCollectedData();
		lunaticXmlCollectedData.setVariableName("var1");
		lunaticXmlCollectedData.setCollected(List.of(new ValueType("1","string"),new ValueType("2","string")));
		lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e","string"),new ValueType("2e","string")));
		lunaticXmlCollectedData.setInputed(List.of(new ValueType("1i","string"),new ValueType("2i","string")));

		lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
		lunaticXmlCollectedData2.setVariableName("var2");
		lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3","string"),new ValueType("4","string")));
		lunaticXmlCollectedData2.setPrevious(List.of(new ValueType("3p","string"),new ValueType("4p","string")));
		collected = List.of(lunaticXmlCollectedData,lunaticXmlCollectedData2);
		lunaticXmlData.setCollected(collected);

		lunaticXmlData.setExternal(external);
		lunaticXmlSurveyUnit5.setId("idUE1");
		lunaticXmlSurveyUnit5.setQuestionnaireModelId("idQuest1");
		lunaticXmlSurveyUnit5.setData(lunaticXmlData);

		//VariablesMap
		Group group = new Group(LOOP_NAME, Constants.ROOT_GROUP_NAME);
		Variable var1 = new Variable("var1", group, VariableType.STRING, "1");
		Variable var2 = new Variable("var2", variablesMap.getRootGroup(), VariableType.STRING, "1");
		variablesMap.putVariable(var1);
		variablesMap.putVariable(var2);
	}

	@Test
	@DisplayName("SurveyUnitUpdateDto should not be null")
	void test01(){
		// When
		List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1,variablesMap, ID_CAMPAIGN);
		// Then
		Assertions.assertThat(suDtos).isNotNull().isNotEmpty();
	}

	@Test
	@DisplayName("SurveyUnitUpdateDto should have the right idQuest")
	void test02(){
		// When
		List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1,variablesMap, ID_CAMPAIGN);
		// Then
		Assertions.assertThat(suDtos.get(0).getIdQuest()).isEqualTo("idQuest1");
	}

	@Test
	@DisplayName("SurveyUnitUpdateDto should have the right id")
	void test03(){
		// When
		List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1,variablesMap, ID_CAMPAIGN);
		// Then
		Assertions.assertThat(suDtos.get(0).getIdUE()).isEqualTo("idUE1");
	}

	@Test
	@DisplayName("SurveyUnitUpdateDto should contains 4 variable state updates")
	void test04(){
		// When
		List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1,variablesMap, ID_CAMPAIGN);
		// Then
		Assertions.assertThat(suDtos.get(0).getCollectedVariables()).hasSize(4);
	}

	//TODO Unit tests from datastate
	@Test
	@DisplayName("There should be a EDITED DTO with EDITED data")
	void test05(){
		// When
		List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit2,variablesMap, ID_CAMPAIGN);
		// Then
		Assertions.assertThat(suDtos).hasSize(2);
		Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
						surveyUnitUpdateDto.getState().equals(DataState.EDITED)
		).isNotEmpty();

		Optional<SurveyUnitUpdateDto> editedDTO = suDtos.stream().filter(surveyUnitUpdateDto ->
				surveyUnitUpdateDto.getState().equals(DataState.EDITED)
		).findFirst();
		Assertions.assertThat(editedDTO).isPresent();

		//Content check
		for(CollectedVariableDto collectedVariableDto : editedDTO.get().getCollectedVariables()){
			Assertions.assertThat(collectedVariableDto.getValues()).containsAnyOf("1e","2e").doesNotContain("1","2");
		}
	}

	@Test
	@DisplayName("There should be both EDITED DTO and FORCED DTO if there is EDITED and FORCED data")
	void test06(){
		// When
		List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit3,variablesMap, ID_CAMPAIGN);
		// Then
		Assertions.assertThat(suDtos).hasSize(3);
		Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
				surveyUnitUpdateDto.getState().equals(DataState.EDITED)
		).isNotEmpty();
		Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
				surveyUnitUpdateDto.getState().equals(DataState.FORCED)
		).isNotEmpty();
	}

	@Test
	@DisplayName("There should be a EDITED DTO and PREVIOUS DTO if there is EDITED and PREVIOUS data")
	void test07(){
		// When
		List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit4,variablesMap, ID_CAMPAIGN);
		// Then
		Assertions.assertThat(suDtos).hasSize(3);
		Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
				surveyUnitUpdateDto.getState().equals(DataState.EDITED)
		).isNotEmpty();
		Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
				surveyUnitUpdateDto.getState().equals(DataState.PREVIOUS)
		).isNotEmpty();
	}

	@Test
	@DisplayName("There should be multiple DTOs if there is different data states (all 4)")
	void test08(){
		// When
		List<SurveyUnitUpdateDto> suDtos = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit5,variablesMap, ID_CAMPAIGN);
		// Then
		Assertions.assertThat(suDtos).hasSize(4);
		Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
				surveyUnitUpdateDto.getState().equals(DataState.EDITED)
		).isNotEmpty();
		Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
				surveyUnitUpdateDto.getState().equals(DataState.PREVIOUS)
		).isNotEmpty();
		Assertions.assertThat(suDtos).filteredOn(surveyUnitUpdateDto ->
				surveyUnitUpdateDto.getState().equals(DataState.INPUTED)
		).isNotEmpty();
	}


}
