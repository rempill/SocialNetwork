package domain;

/**
 * Duck that can both swim and fly; eligible for race events via its swimming capability.
 */
public class AmphibiousDuck extends Duck implements Inotator, Zburator {
    /**
     * Construct an amphibious duck with speed and endurance attributes.
     *
     * @param id unique id
     * @param username display name
     * @param email email
     * @param password password
     * @param viteza speed value (>0)
     * @param rezistenta endurance value (>0)
     */
    public AmphibiousDuck(int id, String username, String email, String password, double viteza, double rezistenta) {
        super(id, username, email, password, TipRata.FLYING_AND_SWIMMING, viteza, rezistenta);
    }
    @Override
    public void inoata() { }
    @Override
    public void zboara() { }
}
