package com.oxygenxml.sdksamples.mathml;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.xml.sax.SAXException;

import com.google.common.net.MediaType;

import lombok.extern.slf4j.Slf4j;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.access.EditingSessionContext;
import ro.sync.ecss.extensions.api.webapp.plugin.ServletPluginExtension;
import ro.sync.ecss.extensions.api.webapp.plugin.servlet.ServletException;
import ro.sync.ecss.extensions.api.webapp.plugin.servlet.http.HttpServletRequest;
import ro.sync.ecss.extensions.api.webapp.plugin.servlet.http.HttpServletResponse;

/**
 * MathML SERVLET used to retrieve the PNG image, after conversion.
 */
@Slf4j
public class MathmlServlet extends ServletPluginExtension {
  
  /**
   * Returns the PNG image that corresponds to the mathml equation.
   * 
   * @param httpRequest The HTTP request.
   * @param httpResponse The HTTP response.
   */
  @Override
  public void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
    // The params used to retrieve the image
    String docId = httpRequest.getParameter("docId");
    String elemId = httpRequest.getParameter("elemId");
    
    AuthorAccess authorAccess = EditingSessionContextManager.getDocument(docId);
    if (authorAccess != null) {
      EditingSessionContext editingContext = authorAccess.getEditorAccess().getEditingContext();
      PerDocumentEquationCache equationCache = (PerDocumentEquationCache) editingContext.getAttribute(EditingSessionContextManager.EQUATION_CACHE);
      
      String xml = equationCache.getXmlFragment(Long.valueOf(elemId));
      BufferedImage image;
      try {
        image = new JEuclidRenderer().convertToImage(authorAccess, xml);
      } catch (SAXException e) {
        log.error("Error parsing MathML content: " + e.getMessage(), e);
        httpResponse.setHeader("Content-Type", MediaType.PLAIN_TEXT_UTF_8.toString());
        httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error parsing MathML content");
        return;
      }
    
      // mime type, cache, image content
      httpResponse.setHeader("Content-Type", MediaType.PNG.toString());
      httpResponse.setHeader("Cache-Control", "max-age=31536000");
      ImageIO.write(image, "png", httpResponse.getOutputStream());
    } else {
      httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "MathML PNG file was not found.");
    }
  }
      
  /**
   * The path where this servlet is mapped.
   */
  @Override
  public String getPath() {
    return "mathml";
  }
}
