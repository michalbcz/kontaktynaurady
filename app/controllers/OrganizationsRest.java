/**
 * 
 */
package controllers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import models.Organization;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.JPA;
import play.mvc.Controller;

import com.google.common.collect.Lists;

import javax.persistence.Query;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
public class OrganizationsRest extends Controller {

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

            if (StringUtils.containsIgnoreCase(fieldName, "radius")
                    || StringUtils.containsIgnoreCase(fieldName, "callback")) {
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
            Gson gson = new Gson();
            String out = gson.toJson(organizations);
            renderText(parameters.get("callback") + "(" + out + ")");
        } else {
            renderJSON(organizations);
        }

	}
	
	public static void help() {
		render();
	}
}
