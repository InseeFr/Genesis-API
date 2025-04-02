package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.domain.ports.spi.LunaticModelPersistancePort;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import fr.insee.genesis.infrastructure.mappers.LunaticModelMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LunaticModelPersistanceStub implements LunaticModelPersistancePort {
    private final List<LunaticModelDocument> mongoStub = new ArrayList<>();

    @Override
    public void save(LunaticModelModel lunaticModelModel) {
        //Replace
        mongoStub.removeIf(document -> document.questionnaireId().equals(lunaticModelModel.questionnaireId()));
        mongoStub.add(LunaticModelMapper.INSTANCE.modelToDocument(lunaticModelModel));
    }

    @Override
    public List<LunaticModelDocument> find(String questionnaireId) {
        return mongoStub.stream().filter(
                document -> document.questionnaireId().equals(questionnaireId)
        ).toList();
    }
}
