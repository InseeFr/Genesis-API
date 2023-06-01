package fr.insee.genesis.controller.sources.xml;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LunaticXmlData {

    private List<LunaticXmlCollectedData> collected;
    private List<LunaticXmlOtherData> calculated;
    private List<LunaticXmlOtherData> external;


}
