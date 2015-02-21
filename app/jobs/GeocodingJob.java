/**
 * 
 */
package jobs;

import java.util.List;

import models.GeoLocation;
import models.Organization;

import org.apache.log4j.Logger;

import play.db.jpa.JPA;
import play.jobs.Job;
import play.jobs.On;
import cz.rhok.prague.osf.governmentcontacts.geocoding.Geocoder;
import cz.rhok.prague.osf.governmentcontacts.scraper.ApiRequestLimitExceededException;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
@On("0 0 2 * * ?")
public class GeocodingJob extends Job {

	private static Logger log = play.Logger.log4j;
	
	@Override
	public void doJob() throws Exception {
		int page = 0;
		Geocoder geocoder = new Geocoder();
		while (true) {
			List<Organization> organizations = Organization.find("byLatitudeIsNull").fetch(page, 100);
			
			if (organizations.isEmpty()) {
                log.info("Geocoding is over. There is no more organizations to geo code its address.");
				break;
			}
			
			for (Organization organization : organizations) {
				try {
					GeoLocation geoLocation;
					try {
						geoLocation = geocoder.geocode(organization);
					} catch (ApiRequestLimitExceededException e) {
						log.warn("Geocoding API limit exceeded.");
						return;
					}
					
					if (geoLocation != null) {
						organization.latitude = geoLocation.lat;
						organization.longitude = geoLocation.lng;

                        if (!JPA.em().getTransaction().isActive()) {
                            JPA.em().getTransaction().begin();
                        }

						organization.save();

                        JPA.em().getTransaction().commit();
					}
				} catch (Throwable e) {
					log.warn("Failed to geocode organization address: " + organization.getAddress(), e);
					JPA.em().getTransaction().rollback();
				}
			}
			
			page++;
		}
	}
	
}
