package fr.insee.genesis.controller.sources.xml;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LunaticXmlData {

    private List<LunaticXmlCollectedData> collected=new ArrayList<>();
    private List<LunaticXmlOtherData> calculated=new ArrayList<>();
    private List<LunaticXmlOtherData> external=new ArrayList<>();


}
