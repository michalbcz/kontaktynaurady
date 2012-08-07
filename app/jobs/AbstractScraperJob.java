/**
 * 
 */
package jobs;

import models.Organization;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
public class AbstractScraperJob extends Job {

	protected void saveOrganization(Organization organization) {
		
		Logger.debug("Saving organization %s", organization);
		
		Organization existingOrganization = Organization.find("byDataBoxId", organization.dataBoxId).first();
		
		if (existingOrganization != null) {
			existingOrganization.copyStateFrom(organization);
			organization = existingOrganization;
		}
		
		organization.save();

		// save immediately
		JPA.em().flush();
	}

}
