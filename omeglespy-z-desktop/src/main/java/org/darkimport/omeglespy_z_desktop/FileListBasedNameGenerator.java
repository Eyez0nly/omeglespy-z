/*
 * #%L omeglespy-z-desktop
 * 
 * $Id$ $HeadURL$ %% Copyright (C) 2011 - 2012 darkimport %% This program is
 * free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation,
 * either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl-2.0.html>. #L%
 */
/**
 * 
 */
package org.darkimport.omeglespy_z_desktop;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.config.ConfigHelper;
import org.darkimport.omeglespy_z.DefaultConversantNameGenerator;
import org.darkimport.omeglespy_z.NameGenerator;
import org.darkimport.omeglespy_z_desktop.constants.ConfigConstants;

/**
 * @author user
 * 
 */
public class FileListBasedNameGenerator implements NameGenerator {
	private static final Log	log	= LogFactory.getLog(FileListBasedNameGenerator.class);
	List<String>				namesList;

	public FileListBasedNameGenerator(final String resourceName) {
		final InputStream is = ConfigHelper.getConfigurationStream(ConfigHelper.getGroup(ConfigConstants.GROUP_MAIN)
				.getProperty(resourceName));
		try {
			namesList = IOUtils.readLines(is, null);
		} catch (final IOException e) {
			log.warn("Unable to load the names list.", e);
			namesList = new ArrayList<String>(Arrays.asList(DefaultConversantNameGenerator.CONVERSANT_NAMES));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.darkimport.omeglespy_z.NameGenerator#next(int, boolean)
	 */
	public String[] next(final int numberOfNames, final boolean uniqueNames) {
		if (numberOfNames > namesList.size() && uniqueNames) { throw new IllegalArgumentException(
				"The number of names requested exceeds the available number of names"); }
		final List<String> names = new ArrayList<String>();
		while (names.size() < numberOfNames) {
			final int index = (int) (Math.random() * namesList.size());
			final String proposedName = namesList.get(index);
			if (!uniqueNames || !names.contains(proposedName)) {
				names.add(proposedName);
			}
		}

		return names.toArray(new String[names.size()]);
	}

}
