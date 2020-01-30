(function(){
  /**
   * Enhancer for mathml form control.
   *
   * @param {HTMLElement} element The element to enhance.
   * @param editingSupport The editingSupport.
   * @constructor
   */
  var MathMLEnhancer = function(element, editingSupport) {
    sync.formctrls.Enhancer.call(this, element, editingSupport);
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
    var alt = this.formControl.childNodes[0].getAttribute("alt");
    mathMlEditor.value = alt;
    
    dialog.show();
    dialog.onSelect(goog.bind(this.commitFragment, this, mathMlEditor, alt));
  };
  
  /**
   * Commits the fragment edited by the user.
   * 
   * @param {object} mathMlEditor The textarea used to edit the MathML fragment.
   * @param {string} alt The original MathML fragment as a string.
   * @param {string} key The result of the user action - 'ok' or 'cancel'.
   */
  MathMLEnhancer.prototype.commitFragment = function(mathMlEditor, alt, key) {
    // If Ok button is pressed and the content of the text area has been changed.
    if (key === 'ok' && mathMlEditor.value != alt) {
      // invoke replace operation
      var xmlNode = this.getParentNode();
      var sel = sync.api.Selection.createEmptySelectionInNode(xmlNode, 'before');
      
      var actionsManager = this.editingSupport.getActionsManager();
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
      var dialogElement = dialog.getElement();
      goog.dom.removeChildren(dialogElement);
      var textarea = goog.dom.createDom('textarea', 'mml-edit-area');
      textarea.setAttribute('spellcheck', 'false');
      goog.dom.append(dialogElement, textarea);
      dialog.setTitle(tr(msgs.MATHML_EDITOR_));
      dialog.setResizable(true);
      dialog.setPreferredSize(500, 500);
      MathMLEnhancer.dialog_ = dialog;

      // Update the disabled state.
      this.disabledStateUpdated(this.isDisabled());
    }
    return MathMLEnhancer.dialog_;
  };

  /**
   * @override
   */
  MathMLEnhancer.prototype.disabledStateUpdated = function(isDisabled) {
    var img = this.formControl.childNodes[0];
    if ('true' === goog.dom.dataset.get(img, 'ro')) {
      isDisabled = true;
    }
    
    var dialog = MathMLEnhancer.dialog_;
    if (dialog) {
      var textarea = dialog.getElement().childNodes[0];
      if (isDisabled) {
        textarea.setAttribute('disabled', 'true');
        dialog.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.CANCEL);
      } else {
        textarea.removeAttribute('disabled');
        dialog.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.OK_CANCEL);
      }
    }
  };

  // Register the form-control renderer with the opened editors.
  goog.events.listen(workspace, sync.api.Workspace.EventType.BEFORE_EDITOR_LOADED, function(e) {
    e.editor.registerEnhancer(
        'com.oxygenxml.sdksamples.mathml.WebappMathMLRenderer', MathMLEnhancer);
  });
})();
