package com.oxygenxml.sdksamples.mathml;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorDocumentType;
import ro.sync.ecss.extensions.api.editor.AuthorInplaceContext;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.webapp.formcontrols.WebappFormControlRenderer;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.util.PrettyPrintException;
import de.schlichtherle.io.FileOutputStream;

/**
 * Form control renderer that renders a MathML fragment as a PNG image.
 *
 * @author costi_dumitrescu
 */
public class WebappMathMLRenderer extends WebappFormControlRenderer {
  /**
   * Logger
   */
  private static Logger logger = Logger
      .getLogger(WebappMathMLRenderer.class.getName());

  /**
   * The location of the cache folder.
   */
  static File cacheFolder = null;

  static {
    String tempdir = System.getProperty("java.io.tmpdir");
    setCacheFolder(new File(tempdir, "tmp"));
  }

  /**
   * Sets the folder where to create temporary .png files with rendered
   * equations.
   * 
   * @param cacheFolder
   *          The cache folder.
   */
  public static void setCacheFolder(File cacheFolder) {
    WebappMathMLRenderer.cacheFolder = cacheFolder;
    cacheFolder.mkdirs();
  }
  

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
    AuthorDocumentController documentController = authorAccess.getDocumentController();
    AuthorDocumentType docType = documentController.getDoctype();
    String systemID = documentController.getAuthorDocumentNode().getSystemID();
    try {
      AuthorDocumentFragment mathMlFrag = documentController
          .createDocumentFragment(mathMlElement, true);
      String xml = documentController.serializeFragmentToXML(mathMlFrag);
      String xmlPPed = this.formatAndIndentXmlFragment(xml, systemID);
      
      // Create the PNG image, and then set its name as the hash of xmlPPed. 
      BufferedImage image = new JEuclidRenderer().convertToImage(xmlPPed, systemID, docType);
      String xmlHash = savePngImage(xmlPPed, image);
      
      // The value of the 'src' attribute is the path of the MathML SERVLET to
      // retrieve the image from server.
      String escapedXML = PluginWorkspaceProvider.getPluginWorkspace().
                              getXMLUtilAccess().escapeAttributeValue(xmlPPed);
      
      out.append(generateImgHtml(image, xmlHash, escapedXML));
    } catch (Exception e) {
      logger.error(e, e);
      out.append("<span style=\"color: red\">Error rendering MathML</span>");
    }
  }


  /**
   * Generates the HTML image which will display the rendered math-ml.
   * @param image The buffered image which will be displayed.
   * @param xmlHash The name of the image.
   * @param escapedXML The math-ml xml content.
   * @return The HTML of the image which will be rendered in Web Author.
   */
  String generateImgHtml(BufferedImage image, String xmlHash, String escapedXML) {
    // Setting image width and height to reduce the impact on the page layout of math-ml.
    return "<img width=\"" + image.getWidth() + "\" height=\"" + image.getHeight() + 
        "\" class=\"mathml-image\" src=\"../plugins-dispatcher/mathml?xmlHash="
        + xmlHash + ".png\" data-alt=\"" + escapedXML + "\"></img>";
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
   * Saves the PNG image.
   * 
   * @param xml The xml content.
   * @param image The image rendering.
   * 
   * @return The filename which is derived from the content.
   * 
   * @throws NoSuchAlgorithmException
   * @throws UnsupportedEncodingException
   * @throws IOException
   * @throws FileNotFoundException
   */
  String savePngImage(String xml, BufferedImage image)
      throws NoSuchAlgorithmException, UnsupportedEncodingException,
      IOException, FileNotFoundException {
    MessageDigest cript = MessageDigest.getInstance("SHA-1");
    cript.reset();
    cript.update(xml.getBytes("utf8"));
    String xmlHash = new String(Hex.encodeHex(cript.digest()));
    
    File outputfile = new File(cacheFolder, xmlHash + ".png");
    if (!outputfile.exists()) {
      FileOutputStream output = new FileOutputStream(outputfile);
      try {
        ImageIO.write(image, "png", output);
      } finally {
        output.close();
      }
    }
    return xmlHash;
  }

  /**
   * @return Returns the description of the renderer.
   */
  @Override
  public String getDescription() {
    return "Math ML Form Control Renderer";
  }
}
