package cz.rhok.prague.osf.governmentcontacts.helper;

import models.Organization;
import models.Person;
import org.apache.commons.lang.builder.ToStringBuilder;
import play.Logger;
import play.db.jpa.JPA;

import java.util.Iterator;

public class OrganizationHelper {

    public static Organization saveOrganization(Organization organization) {
        Logger.debug("Saving organization %s", ToStringBuilder.reflectionToString(organization));

        Organization existingOrganization = Organization.find("byDataBoxId", organization.dataBoxId).first();

        if (existingOrganization != null) {

            organization.id = existingOrganization.id;

            // smazeme vsechny puvodni kontaktni osoby a budou zcela nahrazeny nove nascrapovanyma kontaktnima osobama
            existingOrganization.contactPersons.stream().forEach((person) -> person.delete() );
        }

        Organization savedOrganization = organization.merge(); // attachneme organizaci pokud je potreba
        savedOrganization.save(); // a pak ji ulozime (insert or update)

        return savedOrganization;
    }
}
