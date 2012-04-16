/*
 * #%L
 * omeglespy-z-desktop
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2012 darkimport
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
package org.darkimport.omeglespy_z_desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class DesperationWindow extends JFrame implements ListSelectionListener, HyperlinkListener, ActionListener {
	/**
	 * 
	 */
	private static final long		serialVersionUID	= -6949526271531969609L;
	private static int				UNIT_INCREMENT		= 16;
	private final JList				convoList;
	private MutableComboBoxModel	lister;
	private final JPanel			panepane;
	private final List<JEditorPane>	panes;
	private JEditorPane				currentEp;
	private final JButton			save;

	public DesperationWindow() {
		super("Session Logs");

		convoList = new JList(lister = new DefaultComboBoxModel());
		convoList.addListSelectionListener(this);
		convoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panepane = new JPanel();
		panepane.setLayout(new BorderLayout());
		panes = new ArrayList<JEditorPane>();
		currentEp = null;

		save = new JButton("Save Session to File");
		save.addActionListener(this);
		save.setVisible(false);

		setLayout(new BorderLayout());
		add(scroller(convoList), BorderLayout.WEST);
		add(panepane, BorderLayout.CENTER);
		add(save, BorderLayout.SOUTH);
	}

	public void hyperlinkUpdate(final HyperlinkEvent ev) {
		if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				UrlHelper.openURL(ev.getURL().toString());
			} catch (final NullPointerException ex) {
				showError(this, "Invalid URL");
			} catch (final Exception ex) {
				showError(this, ex.getMessage());
			}
		}
	}

	public void addHTML(final String baseHTML, final String html) {
		final String all = baseHTML.replace("<!--%s-->", html);
		final JEditorPane ep = new JEditorPane("text/html", all);
		ep.addHyperlinkListener(this);
		ep.setEditable(false);
		panes.add(ep);
		if (panes.size() < 10) {
			lister.addElement(" + Session 0" + panes.size() + "   ");
		} else {
			lister.addElement(" + Session " + panes.size() + "   ");
		}
	}

	public void valueChanged(final ListSelectionEvent ev) {
		panepane.removeAll();
		final int index = convoList.getSelectedIndex();
		panepane.add(scroller(currentEp = panes.get(index)));
		if (!save.isVisible()) {
			save.setVisible(true);
		}
		// panepane.validate();
		validate();
	}

	public void actionPerformed(final ActionEvent ev) {
		final Object src = ev.getSource();
		if (src == save) {
			try {
				LogViewerHelper.guiWriteHtmlFile(currentEp.getText(), this);
			} catch (final IOException ex) {
				showError(this, "Could not save file: " + ex.getMessage());
			}
		}
	}

	public static JScrollPane scroller(final Component c) {
		final JScrollPane jsp = new JScrollPane(c);
		jsp.getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);
		jsp.getHorizontalScrollBar().setUnitIncrement(UNIT_INCREMENT);
		return jsp;
	}

	public static void showError(final Component p, final String msg) {
		JOptionPane.showMessageDialog(p, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
