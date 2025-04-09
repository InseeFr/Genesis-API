package fr.insee.genesis.domain.service.lunaticmodel;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.domain.ports.api.LunaticModelApiPort;
import fr.insee.genesis.domain.ports.spi.LunaticModelPersistancePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.mappers.LunaticModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class LunaticModelService implements LunaticModelApiPort {
    @Qualifier("lunaticModelMongoAdapter")
    LunaticModelPersistancePort lunaticModelPersistancePort;

    @Autowired
    public LunaticModelService(LunaticModelPersistancePort lunaticModelPersistancePort) {
        this.lunaticModelPersistancePort = lunaticModelPersistancePort;
    }

    @Override
    public void save(String questionnaireId, Map<String, Object> lunaticModel) {
        lunaticModelPersistancePort.save(
                LunaticModelModel.builder()
                        .questionnaireId(questionnaireId)
                        .lunaticModel(lunaticModel)
                        .recordDate(LocalDateTime.now())
                        .build()
        );
    }

    @Override
    public LunaticModelModel get(String questionnaireId) throws GenesisException {
        if(lunaticModelPersistancePort.find(questionnaireId).isEmpty()){
            throw new GenesisException(404,"Questionnaire not found");
        }
        return LunaticModelMapper.INSTANCE.documentToModel(lunaticModelPersistancePort.find(questionnaireId).getFirst());
    }
}
