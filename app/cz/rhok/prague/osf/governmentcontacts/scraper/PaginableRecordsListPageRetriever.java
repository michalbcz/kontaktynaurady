package cz.rhok.prague.osf.governmentcontacts.scraper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Maps;

/**
 * Retrieve all links to each pagination (<< 1 2 3 4 5 >>) and give us link next series 
 * (link called "nasledujici")
 * 
 * @author Michal Bernhard michal@bernhard.cz / twitter @michalb_cz 
 *
 */
public class PaginableRecordsListPageRetriever extends ScraperHelper {

	public PaginableRecord getListPageLinks(String urlOfPaginableList) {

		Document doc = getHtmlDocumentFor(urlOfPaginableList);

		Map<Long, URL> pages = Maps.newHashMap();
		Elements paginatorLinks = doc.select(".paginator li a");

		for (Element paginatorLink : paginatorLinks) {
			String relativeUrl = paginatorLink.attr("href");
			String urlAsString = urlOfPaginableList.substring(0, urlOfPaginableList.lastIndexOf("?")) + relativeUrl;
			try {
				URL pageUrl = new URL(urlAsString);
				Long pageNumber = Long.valueOf(paginatorLink.text());
				pages.put(pageNumber, pageUrl);
			} catch (MalformedURLException e) {
				throw new RuntimeException("Cannot obtain url for: " + urlAsString,  e);
			}
		}

		Element nextButton = doc.select(".paginator .next").first();

		URL nextPaginable = null;
		if (nextButton != null) {
			String nextAbsLink = nextButton.attr("abs:href");

			try {
				nextPaginable = new URL(nextAbsLink);
			} catch (MalformedURLException e) {
				throw new RuntimeException(
							"Cannot obtain url from element " + nextButton + ".",  e);
			}
		}

		return new PaginableRecord(pages, nextPaginable);

	}


	public static class PaginableRecord {

		private final Map<Long, URL> pages = Maps.newHashMap();
		private final URL nextPaginable;

		/**
		 * @param pages
		 * @param nextPaginable can be null
		 */
		public PaginableRecord(Map<Long, URL> pages, URL nextPaginable) {
			this.pages.putAll(pages);
			this.nextPaginable = nextPaginable;
		}

		public Map<Long, URL> getPages() {
			return pages;
		}
		
		/**
		 * @return URL of link which leads to next page of paginable element (eg. table) or null if there is no
		 *         "next" button (ie. it's last page or there is only one page at all)
		 */
		public URL getNextPaginable() {
			return nextPaginable;
		}

	}

}


