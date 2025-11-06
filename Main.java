import UI.ConsoleUI;
import domain.Duck;
import domain.Persoana;
import repo.InMemoryEventRepository;
import repo.InMemoryUserRepository;
import repo.EventRepository;
import repo.UserRepository;
import service.NetworkService;
import validator.DuckValidator;
import validator.PersoanaValidator;
import validator.ValidationStrategy;

/**
 * Application entry point for the social-network-rempill project.
 *
 * This class initializes the repository, validators and service layer,
 * then starts the console user interface. Keep this class minimal so it
 * can be used as a simple runner from the IDE or command line.
 */
public class Main {
    /**
     * Start the application.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        UserRepository userRepository = new InMemoryUserRepository();
        EventRepository eventRepository = new InMemoryEventRepository();

        ValidationStrategy<Persoana>  persoanaValidator=new PersoanaValidator();
        ValidationStrategy<Duck>  duckValidator=new DuckValidator();

        NetworkService service= new NetworkService(userRepository, eventRepository, persoanaValidator, duckValidator);

        ConsoleUI ui=new ConsoleUI(service);
        ui.run();
    }
}