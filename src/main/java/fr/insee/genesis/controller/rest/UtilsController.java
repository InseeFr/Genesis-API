package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.utils.XMLSplitter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(path = "/utils")
@Controller
@Slf4j
public class UtilsController {

	@Operation(summary = "Split a XML file into smaller ones")
	@PutMapping(path = "/split/lunatic-xml")
	public ResponseEntity<Object> saveResponsesFromXmlFile(@RequestParam("inputFolder") String inputFolder,
														   @RequestParam("outputFolder") String outputFolder,
														   @RequestParam("filename") String filename,
														   @RequestParam("nbResponsesByFile") int nbSU)
			throws Exception {
		log.info("Split XML file : " + filename + " into " + nbSU + " SU by file");
		XMLSplitter.split(inputFolder, filename, outputFolder, "SurveyUnit", nbSU);
		return new ResponseEntity<>("Test", HttpStatus.OK);
	}
}
