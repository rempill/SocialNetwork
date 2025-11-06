package domain;

/**
 * Abstract Duck user adding performance attributes.
 */
public abstract class Duck extends User {
    protected TipRata tipRata;
    protected double viteza, rezistenta;

    /**
     * Construct a new Duck user.
     *
     * @param id unique identifier
     * @param username display name
     * @param email contact email
     * @param password account password
     * @param tipRata type of the duck (FLYING, SWIMMING, ...)
     * @param viteza speed metric (must be positive)
     * @param rezistenta endurance metric (must be positive)
     */
    public Duck(int id, String username, String email, String password, TipRata tipRata, double viteza, double rezistenta) {
        super(id, username, email, password);
        this.tipRata = tipRata;
        this.viteza = viteza;
        this.rezistenta = rezistenta;
    }

    /**
     * Get the duck type.
     * @return the TipRata of this duck
     */
    public TipRata getTipRata() {
        return tipRata;
    }

    /**
     * Get the speed value.
     * @return speed
     */
    public double getViteza() {
        return viteza;
    }

    /**
     * Get the endurance value.
     * @return endurance
     */
    public double getRezistenta() {
        return rezistenta;
    }
}
