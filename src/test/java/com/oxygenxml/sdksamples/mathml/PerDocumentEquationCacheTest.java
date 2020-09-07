package com.oxygenxml.sdksamples.mathml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.UndoManager;

import org.junit.Test;
import org.mockito.Mockito;

import ro.sync.ecss.component.RenderingInfoChangedListener;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorDocumentFilter;
import ro.sync.ecss.extensions.api.AuthorDocumentType;
import ro.sync.ecss.extensions.api.AuthorListener;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.AuthorSchemaManager;
import ro.sync.ecss.extensions.api.SelectionInterpretationMode;
import ro.sync.ecss.extensions.api.UniqueAttributesProcessor;
import ro.sync.ecss.extensions.api.XPathVersion;
import ro.sync.ecss.extensions.api.content.ClipboardFragmentProcessor;
import ro.sync.ecss.extensions.api.content.OffsetInformation;
import ro.sync.ecss.extensions.api.content.RangeProcessor;
import ro.sync.ecss.extensions.api.content.TextContentIterator;
import ro.sync.ecss.extensions.api.filter.AuthorFilteredContent;
import ro.sync.ecss.extensions.api.filter.AuthorNodesFilter;
import ro.sync.ecss.extensions.api.highlights.AuthorPersistentHighlightsFilter;
import ro.sync.ecss.extensions.api.highlights.AuthorPersistentHighlightsListener;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.schemaaware.SchemaAwareHandlerResult;

/**
 * Tests for the equation cache.
 * 
 * @author cristi_talau
 */
public class PerDocumentEquationCacheTest {

  /**
   * <p><b>Description:</b> Test that the cache is compacted.</p>
   * <p><b>Bug ID:</b> WA-1599</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testCacheCompaction() throws Exception {
    AuthorDocumentControllerMock controller = new AuthorDocumentControllerMock();
    
    PerDocumentEquationCache cache = new PerDocumentEquationCache(controller);
    
    // Assert that we insert an equation and immediately delete it in 100 different places.
    // The cache should not keep too many entries for equations that are already deleted.
    for (int i = 0; i < 100; i++) {
      String eq = "<math>" + i + "</math>";
      
      controller.setSerializeReturn(eq);
      
      AuthorElement authorElement = Mockito.mock(AuthorElement.class);
      
      long id = cache.freezeMathMLfrag(
          authorElement);
      
      assertEquals(eq, cache.getXmlFragment(id));
      
      System.gc();
      System.runFinalization();
      System.gc();
      System.runFinalization();

      assertTrue(cache.getSize() <= 8);
    }
  }
  
  /**
   * <p><b>Description:</b> Test that the cache is correct.</p>
   * <p><b>Bug ID:</b> WA-1599</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testCacheCorrectness() throws Exception {
    AuthorDocumentControllerMock controller = new AuthorDocumentControllerMock();
    
    PerDocumentEquationCache cache = new PerDocumentEquationCache(controller);
    AuthorElement node1 = Mockito.mock(AuthorElement.class);
    controller.setSerializeReturn("<math>1</math>");
    long node1ID = cache.freezeMathMLfrag(node1);
    
    
    AuthorElement node2 = Mockito.mock(AuthorElement.class);
    controller.setSerializeReturn("<math>2</math>");
    long node2ID = cache.freezeMathMLfrag(node2);
    
    assertEquals("<math>1</math>", cache.getXmlFragment(node1ID));
    assertEquals("<math>2</math>", cache.getXmlFragment(node2ID));
  }
  
  

/**
 * Mock Author Document Controller.
 * 
 * @author mihai_coanda
 */
  private static class AuthorDocumentControllerMock implements AuthorDocumentController {

    private String serialized;

    /**
     * Helper method to set what a serialization to XML will return.
     * 
     * @param serialized
     *          the serialization.
     */
    public void setSerializeReturn(String serialized) {
      this.serialized = serialized;
    }

    @Override
    public String serializeFragmentToXML(AuthorDocumentFragment fragment) throws BadLocationException {
      return serialized;
    }

    @Override
    public AuthorDocumentFragment createDocumentFragment(AuthorNode node, boolean copyContent)
        throws BadLocationException {
      return Mockito.mock(AuthorDocumentFragment.class);
    }

