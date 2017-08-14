package com.oxygenxml.sdksamples.mathml;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;

import com.google.common.io.Files;
import com.google.common.net.MediaType;

/**
 * MathML SERVLET used to retrieve the PNG image, after conversion.
 */
public class MathmlServlet extends WebappServletPluginExtension {

  /**
   * Returns the PNG image that corresponds to the mathml equation.
   * 
   * @param httpRequest The HTTP request.
   * @param httpResponse The HTTP response.
   */
  @Override
  public void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
	  // The hash of the current wanted image.
	  String xmlHash = httpRequest.getParameter("xmlHash");
	  
	  // the image
	  File outputfile = new File(WebappMathMLRenderer.cacheFolder, xmlHash);
	  
	  if (outputfile.exists()) {
	    // mime type, cache, image content
	    Files.copy(outputfile, httpResponse.getOutputStream());
	    httpResponse.setHeader("Content-Type", MediaType.PNG.toString());
	    httpResponse.setHeader("Cache-Control", "max-age=31536000");
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
