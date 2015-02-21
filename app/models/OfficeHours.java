package models;



public class OfficeHours {

    public String day;

    public String timeInterval;

    @Override
    public String toString() {
        return day + ": " + timeInterval;
    }

}
