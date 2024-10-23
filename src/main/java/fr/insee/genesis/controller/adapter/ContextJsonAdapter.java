package fr.insee.genesis.controller.adapter;

import fr.insee.genesis.controller.sources.contextJson.ContextJsonFile;
import fr.insee.genesis.domain.model.context.ContextModel;
import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ContextJsonAdapter {

    public static ContextModel convert(ContextJsonFile context){
        ArrayList<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = new ArrayList<>();
        context.getPartitions().forEach(partition -> {
            kraftwerkExecutionScheduleList.add(  new KraftwerkExecutionSchedule(
                    frequency,
                    serviceToCall,
                    partition.getDateDebutCollecte(),
                    partition.getDateFinCollecte(), //TODO +1 ?
                    trustParameters // codif, chiffrement, frequence, date fin de sorties...
            ));
        });
        return ContextModel.builder()
                .id(context.getId())
                .kraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList)
                .build();
    }




}
