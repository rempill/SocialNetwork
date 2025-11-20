import UI.ConsoleUI;
import domain.Duck;
import domain.Persoana;
import repo.EventRepository;
import repo.UserRepository;
import repo.PostgresUserRepository;
import repo.PostgresEventRepository;
import service.NetworkService;
import validator.DuckValidator;
import validator.PersoanaValidator;
import validator.ValidationStrategy;

/**
 * Application entry point for the social-network-rempill project.
 *
 * This class initializes the repository, validators and service layer,
 * then starts the console user interface. It now supports choosing a Postgres
 * repository via environment variables DB_URL, DB_USER, DB_PASSWORD (or system properties
 * db.url, db.user, db.password). If not all are present, it falls back to in-memory storage.
 */
public class Main {
    /**
     * Start the application.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        String dbUrl = "jdbc:postgresql://localhost:5432/duck_social_network";
        String dbUser = "postgres";
        String dbPass = "mihai222";

        UserRepository userRepository=new PostgresUserRepository(dbUrl, dbUser, dbPass);
        EventRepository eventRepository = new PostgresEventRepository(dbUrl, dbUser, dbPass);

        ValidationStrategy<Persoana>  persoanaValidator=new PersoanaValidator();
        ValidationStrategy<Duck>  duckValidator=new DuckValidator();

        NetworkService service= new NetworkService(userRepository, eventRepository, persoanaValidator, duckValidator);

        ConsoleUI ui=new ConsoleUI(service);
        ui.run();
    }
}