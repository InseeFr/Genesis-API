package fr.insee.genesis.controller.utils;

import lombok.extern.slf4j.Slf4j;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

@Slf4j
public class ClasspathURIResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        log.debug("Resolving URI with href: " + href + " and base: " + base);
        String resolvedHref;
        if (href.startsWith("..")) {
            if (href.startsWith("../..")) {
                resolvedHref = href.replaceFirst("../..", "/xslt");
                log.debug("Resolved URI is: " + resolvedHref);
            } else {
                resolvedHref = href.replaceFirst("..", "/xslt");
                log.debug("Resolved XSLT URI is: " + resolvedHref);
            }
        } else {
            resolvedHref = href;
            log.debug("Resolved URI href is: " + resolvedHref);
        }
        return new StreamSource(ClasspathURIResolver.class.getResourceAsStream(resolvedHref));
    }

}
