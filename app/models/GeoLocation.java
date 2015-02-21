/**
 * 
 */
package models;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
public class GeoLocation {

	public Double lat;
	public Double lng;

	@Override
	public String toString() {
		return "GeoLocation{" +
				"lat=" + lat +
				", lng=" + lng +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GeoLocation that = (GeoLocation) o;

		if (lat != null ? !lat.equals(that.lat) : that.lat != null) return false;
		if (lng != null ? !lng.equals(that.lng) : that.lng != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = lat != null ? lat.hashCode() : 0;
		result = 31 * result + (lng != null ? lng.hashCode() : 0);
		return result;
	}
}
