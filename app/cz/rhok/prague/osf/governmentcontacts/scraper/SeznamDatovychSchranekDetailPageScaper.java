package cz.rhok.prague.osf.governmentcontacts.scraper;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import models.Address;
import models.Email;
import models.Organization;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Maps;

/**
 * Scraper, kteremu navstup prijde url detailu obce napr. "http://seznam.gov.cz/ovm/municipalityDetail.do?path=KOSMONOSY"
 * a on z neho vyscrapuje udaje.<br/><br/>
 *
 * Ze stranky detailu se lze dostat na dalsi dve zalozky, ktere obsahuji dalsi detailnejsi informace:
 * 1) "Doplnkove udaje" (napr. http://seznam.gov.cz/ovm/municipalityAdditionDetail.do?path=KOSMONOSY) <br/>
 * 2) "Kontaktni osoby" (napr. http://seznam.gov.cz/ovm/contactPersonList.do?path=KOSMONOSY) <br/>
 * 3) "Sestava dle §5 z.č. 106/1999 Sb" (napr. http://seznam.gov.cz/ovm/arrangementDetail.do?path=KOSMONOSY)<br/><br/>
 *
 * Tento scraper ziska dalsi data i z techto stranek.
 *
 *
 */
public class SeznamDatovychSchranekDetailPageScaper {

	private static Logger log = play.Logger.log4j; //org.apache.log4j.Logger.getLogger("another.logger");

	private static final Pattern MAIL_REGEX_PATTERN = Pattern.compile("\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b", Pattern.CASE_INSENSITIVE);
	private static final Pattern DATABOX_ID_PATTERN = Pattern.compile("\\b([0-9a-z]*).*\\b", Pattern.CASE_INSENSITIVE);
	
	/**
	 * @param detailPageUrl page which contains information to be scraped (entry point)
	 * 		  eg.http://seznam.gov.cz/ovm/regionDetail.do?path=KPRAHA&ref=obcan
	 * @return 
	 */
	public Organization scrape(String detailPageUrl) {
		
		log.debug("Start scraping data from municipality page: " + detailPageUrl);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		Organization organizationDetail = scrapeDetailPage(detailPageUrl);
		Organization organizationAdditionalDetail = scrapeAdditionalDetailPage(ScraperHelper.convertFromDetailToAdditionalDetailPage(detailPageUrl));

		Organization organization = Organization.merge(organizationDetail, organizationAdditionalDetail);
		
		stopWatch.stop();

		log.debug("Scraping of " + detailPageUrl + "and other details pages was succesfully done in: " + stopWatch.toString()  );
		String logMessageContext = "(" + detailPageUrl + ")";
		logMissingFields(organization, logMessageContext);
		
		return organization;
		
	}

	private Organization scrapeAdditionalDetailPage(String page) {

		log.debug("Start scraping additional detail data from page: " + page);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		Document doc = ScraperHelper.getHtmlDocumentFor(page);

		Map<String, String> scrappedData = htmlDataRowsToMap(doc);

		String logMessageContext = "(" + page + ")";

		Organization organization = new Organization();

		organization.taxId = scrappedData.get("DIČ");
		organization.bankAccount = scrappedData.get("Bankovní spojení");
		organization.code = scrappedData.get("Kód organizace");
		organization.type = scrappedData.get("Typ instituce");
		organization.www = scrappedData.get("WWW");

		String textIncludingEmail = scrappedData.get("E-mail"); // eg. posta@cityofprague.cz (podatelna)
		organization.email = parseEmail(textIncludingEmail, logMessageContext);


		/* pole "Úřadovny" v sobe obsahuje radu dalsich informaci jako je email (ktery je casto
		* narozdil od pole E-mail vyplnen), telefonni cislo, uredni hodiny */

		List<Email> extractedEmails = extractEmailsFromUradovnyHtml(doc.select(".offices .officeFirst"));

		for (Email email : extractedEmails) {

			if (organization.email == null) {
				//FIXME: dodelat doplnovani emailu
			}


		}

		stopWatch.stop();

		log.debug("Scraping of " + page + "and other details pages was succesfully done in: " + stopWatch.toString()  );

		return organization;

	}

	private List<Email> extractEmailsFromUradovnyHtml(Elements officesDiv) {

		List<Email> emails = Lists.newArrayList();

		Elements officeEmails = officesDiv.select(".officeEmails .officeEmailsVal");

		for (Element officeEmailElement : officeEmails) {

			String emailText = officeEmailElement.text();
			Email email = parseEmail(emailText);

			if (email != null) {
				emails.add(email);
			}
		}

		return emails;

	}

	/**
	 *
	 * @param emailText
	 * @return null, kdyz se email nepodari vyparsovat
	 */
	private Email parseEmail(String emailText) {

		String pattern = "(" + MAIL_REGEX_PATTERN.toString() + ")" + "\\s*\\((.*?\\))";

		Matcher matcher = Pattern.compile(pattern).matcher(emailText);

		Email email = null;

		if (matcher.find()) {

			String emailAddress = matcher.group(1);
			String description = matcher.group(2);

			email = new Email();
			email.uri = emailAddress;
			email.description = description;

		}

		return email;

	}


