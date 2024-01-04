package fr.insee.genesis.controller.sources.xml;

import fr.insee.genesis.Constants;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LunaticXmlDataParser {

    private Document readXmlFile(Path filePath) throws IOException, SAXException, GenesisException, ParserConfigurationException {
        File file = filePath.toFile();
        //Extraction of the file last modified date
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        log.info("lastModifiedTime: " + attr.lastModifiedTime());
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        if (document == null){
            throw new GenesisException(500,"Can't read file {}");
        }
        return document;
    }

    private LocalDateTime getFileDate(Path filePath) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        return LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.of("Europe/Paris"));
    }

    public LunaticXmlCampaign parseDataFile(Path filePath) throws GenesisException, IOException, ParserConfigurationException, SAXException {
        Document document = readXmlFile(filePath);
        log.debug("Begin to parse {} ", filePath);
        LunaticXmlCampaign campaign = new LunaticXmlCampaign();
        Element root = document.getDocumentElement();
        campaign.setIdCampaign(root.getElementsByTagName("Id").item(0).getFirstChild().getNodeValue());
        if(root.getElementsByTagName("Label").item(0).getFirstChild() != null)
            campaign.setLabel(root.getElementsByTagName("Label").item(0).getFirstChild().getNodeValue());
        else campaign.setLabel("");
        NodeList surveyUnits = root.getElementsByTagName("SurveyUnit");
        List<LunaticXmlSurveyUnit> lunaticXmlSurveyUnits = new ArrayList<>();
        for (int i = 0; i < surveyUnits.getLength(); i++) {
            Node surveyUnit = surveyUnits.item(i);
            if (surveyUnit.getNodeType() == Node.ELEMENT_NODE) {
                Element surveyUnitElement = (Element) surveyUnit;
                LunaticXmlSurveyUnit lunaticXmlSurveyUnit = new LunaticXmlSurveyUnit();
                lunaticXmlSurveyUnit.setFileDate(getFileDate(filePath));
                lunaticXmlSurveyUnit.setId(surveyUnitElement.getElementsByTagName("Id").item(0).getFirstChild().getNodeValue());
                lunaticXmlSurveyUnit.setQuestionnaireModelId(surveyUnitElement.getElementsByTagName("QuestionnaireModelId").item(0).getFirstChild().getNodeValue());
                Node data = surveyUnitElement.getElementsByTagName("Data").item(0);
                NodeList dataNodeList = data.getChildNodes();
                lunaticXmlSurveyUnit.setData(getData(dataNodeList));
                lunaticXmlSurveyUnits.add(lunaticXmlSurveyUnit);
            }
        }
        campaign.setSurveyUnits(lunaticXmlSurveyUnits);
        log.info("Successfully parsed Lunatic answers file: {}",filePath);
        return campaign;
    }

    private LunaticXmlData getData(NodeList dataNodeList) throws GenesisException {
        LunaticXmlData lunaticXmlData = new LunaticXmlData();
        for (int j = 0; j < dataNodeList.getLength(); j++) {
            Node dataNode = dataNodeList.item(j);
            if (dataNode.getNodeType() == Node.ELEMENT_NODE && dataNode.getNodeName().equals(Constants.COLLECTED_NODE_NAME)) {
                readCollected(lunaticXmlData, dataNode);
            }
            if (dataNode.getNodeType() == Node.ELEMENT_NODE && dataNode.getNodeName().equals(Constants.CALCULATED_NODE_NAME)) {
                readCalculatedOrExternal(lunaticXmlData, dataNode, Constants.CALCULATED_NODE_NAME);
            }
            if (dataNode.getNodeType() == Node.ELEMENT_NODE && dataNode.getNodeName().equals(Constants.EXTERNAL_NODE_NAME)) {
                readCalculatedOrExternal(lunaticXmlData, dataNode, Constants.EXTERNAL_NODE_NAME);
            }
        }
        return lunaticXmlData;
    }

    private void readCollected(LunaticXmlData lunaticXmlData, Node dataNode) throws GenesisException {
            List<LunaticXmlCollectedData> lunaticXmlCollectedDataList = new ArrayList<>();
            Element dataElement = (Element) dataNode;
            NodeList variablesNodes = dataElement.getChildNodes();
            for (int k = 0; k < variablesNodes.getLength(); k++) {
                Node variableNode = variablesNodes.item(k);
                if (variableNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element collectedElement = (Element) variableNode;
                    LunaticXmlCollectedData variable = extractCollected(collectedElement);
                    lunaticXmlCollectedDataList.add(variable);
                }
            }
            lunaticXmlData.setCollected(lunaticXmlCollectedDataList);
    }

    private void readCalculatedOrExternal(LunaticXmlData lunaticXmlData, Node dataNode, String dataType) {
        List<LunaticXmlOtherData> lunaticXmlOtherData = new ArrayList<>();
        Element dataElement = (Element) dataNode;
        NodeList variablesNodes = dataElement.getChildNodes();
        for (int k = 0; k < variablesNodes.getLength(); k++) {
            Node variableNode = variablesNodes.item(k);
            if (variableNode.getNodeType() == Node.ELEMENT_NODE) {
                Element calculatedOrExternalElement = (Element) variableNode;
                LunaticXmlOtherData variable = extractCalculatedOrExternal(calculatedOrExternalElement);
                lunaticXmlOtherData.add(variable);
            }
        }
        if (dataType.equals(Constants.CALCULATED_NODE_NAME)) {
            lunaticXmlData.setCalculated(lunaticXmlOtherData);
        }
        if (dataType.equals(Constants.EXTERNAL_NODE_NAME)) {
            lunaticXmlData.setExternal(lunaticXmlOtherData);
        }
    }

    /**
     * Extracts the data from a collected variable element
     * @param element
     * @return LunaticXmlCollectedData : the data extracted from the element
     * @throws GenesisException
     */
    private LunaticXmlCollectedData extractCollected(Element element) throws GenesisException {
        if (null == element) {
            return null;
        }
        LunaticXmlCollectedData varData = new LunaticXmlCollectedData();
        varData.setVariableName(element.getTagName());

        NodeList data = element.getChildNodes();
        for (int i = 0; i < data.getLength(); i++) {
            Node value = data.item(i);
            if (value.getNodeType() == Node.ELEMENT_NODE) {
                setValues(varData, value, getCollectedValues(value));
            }
        }
        return varData;
    }

    private List<ValueType> getCollectedValues(Node value) {
        Element valueElement = (Element) value;
        List<ValueType> valueTypes = new ArrayList<>();
        if(hasChildElements(valueElement)){
            NodeList values = valueElement.getChildNodes();
            for (int j = 0; j < values.getLength(); j++) {
                if (values.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    addNodeToJavaList(values.item(j), valueTypes);
                }
            }
        } else {
            addNodeToJavaList(value, valueTypes);
        }
        return valueTypes;
    }

    private LunaticXmlOtherData extractCalculatedOrExternal(Element element) {
        if (null == element) {
            return null;
        }
        LunaticXmlOtherData varData = new LunaticXmlOtherData();
        varData.setVariableName(element.getTagName());
        List<ValueType> valueTypes = new ArrayList<>();
        if (hasChildElements(element)){
            NodeList values = element.getChildNodes();
            for (int j = 0; j < values.getLength(); j++) {
                if (values.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    addNodeToJavaList(values.item(j), valueTypes);
                }
            }
        } else {
            if(element.getFirstChild()!=null){
                valueTypes.add(new ValueType(element.getFirstChild().getNodeValue(), element.getAttribute("type")));
            } else {
                valueTypes.add(new ValueType(null, element.getAttribute("type")));
            }
        }
        varData.setValues(valueTypes);
        return varData;
    }

    private void addNodeToJavaList(Node nodeToAdd, List<ValueType> javaList) {
        if (nodeToAdd.hasChildNodes()){
            javaList.add(new ValueType(nodeToAdd.getFirstChild().getNodeValue(), ((Element)nodeToAdd).getAttribute("type")));
        } else {
            javaList.add(new ValueType(null, ((Element)nodeToAdd).getAttribute("type")));
        }
    }

    /**
     * Check if the element has child elements
     * @param element
     * @return true if the element has child elements, false otherwise
     */
    private boolean hasChildElements(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the values of the LunaticXmlCollectedData object
     * @param varData
     * @param value
     * @param valueTypes
     * @throws GenesisException
     */
    private static void setValues(LunaticXmlCollectedData varData, Node value, List<ValueType> valueTypes) throws GenesisException {
        Element valueElement = (Element) value;
        switch(valueElement.getTagName()){
            case Constants.COLLECTED_NODE_NAME:
                varData.setCollected(valueTypes);
                break;
            case "EDITED":
                varData.setEdited(valueTypes);
                break;
            case "INPUTED":
                varData.setInputed(valueTypes);
                break;
            case "FORCED":
                varData.setForced(valueTypes);
                break;
            case "PREVIOUS":
                varData.setPrevious(valueTypes);
                break;
            default:
                throw new GenesisException(421, String.format("Tag %s not recognized", valueElement.getTagName()));
        }
    }

}
