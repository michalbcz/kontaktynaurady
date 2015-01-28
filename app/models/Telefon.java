package models;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;


@Embeddable
public class Telefon {

    @Column(name = "telefonni_cislo")
    public String telCislo;

    @Column(name = "telefonni_cislo_popis")
    public String typTelCisla;

    /**
     * slouzi pro snazsi debug pripadne jako zaklad budouciho davkoveho zpracovani nascrapovanych dat na pozadi
     */
    @Column(name = "telefonni_cislo_raw")
    public String originalniTextTelCisla;

    @Override
    public String toString() {
        return "Telefon{" +
                "telCislo='" + telCislo + '\'' +
                ", typTelCisla='" + typTelCisla + '\'' +
                ", originalniTextTelCisla='" + originalniTextTelCisla + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Telefon telefon = (Telefon) o;

        if (originalniTextTelCisla != null ? !originalniTextTelCisla.equals(telefon.originalniTextTelCisla) : telefon.originalniTextTelCisla != null)
            return false;
        if (telCislo != null ? !telCislo.equals(telefon.telCislo) : telefon.telCislo != null) return false;
        if (typTelCisla != null ? !typTelCisla.equals(telefon.typTelCisla) : telefon.typTelCisla != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = telCislo != null ? telCislo.hashCode() : 0;
        result = 31 * result + (typTelCisla != null ? typTelCisla.hashCode() : 0);
        result = 31 * result + (originalniTextTelCisla != null ? originalniTextTelCisla.hashCode() : 0);
        return result;
    }
}
