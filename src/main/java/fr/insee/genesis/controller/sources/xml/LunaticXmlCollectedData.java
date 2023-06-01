package fr.insee.genesis.controller.sources.xml;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LunaticXmlCollectedData {

    private String variableName;

    private List<ValueType> collected;

    private List<ValueType>  edited;

    private List<ValueType>  inputed;

    private List<ValueType>  forced;

    private List<ValueType>  previous;
}
