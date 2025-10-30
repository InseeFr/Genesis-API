package fr.insee.genesis.controller.mappers;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataProcessingContextMapperDtoTest {
	@Test
	void dataProcessingContextToScheduleDto() {
		DataProcessingContextMapperDto d = new DataProcessingContextMapperDto();
		DataProcessingContextModel dataProcessingContext = new DataProcessingContextModel();

		ScheduleDto actual = d.dataProcessingContextToScheduleDto(dataProcessingContext);
        assertNotNull(actual);
		assertNull( actual.lastExecution());
        assertNull( actual.surveyName());
        assertNull( actual.kraftwerkExecutionScheduleList());

        DataProcessingContextMapperDto d2 = new DataProcessingContextMapperDto();
        DataProcessingContextModel dataProcessingContext2 = new DataProcessingContextModel();
        dataProcessingContext2.setId(new ObjectId());
        dataProcessingContext2.setWithReview(true);
        dataProcessingContext2.setLastExecution(now());

        ScheduleDto actual2 = d2.dataProcessingContextToScheduleDto(dataProcessingContext2);
        assertNotNull(actual2);
        assertNotNull( actual2.lastExecution());
        assertNull( actual2.surveyName());
	}

	@Test
	void dataProcessingContextListToScheduleDtoList() {
		DataProcessingContextMapperDto d = new DataProcessingContextMapperDto();
		List<DataProcessingContextModel> contexts = new ArrayList<>();
		List<ScheduleDto> expected = new ArrayList<>();
		List<ScheduleDto> actual = d.dataProcessingContextListToScheduleDtoList(contexts);

		assertEquals(expected, actual);
	}
}
