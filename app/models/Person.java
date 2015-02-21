package models;


import com.google.gson.annotations.Expose;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;

import javax.persistence.*;

@Entity
@Table(name = "kontaktni_osoby" /*, uniqueConstraints = { @UniqueConstraint(columnNames = {"email", "urad"}) }*/)
public class Person extends Model {

    @Expose
    public String jmeno;

    @Expose
    public String prijmeni;

    @Expose
    public String typOsoby;

    @Expose
    public String funkce;

    @Embedded @Expose
    public Email email;

    @Expose
    public String telefon;

    @ManyToOne
    @JoinColumn(name = "urad_id")
    public Organization urad;

    public String getCeleJmeno() {
//
//        if (jmeno != null || prijmeni != null) {
//            return StringUtils.defaultString(jmeno) + " " + StringUtils.defaultString(prijmeni);
//        } else {
//            return "";
//        }

        return StringUtils.join(new Object[] {jmeno, prijmeni}, " ");


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (email != null ? !email.equals(person.email) : person.email != null) return false;
        if (funkce != null ? !funkce.equals(person.funkce) : person.funkce != null) return false;
        if (jmeno != null ? !jmeno.equals(person.jmeno) : person.jmeno != null) return false;
        if (prijmeni != null ? !prijmeni.equals(person.prijmeni) : person.prijmeni != null) return false;
        if (telefon != null ? !telefon.equals(person.telefon) : person.telefon != null) return false;
        if (typOsoby != null ? !typOsoby.equals(person.typOsoby) : person.typOsoby != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jmeno != null ? jmeno.hashCode() : 0;
        result = 31 * result + (prijmeni != null ? prijmeni.hashCode() : 0);
        result = 31 * result + (typOsoby != null ? typOsoby.hashCode() : 0);
        result = 31 * result + (funkce != null ? funkce.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (telefon != null ? telefon.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Person{" +
                "jmeno='" + jmeno + '\'' +
                ", prijmeni='" + prijmeni + '\'' +
                ", typOsoby='" + typOsoby + '\'' +
                ", funkce='" + funkce + '\'' +
                ", email=" + email +
                ", telefon='" + telefon + '\'' +
                '}';
    }

}
