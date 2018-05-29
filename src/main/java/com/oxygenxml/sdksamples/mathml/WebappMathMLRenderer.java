package com.oxygenxml.sdksamples.mathml;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.access.EditingSessionContext;
import ro.sync.ecss.extensions.api.editor.AuthorInplaceContext;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.webapp.formcontrols.WebappFormControlRenderer;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.util.PrettyPrintException;

/**
 * Form control renderer that renders a MathML fragment as a PNG image.
 *
 * @author costi_dumitrescu
 */
public class WebappMathMLRenderer extends WebappFormControlRenderer {
  /**
   * Logger
   */
  private static final Logger logger = Logger
      .getLogger(WebappMathMLRenderer.class.getName());

  /**
   * Render control.
   * 
   * @param context
   *          The context of the MathML form control.
   * @param out
   *          The output stream.
   * 
   * @throws IOException
   *           If the form control could not be redered.
   */
  @Override
  public void renderControl(AuthorInplaceContext context, Writer out)
      throws IOException {
    AuthorElement mathMlElement = context.getElem();
    AuthorAccess authorAccess = context.getAuthorAccess();
    EditingSessionContext editingContext = authorAccess.getEditorAccess().getEditingContext();
    PerDocumentEquationCache equationCache = 
        (PerDocumentEquationCache) editingContext.getAttribute(EditingSessionContextManager.EQUATION_CACHE);
    String docId = (String) editingContext.getAttribute(EditingSessionContextManager.DOCUMENT_MODEL_ID);

    try {
      long elemId = equationCache.freezeMathMLfrag(mathMlElement);
      String xml = equationCache.getXmlFragment(elemId);
      
      String xmlHash = DigestUtils.shaHex(xml);
      
      String systemID = authorAccess.getDocumentController().getAuthorDocumentNode().getSystemID();
      String xmlPPed = this.formatAndIndentXmlFragment(xml, systemID);
      
      // Create the PNG image, and then set its name as the hash of xmlPPed.
      BufferedImage image = new JEuclidRenderer().convertToImage(authorAccess, xml);

      // The value of the 'src' attribute is the path of the MathML SERVLET to
      // retrieve the image from server.
      String escapedXML = PluginWorkspaceProvider.getPluginWorkspace().
                              getXMLUtilAccess().escapeAttributeValue(xmlPPed);
      
      out.append(generateImgHtml(image, docId, elemId, xmlHash, escapedXML, context.isReadOnlyContext()));
    } catch (Exception e) {
      logger.error(e, e);
      out.append("<span style=\"color: red\">Error rendering MathML</span>");
    }
  }
  
  /**
   * The client-side rendering supported being wrapped in marker spans.
   */
  @Override
  public boolean isChangeTrackingAware() {
    return true;
  }

  /**
   * Generates the HTML image which will display the rendered MathML.
   * 
   * @param image The buffered image which will be displayed.
   * @param docId The ID of the document.
   * @param elemId The ID of the XML element that represents the equation.
   * @param xmlHash The name of the image.
   * @param escapedXML The math-ml xml content.
   * @param readOnly <code>true</code> if the equation is rendered in a read-only part of the document.
   * @return The HTML of the image which will be rendered in Web Author.
   */
  String generateImgHtml(BufferedImage image, String docId, long elemId, String xmlHash, String escapedXML, boolean readOnly) {
    // Setting image width and height to reduce the impact on the page layout of math-ml.
    return "<img width=\"" + image.getWidth() + "\" height=\"" + image.getHeight() + 
        "\" class=\"mathml-image\" src=\"../plugins-dispatcher/mathml?"
        + "xmlHash=" + xmlHash + ".png&"
        + "elemId=" + elemId + "&"
        + "docId=" + docId + "\""
        + (readOnly ? " data-ro=\"true\"" : "")
        + " data-alt=\"" + escapedXML + "\"></img>";
  }
  
  /**
   * Try to format and indent if possible.
   * 
   * @param xmlContent The content to format and indent
   * @param systemID The system Id of the document that the fragment belongs to.
   * 
   * @return A PP-ed version or the same content if not well formed.
   */
  String formatAndIndentXmlFragment(String xmlContent, String systemID) {
    String formattedContent = "";
    if(xmlContent != null){
      formattedContent = xmlContent;
      try {
        formattedContent = PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess()
            .prettyPrint(new StringReader(xmlContent), systemID);
      } catch (PrettyPrintException e1) {
        if(logger.isDebugEnabled()){
         logger.debug("Content not in XML format");
        }
      }
    }
    
    return formattedContent;
  }

  /**
   * @return Returns the description of the renderer.
   */
  @Override
  public String getDescription() {
    return "Math ML Form Control Renderer";
  }
}
