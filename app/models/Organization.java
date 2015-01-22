/**
 * 
 */
package models;

import javax.persistence.Entity;
import javax.persistence.Lob;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.annotations.Type;

import play.db.jpa.Model;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
@Entity
public class Organization extends Model {

	public String name;
	
	public String addressStreet;
	public String addressCity;
	public String addressZipCode;

	//TODO: pokud se bude scrapovat kontaktni adresa, tak posefit tady a v helpu
//	public String contactAddress;
	
	public String eRegistry;
	
	/**
	 * IC
	 */
	public String organizationId;
	
	/**
	 * DIC
	 */
	public String taxId;
	
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	public String bankAccount;
	
	public String code;
	
	public String type;
	
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	public String www;
	
	public String email;
	public String emailDescription;

	public String email2;
	public String email2Description;

	public String email3;
	public String email3Description;
	
	public String phone;
	
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	public String officeHours;
	
	public String dataBoxId;

	public Double latitude;
	public Double longitude;

    /**
     * url of page where we scraped all organization's informations
     */
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String urlOfSource;


	public void copyStateFrom(Organization organization) {
		this.name = organization.name;
		this.addressStreet = organization.addressStreet;
		this.addressCity = organization.addressCity;
		this.addressZipCode = organization.addressZipCode;
		this.eRegistry = organization.eRegistry;
		this.organizationId = organization.organizationId;
		this.taxId = organization.taxId;
		this.bankAccount = organization.bankAccount;
		this.code = organization.code;
		this.type = organization.type;
		this.www = organization.www;
		this.email = organization.email;
		this.emailDescription = organization.emailDescription;
		this.email2 = organization.email;
		this.email2Description = organization.emailDescription;
		this.email3 = organization.email;
		this.email3Description = organization.emailDescription;
		this.phone = organization.phone;
		this.officeHours = organization.officeHours;
		this.dataBoxId = organization.dataBoxId;
		this.latitude = organization.latitude;
		this.longitude = organization.longitude;
        this.urlOfSource = organization.urlOfSource;
	}
	
	public String getAddress() {
		return addressStreet + ", " + addressCity + " " + addressZipCode + ", Czech Republic";
	}

	public Organization merge(Organization organization) {

		this.name = StringUtils.defaultIfBlank(this.name, organization.name);
		this.addressStreet = StringUtils.defaultIfBlank(this.addressStreet,organization.addressStreet);
		this.addressCity = StringUtils.defaultIfBlank(this.addressCity, organization.addressCity);
		this.addressZipCode = StringUtils.defaultIfBlank(this.addressZipCode, organization.addressZipCode);
		this.eRegistry = StringUtils.defaultIfBlank(this.eRegistry, organization.eRegistry);
		this.organizationId = StringUtils.defaultIfBlank(this.organizationId, organization.organizationId);
		this.taxId = StringUtils.defaultIfBlank(this.taxId, organization.taxId);
		this.bankAccount = StringUtils.defaultIfBlank(this.bankAccount, organization.bankAccount);
		this.code = StringUtils.defaultIfBlank(this.code, organization.code);
		this.type = StringUtils.defaultIfBlank(this.type, organization.type);
		this.www = StringUtils.defaultIfBlank(this.www, organization.www);
		this.email = StringUtils.defaultIfBlank(this.email, organization.email);
		this.emailDescription = StringUtils.defaultIfBlank(this.emailDescription, organization.emailDescription);
		this.email2 = StringUtils.defaultIfBlank(this.email2, organization.email2);
		this.email2Description = StringUtils.defaultIfBlank(this.email2Description, organization.email2Description);
		this.email3 = StringUtils.defaultIfBlank(this.email3, organization.email3);
		this.email3Description = StringUtils.defaultIfBlank(this.email3Description, organization.email3Description);
		this.phone = StringUtils.defaultIfBlank(this.phone, organization.phone);
		this.officeHours = StringUtils.defaultIfBlank(this.officeHours, organization.officeHours);
		this.dataBoxId = StringUtils.defaultIfBlank(this.dataBoxId, organization.dataBoxId);
		this.latitude = this.latitude == null ? organization.latitude : this.latitude;
		this.longitude = this.longitude == null ? organization.longitude :  this.longitude;
		this.urlOfSource = StringUtils.defaultIfBlank(this.urlOfSource, organization.urlOfSource);

		return this;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public static Organization merge(Organization... organizations) {
		Organization organization = new Organization();

		for (int i = 0; i < organizations.length; i++) {
			organization.merge(organizations[i]);
		}

		return organization;
	}
	
}
