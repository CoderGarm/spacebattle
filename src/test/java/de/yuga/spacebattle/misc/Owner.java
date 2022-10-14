package de.yuga.spacebattle.misc;

public class Owner {

    private String id;
    private boolean capital = false;


    public Owner(final String id) {
        this.id = id;
    }

    public Owner() {
        this.id = "0";
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public boolean isCapital() {
        return capital;
    }

    public void setCapital(final boolean capital) {
        this.capital = capital;
    }
}
