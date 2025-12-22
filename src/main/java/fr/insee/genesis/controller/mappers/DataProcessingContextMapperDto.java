package fr.insee.genesis.controller.mappers;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;

import java.util.ArrayList;
import java.util.List;

public class DataProcessingContextMapperDto {

    public ScheduleDto dataProcessingContextToScheduleDto(DataProcessingContextModel dataProcessingContext){
        return ScheduleDto.builder()
                .surveyName(dataProcessingContext.getPartitionId())
                .lastExecution(dataProcessingContext.getLastExecution())
                .kraftwerkExecutionScheduleList(dataProcessingContext.getKraftwerkExecutionScheduleList())
                .build();
    }

    public List<ScheduleDto> dataProcessingContextListToScheduleDtoList(List<DataProcessingContextModel> contexts){
        List<ScheduleDto> dtos = new ArrayList<>();
        for(DataProcessingContextModel context : contexts){
            dtos.add(dataProcessingContextToScheduleDto(context));
        }
        return dtos;
    }

    public DataProcessingContextModel scheduleDtoToDataProcessingContext (ScheduleDto schedule){
        return DataProcessingContextModel.builder()
                .partitionId(schedule.surveyName())
                .collectionInstrumentId(schedule.collectionInstrumentId())
                .lastExecution(schedule.lastExecution())
                .kraftwerkExecutionScheduleList(schedule.kraftwerkExecutionScheduleList())
                .build();
    }
}
