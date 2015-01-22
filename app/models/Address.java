/**
 * 
 */
package models;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
public class Address {

	public String street;
	public String city;
	public String zipCode;

	@Override
	public String toString() {
		return "Address{" +
				"street='" + street + '\'' +
				", city='" + city + '\'' +
				", zipCode='" + zipCode + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Address address = (Address) o;

		if (city != null ? !city.equals(address.city) : address.city != null) return false;
		if (street != null ? !street.equals(address.street) : address.street != null) return false;
		if (zipCode != null ? !zipCode.equals(address.zipCode) : address.zipCode != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = street != null ? street.hashCode() : 0;
		result = 31 * result + (city != null ? city.hashCode() : 0);
		result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
		return result;
	}
}
