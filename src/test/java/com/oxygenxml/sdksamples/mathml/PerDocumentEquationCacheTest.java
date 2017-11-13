package com.oxygenxml.sdksamples.mathml;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorIdIndex;

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
    AuthorDocumentModel documentModel = Mockito.mock(AuthorDocumentModel.class); 
    BiMap<Long, AuthorNode> nodes = HashBiMap.create();
    Mockito.when(documentModel.getNodeIndexer()).thenReturn(new AuthorIdIndexImpl(nodes));
    AuthorDocumentController controller = Mockito.mock(AuthorDocumentController.class);
    Mockito.when(documentModel.getAuthorDocumentController()).thenReturn(controller);
    
    
    
    PerDocumentEquationCache cache = new PerDocumentEquationCache(documentModel);
    
    // Assert that we insert an equation and immediately delete it in 100 different places.
    // The cache should not keep too many entries for equations that are already deleted.
    for (int i = 0; i < 100; i++) {
      AuthorElement node = Mockito.mock(AuthorElement.class);
      nodes.clear();
      nodes.put(i + 0L, node);
      String eq = "<math>" + i + "</math>";
      Mockito.when(controller.serializeFragmentToXML(Mockito.any())).thenReturn(eq);
      long id = cache.freezeMathMLfrag(node);
      assertEquals(eq, cache.getXmlFragment(id));
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
    AuthorDocumentModel documentModel = Mockito.mock(AuthorDocumentModel.class); 
    BiMap<Long, AuthorNode> nodes = HashBiMap.create();
    Mockito.when(documentModel.getNodeIndexer()).thenReturn(new AuthorIdIndexImpl(nodes));
    AuthorDocumentController controller = Mockito.mock(AuthorDocumentController.class);
    Mockito.when(documentModel.getAuthorDocumentController()).thenReturn(controller);
    
    PerDocumentEquationCache cache = new PerDocumentEquationCache(documentModel);
    AuthorElement node1 = Mockito.mock(AuthorElement.class);
    nodes.put(1L, node1);
    Mockito.when(controller.serializeFragmentToXML(Mockito.any())).thenReturn("<math>1</math>");
    long node1ID = cache.freezeMathMLfrag(node1);
    
    
    AuthorElement node2 = Mockito.mock(AuthorElement.class);
    nodes.put(2L, node2);
    Mockito.when(controller.serializeFragmentToXML(Mockito.any())).thenReturn("<math>2</math>");
    long node2ID = cache.freezeMathMLfrag(node2);
    
    assertEquals("<math>1</math>", cache.getXmlFragment(node1ID));
    assertEquals("<math>2</math>", cache.getXmlFragment(node2ID));
  }
  
  /**
   * Stub Author ID indexer.
   * 
   * @author cristi_talau
   */
  private final class AuthorIdIndexImpl implements AuthorIdIndex<AuthorNode> {
    
    private BiMap<Long, AuthorNode> nodes;

    public AuthorIdIndexImpl(BiMap<Long, AuthorNode> nodes) {
      this.nodes = nodes;
    }
    
    @Override
    public AuthorNode getObjectById(long id) {
      return nodes.get(id);
    }

    @Override
    public long getId(AuthorNode object) {
      return nodes.inverse().getOrDefault(object, 0L);
    }

    @Override
    public Long getIdIfExists(AuthorNode object) {
      return nodes.inverse().get(object);
    }
  }
}
