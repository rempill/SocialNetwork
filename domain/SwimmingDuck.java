package domain;

/**
 * Duck that can swim and is eligible to participate in race events.
 */
public class SwimmingDuck extends Duck implements Inotator {
    /**
     * Construct a swimming duck with speed and endurance attributes.
     *
     * @param id unique id
     * @param username display name
     * @param email email
     * @param password password
     * @param viteza speed value (>0)
     * @param rezistenta endurance value (>0)
     */
    public SwimmingDuck(int id, String username, String email, String password, double viteza, double rezistenta) {
        super(id, username, email, password, TipRata.SWIMMING, viteza, rezistenta);
    }

    public SwimmingDuck(String username, String email, String password, double viteza, double rezistenta) {
        super(username, email, password, TipRata.SWIMMING, viteza, rezistenta);
    }
    @Override
    public void inoata() { /* simple behavior placeholder */ }
}
