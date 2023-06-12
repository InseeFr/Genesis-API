package fr.insee.genesis.controller.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.ddi.Group;
import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariableType;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCollectedData;
import fr.insee.genesis.controller.sources.xml.LunaticXmlData;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.sources.xml.ValueType;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import org.assertj.core.api.Assertions;

public class LunaticXmlAdapterTest {

	LunaticXmlSurveyUnit lunaticXmlSurveyUnit = new LunaticXmlSurveyUnit();

	VariablesMap variablesMap = new VariablesMap();

	private static final String LOOP_NAME = "BOUCLE1";

	@BeforeEach
	public void setUp(){
		//Given
		//SurveyUnit
		LunaticXmlData lunaticXmlData = new LunaticXmlData();
		LunaticXmlCollectedData lunaticXmlCollectedData1 = new LunaticXmlCollectedData();
		lunaticXmlCollectedData1.setVariableName("var1");
		lunaticXmlCollectedData1.setCollected(List.of(new ValueType("1","string"),new ValueType("2","string")));
		LunaticXmlCollectedData lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
		lunaticXmlCollectedData2.setVariableName("var2");
		lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3","string"),new ValueType("4","string")));
		List<LunaticXmlCollectedData> collected = List.of(lunaticXmlCollectedData1,lunaticXmlCollectedData2);
		lunaticXmlData.setCollected(collected);
		lunaticXmlSurveyUnit.setId("idUE1");
		lunaticXmlSurveyUnit.setQuestionnaireModelId("idQuest1");
		lunaticXmlSurveyUnit.setData(lunaticXmlData);
		//VariablesMap
		Group group = new Group(LOOP_NAME, Constants.ROOT_GROUP_NAME);
		Variable var1 = new Variable("var1", group, VariableType.STRING, "1");
		Variable var2 = new Variable("var2", variablesMap.getRootGroup(), VariableType.STRING, "1");
		variablesMap.putVariable(var1);
		variablesMap.putVariable(var2);
	}

	@Test
	@DisplayName("SurveyUnitUpdateDto should not be null")
	public void test01(){
		// When
		SurveyUnitUpdateDto suDto = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit,variablesMap);
		// Then
		Assertions.assertThat(suDto).isNotNull();
	}

	@Test
	@DisplayName("SurveyUnitUpdateDto should have the right idQuest")
	public void test02(){
		// When
		SurveyUnitUpdateDto suDto = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit,variablesMap);
		// Then
		Assertions.assertThat(suDto.getIdQuest()).isEqualTo("idQuest1");
	}

	@Test
	@DisplayName("SurveyUnitUpdateDto should have the right id")
	public void test03(){
		// When
		SurveyUnitUpdateDto suDto = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit,variablesMap);
		// Then
		Assertions.assertThat(suDto.getIdUE()).isEqualTo("idUE1");
	}

	@Test
	@DisplayName("SurveyUnitUpdateDto should contains 4 variable state updates")
	public void test04(){
		// When
		SurveyUnitUpdateDto suDto = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit,variablesMap);
		// Then
		Assertions.assertThat(suDto.getVariablesUpdate().size()).isEqualTo(4);
	}

}
