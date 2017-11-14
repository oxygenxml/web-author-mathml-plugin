package com.oxygenxml.sdksamples.mathml;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ro.sync.ecss.extensions.api.access.EditingSessionContext;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.access.WebappEditingSessionLifecycleListener;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Plugin that adds several editing context attributes.
 * 
 * @author cristi_talau
 */
public class EditingSessionContextManager implements WorkspaceAccessPluginExtension {
  /**
   * The ID of the document model.
   */
  static final String DOCUMENT_MODEL_ID = "com.oxygenxml.sdksamples.mathml.doc_id";
  
  /**
   * {@link EditingSessionContext} attribute that points to the equation cache.
   */
  static final String EQUATION_CACHE = "com.oxygenxml.sdksamples.mathml.eq_cache";
  
  /**
   * Secure random generator.
   */
  private static final SecureRandom random = new SecureRandom();
  
  /**
   * The global cache of active documents with MathML equations.
   */
  private static final Cache<String, AuthorDocumentModel> activeDocsCache = CacheBuilder.newBuilder()
      .weakValues()
      .build();

  @Override
  public void applicationStarted(StandalonePluginWorkspace pluginWorkspaceAccess) {
    WebappPluginWorkspace webappPlugin = (WebappPluginWorkspace) pluginWorkspaceAccess;
    webappPlugin.addEditingSessionLifecycleListener(new WebappEditingSessionLifecycleListener() {
      @Override
      public void editingSessionStarted(String sessionId, AuthorDocumentModel documentModel) {
        documentOpened(documentModel);
      }

    });
    
  }

  @Override
  public boolean applicationClosing() {
    return true;
  }

  /**
   * Sets up the editing context when the document is opened.
   * 
   * @param documentModel The document model that was opened.
   */
  void documentOpened(AuthorDocumentModel documentModel) {
    EditingSessionContext editingContext = documentModel.getAuthorAccess().getEditorAccess().getEditingContext();
    String docId = generateId();
    activeDocsCache.put(docId, documentModel);
    editingContext.setAttribute(DOCUMENT_MODEL_ID, docId);
    editingContext.setAttribute(EQUATION_CACHE, new PerDocumentEquationCache(documentModel));
  }

  /**
   * @return A secure unique random ID.
   */
  private String generateId() {
    byte[] bytes = new byte[20];
    random.nextBytes(bytes);
    return Hex.encodeHexString(bytes);
  }

  /**
   * Returns the document stored with the document ID.
   * 
   * @param docId The document ID.
   * 
   * @return The document model.
   */
  public static AuthorDocumentModel getDocument(String docId) {
    return activeDocsCache.getIfPresent(docId);
  }
}
