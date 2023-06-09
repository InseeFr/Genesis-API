package fr.insee.genesis.controller.sources.xml;

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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LunaticXmlDataParser {


/*    public final void parseSurveyData(Path dataPath) throws NullException {
        if (dataPath == null) log.error("Datapath is null");
        else {
            if (Files.isRegularFile(dataPath)) {
                parseDataFile(dataPath);
            }

            else if (Files.isDirectory(dataPath)) {
                try (Stream<Path> stream = Files.list(dataPath)){
                    stream.forEach(t -> {
                        try {
                            parseDataFile(t);
                        } catch (NullException e) {
                            log.error("IOException occurred when trying to list data file: {} in folder {}", t, dataPath);
                        }
                    });
                } catch (IOException e) {
                    log.error(String.format("IOException occurred when trying to list data files of folder: %s", dataPath));
                }
            }

            else {
                log.warn(String.format("Data path given could not be identified as a file or folder: %s", dataPath));
                log.warn("No data was parsed.");
            }
        }
    }*/

    private static final String CALCULATED = "CALCULATED";
    private static final String EXTERNAL = "EXTERNAL";

    private Document readXmlFile(Path filePath) throws IOException, SAXException, GenesisException, ParserConfigurationException {
        File file = filePath.toFile();
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

    public LunaticXmlCampaign parseDataFile(Path filePath) throws Exception {

        Document document = readXmlFile(filePath);
        log.debug("Begin to parse {} ", filePath);
        LunaticXmlCampaign campaign = new LunaticXmlCampaign();
        if (document!=null) {
            Element root = document.getDocumentElement();
            campaign.setId(root.getElementsByTagName("Id").item(0).getFirstChild().getNodeValue());
            campaign.setLabel(root.getElementsByTagName("Label").item(0).getFirstChild().getNodeValue());
            NodeList surveyUnits = root.getElementsByTagName("SurveyUnit");
            List<LunaticXmlSurveyUnit> lunaticXmlSurveyUnits = new ArrayList<>();
            for (int i = 0; i < surveyUnits.getLength(); i++) {
                Node surveyUnit = surveyUnits.item(i);
                if (surveyUnit.getNodeType() == Node.ELEMENT_NODE) {
                    Element surveyUnitElement = (Element) surveyUnit;
                    LunaticXmlSurveyUnit lunaticXmlSurveyUnit = new LunaticXmlSurveyUnit();
                    lunaticXmlSurveyUnit.setId(surveyUnitElement.getElementsByTagName("Id").item(0).getFirstChild().getNodeValue());
                    lunaticXmlSurveyUnit.setQuestionnaireModelId(surveyUnitElement.getElementsByTagName("QuestionnaireModelId").item(0).getFirstChild().getNodeValue());
                    Node data = surveyUnitElement.getElementsByTagName("Data").item(0);
                    NodeList dataNodeList = data.getChildNodes();
                    LunaticXmlData lunaticXmlData = new LunaticXmlData();
                    for (int j = 0; j < dataNodeList.getLength(); j++) {
                        Node dataNode = dataNodeList.item(j);
                        if (dataNode.getNodeType() == Node.ELEMENT_NODE && dataNode.getNodeName().equals("COLLECTED")) {
                            readCollected(lunaticXmlData, dataNode);
                        }
                        if (dataNode.getNodeType() == Node.ELEMENT_NODE && dataNode.getNodeName().equals(CALCULATED)) {
                            readCalculatedOrExternal(lunaticXmlData, dataNode, CALCULATED);
                        }
                        if (dataNode.getNodeType() == Node.ELEMENT_NODE && dataNode.getNodeName().equals(EXTERNAL)) {
                            readCalculatedOrExternal(lunaticXmlData, dataNode, EXTERNAL);
                        }
                    }
                    lunaticXmlSurveyUnit.setData(lunaticXmlData);
                    lunaticXmlSurveyUnits.add(lunaticXmlSurveyUnit);
                }
            }
            campaign.setSurveyUnits(lunaticXmlSurveyUnits);
            log.info("Successfully parsed Lunatic answers file: {}",filePath);
        }
        return campaign;
    }

    private void readCollected(LunaticXmlData lunaticXmlData, Node dataNode) throws Exception {
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
        if (dataType.equals(CALCULATED)) {
            lunaticXmlData.setCalculated(lunaticXmlOtherData);
        }
        if (dataType.equals(EXTERNAL)) {
            lunaticXmlData.setExternal(lunaticXmlOtherData);
        }
    }

    /**
     * Extracts the data from a collected variable element
     * @param element
     * @return LunaticXmlCollectedData : the data extracted from the element
     * @throws Exception
     */
    private LunaticXmlCollectedData extractCollected(Element element) throws Exception {
        if (null == element) {
            return null;
        }
        LunaticXmlCollectedData varData = new LunaticXmlCollectedData();
        varData.setVariableName(element.getTagName());

        NodeList data = element.getChildNodes();
        for (int i = 0; i < data.getLength(); i++) {
            Node value = data.item(i);
            if (value.getNodeType() == Node.ELEMENT_NODE) {
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
                setValues(varData, valueElement, valueTypes);
            }
        }
        return varData;
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
     * @param valueElement
     * @param valueTypes
     * @throws GenesisException
     */
    private static void setValues(LunaticXmlCollectedData varData, Element valueElement, List<ValueType> valueTypes) throws GenesisException {
        switch(valueElement.getTagName()){
            case "COLLECTED":
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
                throw new GenesisException(421, String.format("Tag {} not recognized", valueElement.getTagName()));
        }
    }

}
