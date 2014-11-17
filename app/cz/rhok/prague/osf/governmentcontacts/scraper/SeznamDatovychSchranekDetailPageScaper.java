package cz.rhok.prague.osf.governmentcontacts.scraper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Address;
import models.Organization;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Maps;

//TODO : michal pridat plneni dat pro e-podatelnu (email v v zalozce zakladni info neni vzdy vyplnen)
public class SeznamDatovychSchranekDetailPageScaper {

	private static Logger log = play.Logger.log4j; //org.apache.log4j.Logger.getLogger("another.logger");

	private static final Pattern MAIL_REGEX_PATTERN = Pattern.compile("\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b", Pattern.CASE_INSENSITIVE);
	private static final Pattern DATABOX_ID_PATTERN = Pattern.compile("\\b([0-9a-z]*).*\\b", Pattern.CASE_INSENSITIVE);
	
	/**
	 * @param pageUrl page which contains information to be scraped 
	 * 		  eg.http://seznam.gov.cz/ovm/regionDetail.do?path=KPRAHA&ref=obcan
	 * @return 
	 */
	public Organization scrape(String pageUrl) {
		
		log.debug("Start scraping data from municipality page: " + pageUrl);
		
		Long startTime = System.currentTimeMillis();
			
		Document doc = null;
		try {
			doc = Jsoup.connect(pageUrl).get();
		} catch (IOException ex) {
			throw new RuntimeException("Unable to parse: " + pageUrl, ex);
		}

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
		
		Long endTime = System.currentTimeMillis();

		String logMessageContext = "(" + pageUrl + ")";
		
		Organization organization = new Organization();
		
		organization.dataBoxId = parseDataboxIdentificationNumber(scrappedData.get("Identifikátor datové schránky"), logMessageContext);
		organization.name = scrappedData.get("Název");
		organization.code = scrappedData.get("Kód organizace");
		organization.taxId = scrappedData.get("DIČ");
		organization.organizationId = scrappedData.get("IČ");
		organization.email = parseEmail(scrappedData, logMessageContext);
		organization.phone = scrappedData.get("Telefon");
		organization.bankAccount = scrappedData.get("Bankovní spojení");
		organization.type = scrappedData.get("Typ instituce");
		organization.officeHours = scrappedData.get("Úřední hodiny");
		organization.www = scrappedData.get("WWW");
        organization.urlOfSource = pageUrl;
		
		Address address = parseAddress(scrappedData, logMessageContext);
		organization.addressStreet = address.street;
		organization.addressCity = address.city;
		organization.addressZipCode = address.zipCode;
		
		long timeElapsed = endTime - startTime;
		log.debug("Scraping of " + pageUrl + " succesfully done in " + timeElapsed + " ms");

		logMissingFields(organization, logMessageContext);
		
		return organization;
		
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

	private Address parseAddress(Map<String, String> scrappedData, String logMessageContext) {
		String addressText = scrappedData.get("Adresa sídla");
		
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
	
	private String parseEmail(Map<String, String> scrappedData, String logMessageContext) {
		String rawEmailData = scrappedData.get("E-mail"); // eg. posta@cityofprague.cz (podatelna)

		// extract only mail part from string if there are something else
		String email = "";

		if (rawEmailData != null) {
			Matcher matcher = MAIL_REGEX_PATTERN.matcher(rawEmailData);
			if (matcher.find()) {
				email = matcher.group(0);
			} else {			
				log.error("Unable to parse e-mail. Parsed text : " + rawEmailData + " " + logMessageContext);
			}
		}
						
		return email;
	}

}
