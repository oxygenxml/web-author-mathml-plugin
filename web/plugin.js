(function(){
  /**
   * Enhancer for mathml form control.
   *
   * @param {HTMLElement} element The element to enhance.
   * @param editor is the editor.
   * @constructor
   */
  MathMLEnhancer = function(element, editor) {
    sync.formctrls.Enhancer.call(this, element, editor);
  };
  goog.inherits(MathMLEnhancer, sync.formctrls.Enhancer);

  /**
   * Registers the listener for the current form control.
   *
   * @param {sync.ctrl.Controller} controller The document controller.
   */
  MathMLEnhancer.prototype.enterDocument = function(controller) {
    goog.events.listen(this.formControl,
        goog.events.EventType.CLICK,
        goog.bind(this.beginEditing, this));
    var translationSet = {
          MATHML_EDITOR_: {
            "en_US": "MathML Editor",
            "de_DE": "MathML Editor",
            "fr_FR": "Éditeur MathML",
            "ja_JP": "MathML エディタ",
            "nl_NL": "MathML-editor"
          }
    };
    sync.Translation.addTranslations(translationSet);
  };

  /**
   * Begins the MathML fragment editing.
   */
  MathMLEnhancer.prototype.beginEditing = function() {
    var dialog = this.getDialog();
    
    var mathMlEditor = dialog.getElement().childNodes[0];
    var alt = this.formControl.childNodes[0].getAttribute("data-alt");
    mathMlEditor.value = alt;
    
    dialog.show();
    dialog.onSelect(goog.bind(this.commitFragment, this, mathMlEditor, alt));
  };
  
  /**
   * Commits the fragment edited by the user.
   * 
   * @param mathMlEditor The textarea used to edit the MathML fragment.
   * @param alt The original MathML fragment as a string. 
   * @param key The result of the user action - 'ok' or 'cancel'.
   */
  MathMLEnhancer.prototype.commitFragment = function(mathMlEditor, alt, key) {
    // If Ok button is pressed and the content of the text area has been changed.
    if (key == 'ok' && mathMlEditor.value != alt) {
      // invoke replace operation
      var xmlNode = this.getParentNode();
      var sel = sync.api.Selection.createEmptySelectionInNode(xmlNode, 'before');
      
      var actionsManager = this.editor.getActionsManager();
      actionsManager.invokeOperation('ro.sync.ecss.extensions.commons.operations.InsertOrReplaceFragmentOperation', {
        fragment: mathMlEditor.value,
        insertLocation: "(./ancestor-or-self::*[namespace-uri()='http://www.w3.org/1998/Math/MathML' and local-name() = 'math'])[1]",
        insertPosition: "Replace"
      }, 
      function() { console.log('done'); },
      sel);
    }
  };

  /**
   * Cleans up the listener.
   */
  MathMLEnhancer.prototype.exitDocument = function() {
    MathMLEnhancer.superClass_.exitDocument.call(this);
  };
  
  // The dialog used to edit MathML equations.
  MathMLEnhancer.dialog_ = null;
  
  /**
   * Initialize the editing dialog.
   */
  MathMLEnhancer.prototype.getDialog = function() {
    if (!MathMLEnhancer.dialog_) {
      var dialog = workspace.createDialog();
      dialog.getElement().innerHTML =
        '<textarea class="mml-edit-area"></textarea>';
      dialog.setTitle(tr(msgs.MATHML_EDITOR_));
      dialog.setResizable(true);
      dialog.setPreferredSize(500, 500);
      MathMLEnhancer.dialog_ = dialog;
    }
    return MathMLEnhancer.dialog_;
  };
  
  sync.util.loadCSSFile('../plugin-resources/mml-static/mml.css');

  // Register the form-control renderer with the opened editors.
  goog.events.listen(workspace, sync.api.Workspace.EventType.BEFORE_EDITOR_LOADED, function(e) {
    e.editor.registerEnhancer(
        'com.oxygenxml.sdksamples.mathml.WebappMathMLRenderer', MathMLEnhancer);
  });
})();
