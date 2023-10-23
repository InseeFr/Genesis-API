package fr.insee.genesis.infrastructure.model.document;

import fr.insee.genesis.infrastructure.model.ExternalVariable;
import fr.insee.genesis.infrastructure.model.VariableState;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "responses")
public class SurveyUnitUpdateDocument {


	private String idUpdate;
	private String idCampaign;
	private String idUE;
	private String idQuestionnaire;
	private String state;
	private String source;
	private LocalDateTime date;
	private List<VariableState> variablesUpdate;
	private List<ExternalVariable> externalVariables;
}
