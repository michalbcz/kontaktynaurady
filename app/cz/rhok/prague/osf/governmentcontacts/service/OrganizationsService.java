package cz.rhok.prague.osf.governmentcontacts.service;


import models.Organization;
import models.Person;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class OrganizationsService {

    public static final String CSV_DUMP_CACHE_KEY = "csvDump";

    private static final Map<String, Function<Organization, String>> csvMapperMachinery = new LinkedHashMap<String, Function<Organization, String>>() {{

//		put("id", (urad) -> urad.id.toString());
        put("uredni_nazev", (urad) -> urad.name);
        put("nazev_obce", (urad) -> urad.addressCity);
        put("adresa_ulice", (urad) -> urad.addressStreet);
        put("adresa_obec", (urad) -> urad.addressCity);
        put("adresa_psc", (urad) -> urad.addressZipCode);
        put("telefon", (urad) -> urad.telefon == null ? "" :  urad.telefon.telCislo);
        put("telefon_typ", (urad) -> urad.telefon == null ? "" : urad.telefon.typTelCisla);
        put("starosta_jmeno", (urad) -> starosta(urad).getCeleJmeno());
        put("starosta_email", (urad) -> starosta(urad).email == null ? "" : starosta(urad).email.uri);
        put("starosta_telefon", (urad) -> starosta(urad).telefon == null ? "" : starosta(urad).telefon);
        put("id_datove_schranky", (urad) -> urad.dataBoxId);
        put("webova_stranka_uradu", (urad) -> urad.www);
        put("ic", (urad) -> urad.organizationId);
        put("dic", (urad) -> urad.taxId);
        put("email1", (urad) -> urad.email);
        put("email1_popis", (urad) -> urad.emailDescription);
        put("email2", (urad) -> urad.email2);
        put("email2_popis", (urad) -> urad.email2Description);
        put("email3", (urad) -> urad.email3);
        put("email3_popis", (urad) -> urad.email3Description);
        put("typ_obce", (urad) -> urad.type);
        put("zdroj_dat", (urad) -> urad.urlOfSource);

    }};

    public static String getCsvDump() {
        String csv = Cache.get(CSV_DUMP_CACHE_KEY, String.class);

        if(csv == null) {
            List<Organization> allOrganizations = getCsvDumpFromDb();
            csv = convertToCsv(allOrganizations);
            putCsvDumpToCache(csv);
        }

        return csv;
    }

    private static void putCsvDumpToCache(String csv) {
        Cache.safeSet(CSV_DUMP_CACHE_KEY, csv, /* expire in */ "12h" /* coz we scrape each 24h */);
    }

    public static void refreshCacheForCsvDump() {
        List<Organization> allOrganizations = getCsvDumpFromDb();
        String csv = convertToCsv(allOrganizations);
        putCsvDumpToCache(csv);
    }

    private static List<Organization> getCsvDumpFromDb() {
        List<Organization> allOrganizations = Organization.findAll();
        return allOrganizations;
    }

    private static String convertToCsv(List<Organization> allOrganizations) {

        StringBuilder sb = new StringBuilder();

        addHeader(sb);

        for (Organization urad : allOrganizations) {
            sb.append(lineFor(urad)).append(newLine());
        }

        return sb.toString();

    }

    private static String lineFor(Organization urad) {

        StringBuilder sb = new StringBuilder();

        csvMapperMachinery.values().stream()
                .map((mapperFunction) -> mapperFunction.apply(urad))
                .map((String hodnotaSloupce) -> escapeCsvColumnValue(hodnotaSloupce))
                .forEach((columnValue) -> sb.append(StringUtils.defaultString(columnValue)).append(delimiter()));

        return sb.toString();

    }

    private static String escapeCsvColumnValue(String hodnotaSloupce) {
        String escaped = hodnotaSloupce;
        escaped = StringUtils.trim(escaped);
        escaped = escaped == null ? "" : escaped.replaceAll("[\\n\\r]", " ");

        return escaped;
    }

    private static String delimiter() {
        return ";";
    }

    private static String newLine() {
        return "\n";
    }


    public static void addHeader(StringBuilder sb) {

        String header =
                csvMapperMachinery.keySet().stream()
                                  .reduce("", (result, columnName) -> result + columnName + delimiter());

        sb.append(header + newLine());

    }

    private static Person starosta(Organization urad) {
        return urad.contactPersons.stream().filter(starosta()).findFirst().orElseGet(Person::new);
    }

    private static Predicate<? super Person> starosta() {
        return (person) -> person.funkce.equalsIgnoreCase("starosta");
    }


}
