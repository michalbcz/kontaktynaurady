/**
 *
 */
package controllers;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import play.Play;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
public class Security extends controllers.Secure.Security {

	private static final Logger log = play.Logger.log4j;

	static boolean authenticate(String username, String password) {

		String adminLogin = Play.configuration.getProperty("kontaktynaurady.admin.username");
		String adminPassword = Play.configuration.getProperty("kontaktynaurady.admin.password");

		if (StringUtils.isEmpty(adminLogin)) {
			adminLogin = System.getProperty("kontaktynaurady.admin.username");
		}

		if (StringUtils.isEmpty(adminPassword)) {
			adminPassword = System.getProperty("kontaktynaurady.admin.password");
		}

		if (StringUtils.isEmpty(adminLogin)) {
			log.warn("System property 'kontaktynaurady.admin.username' is not set");
			return false;
		}

		if (StringUtils.isEmpty(adminPassword)) {
			log.warn("System property 'kontaktynaurady.admin.password' is not set");
			return false;
		}

		if (adminLogin.equals(username) && adminPassword.equals(password)) {
			return true;
		}

		return false;
    }

}
