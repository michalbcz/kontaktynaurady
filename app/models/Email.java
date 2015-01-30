package models;


import com.google.gson.annotations.Expose;
import play.db.jpa.Model;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Embeddable
public class Email {

    /**
     * validni email adresa
     * michal@bernhard.cz
     */
    @Expose
    public String uri;

    /**
     * originalni text emailu tak jak je na strankach
     */
    @Expose
    public String originalEmailText;

    /**
     * textovy popisek co je to za mail (jaky ma ucel)
     *
     * "osobni email"
     */
    @Expose
    public String description;

    @Override
    public String toString() {
        return "Email{" +
                "uri='" + uri + '\'' +
                ", originalEmailText='" + originalEmailText + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Email email = (Email) o;

        if (description != null ? !description.equals(email.description) : email.description != null) return false;
        if (originalEmailText != null ? !originalEmailText.equals(email.originalEmailText) : email.originalEmailText != null)
            return false;
        if (uri != null ? !uri.equals(email.uri) : email.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (originalEmailText != null ? originalEmailText.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
