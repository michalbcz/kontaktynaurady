/**
 *
 */
package controllers;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.persistence.Query;

import models.Organization;

import models.Person;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import play.cache.CacheFor;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import play.mvc.Controller;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
public class OrganizationsRest extends Controller {

	private static final Set<String> ORGANIZATION_FIELDS = Sets.newHashSet(
																	"name",
																	"addressstreet",
																	"addresscity",
																	"addresszipcode",
																	"eregistry",
																	"organizationid",
																	"taxid",
																	"bankaccount",
																	"code",
																	"type",
																	"www",
																	"email",
																	"phone",
																	"officehours",
																	"databoxid",
																	"latitude",
																	"longitude");

	private static final Map<String, Function<Organization, String>> csvMapperMachinery = new LinkedHashMap<String, Function<Organization, String>> () {{

//		put("id", (urad) -> urad.id.toString());
		put("uredni_nazev", (urad) -> urad.name);
		put("nazev_obce", (urad) -> urad.addressCity);
		put("adresa_ulice", (urad) -> urad.addressStreet);
		put("adresa_obec", (urad) -> urad.addressCity);
		put("adresa_psc", (urad) -> urad.addressZipCode);
		put("telefon", (urad) -> urad.telefon == null ? "" :  urad.telefon.telCislo);
		put("telefon_typ", (urad) -> urad.telefon == null ? "" : urad.telefon.typTelCisla);
		put("starosta_jmeno", (urad) -> starosta(urad).getCeleJmeno());
		put("starosta_email", (urad) -> starosta(urad).email == null ? "" : starosta(urad).email.uri);
		put("starosta_telefon", (urad) -> starosta(urad).telefon == null ? "" : starosta(urad).telefon);
		put("id_datove_schranky", (urad) -> urad.dataBoxId);
		put("webova_stranka_uradu", (urad) -> urad.www);
		put("ic", (urad) -> urad.organizationId);
		put("dic", (urad) -> urad.taxId);
		put("email1", (urad) -> urad.email);
		put("email1_popis", (urad) -> urad.emailDescription);
		put("email2", (urad) -> urad.email2);
		put("email2_popis", (urad) -> urad.email2Description);
		put("email3", (urad) -> urad.email3);
		put("email3_popis", (urad) -> urad.email3Description);
		put("typ_obce", (urad) -> urad.type);
		put("zdroj_dat", (urad) -> urad.urlOfSource);

	}};

	public static final String CSV_DUMP_CACHE_KEY = "csvDump";

	private static Person starosta(Organization urad) {
		return urad.contactPersons.stream().filter(starosta()).findFirst().orElseGet(Person::new);
	}

	private static Predicate<? super Person> starosta() {
		return (person) -> person.funkce.equalsIgnoreCase("starosta");
	}

	public static void organizations() {
		Map<String, String> parameters = params.allSimple();
		parameters.remove("body"); // hack - play puts empty body parameter to each request???

		if (parameters.isEmpty()) {
			render("OrganizationsRest/help.html");
		}

		String queryFindBy = "";
		List<String> queryParams = Lists.newArrayList();

		for (Entry<String, String> entry : parameters.entrySet()) {

			String fieldName = entry.getKey();
			fieldName = StringUtils.lowerCase(fieldName);
			fieldName = StringUtils.capitalize(fieldName);

			if (!ORGANIZATION_FIELDS.contains(StringUtils.lowerCase(fieldName))) {
                // skip as it's not Organization's field name
                continue;
			}

			String value = entry.getValue();
			value = value.replace("*", "%");

			if (StringUtils.isEmpty(queryFindBy)) {
				queryFindBy = "by";
			} else {
				queryFindBy += "And";
			}

			queryFindBy += fieldName + "Ilike";
			queryParams.add(value);
		}

		List<Organization> organizations;
		if (StringUtils.isEmpty(queryFindBy)) {
			organizations = Organization.all().fetch();
		} else {
			organizations = Organization.find(queryFindBy, queryParams.toArray()).fetch();
		}

		/* radius support aneb "dej mi urady v okruhu 20km" */
        if (parameters.containsKey("radius")) {

            Float radiusAroundLatitude = Float.valueOf(parameters.get("radiusAroundLatitude"));
            Float radiusAroundLongitude = Float.valueOf(parameters.get("radiusAroundLongitude"));
            Long radius = Long.valueOf(parameters.get("radius"));

            List<Long> organizationsIds = Lists.newArrayList();
            for (Organization organization : organizations) {
               organizationsIds.add(organization.getId());
            }

            Query query = JPA.em().createNativeQuery(
                    "SELECT * FROM organization " +
                            "WHERE ST_Intersects(" +
                            "   ST_GeographyFromText('SRID=4326;POINT(' || latitude || ' ' || longitude || ')')," +
                            "   ST_Buffer(ST_GeographyFromText('SRID=4326;POINT(" + radiusAroundLatitude + " " + radiusAroundLongitude + ")'), " +  radius + ")) AND id IN (" +
                            Joiner.on(",").join(organizationsIds) +
                            ");", Organization.class);

            List<Organization> radiusFilteredOrganization = query.getResultList();

            organizations = radiusFilteredOrganization;

        }

		/* jsonp support */
        if (parameters.containsKey("callback")) {
			/* jsonp support */
            Gson gson = new Gson();
            String out = gson.toJson(organizations);
            renderText(parameters.get("callback") + "(" + out + ")");
        } else {
            renderJSON(organizations);
        }

	}

	/** umoznuje kompletni dynamicky dump vsech dat */

	public static void csvDump() {

		List<Organization> allOrganizations = Cache.get(CSV_DUMP_CACHE_KEY, List.class);

		if (allOrganizations == null) {
			allOrganizations = Organization.findAll();
			Cache.safeSet(CSV_DUMP_CACHE_KEY, allOrganizations, /* expire in */ "12h" /* coz we scrape each 24h */);
		}

		String csv = convertToCsv(allOrganizations);
		response.contentType = "text/csv";
		response.setHeader("Content-Disposition", "attachment; filename=urady.csv");
		renderText(csv);

	}

	private static String convertToCsv(List<Organization> allOrganizations) {

		StringBuilder sb = new StringBuilder();

		addHeader(sb);

		for (Organization urad : allOrganizations) {
			sb.append(lineFor(urad)).append(newLine());
		}

		return sb.toString();

	}

	private static StringBuilder lineFor(Organization urad) {

		StringBuilder sb = new StringBuilder();

		csvMapperMachinery.values().stream()
				          .map((mapperFunction) -> mapperFunction.apply(urad))
						  .forEach((columnValue) -> sb.append(StringUtils.defaultString(columnValue)).append(delimiter()));

		return sb;

	}

	private static void addHeader(StringBuilder sb) {

		String header = csvMapperMachinery.keySet()
				.stream()
				.reduce("", (result, columnName) -> result + columnName + delimiter());

		sb.append(header + newLine());

	}

	private static String delimiter() {
		return ";";
	}

	private static String newLine() {
		return "\n";
	}

	public static void help() {
		render();
	}
}
