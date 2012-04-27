package org.darkimport.omeglespy;

@Deprecated
interface OmegleListener {
	public void eventFired(Omegle src, String event, String... args);

	public void messageSent(Omegle src, String msg);
}
