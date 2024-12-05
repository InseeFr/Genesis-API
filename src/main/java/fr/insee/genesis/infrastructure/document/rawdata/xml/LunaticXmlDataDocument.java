package fr.insee.genesis.infrastructure.document.rawdata.xml;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = Constants.MONGODB_LUNATIC_XML_DATA_COLLECTION_NAME)
public class  LunaticXmlDataDocument {
    @Id
    private ObjectId id;
    private LunaticXmlCampaign lunaticXmlData;
    private Mode mode;
    private LocalDateTime recordDate;
}
