package ains.network.utils;

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.XMLParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLParser {

    private static final Logger log = LoggerFactory.getLogger(XMLParser.class);
    private static IXMLParser xmlParser;

    static {
        try {
            xmlParser = XMLParserFactory.createDefaultXMLParser();
            xmlParser.setResolver(new VoidXMLEntityResolver());
        } catch (Exception e) {
            log.error("Can't instantiate XML Parser! Server quits");
        }
    }

    public static IXMLParser xmlParser() {
        return xmlParser;
    }
}
