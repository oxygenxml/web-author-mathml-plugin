package com.oxygenxml.sdksamples.mathml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URL;
import java.util.Set;

import javax.swing.text.BadLocationException;

import org.junit.Test;
import org.mockito.Mockito;
import org.xml.sax.XMLReader;

import ro.sync.ecss.component.AuthorClipboardObject;
import ro.sync.ecss.dita.ContextKeyManager;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorChangeTrackingController;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorListener;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.AuthorResourceBundle;
import ro.sync.ecss.extensions.api.AuthorReviewController;
import ro.sync.ecss.extensions.api.AuthorViewToModelInfo;
import ro.sync.ecss.extensions.api.ClassPathResourcesAccess;
import ro.sync.ecss.extensions.api.OptionsStorage;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.access.AuthorOutlineAccess;
import ro.sync.ecss.extensions.api.access.AuthorTableAccess;
import ro.sync.ecss.extensions.api.access.AuthorUtilAccess;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.access.AuthorXMLUtilAccess;
import ro.sync.ecss.extensions.api.access.EditingSessionContext;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Tests for the editing session context manager.
 * 
 * @author cristi_talau
 */
public class EditingSessionContextManagerTest {

  /**
   * <p><b>Description:</b> Test that we have no memory leak in document tracking.</p>
   * <p><b>Bug ID:</b> WA-1599</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testNoLeak() throws Exception {
    EditingSessionContextImpl ctx = new EditingSessionContextImpl();
    
    AuthorAccess authorAccess = new AuthorAccessMock(ctx);
    EditingSessionContextManager.ensureInitialized(authorAccess);
    
    assertNotNull(ctx.docId);
    assertEquals(40, ctx.docId.length());
    
    // remove reference to the author access.
    authorAccess = null;
    
    System.gc();
    
    authorAccess = EditingSessionContextManager.getDocument(ctx.docId);
    assertNull(authorAccess);
  }

  /**
   * Stub editing session context impl.
   * 
   * @author cristi_talau
   */
  private static final class EditingSessionContextImpl extends EditingSessionContext {

    /**
     * The recorded doc ID.
     */
    private String docId = null;
    
    @Override
    public ContextKeyManager getContextKeyManager() {
      return null;
    }

    @Override
    public void setAttribute(String attr, Object value) {
      if (attr.equals(EditingSessionContextManager.AUTHOR_ACCESS_ID)) {
        docId = (String) value;
      }
    }

    @Override
    public Set<String> getAttributes() {
      return null;
    }

    @Override
    public Object getAttribute(String attr) {
      return null;
    }
  }

  /**
   * Mock Author access.
   * 
   * @author mihai_coanda
   */
  private static class AuthorAccessMock implements AuthorAccess {

    private EditingSessionContextImpl ctx;

    public AuthorAccessMock(EditingSessionContextImpl ctx) {
      this.ctx = ctx;
    }
    
    @Override
    public AuthorEditorAccess getEditorAccess() {
      AuthorEditorAccess authorEditorAccess = Mockito.mock(AuthorEditorAccess.class);
      Mockito.when(authorEditorAccess.getEditingContext()).thenReturn(ctx);
      
      return authorEditorAccess;
    }
    
    @Override
    public int getSelectionStart() {
      return 0;
    }

    @Override
    public int getSelectionEnd() {
      return 0;
    }

    @Override
    public String getSelectedText() {
      return null;
    }

    @Override
    public int getCaretOffset() {
      return 0;
    }

    @Override
    public void insertText(String text, int offset) {
    }

    @Override
    public void insertXMLFragment(String xmlFragment, int offset) throws AuthorOperationException {
    }

    @Override
    public void insertXMLFragment(String xmlFragment, String xpathLocation, String relativePosition)
        throws AuthorOperationException {
    }

    @Override
    public void deleteSelection() {
    }

    @Override
    public boolean hasSelection() {
      return false;
    }

    @Override
    public void selectWord() {
    }

    @Override
    public void surroundInFragment(String xmlFragment, int startOffset, int endOffset) throws AuthorOperationException {
    }

    @Override
    public void surroundInText(String header, String footer, int startOffset, int endOffset) {
      
    }

    @Override
    public void setCaretPosition(int offset) {
      
    }

    @Override
    public void select(int startOffset, int endOffset) {
      
    }

    @Override
    public int[] getWordAtCaret() {
      return null;
    }

    @Override
    public Object getParentFrame() {
      return null;
    }

    @Override
    public String makeRelative(URL baseURL, URL childURL) {
      return null;
    }

    @Override
    public String escapeAttributeValue(String attributeValue) {
      return null;
    }

