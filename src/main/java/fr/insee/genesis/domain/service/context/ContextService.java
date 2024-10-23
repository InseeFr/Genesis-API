package fr.insee.genesis.domain.service.context;

import fr.insee.genesis.domain.model.context.ContextModel;
import fr.insee.genesis.domain.ports.api.ContextApiPort;
import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ContextService implements ContextApiPort {

    private final ScheduleApiPort scheduleApiPort;

    @Autowired
    public ContextService(ScheduleApiPort scheduleApiPort) {
        this.scheduleApiPort = scheduleApiPort;
    }


    @Override
    public void addContext(ContextModel contextModel) {
        //Check if schedules already exists
        //scheduleApiPort;

        //Create all schedules for each partition

        //Save DDI and lunaticmodel

        //Get nomenclatures ?
    }

}