    @Override
    public boolean delete(int startOffset, int endOffset) {
      return false;
    }

    @Override
    public boolean delete(int startOffset, int endOffset, boolean backspace) {
      return false;
    }

    @Override
    public boolean deleteNode(AuthorNode node) {
      return false;
    }

    @Override
    public void replaceRoot(AuthorDocumentFragment fragment) {

    }

    @Override
    public AuthorDocumentFragment createDocumentFragment(int startOffset, int endOffset) throws BadLocationException {
      return null;
    }

    @Override
    public AuthorDocumentFragment createNewDocumentFragmentInContext(String xmlFragment, int contentOffset)
        throws AuthorOperationException {
      return null;
    }

    @Override
    public AuthorDocumentFragment[] createNewDocumentFragmentsInContext(String[] xmlFragments, int[] contentOffsets)
        throws AuthorOperationException {
      return null;
    }

    @Override
    public AuthorDocumentFragment createNewDocumentTextFragment(String textFragment) throws AuthorOperationException {
      return null;
    }

    @Override
    public void setAttribute(String attributeName, AttrValue value, AuthorElement element) {

    }

    @Override
    public void setMultipleAttributes(int parentElementStartOffset, int[] elementOffsets,
        Map<String, AttrValue> attributes) {

    }

    @Override
    public void setMultipleDistinctAttributes(int parentElementStartOffset, int[] elementOffsets,
        List<Map<String, AttrValue>> attributes) {

    }

    @Override
    public void removeAttribute(String attributeName, AuthorElement element) {
    }

    @Override
    public void setPseudoClass(String pseudoClass, AuthorElement element) {
    }

    @Override
    public void setPseudoClassUndoable(String pseudoClass, AuthorElement element) {
    }

    @Override
    public void removePseudoClass(String pseudoClass, AuthorElement element) {
    }

    @Override
    public void removePseudoClassUndoable(String pseudoClass, AuthorElement element) {}
    
    @Override
    public AuthorNode getNodeAtOffset(int offset) throws BadLocationException {
      return null;
    }

    @Override
    public OffsetInformation getContentInformationAtOffset(int offset) throws BadLocationException {
      return null;
    }

    @Override
    public String getText(int offset, int length) throws BadLocationException {
      return null;
    }

    @Override
    public int getTextContentLength() throws BadLocationException {
      return 0;
    }

    @Override
    public UndoManager getUndoManager() {
      return null;
    }

    @Override
    public void beginCompoundEdit() {
    }

    @Override
    public void endCompoundEdit() {
    }

    @Override
    public void cancelCompoundEdit() {
    }

    @Override
    public void insertText(int offset, String text) {
    }

    @Override
    public void insertXMLFragment(String xmlFragment, int offset) throws AuthorOperationException {
    }

    @Override
    public void insertXMLFragment(String xmlFragment, String xpathLocation, String relativePosition)
        throws AuthorOperationException {
    }

    @Override
    public void insertXMLFragment(String xmlFragment, AuthorNode relativeTo, String relativePosition)
        throws AuthorOperationException {
    }

    @Override
    public SchemaAwareHandlerResult insertXMLFragmentSchemaAware(String xmlFragment, int offset)
        throws AuthorOperationException {
      return null;
    }

    @Override
    public SchemaAwareHandlerResult insertXMLFragmentSchemaAware(String xmlFragment, int offset,
        boolean replaceSelection) throws AuthorOperationException {
      return null;
    }

    @Override
    public SchemaAwareHandlerResult insertXMLFragmentSchemaAware(String xmlFragment, int offset, int actionID,
        boolean replaceSelection) throws AuthorOperationException {
      return null;
    }

    @Override
    public void insertFragment(int insertOffset, AuthorDocumentFragment frag) {
    }

    @Override
    public boolean processContentRange(int startOffset, int endOffset, RangeProcessor rangeProcessor)
        throws BadLocationException, AuthorOperationException {
      return false;
    }

    @Override
    public SchemaAwareHandlerResult insertFragmentSchemaAware(int insertOffset, AuthorDocumentFragment frag)
        throws AuthorOperationException {
      return null;
    }

