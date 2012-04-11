/**
 * 
 */
package org.darkimport.config;

import java.util.List;

/**
 * @author user
 * 
 */
public class Version implements Comparable<Version> {
	private static final String	SNAPSHOT_INDICATOR	= "SNAPSHOT";

	private List<String>		versionElements;
	private boolean				isSnapshot;

	public Version(final String version) {

	}

	public int compareTo(final Version o) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

}
