package org.darkimport.omeglespy;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReCaptchaWindow extends JFrame implements KeyListener {
	private static final Log	log					= LogFactory.getLog(ReCaptchaWindow.class);

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6080411410382372313L;
	static final int			SIZED				= ImageObserver.HEIGHT | ImageObserver.WIDTH;
	private static final int	DEFAULT_IMAGE_TYPE	= BufferedImage.TYPE_INT_RGB;
	ImageIcon					chal;
	JLabel						label;
	JTextField					captcha;
	Omegle						chat;
	String						challenge;
	final Tesseract				instance			= Tesseract.getInstance();

	public ReCaptchaWindow(final Omegle chat, final String challenge, final boolean useOcr) throws Exception {
		this.chat = chat;
		this.challenge = challenge;
		setTitle("Enter Captcha");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());

		label = new JLabel();
		label.setVerticalTextPosition(JLabel.BOTTOM);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setHorizontalAlignment(JLabel.CENTER);

		add(label, BorderLayout.CENTER);
		setLocationRelativeTo(null);

		final ImageIcon image = new ImageIcon(new URL(Common.GOOGLE_RECAPTCHA_URL + challenge));
		String captchaGuess = StringUtils.EMPTY;
		if (useOcr) {
			try {
				captchaGuess = instance.doOCR(bufferImage(image.getImage()));
				log.debug("Captcha guess: " + captchaGuess);
			} catch (final TesseractException e) {
				log.warn("Captcha failed.", e);
			}
		}
		final Dimension d = new Dimension(image.getIconWidth(), image.getIconHeight());
		label.setMinimumSize(d);
		label.setMaximumSize(d);
		label.setIcon(image);

		final JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		captcha = new JTextField(27);
		captcha.setFont(OmegleSpyPanel.FONT);
		captcha.setHorizontalAlignment(JTextField.CENTER);
		captcha.addKeyListener(this);
		captcha.setText(captchaGuess);

		inputPanel.add(captcha);
		add(inputPanel, BorderLayout.SOUTH);
		pack();
	}

	public void keyTyped(final KeyEvent e) {
	}

	public void keyReleased(final KeyEvent e) {
	}

	public void keyPressed(final KeyEvent e) {
		final int key = e.getKeyCode();

		if (key == KeyEvent.VK_ENTER) {
			Omegle.wget(chat.getRecaptcha_url(), true, "id", chat.getChatId(), "challenge", challenge, "response",
					captcha.getText());
			dispose();
		}
	}

	public BufferedImage bufferImage(final Image image) {
		return bufferImage(image, DEFAULT_IMAGE_TYPE);
	}

	public BufferedImage bufferImage(final Image image, final int type) {
		final BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		final Graphics2D g = bufferedImage.createGraphics();
		g.drawImage(image, null, null);
		// waitForImage(bufferedImage);
		return bufferedImage;
	}
}