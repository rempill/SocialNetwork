package domain;
import java.time.LocalDate;

/**
 * Persoana is a concrete {@link User} representing a human person with
 * personal attributes such as name, occupation, date of birth and empathy level.
 */
public class Persoana extends User{
    private String nume,prenume,ocupatie;
    private LocalDate dataNasterii;
    private int nivelEmpatie; // 1-10

    /**
     * Create a new Persoana.
     *
     * @param id unique identifier
     * @param username display username
     * @param email contact email
     * @param password account password
     * @param nume family name
     * @param prenume given name
     * @param ocupatie occupation description
     * @param dataNasterii date of birth
     * @param nivelEmpatie empathy level in range 1..10
     */
    public Persoana(int id, String username, String email, String password, String nume, String prenume, String ocupatie, LocalDate dataNasterii, int nivelEmpatie) {
        super(id, username, email, password);
        this.nume = nume;
        this.prenume = prenume;
        this.ocupatie = ocupatie;
        this.dataNasterii = dataNasterii;
        this.nivelEmpatie = nivelEmpatie;
    }

    /**
     * Get family name.
     * @return family name
     */
    public String getNume() {
        return nume;
    }
    /**
     * Get given name.
     * @return given name
     */
    public String getPrenume() {
        return prenume;
    }
    /**
     * Get occupation.
     * @return occupation
     */
    public String getOcupatie() {
        return ocupatie;
    }
    /**
     * Get date of birth.
     * @return birth date
     */
    public LocalDate getDataNasterii() {
        return dataNasterii;
    }
    /**
     * Get empathy level.
     * @return empathy level (1-10)
     */
    public int getNivelEmpatie() {
        return nivelEmpatie;
    }
    @Override
    public String toString() {
        return "Persoana{" +
                "nume='" + nume + '\'' +
                ", prenume='" + prenume + '\'' +
                ", ocupatie='" + ocupatie + '\'' +
                ", dataNasterii=" + dataNasterii +
                ", nivelEmpatie=" + nivelEmpatie +
                ", id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
