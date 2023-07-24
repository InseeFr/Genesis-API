package fr.insee.genesis.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.sources.ddi.DDIReader;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.json.LunaticJsonDataFile;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
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

    @Operation(summary = "Read responses in JSON Lunatic")
    @PostMapping(path = "/readJSONLunatic", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> readSample(@RequestBody String jsonContent) throws JsonProcessingException {
        log.info("Read JSON file");
        ObjectMapper objectMapper = new ObjectMapper();
        LunaticJsonDataFile test = objectMapper.readValue(jsonContent,LunaticJsonDataFile.class);
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

    @Operation(summary = "Read responses in JSON Lunatic and specification in DDI from files")
    @GetMapping(path = "/readJSONLunatic")
    public ResponseEntity<Object> readSampleFromJsonFile(   @RequestParam("pathLunaticJson") String jsonFile,
                                                        @RequestParam("pathDDI") String ddiFile)
            throws IOException, SAXException, ParserConfigurationException {
        log.info(String.format("Try to read JSON file : %s", jsonFile));
        Path jsonFilePath = Paths.get(jsonFile);
        LunaticJsonDataFile data = new LunaticJsonDataFile();
        try {
            data = readJsonFile(jsonFilePath);
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
        log.info(String.format("Try to read DDI file : %s", ddiFile));
        Path ddiFilePath = Paths.get(ddiFile);
        VariablesMap variablesMap= new VariablesMap();
        try {
            variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

    @Operation(summary = "Read responses in XML Lunatic and specification in DDI from files")
    @GetMapping(path = "/readXMLLunatic")
    public ResponseEntity<Object> readSampleFromXmlFile(   @RequestParam("pathLunaticXml") String xmlFile,
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
        log.info("File {} treated", xmlFile);
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

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

    private static LunaticJsonDataFile readJsonFile(Path jsonFilePath) throws GenesisException, IOException {
        if (Files.exists(jsonFilePath)) {
            log.info(String.format("Found file : %s", jsonFilePath));
        } else {
            log.info("No file found at path : " + jsonFilePath);
            throw new GenesisException(400, String.format("JSON file not found at path : %s", jsonFilePath));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(jsonFilePath.toUri()),LunaticJsonDataFile.class);
    }


}
