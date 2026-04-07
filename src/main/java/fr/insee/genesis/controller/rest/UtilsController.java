package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.volumetry.VolumetryLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequestMapping(path = "/")
@Controller
@AllArgsConstructor
@Slf4j
@Tag(name = "Technical services")
public class UtilsController implements CommonApiResponse{

	private final VolumetryLogService volumetryLogService;
	private final SurveyUnitApiPort surveyUnitService;
	private final LunaticJsonRawDataApiPort lunaticJsonRawDataService;
	private final RawResponseApiPort rawResponseApiPort;

	@Operation(summary = "Record volumetrics of each campaign in a folder")
	@PutMapping(path = "/volumetrics/save-all-campaigns")
	@PreAuthorize("hasRole('SCHEDULER')")
	public ResponseEntity<Object> saveVolumetry() throws IOException {
		String responsesKeyInReturnedMap = "responsesVolumetrics";
		String rawResponsesKeyInReturnedMap = "rawResponsesVolumetrics";
		Map<String, Object> volumetricsMap = new HashMap<>();
		volumetricsMap.put(
				responsesKeyInReturnedMap,
				volumetryLogService.writeVolumetries(surveyUnitService));
		volumetricsMap.put(
				rawResponsesKeyInReturnedMap,
				volumetryLogService.writeRawDataVolumetries(lunaticJsonRawDataService, rawResponseApiPort)
		);
		volumetryLogService.cleanOldFiles();
		return ResponseEntity.ok(volumetricsMap);
	}
}