    @Override
    public void surroundInFragment(String xmlFragment, int startOffset, int endOffset) throws AuthorOperationException {

    }

    @Override
    public void surroundInFragment(AuthorDocumentFragment xmlFragment, int startOffset, int endOffset)
        throws AuthorOperationException {
    }

    @Override
    public void surroundInText(String header, String footer, int startOffset, int endOffset)
        throws AuthorOperationException {
    }

    @Override
    public boolean inInlineContext(int offset) throws BadLocationException, AuthorOperationException {
      return false;
    }

    @Override
    public void addAuthorListener(AuthorListener listener) {
    }

    @Override
    public void addAuthorPersistentHighlightListener(AuthorPersistentHighlightsListener listener) {
    }

    @Override
    public void removeAuthorPersistentHighlightListener(AuthorPersistentHighlightsListener listener) {
    }

    @Override
    public void addPersistentHighlightsFilter(AuthorPersistentHighlightsFilter persistentHighlightsFilter) {
    }

    @Override
    public void removeAuthorListener(AuthorListener listener) {
    }

    @Override
    public Object[] evaluateXPath(String xpathExpression, boolean ignoreTexts, boolean ignoreCData,
        boolean ignoreComments, boolean processChangeMarkers) throws AuthorOperationException {
      return null;
    }

    @Override
    public Object[] evaluateXPath(String xpathExpression, AuthorNode contextNode, boolean ignoreTexts,
        boolean ignoreCData, boolean ignoreComments, boolean processChangeMarkers, XPathVersion xpathVersion)
        throws AuthorOperationException {
      return null;
    }

    @Override
    public Object[] evaluateXPath(String xpathExpression, boolean ignoreTexts, boolean ignoreCData,
        boolean ignoreComments) throws AuthorOperationException {
      return null;
    }

    @Override
    public AuthorNode[] findNodesByXPath(String xpathExpression, boolean ignoreTexts, boolean ignoreCData,
        boolean ignoreComments, boolean processChangeMarkers) throws AuthorOperationException {
      return null;
    }

    @Override
    public AuthorNode[] findNodesByXPath(String xpathExpression, boolean ignoreTexts, boolean ignoreCData,
        boolean ignoreComments) throws AuthorOperationException {
      return null;
    }

    @Override
    public int getXPathLocationOffset(String xpathLocation, String relativePosition, boolean processChangeMarkers)
        throws AuthorOperationException {
      return 0;
    }

    @Override
    public int getXPathLocationOffset(String xpathLocation, String relativePosition) throws AuthorOperationException {
      return 0;
    }

    @Override
    public void insertMultipleElements(AuthorElement parentElement, String[] elementNames, int[] offsets,
        String namespace) {
    }

    @Override
    public boolean insertMultipleFragments(AuthorElement parentElement, AuthorDocumentFragment[] fragments,
        int[] offsets) {
      return false;
    }

    @Override
    public void multipleDelete(AuthorElement parentElement, int[] startOffsets, int[] endOffsets) {
    }

    @Override
    public void setDoctype(AuthorDocumentType docType) {
    }

    @Override
    public AuthorDocumentType getDoctype() {
      return null;
    }

    @Override
    public AuthorNode getCommonParentNode(AuthorDocument doc, int startOffset, int endOffset)
        throws BadLocationException {
      return null;
    }

    @Override
    public List<AuthorNode> getNodesToSelect(int selectionStart, int selectionEnd) throws BadLocationException {
      return null;
    }

    @Override
    public AuthorNode getCommonAncestor(AuthorNode[] nodes) {
      return null;
    }

    @Override
    public AuthorNode getStrictCommonAncestor(AuthorNode[] nodes) {
      return null;
    }

    @Override
    public AuthorDocument getAuthorDocumentNode() {
      return null;
    }

    @Override
    public void setDocumentFilter(AuthorDocumentFilter authorDocumentFilter) {
    }

    @Override
    public AuthorDocumentFilter getDocumentFilter() {
      return null;
    }

    @Override
    public void getChars(int where, int len, Segment chars) throws BadLocationException {
    }

    @Override
    public AuthorFilteredContent getFilteredContent(int start, int end, AuthorNodesFilter nodesFilter) {
      return null;
    }

