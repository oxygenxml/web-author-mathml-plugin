package com.oxygenxml.sdksamples.mathml;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.Test;

public class WebappMathMlRendererTest {

  /**
   * Test that the temp folder exists.
   * 
   * @throws Exception
   */
  @Test
  public void testTempFolder() throws Exception {
    File tempFolder = WebappMathMLRenderer.cacheFolder;
    assertTrue(tempFolder.exists());
  }
  
  /**
   * Tests the PNG rendering of mathml equations.
   * 
   * @throws Exception
   */
  @Test
  public void testPngRendering() throws Exception {
    String pngRendering = new WebappMathMLRenderer().savePngImage("<mml:math" + 
        "                        xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">" + 
        "                        <mml:mstyle>" + 
        "                            <mml:mrow>" + 
        "                                <mml:mfrac>" + 
        "                                    <mml:mrow>" + 
        "                                        <mml:mi>sin</mml:mi>" + 
        "                                        <mml:mo rspace=\"verythinmathspace\">&#x2061;</mml:mo>" + 
        "                                        <mml:mi>θ</mml:mi>" + 
        "                                    </mml:mrow>" + 
        "                                    <mml:mi>π</mml:mi>" + 
        "                                </mml:mfrac>" + 
        "                            </mml:mrow>" + 
        "                        </mml:mstyle>" + 
        "                    </mml:math>", new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    
    File outputFile = new File(WebappMathMLRenderer.cacheFolder, pngRendering + ".png");
    assertTrue(outputFile.exists());
  }
  
  /**
   * WA-895: Tests that math-ml images have width and height attributes set.
   */
  @Test
  public void testMathMlImgsHaveWidthAndHeight() {
    BufferedImage image = new BufferedImage(500, 600, BufferedImage.TYPE_BYTE_BINARY);
    String generatedImgHtml = new WebappMathMLRenderer().generateImgHtml(image, "hash", "<xml_content/>");
    
    assertTrue(generatedImgHtml.indexOf("width=\"500\"") != -1);
    assertTrue(generatedImgHtml.indexOf("height=\"600\"") != -1);
  }
}
