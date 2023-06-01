package fr.insee.genesis.controller.sources.xml;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LunaticXmlOtherData {

    private String variableName;

    private List<ValueType> values;

}
