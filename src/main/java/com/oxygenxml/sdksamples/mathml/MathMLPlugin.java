package com.oxygenxml.sdksamples.mathml;

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.plugin.PluginDescriptor;

/**
 * MathMLPlugin is a sample plug-in for XML to math equations converter.
 * 
 * @author costi_dumitrescu
 * 
 */
public class MathMLPlugin extends Plugin {

	/**
	 * Plugin instance.
	 */
	private static MathMLPlugin instance = null;

	/**
	 * MathML plugin constructor.
	 * 
	 * @param descriptor
	 *            Plugin descriptor.
	 */
	public MathMLPlugin(PluginDescriptor descriptor) {
		super(descriptor);

		if (instance != null) {
			throw new IllegalStateException("Already instantiated!");
		}
		instance = this;
	}

	/**
	 * Get the plugin instance.
	 * 
	 * @return the shared plugin instance.
	 */
	public static MathMLPlugin getInstance() {
		return instance;
	}
}
