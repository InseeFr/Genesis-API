package fr.insee.genesis.controller.sources.ddi;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.utils.SaxonTransformer;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class DDIReader {

    private DDIReader() {
        throw new IllegalStateException("Utility class");
    }

    public static VariablesMap getVariablesFromDDI(URL ddiUrl) throws GenesisException {
        boolean isCreated = false;
        try  {
            File variablesFile = File.createTempFile("variables", ".xml");
            isCreated = true;
            variablesFile.deleteOnExit();
            Path variablesTempFilePath = variablesFile.toPath();
            transformDDI(ddiUrl, variablesTempFilePath);
            VariablesMap variablesMap = readVariables(variablesTempFilePath);
            Files.delete(variablesFile.toPath());
            log.debug("File {} deleted",variablesFile.toPath());
            return variablesMap;
        }
        catch (IOException e) {
            if (isCreated) {
                throw new GenesisException(500,"Impossible to delete variables temp file");
            } else {
                throw new GenesisException(500,"Unable to create variables temp file.");
            }
        } catch (SAXException | ParserConfigurationException e) {
            throw new GenesisException(500,"Unable to read Variables in DDI file.");
        }
    }

    private static VariablesMap readVariables(Path variablesFilePath)
            throws GenesisException, SAXException, IOException, ParserConfigurationException {
        VariablesMap variablesMap = new VariablesMap();

        // Parse
        Element root = readXmlFile(variablesFilePath);

        // Get XML groups
        NodeList groupElements = root.getChildNodes();

        // temporary save the root group name to normalize it
        String rootGroupName = null;

        for (int i = 0; i < groupElements.getLength(); i++) {

            try {
                Node groupNode = groupElements.item(i);
                if ("Group".equals(groupNode.getNodeName()) && groupNode.getNodeType() == Node.ELEMENT_NODE) {
                    // Get the group name
                    Element groupElement = (Element) groupNode;

                    String groupName = groupElement.getAttribute("name");
                    String parentGroupName = groupElement.getAttribute("parent");

                    // Store the group
                    Group group;
                    if (!groupElement.hasAttribute("parent") || StringUtils.isEmpty(parentGroupName)) {
                        rootGroupName = groupName;
                        group = variablesMap.getRootGroup();

                    } else {
                        group = new Group(groupName, parentGroupName);
                        variablesMap.putGroup(group);
                    }

                    // Variables in the group
                    getVariablesInGroup(variablesMap, groupNode, group);
                }
            } catch (NullPointerException e) {
                log.error(String.format("Missing field in mandatory information for variable %s",
                        ((Element) groupElements.item(i)).getAttribute("name")));
            }

            normalizeRootGroupName(variablesMap, rootGroupName);

        }
        // Normalize the root group name
        if (rootGroupName == null) {
            log.debug("Failed to identify the root group while reading variables files: " + variablesFilePath);
        }
        return variablesMap;
    }

    private static void normalizeRootGroupName(VariablesMap variablesMap, String rootGroupName) {
        for (String groupName : variablesMap.getSubGroupNames()) {
            Group group = variablesMap.getGroup(groupName);
            if (group.getParentName().equals(rootGroupName)) {
                group.setParentName(Constants.ROOT_GROUP_NAME);
            }
        }
    }

    private static void getVariablesInGroup(VariablesMap variablesMap, Node groupNode, Group group) {
        NodeList variableNodes = groupNode.getChildNodes();
        for (int j = 0; j < variableNodes.getLength(); j++) {
            Node variableNode = variableNodes.item(j);
            if ("Variable".equals(variableNode.getNodeName()) && // Add only Variable
                    variableNode.getNodeType() == Node.ELEMENT_NODE) {
                Element variableElement = (Element) variableNode;

                addVariableToMap(variablesMap, group, variableElement);
            }
        }
    }

    private static void addVariableToMap(VariablesMap variablesMap, Group group, Element variableElement) {
        // Variable name, type and size
        String variableName = getFirstChildValue(variableElement, "Name");
        VariableType variableType = VariableType.valueOf(getFirstChildValue(variableElement, "Format"));
        String variableLength = getFirstChildValue(variableElement, "Size");

        Node questionItemName = getFirstChildNode(variableElement, "QuestionItemName");
        Node valuesElement = getFirstChildNode(variableElement, "Values");
        Node mcqElement = getFirstChildNode(variableElement, "QGrid");

        if (valuesElement != null) {
            UcqVariable variable = new UcqVariable(variableName, group, variableType, variableLength);
            if (questionItemName != null) {
                variable.setQuestionItemName(questionItemName.getTextContent());
            } else if (mcqElement != null) {
                variable.setQuestionItemName(mcqElement.getTextContent());
                variable.setInQuestionGrid(true);
            }
            NodeList valueElements = valuesElement.getChildNodes();
            getModalities(variable, valueElements);
            variablesMap.putVariable(variable);
        } else if (mcqElement != null) {
            McqVariable variable = new McqVariable(variableName, group, variableType, variableLength);
            variable.setQuestionItemName(mcqElement.getTextContent());
            variable.setInQuestionGrid(true);
            variable.setText(getFirstChildValue(variableElement, "Label"));
            variablesMap.putVariable(variable);
        } else {
            Variable variable = new Variable(variableName, group, variableType, variableLength);
            if (questionItemName != null) {
                variable.setQuestionItemName(questionItemName.getTextContent());
            } else {
                variable.setQuestionItemName(variableName);
            }
            variablesMap.putVariable(variable);
        }
    }

    private static void getModalities(UcqVariable variable, NodeList valueElements) {
        for (int k = 0; k < valueElements.getLength(); k++) {
            Node valueElement = valueElements.item(k);
            if (valueElement.getNodeType() == Node.ELEMENT_NODE
                    && "Value".equals(valueElement.getNodeName())) {
                variable.addModality(valueElement.getTextContent(),
                        ((Element) valueElement).getAttribute("label"));

            }
        }
    }

    private static String getFirstChildValue(Element variableElement, String childTagName) {
        Node child = getFirstChildNode(variableElement, childTagName);
        if (child == null)
            return null;
        return child.getTextContent();
    }

    private static Node getFirstChildNode(Element variableElement, String childTagName) {
        NodeList children = variableElement.getElementsByTagName(childTagName);
        if (children.getLength() == 0){
            return null;
        }
        return children.item(0);
    }

    private static void transformDDI(URL ddiUrl, Path variablesFilePath) {
        SaxonTransformer saxonTransformer = new SaxonTransformer();
        saxonTransformer.xslTransform(ddiUrl, Constants.XSLT_STRUCTURED_VARIABLES, variablesFilePath);
    }

    private static Element readXmlFile(Path filePath) throws ParserConfigurationException, IOException, SAXException, GenesisException {
        File file = filePath.toFile();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder= factory.newDocumentBuilder();
        Document document  = builder.parse(file);
        if (document == null) throw new GenesisException(500,"Can't read DDI - DDI is null");
        return document.getDocumentElement();
    }
}
