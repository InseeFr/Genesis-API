package fr.insee.genesis.infrastructure.model.document;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "responses")
public class SurveyUnitDocument {

	private String idUE;

	private String source;

}
