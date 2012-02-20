package org.darkimport.omeglespy;

public interface OmegleSpyListener {
	public void messageTransferred(OmegleSpy src, String msg);

	public void messageBlocked(OmegleSpy src, String msg);

	public void externalMessageSent(OmegleSpy src, String msg);

	public void isTyping(OmegleSpy src);

	public void stoppedTyping(OmegleSpy src);
}