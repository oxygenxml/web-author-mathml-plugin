package com.oxygenxml.sdksamples.mathml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

import ro.sync.ecss.dita.ContextKeyManager;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.access.EditingSessionContext;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;

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
    EditingSessionContextManager mgr = new EditingSessionContextManager();
    AuthorDocumentModel documentModel = createDocumentModel();
    EditingSessionContextImpl ctx = new EditingSessionContextImpl();
    Mockito.when(documentModel.getAuthorAccess().getEditorAccess().getEditingContext()).thenReturn(ctx);
    
    mgr.ensureInitialized(documentModel.getAuthorAccess());
    
    assertNotNull(ctx.docId);
    assertEquals(40, ctx.docId.length());
    documentModel = null;
    System.gc();
    
    AuthorAccess authorAccess = EditingSessionContextManager.getDocument(ctx.docId);
    assertNull(authorAccess);
  }

  /**
   * @return A mocked author document model.
   */
  private AuthorDocumentModel createDocumentModel() {
    AuthorDocumentModel documentModel = Mockito.mock(AuthorDocumentModel.class); 
    AuthorAccess authorAccess = Mockito.mock(AuthorAccess.class);
    AuthorEditorAccess authorEditorAccess = Mockito.mock(AuthorEditorAccess.class);

    Mockito.when(documentModel.getAuthorAccess()).thenReturn(authorAccess);
    Mockito.when(authorAccess.getEditorAccess()).thenReturn(authorEditorAccess);
    return documentModel;
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
      if (attr.equals(EditingSessionContextManager.DOCUMENT_MODEL_ID)) {
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


}
