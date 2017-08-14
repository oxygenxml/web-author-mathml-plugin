package com.oxygenxml.sdksamples.mathml;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.apache.xerces.parsers.DOMParser;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ro.sync.xml.parser.ParserCreator;

public class JEuclidRendererTest {

  /**
   * Tests adding of a namespace mapping.
   * 
   * @throws Exception
   */
  @Test
  public void testAddNamespaceMapping() throws Exception {
    JEuclidRenderer renderer = new JEuclidRenderer();
    
    String xmlWithNamespace = renderer.addMathmlNamespacePrefixMapping("<ggg:math/>", "ggg");
    assertTrue(renderer.hasNamespacePrefixDeclaration(xmlWithNamespace));
    assertWellformed(xmlWithNamespace);
    
    xmlWithNamespace = renderer.addMathmlNamespacePrefixMapping("<ggg:math />", "ggg");
    assertTrue(renderer.hasNamespacePrefixDeclaration(xmlWithNamespace));
    assertWellformed(xmlWithNamespace);
    
    xmlWithNamespace = renderer.addMathmlNamespacePrefixMapping("<ggg:math></ggg:math>", "ggg");
    assertTrue(renderer.hasNamespacePrefixDeclaration(xmlWithNamespace));
    assertWellformed(xmlWithNamespace);
  }
  
  /**
   * Asserts that the XML is well formed.
   * 
   * @param xml The xml.
   * 
   * @throws SAXException
   * @throws IOException
   */
  public void assertWellformed(String xml) throws SAXException, IOException {
    DOMParser domParser = ParserCreator.createDOMParser();
    domParser.parse(new InputSource(new StringReader(xml)));
  }
}
