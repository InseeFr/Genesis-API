package fr.insee.genesis.infrastructure.document.variabletype;

import fr.insee.genesis.Constants;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = Constants.MONGODB_VARIABLETYPE_COLLECTION_NAME)
public class VariableTypeDocument {

}
