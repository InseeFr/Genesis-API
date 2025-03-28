package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.volumetry.VolumetryLogService;
import fr.insee.genesis.domain.utils.XMLSplitter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@RequestMapping(path = "/")
@Controller
@Slf4j
@Tag(name = "Technical services")
public class UtilsController implements CommonApiResponse{

	private final VolumetryLogService volumetryLogService;
	private final SurveyUnitApiPort surveyUnitService;
	private final LunaticJsonRawDataApiPort lunaticJsonRawDataService;



	public UtilsController(SurveyUnitApiPort surveyUnitService, VolumetryLogService volumetryLogService, LunaticJsonRawDataApiPort lunaticJsonRawDataService) {
		this.surveyUnitService = surveyUnitService;
		this.volumetryLogService = volumetryLogService;
        this.lunaticJsonRawDataService = lunaticJsonRawDataService;
    }



	@Operation(summary = "Split a XML file into smaller ones")
	@PutMapping(path = "/utils/split/lunatic-xml")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Object> saveResponsesFromXmlFile(@RequestParam("inputFolder") String inputFolder,
														   @RequestParam("outputFolder") String outputFolder,
														   @RequestParam("filename") String filename,
														   @RequestParam("nbResponsesByFile") int nbSU)
			throws Exception {
		XMLSplitter.split(inputFolder, filename, outputFolder, "SurveyUnit", nbSU);
		return ResponseEntity.ok("File split");
	}

	@Operation(summary = "Record volumetrics of each campaign in a folder")
	@PutMapping(path = "/volumetrics/save-all-campaigns")
	@PreAuthorize("hasRole('SCHEDULER')")
	public ResponseEntity<Object> saveVolumetry() throws IOException {
		volumetryLogService.writeVolumetries(surveyUnitService);
		volumetryLogService.writeRawDataVolumetries(lunaticJsonRawDataService);
		volumetryLogService.cleanOldFiles();
		return ResponseEntity.ok("Volumetric saved");
	}
}
