package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticXmlDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticXmlPersistancePort;
import fr.insee.genesis.infrastructure.document.rawdata.xml.LunaticXmlDataDocument;
import fr.insee.genesis.infrastructure.mappers.LunaticXmlDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.repository.LunaticXmlMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Qualifier("lunaticXmlMongoAdapter")
public class LunaticXmlMongoAdapter implements LunaticXmlPersistancePort {

	private final LunaticXmlMongoDBRepository mongoRepository;

	@Autowired
	public LunaticXmlMongoAdapter(LunaticXmlMongoDBRepository mongoRepository, MongoTemplate mongoTemplate) {
		this.mongoRepository = mongoRepository;
	}

	@Override
	public void save(LunaticXmlDataModel lunaticXmlDataModel) {
		LunaticXmlDataDocument document = LunaticXmlDocumentMapper.INSTANCE
				.modelToDocument(lunaticXmlDataModel);
		mongoRepository.insert(document);
	}
}
