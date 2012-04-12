/**
 * 
 */
package org.darkimport.omeglespy$z;

import java.net.URL;
import java.text.MessageFormat;

/**
 * @author user
 * 
 */
public class RecaptchaHelper {
	public static final String	GOOGLE_RECAPTCHA_ASSET_URL	= "http://www.google.com/recaptcha/api/image?c={0}";
	private static final String	CHALLENGE_URL_STRING		= "http://www.google.com/recaptcha/api/challenge?k={0}&type={1}&ajax=1&cachestop=0.34919850158610977";
	private static final String	IMAGE_TYPE					= "image";
	private static final String	AUDIO_TYPE					= "audio";

	public static String getImageChallengeString(final String id) {
		return getChallengeUrlStringByType(id, IMAGE_TYPE);
	}

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
	 * @return
	 */
	public static String getChallengeAssetUrlString(final String challenge) {
		final String googleRecaptchaUrl = MessageFormat.format(GOOGLE_RECAPTCHA_ASSET_URL, challenge);
		return googleRecaptchaUrl;
	}
}
