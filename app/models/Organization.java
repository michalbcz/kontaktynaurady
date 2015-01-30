/**
 * 
 */
package models;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * @author Vlastimil Dolejs (vlasta.dolejs@gmail.com)
 *
 */
@Entity
@javax.persistence.Table(name = "urady")
public class Organization extends Model {

	@Column(name = "nazev")
	public String name;
	
	public String addressStreet;
	public String addressCity;
	public String addressZipCode;

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
	
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	public String officeHours;
	
	public String dataBoxId;

	public Double latitude;
	public Double longitude;



	// michal 28.01.2015: z nejakeho duvodu attribute overrides nefunguje...upravim jako hack nazvy sloupcu primo v definici tridy Telefon
//	@AttributeOverrides({
//			@AttributeOverride(name = "cislo", column = @Column(name = "telefonni_cislo")),
//			@AttributeOverride(name = "typ", column = @Column(name = "telefonni_cislo_popis")),
//			@AttributeOverride(name = "puvodni_text", column = @Column(name = "telefonni_cislo_raw"))
//	})
	@Embedded /* michal: pozor prekvapko - CRUD modul neumi Embedded, takze v CRUD adminovy nebudou videt ale v DB budou */
	public Telefon telefon = new Telefon();

	/**
     * url of page where we scraped all organization's informations
     */
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String urlOfSource;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = Person.class, mappedBy = "urad")
	public Set<Person> contactPersons = Sets.newHashSet();

	public void addPerson(Person person) {
		person.urad = this;
		contactPersons.add(person);
	}

	public String getAddress() {
		return addressStreet + ", " + addressCity + " " + addressZipCode + ", Czech Republic";
	}

	/**
	 *
	 *
	 * @param organization
	 * @return
	 */
	public Organization fillBlankFields(Organization organization) {

		this.id = this.id == null ? organization.id : this.id;
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
		this.officeHours = StringUtils.defaultIfBlank(this.officeHours, organization.officeHours);
		this.dataBoxId = StringUtils.defaultIfBlank(this.dataBoxId, organization.dataBoxId);
		this.latitude = this.latitude == null ? organization.latitude : this.latitude;
		this.longitude = this.longitude == null ? organization.longitude :  this.longitude;
		this.urlOfSource = StringUtils.defaultIfBlank(this.urlOfSource, organization.urlOfSource);
		this.telefon = this.telefon == null ? organization.telefon : this.telefon;

		return this;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public static Organization merge(Organization... organizations) {
		Organization organization = new Organization();

		for (int i = 0; i < organizations.length; i++) {
			organization.fillBlankFields(organizations[i]);
		}

		return organization;
	}

}
