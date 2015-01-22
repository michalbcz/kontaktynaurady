package jobs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import models.Organization;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import play.Logger;
import play.db.jpa.JPA;
import play.jobs.On;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cz.rhok.prague.osf.governmentcontacts.helper.RepeatOnTimeoutTask;
import cz.rhok.prague.osf.governmentcontacts.scraper.PaginableRecordsListPageRetriever;
import cz.rhok.prague.osf.governmentcontacts.scraper.PaginableRecordsListPageRetriever.PaginableRecord;
import cz.rhok.prague.osf.governmentcontacts.scraper.SeznamDatovychSchranekDetailPageScaper;
import cz.rhok.prague.osf.governmentcontacts.scraper.SeznamDatovychSchranekKrajeListPageScraper;
import cz.rhok.prague.osf.governmentcontacts.scraper.SeznamDatovychSchranekMunicipalityListPageScraper;

@On("0 0 23 * * ?") /* each day in 23:00 */
public class KrajeScraperJob extends AbstractScraperJob {

	private static final String KRAJS_LISTING_PAGE = "http://seznam.gov.cz/ovm/regionList.do";

	@Override
	public void doJob() throws Exception {
		
		Logger.info("Scraping job (scraping of all the municipalities) started");
		
		StopWatch watches = new StopWatch();
		watches.start();

		List<URL> krajDetailPageUrls = getKrajDetailPageUrls();

		List<URL> krajDetailPageWithMunicipalitiesListUrls = Lists.newArrayList();

		Logger.debug("Converting municipality urls to detail's urls");
		for (final URL krajDetailPageUrl : krajDetailPageUrls) {

			Document document = getDocumentFor(krajDetailPageUrl);

			Element linkToDetailWithAllMunicipalities = document.select("a[href*=allMunicipality]").first();

			if (linkToDetailWithAllMunicipalities == null) {
				// maybe it's "Prague" which has other structure... there are no municipalities but neighbourhoods
				linkToDetailWithAllMunicipalities = document.select("a[href*=urbanNeighbourhoods]").first();
			}
			
			if (linkToDetailWithAllMunicipalities != null) {
				String relativeStringUrl = linkToDetailWithAllMunicipalities.attr("href");
				String dataBoxBaseUrl = "http://seznam.gov.cz/ovm/regionDetail.do";
				String urlAsString = dataBoxBaseUrl + relativeStringUrl;
				try {
					URL url = new URL(urlAsString);
					krajDetailPageWithMunicipalitiesListUrls.add(url);
					Logger.debug("Found municipality page %s", url.toExternalForm());
				} catch (MalformedURLException e) {
					Logger.error(
							"Seems that municipality url is malformed. Malformed url: %s. When parsed document on: %s",
							urlAsString, krajDetailPageUrl);
				}
			}

		}


		for (URL krajDetailUrl : krajDetailPageWithMunicipalitiesListUrls) {

			Logger.info("Scraping url of municipality pages of kraj in kraj's detail page (%s)", krajDetailUrl);
			
			/* get all urls for list pages of municipalities */
			Map<Long, URL> allPages = Maps.newHashMap();

			Logger.debug("Getting all pages in paginable element (table)...");
			URL nextPaginable = krajDetailUrl;
			while(nextPaginable != null) {
				final String url = nextPaginable.toExternalForm();

				RepeatOnTimeoutTask<PaginableRecord> listPagesRetrieverTask = new RepeatOnTimeoutTask<PaginableRecord>() {

					@Override
					public PaginableRecord doTask() {
						PaginableRecordsListPageRetriever listPagesRetriever = new PaginableRecordsListPageRetriever();
						return listPagesRetriever.getListPageLinks(url);
					}

				};

				PaginableRecord paginable = listPagesRetrieverTask.call();
				allPages.putAll(paginable.getPages());
				nextPaginable = paginable.getNextPaginable();
			}


			for(final URL municipalityListPage : allPages.values()) {

				
				RepeatOnTimeoutTask<List<URL>> municipalityListPageScraperTask = new RepeatOnTimeoutTask<List<URL>>() {

					@Override
					public List<URL> doTask() {
						SeznamDatovychSchranekMunicipalityListPageScraper municipalityListPageScraper = 
								new SeznamDatovychSchranekMunicipalityListPageScraper();

						return municipalityListPageScraper.extractDetailPageUrlsFrom(municipalityListPage.toExternalForm());
					}

				};

				List<URL> detailPageLinks = municipalityListPageScraperTask.call();

				for (final URL municipalityDetailPageUrl : detailPageLinks) {
					RepeatOnTimeoutTask<Organization> detailPageScrapeTask = new RepeatOnTimeoutTask<Organization>() {

						@Override
						public Organization doTask() {
							SeznamDatovychSchranekDetailPageScaper detailPageScaper = new SeznamDatovychSchranekDetailPageScaper();
							return detailPageScaper.scrape(municipalityDetailPageUrl.toExternalForm());
						}

					};

					Organization organization = detailPageScrapeTask.call();

					try {
						
						if (!JPA.em().getTransaction().isActive()) {
							JPA.em().getTransaction().begin();
						}
						
						saveOrganization(organization);
						
						JPA.em().flush();
						
						// save immediately (copy paste from stackoverflow - not sure why flush and clear are needed)
						JPA.em().getTransaction().commit(); /* transaction#begin is implicitly called by play framework 
															   before calling this job's #doJob method to ensure that jobs 
															   are transactional (ie. when error happened everything 
															   in job is rollbacked) */
						
						
						
					    
					} catch (RuntimeException ex) {
						Logger.error(
								"Unable to save scraped organization (%s). Exception throwed: %s", 
								ToStringBuilder.reflectionToString(organization), ExceptionUtils.getFullStackTrace(ex));
						JPA.em().getTransaction().rollback();
					}

					JPA.em().clear(); // clear managed entities between two transactions
					

				}

			}


		}

		watches.stop();
		
		Logger.info("Municipalities have been successfully scraped. It lasts %s ms", watches.getTime());

	}

	private Document getDocumentFor(final URL krajDetailPageUrl) throws Exception {
		
		RepeatOnTimeoutTask<Document> documentRetrieveTask = new RepeatOnTimeoutTask<Document>() {

			@Override
			public Document doTask() {
				try {
					return Jsoup.connect(krajDetailPageUrl.toExternalForm()).get();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		};

		Document document = documentRetrieveTask.call();
		return document;
	}

	private List<URL> getKrajDetailPageUrls() {
		final SeznamDatovychSchranekKrajeListPageScraper krajsListPageScraper = 
				new SeznamDatovychSchranekKrajeListPageScraper();

		RepeatOnTimeoutTask<List<URL>> repeatingExtractDetailPageForKrajsPage = new RepeatOnTimeoutTask<List<URL>>() {

			@Override
			public List<URL> doTask() {
				return krajsListPageScraper.extractDetailPageUrlsFrom(KRAJS_LISTING_PAGE);
			}

		};
		
		try {
			return repeatingExtractDetailPageForKrajsPage.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}


}