    @Override
    public URL getEditorLocation() {
      return null;
    }

    @Override
    public File locateFile(URL url) {
      return null;
    }

    @Override
    public File chooseFile(String title, String[] allowedExtensions, String filterDescr, boolean openForSave) {
      return null;
    }

    @Override
    public File chooseFile(String title, String[] allowedExtensions, String filterDescr) {
      return null;
    }

    @Override
    public URL chooseURL(String title, String[] allowedExtensions, String filterDescr) {
      return null;
    }

    @Override
    public AuthorElement getTableCellAbove(AuthorElement cellElement) {
      return null;
    }

    @Override
    public AuthorElement getTableCellBelow(AuthorElement cellElement) {
      return null;
    }

    @Override
    public int[] getTableCellIndex(AuthorElement authorElement) {
      return null;
    }

    @Override
    public AuthorElement getTableCellAt(int row, int column, AuthorElement tableElement) {
      return null;
    }

    @Override
    public AuthorElement getTableRow(int index, AuthorElement tableElement) {
      return null;
    }

    @Override
    public int getTableRowCount(AuthorElement tableElement) {
      return 0;
    }

    @Override
    public int getTableNumberOfColumns(AuthorElement tableElement) {
      return 0;
    }

    @Override
    public int[] getTableColSpanIndices(AuthorElement cellElement) {
      return null;
    }

    @Override
    public boolean isStandalone() {
      return false;
    }

    @Override
    public boolean inInlineContext(int offset) throws BadLocationException {
      return false;
    }

    @Override
    public void insertMultipleElements(AuthorElement parentElement, String[] elementNames, int[] offsets,
        String namespace) {
      
    }

    @Override
    public void multipleDelete(AuthorElement parentElement, int[] startOffsets, int[] endOffsets) {
      
    }

    @Override
    public void removeClonedElementAttribute(AuthorElement element, String attrName) {
      
    }

    @Override
    public void setClonedElementAttribute(AuthorElement element, String name, AttrValue attributeValue) {
      
    }

    @Override
    public int showConfirmDialog(String title, String message, String[] buttonNames, int[] buttonIds) {
      return 0;
    }

    @Override
    public XMLReader newNonValidatingXMLReader() {
      return null;
    }

    @Override
    public String correctURL(String url) {
      return null;
    }

    @Override
    public void showErrorMessage(String message) {
      
    }

    @Override
    public URL resolvePath(URL baseURL, String relativeLocation, boolean entityResolve, boolean uriResolve) {
      return null;
    }

    @Override
    public AuthorNode[] findNodesByXPath(String xpathExpression, boolean ignoreTexts, boolean ignoreCData,
        boolean ignoreComments) throws AuthorOperationException {
      return null;
    }

    @Override
    public Object[] evaluateXPath(String xpathExpression, boolean ignoreTexts, boolean ignoreCData,
        boolean ignoreComments) throws AuthorOperationException {
      return null;
    }

    @Override
    public void addAuthorListener(AuthorListener listener) {
      
    }

    @Override
    public void removeAuthorListener(AuthorListener listener) {
      
    }

    @Override
    public AuthorViewToModelInfo viewToModel(int x, int y) {
      return null;
    }

    @Override
    public boolean isTrackingChanges() {
      return false;
    }

    @Override
    public void toggleTrackChanges() {
      
    }

    @Override
    public AuthorChangeTrackingController getChangeTrackingController() {
      return null;
    }

    @Override
    public AuthorDocumentController getDocumentController() {
      return null;
    }

    @Override
    public AuthorWorkspaceAccess getWorkspaceAccess() {
      return null;
    }

    @Override
    public AuthorUtilAccess getUtilAccess() {
      return null;
    }

    @Override
    public AuthorXMLUtilAccess getXMLUtilAccess() {
      return null;
    }

    @Override
    public AuthorTableAccess getTableAccess() {
      return null;
    }

    @Override
    public AuthorReviewController getReviewController() {
      return null;
    }

    @Override
    public OptionsStorage getOptionsStorage() {
      return null;
    }

    @Override
    public AuthorOutlineAccess getOutlineAccess() {
      return null;
    }

    @Override
    public ClassPathResourcesAccess getClassPathResourcesAccess() {
      return null;
    }

    @Override
    public AuthorResourceBundle getAuthorResourceBundle() {
      return null;
    }

    @Override
    public AuthorClipboardObject getAuthorObjectFromClipboard() {
      return null;
    }

    @Override
    public AuthorElement getElementByAnchor(String anchor) {
      return null;
    }

    @Override
    public int getCaretOffsetByAnchor(String anchor) {
      return 0;
    }
    
  }

}