	private Organization scrapeDetailPage(String detailPageUrl) {

		log.debug("Start scraping detail data from municipality detail page: " + detailPageUrl);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		Document doc = ScraperHelper.getHtmlDocumentFor(detailPageUrl);

		Map<String, String> scrappedData = htmlDataRowsToMap(doc);

		String logMessageContext = "(" + detailPageUrl + ")";

		Organization organization = new Organization();

		organization.name = scrappedData.get("Název");

		String addressText = scrappedData.get("Adresa sídla");
		Address address = parseAddress(addressText, logMessageContext);
		organization.addressStreet = address.street;
		organization.addressCity = address.city;
		organization.addressZipCode = address.zipCode;

		organization.organizationId = scrappedData.get("IČ");
		organization.dataBoxId = parseDataboxIdentificationNumber(scrappedData.get("Identifikátor datové schránky"), logMessageContext);
		organization.urlOfSource = detailPageUrl;

		stopWatch.stop();
		log.debug("Scraping of " + detailPageUrl + "page succesfully done in: " + stopWatch.toString()  );

		return organization;
	}

	private Document getHtmlDocument(String pageUrl) {

		Document doc = null;
		try {
			doc = Jsoup.connect(pageUrl).get();
		} catch (IOException ex) {
			throw new RuntimeException("Unable to parse: " + pageUrl, ex);
		}
		return doc;
	}

	/**
	 *
	 * Na strance detailu uradu jsou data organizovany jako tabulka (html tag <table>)<br/><br/>
	 * <pre>Label   |   Hodnota</pre><br/><br/>
	 *
	 * napr.:<br/><br/>
	 *
	 * <pre>
	 *     &lt;tr&gt;
	 *         &lt;td&gt;Kód&lt;/td&gt;
	 *         &lt;td>Breclav&lt;/td&gt;
	 *     &lt;/tr&gt;
	 *
	 * </pre>
	 *
	 * Proto data prevedeme na mapu a pak pomoci klice vydobavame podle labelu, co nas zajima.
	 *
	 * @param doc
	 * @return
	 */
	private Map<String, String> htmlDataRowsToMap(Document doc) {
		Map<String, String> scrappedData = Maps.newHashMap();

		Elements dataRows = doc.select("#colData.institution .data tr");

		for (Element dataRow : dataRows) {

			String label = dataRow.select("th").text();

			// remove trailing ":" from label so
			// instead of "Identifikátor datové schránky:" its just
			// "Identifikátor datové schránky"
			label = label.replace(":", "");

			String value;
			if ("Adresa sídla".equals(label)) {
				// replace <br/> tags with newline
				value = dataRow.select("td").html(); // as this cause html escaping...
				value = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(value); // ... we need to unescape it
				value = value.replace("<br />", "\n");
				value = value.replace("<br/>", "\n");
				value = value.replaceAll("<span class=\"incompleteAddress\">.*</span>", "");
			} else {
				value = dataRow.select("td").text();
			}

			scrappedData.put(label, value);

		}
		return scrappedData;
	}

	private void logMissingFields(Organization organization, String logMessageContext) {
		Set<String> missingFields = new HashSet<String>();
		
		if (StringUtils.isEmpty(organization.name)) {
			missingFields.add("name");
		}
		
		if (StringUtils.isEmpty(organization.dataBoxId)) {
			missingFields.add("dataBoxId");
		}
		
		if (StringUtils.isEmpty(organization.addressStreet)) {
			missingFields.add("addressStreet");
		}
		
		if (StringUtils.isEmpty(organization.addressCity)) {
			missingFields.add("addressCity");
		}
		
		if (!missingFields.isEmpty()) {
			log.warn("Organization is missing required fields: " + StringUtils.join(missingFields, ", ") + " " + logMessageContext);
		}
	}
	
	private String parseDataboxIdentificationNumber(String rawDataboxId, String logMessageContext) {
		
		String databoxId = "";
				
		if (StringUtils.isNotEmpty(rawDataboxId)) {
			
			Matcher matcher = DATABOX_ID_PATTERN.matcher(rawDataboxId);
			
			if (matcher.find()) {
				if(matcher.groupCount() < 1) {
					log.error("Cannot parse databox identification number from: " + rawDataboxId + " " + logMessageContext);
				}
				else {
					databoxId = matcher.group(1);
				}
			}
			
		}
		
		return databoxId;
		
	}

	private Address parseAddress(String addressText, String logMessageContext) {

		Address address = new Address();
		
		if (addressText != null) {
			String[] lines = addressText.split("\n");
			
			boolean validAddress = false;
			
			if (lines.length == 3 && lines[2].trim().isEmpty()) {
				String zipAndCity = lines[1].trim();
				int firstSpace = zipAndCity.indexOf(" ");
				
				if (firstSpace > 0) {
					address.street = lines[0].trim();
					address.zipCode = zipAndCity.substring(0, firstSpace).trim();
					address.city = zipAndCity.substring(firstSpace).trim();

					validAddress = true;
				}
			} 
			
			if (!validAddress) {
				address.street = addressText;
				log.warn("Unknown address format: " + addressText + " " + logMessageContext);
			}
		}
		
		return address;
	}
	
	private String parseEmail(String textIncludingEmail, String logMessageContext) {

		// extract only mail part from string if there are something else
		String email = "";

		if (textIncludingEmail != null) {
			Matcher matcher = MAIL_REGEX_PATTERN.matcher(textIncludingEmail);
			if (matcher.find()) {
				email = matcher.group(0);
			} else {			
				log.error("Unable to parse e-mail. Parsed text : " + textIncludingEmail	 + " " + logMessageContext);
			}
		}
						
		return email;
	}

}
