package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ScheduleDto (String surveyName,
						   String collectionInstrumentId,
						   LocalDateTime lastExecution,
						   List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList
){}
