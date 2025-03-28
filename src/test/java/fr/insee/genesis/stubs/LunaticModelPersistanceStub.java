package fr.insee.genesis.stubs;

import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LunaticModelPersistanceStub {
    private final List<LunaticModelDocument> mongoStub = new ArrayList<>();
}
