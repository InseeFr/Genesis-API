package fr.insee.genesis.domain.model.context;

import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContextModel {

    @NotNull
    String id;

    List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;


}
