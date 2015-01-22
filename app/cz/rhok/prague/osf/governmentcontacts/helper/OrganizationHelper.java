package cz.rhok.prague.osf.governmentcontacts.helper;

import models.Organization;
import org.apache.commons.lang.builder.ToStringBuilder;
import play.Logger;
import play.db.jpa.JPA;

public class OrganizationHelper {

    public static Organization saveOrganization(Organization organization) {
        Logger.debug("Saving organization %s", ToStringBuilder.reflectionToString(organization));

        Organization existingOrganization = Organization.find("byDataBoxId", organization.dataBoxId).first();

        if (existingOrganization != null) {
            organization.id = existingOrganization.id;
        }

        Organization savedOrganization = organization.merge();

        return savedOrganization;
    }
}
