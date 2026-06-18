package fr.insee.genesis.domain.model.context.schedule;

import java.util.List;

public record DeletedExpiredSchedules(
        List<KraftwerkExecutionSchedule> v1Schedules,
        List<KraftwerkExecutionScheduleV2> v2Schedules
) {
    public boolean isEmpty() {
        return v1Schedules.isEmpty() && v2Schedules.isEmpty();
    }
}
