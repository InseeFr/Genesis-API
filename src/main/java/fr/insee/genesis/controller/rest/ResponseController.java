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
    @PutMapping(path = "/saveXMLLunatic")
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

    private static void readDDIFile(Path ddiFilePath) throws GenesisException, IOException {
        if (Files.exists(ddiFilePath)) {
            log.info(String.format("Found file : %s", ddiFilePath));
        } else {
            log.info("No file found at path : " + ddiFilePath);
            throw new GenesisException(400, String.format("DDI file not found at path : %s", ddiFilePath));
        }
    }


}
