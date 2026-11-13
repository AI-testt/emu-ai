package ains.network.utils;

import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.XMLEntityResolver;
import net.n3.nanoxml.XMLParseException;

import java.io.Reader;
import java.io.StringReader;

public class VoidXMLEntityResolver extends XMLEntityResolver {

    protected Reader openExternalEntity(IXMLReader xmlReader, String publicID, String systemID) throws XMLParseException {
        return new StringReader("");
    }
}
