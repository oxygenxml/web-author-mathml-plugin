package com.oxygenxml.sdksamples.mathml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;

/**
 * Cache of equation descriptors per opened document.
 * 
 * @author cristi_talau
 */
public class PerDocumentEquationCache {

  /**
   * The document controller.
   */
  private final AuthorDocumentController docController;
  
  /**
   * Map from node identifiers to equation XML fragments. 
   */
  private final Map<Long, String> mathMLElements = new HashMap<>(0);

  /**
   * The cache size after the last compaction.
   */
  private long lastCompactedCacheSize = 4L;

  /**
   * Author elements node indexer.
   */
  Map<AuthorElement, Long> nodeIndexer = new WeakHashMap<>();
  
  /**
   * Counter used to index nodes.
   */
  private long counter = 0;
  /**
   * Constructor.
   * 
   * @param controller the author document controller.
   */
  public PerDocumentEquationCache(AuthorDocumentController controller) {
    this.docController = controller;
    
  }
  
  /**
   * Freezes the XML content that corresponds to the given element.
   * 
   * @param elem The author element.
   * 
   * @return The id of the cache entry.
   * 
   * @throws BadLocationException
   */
  public synchronized long freezeMathMLfrag(AuthorElement elem) throws BadLocationException {
    long elemId = nodeIndexer.computeIfAbsent(elem, new Function<AuthorElement, Long>() {
      @Override
      public Long apply(AuthorElement t) {
        return counter++;
      }
    });
    
    AuthorDocumentFragment mathMlFrag = docController
        .createDocumentFragment(elem, true);
    String xml = docController.serializeFragmentToXML(mathMlFrag);
    
    mathMLElements.put(elemId, xml);
    if (mathMLElements.size() > 2 * lastCompactedCacheSize) {
      compactCache();
    }
    return elemId;
  }
  
  /**
   * Compact the cache, removing entries that correspond to stale AuthorElements.
   */
  private void compactCache() {
    HashSet<Long> valuesSet = new HashSet<>(nodeIndexer.values());

    mathMLElements.entrySet().removeIf(entry -> !valuesSet.contains(entry.getKey()));
    
    lastCompactedCacheSize = mathMLElements.size();
  }
  
  /**
   * @return The size of the cache.
   */
  int getSize() {
    return mathMLElements.size();
  }
  
  /**
   * The XML fragment of the given node.
   * 
   * @param elemId
   * @return The XML fragment that corresponds to the given element
   */
  public synchronized String getXmlFragment(long elemId) {
    return mathMLElements.get(elemId);
  }
}
