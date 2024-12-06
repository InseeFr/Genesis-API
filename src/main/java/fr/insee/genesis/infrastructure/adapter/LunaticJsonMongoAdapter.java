package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonPersistancePort;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonDocumentMapper;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Qualifier("lunaticJsonMongoAdapter")
public class LunaticJsonMongoAdapter implements LunaticJsonPersistancePort {

	private final LunaticJsonMongoDBRepository lunaticJsonMongoDBRepository;

	@Autowired
	public LunaticJsonMongoAdapter(LunaticJsonMongoDBRepository lunaticJsonMongoDBRepository) {
		this.lunaticJsonMongoDBRepository = lunaticJsonMongoDBRepository;
	}

	@Override
	public void save(LunaticJsonDataModel lunaticJsonDataModel) {
		LunaticJsonDataDocument document = LunaticJsonDocumentMapper.INSTANCE
				.modelToDocument(lunaticJsonDataModel);
		lunaticJsonMongoDBRepository.insert(document);
	}
}
