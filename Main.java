import UI.ConsoleUI;
import domain.Duck;
import domain.Persoana;
import repo.InMemoryUserRepository;
import repo.UserRepository;
import service.NetworkService;
import validator.DuckValidator;
import validator.PersoanaValidator;
import validator.ValidationStrategy;

public class Main {
    public static void main(String[] args) {
        UserRepository userRepository = new InMemoryUserRepository();

        ValidationStrategy<Persoana>  persoanaValidator=new PersoanaValidator();
        ValidationStrategy<Duck>  duckValidator=new DuckValidator();

        NetworkService service= new NetworkService(userRepository, persoanaValidator, duckValidator);

        ConsoleUI ui=new ConsoleUI(service);
        ui.run();
    }
}