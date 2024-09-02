package fr.insee.genesis.domain.model.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleModel {
	private String surveyName;

	private LocalDateTime lastExecution;

	List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;
}
