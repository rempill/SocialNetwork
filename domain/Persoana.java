package domain;
import java.time.LocalDate;
public class Persoana extends User{
    private String nume,prenume,ocupatie;
    private LocalDate dataNasterii;
    private int nivelEmpatie; // 1-10

    public Persoana(int id, String username, String email, String password, String nume, String prenume, String ocupatie, LocalDate dataNasterii, int nivelEmpatie) {
        super(id, username, email, password);
        this.nume = nume;
        this.prenume = prenume;
        this.ocupatie = ocupatie;
        this.dataNasterii = dataNasterii;
        this.nivelEmpatie = nivelEmpatie;
    }

    public String getNume() {
        return nume;
    }
    public String getPrenume() {
        return prenume;
    }
    public String getOcupatie() {
        return ocupatie;
    }
    public LocalDate getDataNasterii() {
        return dataNasterii;
    }
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
