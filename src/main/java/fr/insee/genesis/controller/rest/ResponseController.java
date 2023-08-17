package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.sources.ddi.DDIReader;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequestMapping(path = "/response")
@Controller
@Slf4j
public class ResponseController {

    @Autowired
    SurveyUnitUpdateApiPort surveyUnitService;

    @Autowired
    FileUtils fileUtils;

    @Operation(summary = "Save responses from XML Lunatic in Genesis Database")
    @PutMapping(path = "/save/lunatic-xml/one-file")
    public ResponseEntity<Object> saveResponsesFromXmlFile(     @RequestParam("pathLunaticXml") String xmlFile,
                                                                @RequestParam("pathDDI") String ddiFile)
            throws Exception {
        log.info(String.format("Try to read Xml file : %s", xmlFile));
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
        LunaticXmlCampaign campaign = parser.parseDataFile(Paths.get(xmlFile));
        log.info(String.format("Try to read DDI file : %s", ddiFile));
        Path ddiFilePath = Paths.get(ddiFile);
        VariablesMap variablesMap;
        try {
            variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
        List<SurveyUnitUpdateDto> suDtos = new ArrayList<>();
        for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
            SurveyUnitUpdateDto suDto = LunaticXmlAdapter.convert(su, variablesMap);
            suDtos.add(suDto);
        }
        surveyUnitService.saveSurveyUnits(suDtos);
        log.info("File {} treated", xmlFile);
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

    @Operation(summary = "Save multiples files in Genesis Database")
    @PutMapping(path = "/save/lunatic-xml")
    public ResponseEntity<Object> saveResponsesFromXmlCampaignFolder(@RequestParam("pathFolder") String pathFolder,
                                                                     @RequestParam("dataSource") String dataSource,
                                                                    @RequestParam("mode") String mode)
            throws Exception {
        log.info("Try to import data from folder : {}", pathFolder);
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
        String dataFolder = fileUtils.getDataFolder(pathFolder,dataSource);
        List<String> dataFiles = fileUtils.listFiles(dataFolder);
        log.info("Numbers of files to load in folder {} : {}", pathFolder, dataFiles.size());
        for(String fileName : dataFiles){
            String pathFile = String.format("%s/%s", dataFolder, fileName);
            log.info("Try to read Xml file : {}", fileName);
            LunaticXmlCampaign campaign = parser.parseDataFile(Paths.get(pathFile));
            Path ddiFilePath = fileUtils.findDDIFile(pathFolder, mode);
            VariablesMap variablesMap;
            try {
                variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
            } catch (GenesisException e) {
                return ResponseEntity.status(e.getStatus()).body(e.getMessage());
            }
            List<SurveyUnitUpdateDto> suDtos = new ArrayList<>();
            for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
                SurveyUnitUpdateDto suDto = LunaticXmlAdapter.convert(su, variablesMap);
                suDtos.add(suDto);
            }
            surveyUnitService.saveSurveyUnits(suDtos);
            log.info("File {} saved", fileName);
            fileUtils.moveDataFile(pathFolder, dataSource, fileName, mode);
        }
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

    @Operation(summary = "Retrieve responses with IdUE and IdQuestionnaire from Genesis Database")
    @GetMapping(path = "/findResponsesByUEAndQuestionnaire")
    public ResponseEntity<List<SurveyUnitUpdateDto>> findResponsesByUEAndQuestionnaire(     @RequestParam("idUE") String idUE,
                                                                                            @RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findByIdsUEAndQuestionnaire(idUE, idQuestionnaire);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve all responses of one questionnaire")
    @GetMapping(path = "/findAllResponsesByQuestionnaire")
    public ResponseEntity<List<SurveyUnitUpdateDto>> findAllResponsesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        log.info("Try to find all responses of questionnaire : " + idQuestionnaire);
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findByIdQuestionnaire(idQuestionnaire);
        log.info("Responses found : " + responses.size());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

}
