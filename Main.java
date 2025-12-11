import UI.ConsoleUI;
import UI.LoginController;
import domain.Duck;
import domain.Persoana;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import repo.*;
import service.NetworkService;
import validator.DuckValidator;
import validator.PersoanaValidator;
import validator.ValidationStrategy;
import UI.Controller;

/**
 * Application entry point for the social-network-rempill project.
 *
 * This class initializes the repository, validators and service layer,
 * then starts the console user interface. It now supports choosing a Postgres
 * repository via environment variables DB_URL, DB_USER, DB_PASSWORD (or system properties
 * db.url, db.user, db.password). If not all are present, it falls back to in-memory storage.
 */
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        String dbUrl = "jdbc:postgresql://localhost:5432/duck_social_network";
        String dbUser = "postgres";
        String dbPass = "mihai222";

        UserRepository userRepository = new PostgresUserRepository(dbUrl, dbUser, dbPass);
        CardRepository cardRepository = new PostgresCardRepository(dbUrl, dbUser, dbPass, userRepository);
        EventRepository eventRepository = new PostgresEventRepository(dbUrl, dbUser, dbPass, userRepository);
        MessageRepository messageRepository = new PostgresMessageRepository(dbUrl, dbUser, dbPass, userRepository);

        ValidationStrategy<Persoana>  persoanaValidator=new PersoanaValidator();
        ValidationStrategy<Duck>  duckValidator=new DuckValidator();

        NetworkService service= new NetworkService(userRepository, eventRepository, persoanaValidator, duckValidator, cardRepository, messageRepository);

        Controller controller = new Controller();
        controller.setService(service);

        BorderPane root = controller.buildView();

        LoginController loginController = new LoginController(service, controller, stage, root);
        loginController.show();
    }

    public static void main(String[] args) {
        launch();
    }

//    /**
//     * Start the application.
//     *
//     * @param args command-line arguments (ignored)
//     */
//    public static void main(String[] args) {
//        String dbUrl = "jdbc:postgresql://localhost:5432/duck_social_network";
//        String dbUser = "postgres";
//        String dbPass = "mihai222";
//
//        UserRepository userRepository = new PostgresUserRepository(dbUrl, dbUser, dbPass);
//        CardRepository cardRepository = new PostgresCardRepository(dbUrl, dbUser, dbPass, userRepository);
//        EventRepository eventRepository = new PostgresEventRepository(dbUrl, dbUser, dbPass, userRepository);
//
//        ValidationStrategy<Persoana>  persoanaValidator=new PersoanaValidator();
//        ValidationStrategy<Duck>  duckValidator=new DuckValidator();
//
//        NetworkService service= new NetworkService(userRepository, eventRepository, persoanaValidator, duckValidator, cardRepository);
//
//        ConsoleUI ui=new ConsoleUI(service);
//
//        ui.run();
//    }
}