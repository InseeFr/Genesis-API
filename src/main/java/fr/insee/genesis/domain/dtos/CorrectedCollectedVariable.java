package fr.insee.genesis.domain.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
/*
 * An object indicating which values are invalid in a collected variable DTO and which value is originated from which datastate
 */
public class CorrectedCollectedVariable extends CollectedVariableDto{
    private List<Integer> incorrectValueIndexes;
    private Map<Integer, DataState> valueIndexDataStateMap;

    public CorrectedCollectedVariable(String idVar, List<String> values, String idLoop, String idParent, DataState dataState) {
        super(idVar, values, idLoop, idParent);
        this.incorrectValueIndexes = new ArrayList<>();
        int valueIndex = 0;
        valueIndexDataStateMap = new HashMap<>();
        while(valueIndex < values.size()){
            valueIndexDataStateMap.put(valueIndex, dataState);
            valueIndex++;
        }
    }
}