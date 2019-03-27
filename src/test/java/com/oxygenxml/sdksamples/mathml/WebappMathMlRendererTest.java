package com.oxygenxml.sdksamples.mathml;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.Test;

public class WebappMathMlRendererTest {
  
  /**
   * WA-895: Tests that math-ml images have width and height attributes set.
   */
  @Test
  public void testMathMlImgsHaveWidthAndHeight() {
    BufferedImage image = new BufferedImage(500, 600, BufferedImage.TYPE_BYTE_BINARY);
    String generatedImgHtml = new WebappMathMLRenderer().generateImgHtml(image, "", 1L, "hash", "<xml_content/>", false);
    
    
    System.out.println(generatedImgHtml);
    
    assertTrue(generatedImgHtml.indexOf("width=\"500\"") != -1);
    assertTrue(generatedImgHtml.indexOf("height=\"600\"") != -1);
    
    assertTrue(generatedImgHtml.indexOf("\" alt=\"") != -1);
    assertTrue(generatedImgHtml.indexOf("data-alt=\"") == -1);
  }
}
