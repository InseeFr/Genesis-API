package fr.insee.genesis.infrastructure.document.schedule;

import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ScheduleDocumentTest {
    @Test
    void constructorTest(){
        //GIVEN
        String surveyName = "testSurvey";
        List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = new ArrayList<>();

        //WHEN
        ScheduleDocument scheduleDocument = new ScheduleDocument(
                surveyName,
                kraftwerkExecutionScheduleList
        );

        //THEN
        Assertions.assertThat(scheduleDocument.getSurveyName()).isEqualTo(surveyName);
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList())
                .isEqualTo(kraftwerkExecutionScheduleList);
    }

}