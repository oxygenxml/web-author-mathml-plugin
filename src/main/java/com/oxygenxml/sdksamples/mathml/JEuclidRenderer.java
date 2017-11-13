package com.oxygenxml.sdksamples.mathml;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.converter.Converter;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorDocumentType;
import ro.sync.xml.parser.ParserCreator;

/**
 * MathML renderer using the JEuclid library.
 * @author costi_dumitrescu
 */
public class JEuclidRenderer {
  
  /**
   * MathML doctype.
   */
  private static final String MATH_ML_DOCTYPE = "<!DOCTYPE math PUBLIC \"-//W3C//DTD MathML 2.0//EN\" \"http://www.w3.org/Math/DTD/mathml2/mathml2.dtd\">\n";

  /**
   * Pattern used to detect the prefix for the MathML namespace.
   */
  private static final Pattern prefixPattern = Pattern.compile("<([a-z]+):math");
  
  /**
   * Pattern used to detect named entities.
   */
  private static final Pattern namedEntityPattern = Pattern.compile("&[^#]");

  /**
   * Constructor.
   */
  public JEuclidRenderer() {
  }

	/**
	 * Converts from String to org.w3c.dom.Document.
	 * 
	 * @param xml The mathML fragment as String
	 * @param systemID The system identifier.
	 * @param docType The document type definition.
	 * 
	 * @return The XML Document
	 * 
	 * @throws IOException If it fails.
	 * @throws SAXException If it fails.
	 */
	Document loadXMLFromString(String xml, String systemID, AuthorDocumentType docType)
			throws IOException, SAXException {
		DOMParser domParser = ParserCreator.createDOMParser();
		domParser.getXMLParserConfiguration().setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", false);
		
		if (containsNamedEntities(xml)) {
		  String docTypeStr;
		  if (docType != null) {
		    docTypeStr = docType.serializeDoctype(); 
		  } else {
		    docTypeStr = MATH_ML_DOCTYPE;
		  }
		  // Add document doctype.
		  xml = docTypeStr + xml;
		} else if (!hasNamespacePrefixDeclaration(xml)) {
		  // detect namespace prefix and declare it.
		  String prefix = detectNamespacePrefix(xml);
		  if (prefix != null) {
		    xml = addMathmlNamespacePrefixMapping(xml, prefix);
		  }
		}
		InputSource inputSource = new InputSource(new StringReader(xml));
		inputSource.setSystemId(systemID);
		domParser.parse(inputSource);
		return domParser.getDocument();
	}

	/**
	 * Adds the MathML namespace prefix mapping with the given prefix.
	 * @param xml The XML.
	 * @param prefix The prefix.
	 * @return The XML with prefix mapping.
	 */
  String addMathmlNamespacePrefixMapping(String xml, String prefix) {
    return xml.replaceFirst("<" + prefix + ":math(/|\\s|>)", 
        "<" + prefix + ":math " + "xmlns:" + prefix + "=\"http://www.w3.org/1998/Math/MathML\"$1");
  }
	
	/**
	 * Returns true if the xml content may contain named entities.
	 * @param xml The xml content.
	 * @return <code>true</code> if the xml may contain named entities.
	 */
	boolean containsNamedEntities(String xml) {
	  Matcher matcher = namedEntityPattern.matcher(xml);
	  return matcher.find();
	}
	
	/**
	 * @param xml The xml content.
	 * @return <code>true</code> if the content has a namespace prefix declaration.
	 */
	boolean hasNamespacePrefixDeclaration(String xml) {
	  return xml.contains("http://www.w3.org/1998/Math/MathML");
	}

	/**
	 * @param xml The xml content.
	 * @return The prefix for the MathML namespace or null if there is no prefix used.
	 */
	String detectNamespacePrefix(String xml) {
	  Matcher matcher = prefixPattern.matcher(xml);
	  if (matcher.find()) {
	    return matcher.group(1);
	  }
	  return null;
	}

  /**
   * Converts an mathML fragment to image.
   * 
   * @param equationDescriptor The equation descriptor. 
   * 
   * @return The image
   * 
   * @throws IOException If it fails.
   * @throws SAXException If it fails.
   */
  public BufferedImage convertToImage(AuthorAccess authorAccess, String xml) throws IOException, SAXException {
      AuthorDocumentController documentController = authorAccess.getDocumentController();
      AuthorDocumentType docType = documentController.getDoctype();
      String systemID = documentController.getAuthorDocumentNode().getSystemID();
      
      Converter converter = Converter.getInstance();
      Document doc = this.loadXMLFromString(xml, systemID, docType);
      return converter.render(
                doc, 
                new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext())
                );
  }

}
