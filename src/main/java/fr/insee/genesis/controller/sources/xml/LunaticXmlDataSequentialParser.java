package fr.insee.genesis.controller.sources.xml;

import fr.insee.genesis.Constants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to read large lunatic XML files
 * It iterates through the file instead of storing the entire file into memory
 */
public class LunaticXmlDataSequentialParser{
    private final LocalDateTime fileDate;
    private final XMLEventReader reader;


     public LunaticXmlDataSequentialParser(final Path filePath, final InputStream stream) throws IOException, XMLStreamException {
         this.fileDate = getFileDate(filePath);

         XMLInputFactory factory = XMLInputFactory.newInstance();
         factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

         reader = factory.createXMLEventReader(stream);
     }

    /**
     * Gets the Campaign Information from the file
     * @return the Campaign
     */
     public LunaticXmlCampaign getCampaign() throws XMLStreamException {
        LunaticXmlCampaign campaign = new LunaticXmlCampaign();

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(Constants.SURVEY_UNITS_NODE_NAME)) {
                return campaign;
            }

            if(event.isStartElement()) {
                final StartElement element = event.asStartElement();
                final String elementName = element.getName().getLocalPart();

                switch (elementName){
                    case Constants.CAMPAIGN_ID_ELEMENT_NAME:
                        campaign.setIdCampaign(reader.getElementText());
                        break;
                    case Constants.CAMPAIGN_LABEL_ELEMENT_NAME:
                        campaign.setLabel(reader.getElementText());
                        break;
                    default:
                }
            }
        }
        return null;
     }

    /**
     * Read the next SurveyUnit from the file
     * @return the SurveyUnit
     */
    public LunaticXmlSurveyUnit readNextSurveyUnit() throws XMLStreamException {
             while (reader.hasNext()) {
                 final XMLEvent event = reader.nextEvent();
                 if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(Constants.SURVEY_UNIT_ELEMENT_NAME)) {
                     return parseSurveyUnit(reader);
                 }
             }
             return null;
    }

    /**
     * Parses a survey unit
     * @param reader an XMLEventReader associated to XML file stream
     * @return the survey unit filled with data
     */
    private LunaticXmlSurveyUnit parseSurveyUnit(final XMLEventReader reader) throws XMLStreamException {
        LunaticXmlSurveyUnit xmlSurveyUnit = new LunaticXmlSurveyUnit();
        xmlSurveyUnit.setFileDate(this.fileDate);

        LunaticXmlData data = new LunaticXmlData();

        while(reader.hasNext()){ //For each line
            final XMLEvent event = reader.nextEvent();
            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(Constants.SURVEY_UNIT_ELEMENT_NAME)){
                // End of survey unit
                xmlSurveyUnit.setData(data);
                return xmlSurveyUnit;
            }

            if(event.isStartElement()){
                final StartElement element = event.asStartElement();
                final String elementName = element.getName().getLocalPart();

                switch (elementName){
                    case Constants.SURVEYUNIT_ID_ELEMENT_NAME:
                        xmlSurveyUnit.setId(reader.getElementText());
                        break;
                    case Constants.SURVEYUNIT_SURVEYMODELID_ELEMENT_NAME:
                        xmlSurveyUnit.setQuestionnaireModelId(reader.getElementText());
                        break;
                    case Constants.SURVEYUNIT_DATA_COLLECTED_NODE_NAME:
                        data.setCollected(readCollected(reader));
                        break;
                    case Constants.SURVEYUNIT_DATA_CALCULATED_NODE_NAME:
                        data.setCalculated(readCalculatedOrExternal(reader));
                        break;
                    case Constants.SURVEYUNIT_DATA_EXTERNAL_NODE_NAME:
                        data.setExternal(readCalculatedOrExternal(reader));
                        break;
                    default:
                }
            }
        }
        return xmlSurveyUnit;
    }

    private List<LunaticXmlCollectedData> readCollected(XMLEventReader reader) throws XMLStreamException {
        List<LunaticXmlCollectedData> lunaticXmlCollectedDataList = new ArrayList<>();
        while(reader.hasNext()){
            final XMLEvent event = reader.nextEvent();
            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(Constants.SURVEYUNIT_DATA_COLLECTED_NODE_NAME)){
                // End of collected data part
                return lunaticXmlCollectedDataList;
            }

            if(event.isStartElement()){
                final StartElement element = event.asStartElement();
                final String variableName = element.getName().getLocalPart();
                LunaticXmlCollectedData variable = readNextCollectedVariable(reader, variableName);
                lunaticXmlCollectedDataList.add(variable);
            }
        }

        return lunaticXmlCollectedDataList;
    }

    private LunaticXmlCollectedData readNextCollectedVariable(XMLEventReader reader, String variableName) throws XMLStreamException {
        LunaticXmlCollectedData variable = new LunaticXmlCollectedData();
        variable.setVariableName(variableName);

        while(reader.hasNext()){
            final XMLEvent event = reader.nextEvent();
            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(variableName)){
                // End of variable
                return variable;
            }

            if(event.isStartElement()) {
                final StartElement element = event.asStartElement();
                final String stateName = element.getName().getLocalPart();

                List<ValueType> values = new ArrayList<>();
                if(element.isStartElement() && getType(element) != null && !getType(element).getValue().equals("null")){
                    //If only 1 value (determined by presence of type)
                    String type = getType(element).getValue();
                    String value = reader.getElementText();
                    values.add(new ValueType(value, type));
                }else{
                    //If multiple value
                    values = readValues(reader, stateName);
                }

                switch (stateName) {
                    case "COLLECTED":
                        variable.setCollected(values);
                        break;
                    case "EDITED":
                        variable.setEdited(values);
                        break;
                    case "INPUTED":
                        variable.setInputed(values);
                        break;
                    case "FORCED":
                        variable.setForced(values);
                        break;
                    case "PREVIOUS":
                        variable.setPrevious(values);
                        break;
                    default:
                }
            }
        }

        return variable;
    }

	private static Attribute getType(StartElement element) {
		return element.getAttributeByName(new QName("type"));
	}

    private List<ValueType> readValues(XMLEventReader reader, String stateName) throws XMLStreamException {
        List<ValueType> values = new ArrayList<>();

        while(reader.hasNext()){
            final XMLEvent event = reader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(stateName)){
                // End of variable
                return values;
            }

            if(event.isStartElement()) {
                final StartElement element = event.asStartElement();

                String type = getType(element).getValue();
                String value = reader.getElementText();

                values.add(new ValueType(value, type));
            }
        }

        return values;
    }

    private List<LunaticXmlOtherData> readCalculatedOrExternal(XMLEventReader reader) throws XMLStreamException {
        List<LunaticXmlOtherData> lunaticXmlOtherDataList = new ArrayList<>();
        while(reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();

            if (event.isEndElement() &&
                    (event.asEndElement().getName().getLocalPart().equals(Constants.SURVEYUNIT_DATA_CALCULATED_NODE_NAME)
                            || event.asEndElement().getName().getLocalPart().equals(Constants.SURVEYUNIT_DATA_EXTERNAL_NODE_NAME))
            ){
                // End of calculated or external data part
                return lunaticXmlOtherDataList;
            }
            if(event.isStartElement()) {
                final StartElement element = event.asStartElement();
                final String variableName = element.getName().getLocalPart();

                LunaticXmlOtherData variable = new LunaticXmlOtherData();
                variable.setVariableName(variableName);

                String type = getType(element).getValue();
                String value = reader.getElementText();

                List<ValueType> values = new ArrayList<>();
                values.add(new ValueType(value, type));
                variable.setValues(values);

                lunaticXmlOtherDataList.add(variable);
            }
        }

        return lunaticXmlOtherDataList;
    }

    private LocalDateTime getFileDate(Path filePath) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        return LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.of("Europe/Paris"));
    }
}
