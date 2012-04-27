/*
 * #%L omeglespy-z-core
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
package org.darkimport.omeglespy_z;

import java.net.URL;
import java.text.MessageFormat;

/**
 * Helper class for recaptchas.
 * 
 * @author user
 * @version $Id: $
 */
public class RecaptchaHelper {
	/**
	 * Constant
	 * <code>GOOGLE_RECAPTCHA_ASSET_URL="http://www.google.com/recaptcha/api/ima"{trunked}</code>
	 */
	private static final String	GOOGLE_RECAPTCHA_ASSET_URL	= "http://www.google.com/recaptcha/api/image?c={0}";
	private static final String	CHALLENGE_URL_STRING		= "http://www.google.com/recaptcha/api/challenge?k={0}&type={1}&ajax=1&cachestop=0.34919850158610977";
	private static final String	IMAGE_TYPE					= "image";
	private static final String	AUDIO_TYPE					= "audio";

	/**
	 * Get a challenge string for an image challenge for the given site id.
	 * 
	 * @param id
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getImageChallengeString(final String id) {
		return getChallengeUrlStringByType(id, IMAGE_TYPE);
	}

	/**
	 * Get a challenge string for an audio challenge for the given site id.
	 * 
	 * @param id
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getMp3ChallengeString(final String id) {
		return getChallengeUrlStringByType(id, AUDIO_TYPE);
	}

	private static String getChallengeUrlStringByType(final String id, final String type) {
		try {
			final String challengeUrl = MessageFormat.format(CHALLENGE_URL_STRING, id, type);
			final String captcha = CommunicationHelper.wget(new URL(challengeUrl), false);
			final int idx0 = captcha.indexOf("challenge : '") + "challenge : '".length();
			final int idx1 = captcha.indexOf('\'', idx0);
			final String challenge = captcha.substring(idx0, idx1);
			return challenge;
		} catch (final Exception e) {
			LogHelper.log(RecaptchaHelper.class, LogLevel.WARN, "Unable to generate challenge.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the URL string of the given challenge.
	 * 
	 * @param challenge
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getChallengeAssetUrlString(final String challenge) {
		final String googleRecaptchaUrl = MessageFormat.format(GOOGLE_RECAPTCHA_ASSET_URL, challenge);
		return googleRecaptchaUrl;
	}
}
