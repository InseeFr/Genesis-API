package fr.insee.genesis.controller.utils;

import lombok.extern.slf4j.Slf4j;

import javax.xml.stream.*;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class XMLSplitter {

	// We use StAX in this class to deal with memory issues on huge XML files
	private XMLSplitter() {
		throw new IllegalStateException("Utility class");
	}

	public static void split(String inputfolder, String xmlfile, String outputFolder, String condition, int nbElementsByFile) throws Exception {

		String xmlResource = inputfolder + xmlfile;
		List<XMLEvent> header = getHeader(xmlResource, condition);

		XMLEventFactory xef = XMLEventFactory.newFactory();
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLEventReader xer = xif.createXMLEventReader(new FileReader(xmlResource));
		StartElement rootStartElement = xer.nextTag().asStartElement();
		StartDocument startDocument = xef.createStartDocument();
		EndDocument endDocument = xef.createEndDocument();

		XMLOutputFactory xof = XMLOutputFactory.newFactory();
		int fileCount = 1;
		while(xer.hasNext() && !xer.peek().isEndDocument()) {
			XMLEvent xmlEvent = xer.nextEvent();

			if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals(condition)) {
				// Create a file for the fragment, the name is derived from the value of the id attribute
				FileWriter fileWriter = null;
				fileWriter = new FileWriter(outputFolder + "split" + fileCount + ".xml");

				// A StAX XMLEventWriter will be used to write the XML fragment
				XMLEventWriter xew = xof.createXMLEventWriter(fileWriter);
				xew.add(startDocument);

				// Add the elements which are common to all split files
				for (XMLEvent headerEvents : header) {
					xew.add(headerEvents);
				}

				// Write the XMLEvents that are part of SurveyUnit element
				xew.add(xmlEvent);
				xmlEvent = xer.nextEvent();
				int nbResponses = 1;
				// We loop until we reach the end tag Survey units indicating the near end of the document
				while (xer.hasNext() && !(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("SurveyUnits"))) {
					// We exit the specified number of elements by file is reached
					if ((xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(condition)) && nbResponses >= nbElementsByFile) {
						break;
					}
					xew.add(xmlEvent);
					xmlEvent = xer.nextEvent();
					// We count the number of survey unit in the file
					if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals(condition)) {
						nbResponses++;
					}
				}

				// Write the file, close everything we opened and update the file's counter
				xew.add(xef.createEndElement(rootStartElement.getName(), null));
				xew.add(endDocument);
				fileWriter.close();

				fileCount++;

			}
		}
	}

	private static List<XMLEvent> getHeader(String xmlResource, String condition) throws Exception {
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLEventReader xer = xif.createXMLEventReader(new FileReader(xmlResource));

		List<XMLEvent> cachedXMLEvents = new ArrayList<>();
		while(xer.hasNext() && !xer.peek().isEndDocument()) {
			XMLEvent xmlEvent = xer.nextTag();
			if (!xmlEvent.isStartElement()) {
				break;
			}
			StartElement breakStartElement = xmlEvent.asStartElement();

			cachedXMLEvents.add(breakStartElement);
			xmlEvent = xer.nextEvent();
			while (!(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().equals(breakStartElement.getName()))) {
				if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals(condition)) {
					xer.close();
					return cachedXMLEvents;
				}
				cachedXMLEvents.add(xmlEvent);
				xmlEvent = xer.nextEvent();
			}
		}
		xer.close();
		return List.of();
	}

}
