package domain;

/**
 * Duck that can fly; not eligible for swim races by itself.
 */
public class FlyingDuck extends Duck implements Zburator {
    /**
     * Construct a flying duck with speed and endurance attributes.
     *
     * @param id unique id
     * @param username display name
     * @param email email
     * @param password password
     * @param viteza speed value (>0)
     * @param rezistenta endurance value (>0)
     */
    public FlyingDuck(int id, String username, String email, String password, double viteza, double rezistenta) {
        super(id, username, email, password, TipRata.FLYING, viteza, rezistenta);
    }

    public FlyingDuck(String username, String email, String password, double viteza, double rezistenta) {
        super(username, email, password, TipRata.FLYING, viteza, rezistenta);
    }
    @Override
    public void zboara() { /* simple behavior placeholder */ }
}
