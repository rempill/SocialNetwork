package domain;

public class Duck extends User{
    private TipRata tipRata;
    private double viteza,rezistenta;

    public Duck(int id, String username, String email, String password, TipRata tipRata, double viteza, double rezistenta) {
        super(id, username, email, password);
        this.tipRata = tipRata;
        this.viteza = viteza;
        this.rezistenta = rezistenta;
    }

    public TipRata getTipRata() {
        return tipRata;
    }
    public double getViteza() {
        return viteza;
    }
    public double getRezistenta() {
        return rezistenta;
    }
}
