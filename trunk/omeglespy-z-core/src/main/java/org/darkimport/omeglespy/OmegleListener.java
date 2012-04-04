package org.darkimport.omeglespy;

interface OmegleListener {
	public void eventFired(Omegle src, String event, String... args);

	public void messageSent(Omegle src, String msg);
}