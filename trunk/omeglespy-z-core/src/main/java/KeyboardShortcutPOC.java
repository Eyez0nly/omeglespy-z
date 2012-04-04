import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.darkimport.omeglespy.ui.util.KeyEventTranslator;
import org.darkimport.omeglespy.ui.util.KeyEventTranslator.Key;

/**
 * 
 */

/**
 * @author user
 * 
 */
public class KeyboardShortcutPOC {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final JFrame frame = new JFrame();
		final JTextField textField = new JTextField(30);
		final JLabel label = new JLabel("Key press is displayed here.");
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(label);
		frame.getContentPane().add(textField);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		textField.addKeyListener(new KeyAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyPressed(final KeyEvent e) {
				final Key key = KeyEventTranslator.translateKeyEvent(e);
				final String modifiers;
				final char input;
				final int _key;
				if (key != null) {
					modifiers = key.modifiers;
					input = key.input;
					_key = key.key;
				} else {
					modifiers = StringUtils.EMPTY;
					input = 0;
					_key = 0;
				}
				label.setText(new StringBuffer(modifiers != null ? modifiers : StringUtils.EMPTY).append(' ')
						.append('"').append((int) input).append('"').append(' ').append(_key).toString());
				textField.setText(StringUtils.EMPTY);
			}
		});

		frame.setSize(350, 100);
		frame.setVisible(true);
	}

}
