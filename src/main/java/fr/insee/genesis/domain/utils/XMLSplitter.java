package fr.insee.genesis.domain.utils;

import fr.insee.genesis.exceptions.GenesisException;
import lombok.experimental.UtilityClass;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class XMLSplitter {

	// We use StAX in this class to deal with memory issues on huge XML files
	public static void split(String inputfolder, String xmlfile, String outputFolder, String condition, int nbElementsByFile) throws XMLStreamException, IOException, GenesisException {

		String xmlResource = inputfolder + xmlfile;
		List<XMLEvent> header = getHeader(xmlResource, condition);

		XMLEventFactory xef = XMLEventFactory.newFactory();
		XMLInputFactory xif = XMLInputFactory.newInstance();
		xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		xif.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try (FileReader fr = new FileReader(xmlResource)){
            XMLEventReader xer = xif.createXMLEventReader(fr);

		    StartElement rootStartElement = xer.nextTag().asStartElement();
		    StartDocument startDocument = xef.createStartDocument();
		    EndDocument endDocument = xef.createEndDocument();

		    XMLOutputFactory xof = XMLOutputFactory.newFactory();
		    int fileCount = 1;
		    while(xer.hasNext() && !xer.peek().isEndDocument()) {
		    	XMLEvent xmlEvent = xer.nextEvent();

		    	if (isStartElementWithName(condition, xmlEvent)) {
                    // Create a file for the fragment, the name is derived from the value of the id attribute
                    FileWriter fileWriter = new FileWriter(outputFolder + "split" + fileCount + ".xml");

                    // A StAX XMLEventWriter will be used to write the XML fragment
                    XMLEventWriter xew = xof.createXMLEventWriter(fileWriter);
                    xew.add(startDocument);

                    // Add the elements which are common to all split files
                    addHeadersToNewFile(header, xew);

                    // Write the XMLEvents that are part of SurveyUnit element
                    xew.add(xmlEvent);
                    xmlEvent = xer.nextEvent();
                    int nbResponses = 1;
                    // We loop until we reach the end tag Survey units indicating the near end of the document
                    iterateOnSurveyUnits(condition, nbElementsByFile, xer, xmlEvent, xew, nbResponses);

                    // Write the file, close everything we opened and update the file's counter
                    xew.add(xef.createEndElement(rootStartElement.getName(), null));
                    xew.add(endDocument);
                    fileWriter.close();

                    fileCount++;
                }
			}
            xer.close();
		}
	}

	private static void iterateOnSurveyUnits(String condition, int nbElementsByFile, XMLEventReader xer,
			XMLEvent xmlEvent, XMLEventWriter xew, int nbResponses) throws XMLStreamException {
		while (xer.hasNext() && !(isEndElementWithName(xmlEvent,"SurveyUnits"))) {
			// We exit the specified number of elements by file is reached
			if (isEndElementWithName(xmlEvent, condition) && nbResponses >= nbElementsByFile) {
				break;
			}
			xew.add(xmlEvent);
			xmlEvent = xer.nextEvent();
			// We count the number of survey unit in the file
			if (isStartElementWithName(condition, xmlEvent)) {
				nbResponses++;
			}
		}
	}

	private static void addHeadersToNewFile(List<XMLEvent> header, XMLEventWriter xew) throws XMLStreamException {
		for (XMLEvent headerEvents : header) {
			xew.add(headerEvents);
		}
	}

	private static boolean isStartElementWithName(String condition, XMLEvent xmlEvent) {
		return xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals(condition);
	}

	private static boolean isEndElementWithName(XMLEvent xmlEvent, String condition) {
		return xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(condition);
	}

	private static List<XMLEvent> getHeader(String xmlResource, String condition) throws XMLStreamException, GenesisException {
		XMLInputFactory xif = XMLInputFactory.newInstance();
		xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		xif.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try (FileReader fr = new FileReader(xmlResource)) {
            XMLEventReader xer = xif.createXMLEventReader(fr);

            List<XMLEvent> cachedXMLEvents = new ArrayList<>();
            while (xer.hasNext() && !xer.peek().isEndDocument()) {
                XMLEvent xmlEvent = xer.nextTag();
                if (!xmlEvent.isStartElement()) {
                    break;
                }
                StartElement breakStartElement = xmlEvent.asStartElement();

                cachedXMLEvents.add(breakStartElement);
                xmlEvent = xer.nextEvent();
                while (!(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().equals(breakStartElement.getName()))) {
                    if (isStartElementWithName(condition, xmlEvent)) {
                        xer.close();
                        return cachedXMLEvents;
                    }
                    cachedXMLEvents.add(xmlEvent);
                    xmlEvent = xer.nextEvent();
                }
            }
            xer.close();
        } catch (IOException e) {
            throw new GenesisException(500,e.getMessage());
        }
        return List.of();
	}

}
