/**
 * 
 */
package jobs;

import cz.rhok.prague.osf.governmentcontacts.helper.OrganizationHelper;
import org.apache.commons.lang.builder.ToStringBuilder;

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
		OrganizationHelper.saveOrganization(organization);
	}

}
