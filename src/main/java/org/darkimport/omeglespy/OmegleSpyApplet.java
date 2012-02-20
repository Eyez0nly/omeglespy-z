package org.darkimport.omeglespy;

import javax.swing.JApplet;

public class OmegleSpyApplet extends JApplet {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -7940392051930953803L;

	@Override
	public void init() {
		add(new OmegleSpyPanel(false));
	}
}
