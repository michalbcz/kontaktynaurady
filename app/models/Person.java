package models;


import play.db.jpa.Model;

import javax.persistence.*;

@Entity
@Table(name = "kontaktni_osoby" /*, uniqueConstraints = { @UniqueConstraint(columnNames = {"email", "urad"}) }*/)
public class Person extends Model {

    public String jmeno;
    public String prijmeni;

    public String typOsoby;

    public String funkce;

    @Embedded
    public Email email;

    public String telefon;

    @ManyToOne
    public Organization urad;

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
