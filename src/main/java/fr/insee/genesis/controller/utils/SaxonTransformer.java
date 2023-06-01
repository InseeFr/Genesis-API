package fr.insee.genesis.controller.utils;

import lombok.extern.slf4j.Slf4j;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

@Slf4j
public class SaxonTransformer {

    public void xslTransform(URL inputXmlURL, String inputXslPath, Path outputXmlPath) {
        log.info("About to transform the file from URL: " + inputXmlURL);
        log.info("using the XSL file " + inputXslPath);

        // Get the XML input file
        StreamSource xmlSource;
        InputStream xmlInput;
        try {
            xmlInput = inputXmlURL.openStream();
            xmlSource = new StreamSource(xmlInput);
            xmlSource.setSystemId(inputXmlURL.toString());
        } catch (IOException e) {
            log.error(String.format("IOException when trying to read file from URL: %s", inputXmlURL), e);
            return; // to break here if the xml input file is not found
        }

        // Get the XSL file
        StreamSource xslSource;
        InputStream xslInput;
        xslInput = SaxonTransformer.class.getClassLoader().getResourceAsStream(inputXslPath);
        xslSource = new StreamSource(xslInput);
        xslSource.setSystemId(inputXslPath);

        // Instantiation of the XSL transformer factory
        TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl(); // basic transformer
        transformerFactory.setURIResolver(new ClasspathURIResolver());

        // Apply the XSL transformation
        try {
            Transformer transformer = transformerFactory.newTransformer(xslSource);
            StreamResult sr = new StreamResult(outputXmlPath.toFile());
            transformer.transform(xmlSource, sr);
        } catch (TransformerConfigurationException e) {
            log.error("Error when trying to configure the XSL transformer using XSL file: " + inputXslPath, e);
        } catch (TransformerException e) {
            log.error("Error when trying to apply the XSL transformation using XSL file: " + inputXslPath, e);
        }

        try {
            xmlInput.close();
            xslInput.close();
        } catch (IOException e) {
            log.error("IOException occurred when trying to close the streams after XSL transformation.", e);
        }


    }
}
