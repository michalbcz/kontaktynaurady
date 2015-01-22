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

            // solve duplicates in contact persons
            Iterator<Person> contactPersonsIter = organization.contactPersons.iterator();
            while (contactPersonsIter.hasNext()) {

                Person person = contactPersonsIter.next();

                //FIXME: tohle nepomaha a porad se duplikuji osoby

                if (existingOrganization.contactPersons.contains(person)) {
                    // already contains so remove it from organization before merging
                    contactPersonsIter.remove();
                }
            }

        }

        Organization savedOrganization = organization.merge();

        return savedOrganization;
    }
}
