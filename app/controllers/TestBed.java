package controllers;

import cz.rhok.prague.osf.governmentcontacts.helper.OrganizationHelper;
import cz.rhok.prague.osf.governmentcontacts.scraper.SeznamDatovychSchranekDetailPageScaper;
import groovy.lang.GroovyShell;
import jobs.KrajeScraperJob;
import models.Organization;
import org.apache.commons.lang.exception.ExceptionUtils;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class TestBed extends Controller {

	public static void index() {
		render();
	}

	public static void scrapeDetailPage(String urlOfDetailPage) throws Exception {

		SeznamDatovychSchranekDetailPageScaper scraper = new SeznamDatovychSchranekDetailPageScaper();
		Organization scrapedOrganization = scraper.scrape(urlOfDetailPage);

		Organization savedOrganization = OrganizationHelper.saveOrganization(scrapedOrganization);

		redirect("Organizations.show", savedOrganization.id.toString());
	}

	public static void startScrapeJob() {
		new KrajeScraperJob().now();
		flash.put("message", "scraping job started");
		index();
	}    

	public static void evaluateGroovyScript(String script) {

		GroovyShell groovyShell = new GroovyShell(getControllerClass().getClassLoader());

		//groovyShell.setVariable("scraper", new SeznamDatovychSchranekMunicipalityListPageScraper());
		
		String result  = "";
		try {
			result = groovyShell.evaluate(script).toString();
		}
		catch (Exception e) {
			result = ExceptionUtils.getFullStackTrace(e);
			flash.put("type", "error");
		}
		
		flash.put("message", result);
		index();

	}
}
