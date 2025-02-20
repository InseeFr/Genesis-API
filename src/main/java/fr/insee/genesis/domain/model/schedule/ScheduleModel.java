package fr.insee.genesis.domain.model.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleModel {
	@Id
	private ObjectId id; //Used to remove warning

	private String surveyName;

	private LocalDateTime lastExecution;

	List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;
}