    @Override
    public AuthorSchemaManager getAuthorSchemaManager() {
      return null;
    }

    @Override
    public SchemaAwareHandlerResult insertXMLFragmentSchemaAware(String xmlFragment, String xpathLocation,
        String relativePosition) throws AuthorOperationException {
      return null;
    }

    @Override
    public boolean insertElement(int caretOffset, AuthorNode element) {
      return false;
    }

    @Override
    public AuthorElement createElement(String qName) {
      return null;
    }

    @Override
    public boolean isEditable(AuthorNode node) {
      return false;
    }

    @Override
    public void renameElement(AuthorElement contextNode, String newName) {

    }

    @Override
    public TextContentIterator getTextContentIterator(int startOffset, int endOffset) {
      return null;
    }

    @Override
    public Position createPositionInContent(int offset) throws BadLocationException {
      return null;
    }

    @Override
    public void addClipboardFragmentProcessor(ClipboardFragmentProcessor clipboardFragmentProcessor) {

    }

    @Override
    public void removeClipboardFragmentProcessor(ClipboardFragmentProcessor clipboardFragmentProcessor) {
    }

    @Override
    public void addUniqueAttributesProcessor(UniqueAttributesProcessor uniqueAttributesProcessor) {
    }

    @Override
    public UniqueAttributesProcessor getUniqueAttributesProcessor() {
      return null;
    }

    @Override
    public void removeUniqueAttributesProcessor(UniqueAttributesProcessor uniqueAttributesProcessor) {
    }

    @Override
    public AuthorNode[] findNodesByXPath(String xpathExpression, AuthorNode contextNode, boolean ignoreTexts,
        boolean ignoreCData, boolean ignoreComments, boolean processChangeMarkers) throws AuthorOperationException {
      return null;
    }

    @Override
    public AuthorNode[] findNodesByXPath(String xpathExpression, AuthorNode contextNode, boolean ignoreTexts,
        boolean ignoreCData, boolean ignoreComments, boolean processChangeMarkers, XPathVersion xpathVersion)
        throws AuthorOperationException {
      return null;
    }

    @Override
    public AuthorNode[] findNodesByXPath(String xpathExpression, AuthorNode contextNode, boolean ignoreTexts,
        boolean ignoreCData, boolean ignoreComments, boolean processChangeMarkers, XPathVersion xpathVersion,
        boolean transparentReferences) throws AuthorOperationException {
      return null;
    }

    @Override
    public Object[] evaluateXPath(String xpathExpression, AuthorNode contextNode, boolean ignoreTexts,
        boolean ignoreCData, boolean ignoreComments, boolean processChangeMarkers) throws AuthorOperationException {
      return null;
    }

    @Override
    public AuthorDocumentFragment unwrapDocumentFragment(AuthorDocumentFragment fragmentToUnwrap)
        throws BadLocationException {
      return null;
    }

    @Override
    public String getUnparsedEntityUri(AuthorNode contextNode, String entityName) {
      return null;
    }

    @Override
    public void refreshNodeReferences(AuthorNode node) {

    }

    @Override
    public void setRenderingInfoChangedListener(RenderingInfoChangedListener listener) {

    }

    @Override
    public String getXPathExpression(int offset) throws BadLocationException {
      return null;
    }

    @Override
    public String getXPathExpression(int offset, boolean processChanges) throws BadLocationException {
      return null;
    }

    @Override
    public void disableLayoutUpdate() {

    }

    @Override
    public void enableLayoutUpdate(AuthorNode ancestorOfChanges) {
    }

    @Override
    public boolean split(AuthorNode toSplit, int splitOffset) {
      return false;
    }

    @Override
    public String getFilteredText(int offset, int length) throws BadLocationException {
      return null;
    }

    @Override
    public void markSelection(List<int[]> newSelection, int newCaretOffset,
        SelectionInterpretationMode newSelectionType, List<int[]> oldSelection, int oldCaretOffset,
        SelectionInterpretationMode oldSelectionType) {

    }

    @Override
    public AuthorDocumentFragment createDocumentFragment(int arg0, int arg1, boolean arg2) throws BadLocationException {
      return null;
    }

  }
}
