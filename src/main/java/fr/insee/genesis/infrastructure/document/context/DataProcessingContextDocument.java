package fr.insee.genesis.infrastructure.document.context;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = Constants.MONGODB_CONTEXT_COLLECTION_NAME)
public class DataProcessingContextDocument{

        @Id
        private ObjectId id;
        @Indexed
        @Deprecated
        private String partitionId; //ex Survey Name, campaignId
        @Indexed
        private String collectionInstrumentId; // QuestionnaireId
        private LocalDateTime lastExecution;
        private List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;
        private boolean withReview;
}
