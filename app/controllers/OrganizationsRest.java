/**
 *
 */
package controllers;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.persistence.Query;

import com.google.gson.ExclusionStrategy;
import com.google.gson.GsonBuilder;
import cz.rhok.prague.osf.governmentcontacts.service.OrganizationsService;
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

	private static final Gson gson =
								new GsonBuilder().setPrettyPrinting()
												 .excludeFieldsWithoutExposeAnnotation()
												 .disableHtmlEscaping() /* jinak by se escapovalo url zdroje, ktere by pak nefungovalo */
												 .serializeNulls()
												 .create();

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


        if (parameters.containsKey("callback")) {
			/* jsonp support */
            String out = gson.toJson(organizations);
            renderText(parameters.get("callback") + "(" + out + ")");
        } else {
			String json = gson.toJson(organizations);
            renderJSON(json);
        }

	}

	/** umoznuje kompletni dynamicky dump vsech dat */

	public static void csvDump() {

		String csv = OrganizationsService.getCsvDump();

		response.contentType = "text/csv";
		response.setHeader("Content-Disposition", "attachment; filename=urady.csv");
		renderText(csv);

	}



	public static void help() {
		render();
	}
}
