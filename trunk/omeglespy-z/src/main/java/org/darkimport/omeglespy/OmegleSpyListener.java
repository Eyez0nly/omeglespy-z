package org.darkimport.omeglespy;

public interface OmegleSpyListener {
	public void messageTransferred(OmegleSpy src, String msg);

	public void messageBlocked(OmegleSpy src, String msg);

	public void externalMessageSent(OmegleSpy src, String msg);

	public void isTyping(OmegleSpy src);

	public void stoppedTyping(OmegleSpy src);

	public void chatStarted(OmegleSpy src);

	public void disconnected(OmegleSpy src);

	public void recaptchaRejected(OmegleSpy src, String id);

	public void recaptcha(OmegleSpy src, String id);
}
