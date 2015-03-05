package jobs;

import cz.rhok.prague.osf.governmentcontacts.service.OrganizationsService;
import play.Logger;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;


@OnApplicationStart
@On("0 0 6 * * ?") /* each day in 6:00 - just when we are sure that scraping of all data is done */
public class CacheWarmupJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Refreshing CSV dump cache...");
        OrganizationsService.refreshCacheForCsvDump();
        Logger.info("CSV dump cache refreshed");
    }
}
