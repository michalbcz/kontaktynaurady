package controllers;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import jobs.GeocodingJob;
import jobs.KrajeScraperJob;
import models.Organization;

import org.apache.commons.lang.time.StopWatch;

import play.db.jpa.JPABase;
import play.mvc.Controller;
import play.mvc.With;

import com.google.common.collect.Lists;

import cz.rhok.prague.osf.governmentcontacts.scraper.SeznamDatovychSchranekDetailPageScaper;
import cz.rhok.prague.osf.governmentcontacts.scraper.SeznamDatovychSchranekListPageScraper;

@With(Secure.class)
public class TestBed extends Controller {

    public static void index() {
        render();
    }
       
    public static void scrapeDetailPage(String urlOfDetailPage) throws Exception {
    	
    	SeznamDatovychSchranekDetailPageScaper scraper = new SeznamDatovychSchranekDetailPageScaper();
    	Organization scrapedOrganization = scraper.scrape(urlOfDetailPage);
    	scrapedOrganization.save();
    	
    	redirect("Organizations.show", scrapedOrganization.id.toString());
    }
    
    public static void startScrapeJob() {
    	new KrajeScraperJob().now();
    	flash.put("message", "scraping job started");
    	index();
    }    

    public static void startGeocodingJob() {
    	new GeocodingJob().now();
    }

}
