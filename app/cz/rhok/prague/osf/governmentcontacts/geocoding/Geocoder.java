/**
 * 
 */
package cz.rhok.prague.osf.governmentcontacts.geocoding;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import cz.rhok.prague.osf.governmentcontacts.helper.RepeatOnTimeoutTask;
import cz.rhok.prague.osf.governmentcontacts.scraper.ApiRequestLimitExceededException;
import models.GeoLocation;
import models.Organization;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import play.Play;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
public class Geocoder {

    private static Logger log = play.Logger.log4j;

	public GeoLocation geocode(final Organization organization) {

        RepeatOnTimeoutTask<GeoLocation> repeatableTask = new RepeatOnTimeoutTask<GeoLocation>() {

            @Override
            public GeoLocation doTask() {
                return doGeoLocation(organization);
            }

        };

        return repeatableTask.call();

	}

    private GeoLocation doGeoLocation(Organization organization) {

        if (organization == null) {
            throw new IllegalArgumentException("Organization cannot be null");
        }

        try {

            log.debug("Geo coding organization: " + organization);

            StringBuilder requestUrl = new StringBuilder();

            requestUrl
                    .append("https://maps.googleapis.com/maps/api/geocode/json?address=")
                    .append(URLEncoder.encode(organization.getAddress(), "utf-8"));

            String apiKey = Play.configuration.getProperty("google.geocoder.api.key");
            if (StringUtils.isNotBlank(apiKey)) {
                requestUrl.append("key=").append(apiKey);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream stream = new URL(requestUrl.toString()).openStream();
            IOUtils.copy(stream, outputStream);

            String responseString = outputStream.toString();

            if ( ! responseString.contains("OK")) {
                log.debug("Response indicating that there is some problem. Response: " + responseString);
            }

            if (responseString.contains("ZERO_RESULTS")) {
                log.warn("Cannot geocode organization: " + organization.name +
                         " (id: " + organization.id + ") with address: " + organization.getAddress());
                return null;
            } else if (responseString.contains("OVER_QUERY_LIMIT")) {
                throw new ApiRequestLimitExceededException();
            }

            int index = responseString.indexOf("\"location\"");
            int begin = responseString.indexOf("{", index);
            int end = responseString.indexOf("}", index);
            String latLngJson = responseString.substring(begin, end + 1);
            Gson gson = new Gson();
            Map<String, Double> jsonObject = gson.fromJson(latLngJson, new TypeToken<Map<String, Double>>() {}.getType());

            GeoLocation result = new GeoLocation();
            result.lat = jsonObject.get("lat");
            result.lng = jsonObject.get("lng");

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
