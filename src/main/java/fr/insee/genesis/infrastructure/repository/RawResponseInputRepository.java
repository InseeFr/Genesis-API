package fr.insee.genesis.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RawResponseInputRepository {

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public void saveAsRawJson(RawResponseDto dto) {
        // Convertir le DTO en JSON brut
        String json = null;

        try {
            json = objectMapper.writeValueAsString(dto);
            // Parser en Document BSON (Mongo gère automatiquement les types)
            Document payload = Document.parse(json);

            Map<String, Object> document = new java.util.HashMap<>();
            document.put("interrogationId", dto.getInterrogationId());
            document.put("collectionInstrumentId", dto.getCollectionInstrumentId());
            document.put("recordDate", Instant.now());
            document.put("payload", payload);
            mongoTemplate.save(document, "rawResponses");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
