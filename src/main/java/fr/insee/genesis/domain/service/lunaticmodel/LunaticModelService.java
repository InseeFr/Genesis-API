package fr.insee.genesis.domain.service.lunaticmodel;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.domain.ports.api.LunaticModelApiPort;
import fr.insee.genesis.domain.ports.spi.LunaticModelPersistancePort;
import fr.insee.genesis.infrastructure.mappers.LunaticModelMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class LunaticModelService implements LunaticModelApiPort {
    LunaticModelPersistancePort lunaticModelPersistancePort;

    @Override
    public void save(String questionnaireId, Map<String, Object> lunaticModel) {
        lunaticModelPersistancePort.save(
                LunaticModelModel.builder()
                        .questionnaireId(questionnaireId)
                        .lunaticModel(lunaticModel)
                        .build()
        );
    }

    @Override
    public LunaticModelModel get(String questionnaireId) {
        return LunaticModelMapper.INSTANCE.documentToModel(lunaticModelPersistancePort.find(questionnaireId).getFirst());
    }
}
