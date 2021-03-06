package cz.rhok.prague.osf.governmentcontacts.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public abstract class ScraperHelper {

	/**
	 * @param url 
	 * @return 
	 * @throws UnableToConnectToServer when connection is unavailable
	 */
	public static Document getDocumentFor(URL url) {
		Document doc;
		
		try {
			doc = Jsoup.connect(url.toExternalForm()).timeout(10 * 1000).get();
			return doc;
		} catch (IOException ex) {
			
			if (ex instanceof SocketTimeoutException) {
				throw new UnableToConnectToServer("Unable to connect to: " + url, ex);
			}
			
			throw new RuntimeException("Unable to parse: " + url, ex);
		}
		
	}

	public static Document getHtmlDocumentFor(String url) {
		try {
			return getDocumentFor(new URL(url));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static String convertFromDetailToAdditionalDetailPage(String detailPageUrl) {
		return detailPageUrl.replace("municipalityDetail.do", "municipalityAdditionDetail.do");
	}

	public static String convertFromDetailToContactPersonsPage(String detailPageUrl) {
		return detailPageUrl.replace("municipalityDetail.do", "contactPersonList.do");
	}


}
