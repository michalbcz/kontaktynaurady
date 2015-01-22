package cz.rhok.prague.osf.governmentcontacts.scraper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Lists;

public class SeznamDatovychSchranekKrajeListPageScraper implements DetailPageUrlRetriever {

	private static final Logger log = play.Logger.log4j;
	
	@Override
	public List<URL> extractDetailPageUrlsFrom(String url) {
		
		Document doc = ScraperHelper.getHtmlDocumentFor(url);

		Elements anchorElementsWithDetailPageLink = doc.select(".areaList .listItem a");

		List<URL> detailPagesUrl = Lists.newArrayList();
		
		for (Element detailPageLinkAnchorElement : anchorElementsWithDetailPageLink) {
			String urlAsString = detailPageLinkAnchorElement.attr("abs:href"); // abs: trick convert relative uri of href to absolute uri - see org.jsoup.nodes.Document#absUrl

			try {
				detailPagesUrl.add(new URL(urlAsString));
			} catch (MalformedURLException e) {
				log.error(
						"URL " + urlAsString + " seems malformed. This url will be skipped." +
						"List page url when this malformed link appeared : " + url + ")");
			}
			
		}
		
		return detailPagesUrl;
		
	}

}
