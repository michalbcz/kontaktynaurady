/**
 * 
 */
package controllers;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
public class Security extends controllers.Secure.Security {
	
	static boolean authenticate(String username, String password) {
		
		String adminLogin = System.getProperty("kontaktynaurady.admin.username");
		String adminPassword = System.getProperty("kontaktynaurady.admin.password");
		
		if (adminLogin.equals(username) && adminPassword.equals(password)) {
			return true;
		}
		
		return false;
    }
	
}
