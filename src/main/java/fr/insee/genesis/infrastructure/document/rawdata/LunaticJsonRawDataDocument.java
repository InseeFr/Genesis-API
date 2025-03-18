package fr.insee.genesis.infrastructure.document.rawdata;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataCollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataVariable;
import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Builder
public record LunaticJsonRawDataDocument(
    @Field("COLLECTED")
    Map<String, Map<DataState,Object>> collectedVariables,
    @Field("EXTERNAL")
    Map<String, Object> externalVariables

){}