package org.darkimport.omeglespy;

import java.awt.BorderLayout;
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
import javax.swing.JPanel;
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
	private static final long	serialVersionUID	= -6949526271531969609L;
	JList						convoList;
	MutableComboBoxModel		lister;
	JPanel						panepane;
	List<JEditorPane>			panes;
	JEditorPane					currentEp;
	JButton						save;

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
		add(Common.scroller(convoList), BorderLayout.WEST);
		add(panepane, BorderLayout.CENTER);
		add(save, BorderLayout.SOUTH);
	}

	public void hyperlinkUpdate(final HyperlinkEvent ev) {
		if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				Common.openURL(ev.getURL().toString());
			} catch (final NullPointerException ex) {
				Common.showError(this, "Invalid URL");
			} catch (final Exception ex) {
				Common.showError(this, ex.getMessage());
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
		panepane.add(Common.scroller(currentEp = panes.get(index)));
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
				Common.guiWriteHtmlFile(currentEp.getText(), this);
			} catch (final IOException ex) {
				Common.showError(this, "Could not save file: " + ex.getMessage());
			}
		}
	}
}
