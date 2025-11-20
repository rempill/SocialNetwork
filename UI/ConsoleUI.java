package UI;

import domain.*;
import errors.RepoError;
import errors.ValidationError;
import service.NetworkService;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Simple console-based user interface for interacting with the NetworkService.
 * Provides commands to add/remove users, manage friendships and display
 * network-related information.
 */
public class ConsoleUI {
    private NetworkService service;
    private Scanner scanner;

    /**
     * Create a ConsoleUI bound to the provided {@link NetworkService}.
     *
     * @param service service layer used to perform operations
     */
    public ConsoleUI(NetworkService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Run the interactive console loop. This method blocks and reads user input
     * from standard input until the user chooses to exit.
     */
    public void run() {
        //addTestData();
        while (true) {
            printMenu();
            System.out.print("> ");
            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1":
                        addUser();
                        break;
                    case "2":
                        removeUser();
                        break;
                    case "3":
                        addFriend();
                        break;
                    case "4":
                        removeFriend();
                        break;
                    case "5":
                        showAllUsers();
                        break;
                    case "6":
                        showCommunities();
                        break;
                    case "7":
                        showMostSocial();
                        break;
                    case "8":
                        createCard();
                        break;
                    case "9":
                        listCards();
                        break;
                    case "10":
                        addDuckToCard();
                        break;
                    case "11":
                        removeDuckFromCard();
                        break;
                    case "12":
                        showCardPerformance();
                        break;
                    case "13":
                        createRaceEvent();
                        break;
                    case "14":
                        subscribeToEvent();
                        break;
                    case "15":
                        runRaceEvent();
                        break;
                    case "16":
                        showEventNotifications();
                        break;
                    case "17":
                        showUserNotifications();
                        break;
                    case "0":
                        System.out.println("Ieșire...");
                        return;
                    default:
                        System.out.println("Opțiune invalidă!");
                }
            } catch (ValidationError | RepoError | NumberFormatException e) {
                System.err.println("Eroare de service: " + e.getMessage());
            } catch (DateTimeException e) {
                System.err.println("Eroare de format data: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Eroare de argument: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Eroare neașteptată: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("---");
        }
    }

    private void printMenu() {
        System.out.println("1. Adaugă utilizator");
        System.out.println("2. Șterge utilizator");
        System.out.println("3. Adaugă prietenie");
        System.out.println("4. Șterge prietenie");
        System.out.println("5. Afișează toți utilizatorii");
        System.out.println("6. Afișează numărul de comunități");
        System.out.println("7. Afișează cea mai sociabilă comunitate");
        System.out.println("8. Creează card (flock) de rațe");
        System.out.println("9. Listează cardurile");
        System.out.println("10. Adaugă rață în card");
        System.out.println("11. Elimină rață din card");
        System.out.println("12. Afișează performanța medie a cardului");
        System.out.println("13. Creează RaceEvent");
        System.out.println("14. Abonare utilizator la un eveniment");
        System.out.println("15. Rulează RaceEvent și afișează rezultate");
        System.out.println("16. Vezi notificări eveniment");
        System.out.println("17. Vezi notificările unui utilizator");
        System.out.println("0. Ieșire");
    }

    private void addUser() {
        System.out.print("Tip (persoana/swimduck/flyduck/amphduck): ");
        String type = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password (min 8 characters): ");
        String password = scanner.nextLine();

        User user;
        List<String> duckTypes = Arrays.asList("swimduck", "flyduck", "amphduck");
        if (type.equalsIgnoreCase("persoana")) {
            System.out.print("Nume: ");
            String nume = scanner.nextLine();
            System.out.print("Prenume: ");
            String prenume = scanner.nextLine();
            System.out.print("Ocupatie: ");
            String ocupatie = scanner.nextLine();
            System.out.print("Data nasterii (YYYY-MM-DD): ");
            LocalDate dataNasterii = LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ISO_LOCAL_DATE);
            System.out.print("Nivel empatie (1-10): ");
            int nivelEmpatie = Integer.parseInt(scanner.nextLine());
            user = new Persoana(username, email, password, nume, prenume, ocupatie, dataNasterii, nivelEmpatie);
        } else if (duckTypes.contains(type.toLowerCase())) {
            System.out.print("Viteza (double): ");
            double viteza = Double.parseDouble(scanner.nextLine());
            System.out.print("Rezistenta (double): ");
            double rezistenta = Double.parseDouble(scanner.nextLine());
            switch (type.toLowerCase()) {
                case "swimduck":
                    user = new SwimmingDuck(username, email, password, viteza, rezistenta);
                    break;
                case "flyduck":
                    user = new FlyingDuck(username, email, password, viteza, rezistenta);
                    break;
                case "amphduck":
                    user = new AmphibiousDuck(username, email, password, viteza, rezistenta);
                    break;
                default:
                    System.out.println("Tip necunoscut!");
                    return;
            }
        } else {
            System.out.println("Tip necunoscut!");
            return;
        }
        service.addUser(user);
        System.out.println("Utilizator adăugat cu succes.");
    }

    private void removeUser() {
        System.out.print("ID-ul utilizatorului de șters: ");
        int id = Integer.parseInt(scanner.nextLine());
        User removed = service.removeUser(id);
        System.out.println("Utilizatorul " + removed.getUsername() + " a fost șters cu succes.");
    }

    private void addFriend() {
        System.out.print("ID-ul primului utilizator: ");
        int id1 = Integer.parseInt(scanner.nextLine());
        System.out.print("ID-ul celui de-al doilea utilizator: ");
        int id2 = Integer.parseInt(scanner.nextLine());
        service.addFriendship(id1, id2);
        System.out.println("Prietenie adăugată cu succes între utilizatorii " + id1 + " și " + id2 + ".");
    }

    private void removeFriend() {
        System.out.print("ID-ul primului utilizator: ");
        int id1 = Integer.parseInt(scanner.nextLine());
        System.out.print("ID-ul celui de-al doilea utilizator: ");
        int id2 = Integer.parseInt(scanner.nextLine());
        service.removeFriendship(id1, id2);
        System.out.println("Prietenia dintre utilizatorii " + id1 + " și " + id2 + " a fost ștearsă cu succes.");
    }

    private void showAllUsers() {
        System.out.println("Utilizatorii retelei:");
        for (User u : service.getAllUsers()) {
            System.out.println(u.getId() + ": " + u.getUsername() + " " + u.getFriends());
        }
    }

    private void showCommunities() {
        System.out.println("Numarul de comunitati (componente conexe): " + service.getNumberOfCommunities());
    }

    private void showMostSocial() {
        System.out.println("Cea mai sociabila comunitate este:");
        List<User> community = service.getMostSocialCommunity();
        for (User u : community) {
            System.out.println(" - " + u.getUsername());
        }
    }

    // CARD UI
    private void createCard() {
        System.out.print("Nume card: ");
        String nume = scanner.nextLine();
        Card c = service.createCard(nume);
        System.out.println("Card creat: " + c.getId() + " - " + c.getNumeCard());
    }

    private void listCards() {
        System.out.println("Carduri:");
        for (Card c : service.getAllCards()) {
            System.out.println(c.getId() + ": " + c.getNumeCard() + " membri=" + c.getMembri().size() + " perfMedie=" + String.format("%.2f", c.getPerformantaMedie()));
        }
    }

    private void addDuckToCard() {
        System.out.print("ID card: ");
        int cardId = Integer.parseInt(scanner.nextLine());
        System.out.print("ID duck: ");
        int duckId = Integer.parseInt(scanner.nextLine());
        service.addDuckToCard(cardId, duckId);
        System.out.println("Rață adăugată în card.");
    }

    private void removeDuckFromCard() {
        System.out.print("ID card: ");
        int cardId = Integer.parseInt(scanner.nextLine());
        System.out.print("ID duck: ");
        int duckId = Integer.parseInt(scanner.nextLine());
        service.removeDuckFromCard(cardId, duckId);
        System.out.println("Rață eliminată din card.");
    }

    private void showCardPerformance() {
        System.out.print("ID card: ");
        int cardId = Integer.parseInt(scanner.nextLine());
        double perf = service.getCardPerformantaMedie(cardId);
        System.out.println("Performanță medie card: " + String.format("%.3f", perf));
    }

    // EVENT UI
    private void createRaceEvent() {
        System.out.print("Nume cursă: ");
        String name = scanner.nextLine();
        System.out.print("Număr de linii (M): ");
        int lanes = Integer.parseInt(scanner.nextLine());
        RaceEvent ev = service.createRaceEvent(name, lanes);
        System.out.println("RaceEvent creat: " + name + " (#" + ev.getId() + ") cu " + lanes + " linii.");
    }

    private void subscribeToEvent() {
        System.out.print("ID eveniment: ");
        int eventId = Integer.parseInt(scanner.nextLine());
        System.out.print("ID utilizator pentru abonare: ");
        int userId = Integer.parseInt(scanner.nextLine());
        service.subscribeToEvent(eventId, userId);
        System.out.println("Utilizator abonat.");
    }

    /**
     * Prompt for an event id, display the number of lanes (M), optionally accept
     * user-provided per-lane distances and run the race, printing the results.
     */
    private void runRaceEvent() {
        System.out.print("ID eveniment (RaceEvent): ");
        int eventId = Integer.parseInt(scanner.nextLine());
        // Show lane count to help the user provide correct distances
        int lanes = -1;
        for (Event e : service.getAllEvents()) {
            if (e.getId() == eventId && e instanceof RaceEvent) {
                lanes = ((RaceEvent) e).getLanes();
                break;
            }
        }
        if (lanes > 0) {
            System.out.println("Evenimentul are " + lanes + " linii (M).");
        }
        System.out.print("Introduceți distanțele pe linii (M valori separate prin spațiu, ex: 1 2 3). Lăsați gol pentru implicit: ");
        String line = scanner.nextLine().trim();
        if (!line.isEmpty()) {
            double[] distances = Arrays.stream(line.split("\\s+")).mapToDouble(Double::parseDouble).toArray();
            service.setRaceDistances(eventId, distances);
        }
        List<String> results = service.runRace(eventId);
        System.out.println("Rezultatele cursei:");
        for (String r : results) {
            System.out.println(r);
        }
    }

    private void showEventNotifications() {
        System.out.print("ID eveniment: ");
        int eventId = Integer.parseInt(scanner.nextLine());
        for (domain.Event e : service.getAllEvents()) {
            if (e.getId() == eventId) {
                System.out.println("Notificări pentru eveniment " + e.getName() + ":");
                for (String n : e.getNotificationLog()) System.out.println(" - " + n);
                return;
            }
        }
        System.out.println("Eveniment inexistent.");
    }

    /**
     * Display the notifications stored on a user (Observer inbox).
     */
    private void showUserNotifications() {
        System.out.print("ID utilizator: ");
        int userId = Integer.parseInt(scanner.nextLine());
        List<String> notes = service.getUserNotifications(userId);
        if (notes.isEmpty()) {
            System.out.println("(fără notificări)");
        } else {
            System.out.println("Notificări pentru utilizatorul #" + userId + ":");
            for (String n : notes) {
                System.out.println(" - " + n);
            }
        }
    }

    /**
     * Seed the application with a mix of persons and ducks, including enough swimmers
     * (SwimmingDuck and AmphibiousDuck) to make race events runnable out-of-the-box.
     * Also creates two race events: one auto-run at startup for demo purposes and one
     * left for interaction via the menu.
     */
    private void addTestData() {
        try {
            // Comunitatea 1 (linie)
            service.addUser(new Persoana(1, "ana", "a@g.com", "pass1231", "Ana", "Pop", "Student", LocalDate.of(2000, 5, 15), 8));
            service.addUser(new Persoana(2, "bogdan", "b@g.com", "pass4561", "Bogdan", "Ion", "Inginer", LocalDate.of(1995, 10, 20), 5));
            service.addUser(new Persoana(3, "cipi", "c@g.com", "pass7891", "Cipi", "Vlad", "Artist", LocalDate.of(1998, 2, 1), 9));
            service.addFriendship(1, 2);
            service.addFriendship(2, 3);

            // Comunitatea 2 (triunghi) + more ducks
            service.addUser(new SwimmingDuck(4, "donald", "d1@g.com", "quack111", 15.5, 20.0));
            service.addUser(new FlyingDuck(5, "daffy", "d2@g.com", "quack111", 50.0, 10.5));
            service.addUser(new Persoana(6, "elena", "e@g.com", "pass1011", "Elena", "Turc", "Medic", LocalDate.of(1990, 7, 30), 7));
            service.addFriendship(4, 5);
            service.addFriendship(5, 6);
            service.addFriendship(4, 6);

            // Comunitatea 3 (un singur nod)
            service.addUser(new Persoana(7, "singur", "s@g.com", "passSolitar", "Singur", "Singurel", "Gardian", LocalDate.of(1985, 1, 1), 3));

            // Extra ducks for richer race/card scenarios (ensure enough swimmers)
            service.addUser(new SwimmingDuck(8, "splash", "sp@g.com", "swimPass1", 18.0, 19.0));
            service.addUser(new AmphibiousDuck(9, "amphi", "am@g.com", "amphPass1", 22.0, 25.0));
            service.addUser(new FlyingDuck(10, "swift", "sw@g.com", "flyPass11", 60.0, 11.0));
            service.addUser(new SwimmingDuck(11, "wave", "wa@g.com", "swimPass2", 14.0, 30.0));
            service.addUser(new AmphibiousDuck(12, "combo", "co@g.com", "amphPass2", 26.0, 18.0));
            // Additional swimmers to guarantee enough eligible participants
            service.addUser(new SwimmingDuck(13, "marina", "m1@g.com", "swimPass3", 16.5, 21.0));
            service.addUser(new SwimmingDuck(14, "delta", "dlt@g.com", "swimPass4", 19.2, 17.5));
            service.addUser(new AmphibiousDuck(15, "hybrid", "hy@g.com", "amphPass3", 24.0, 23.0));

            // Create cards using auto-generated ids
            Card swimMasters = service.createCard("SwimMasters");
            Card skyFlyers = service.createCard("SkyFlyers");
            Card hybridElite = service.createCard("HybridElite");

            // Populate cards with ducks (using actual ids)
            service.addDuckToCard(swimMasters.getId(), 4);  // donald (SWIMMING)
            service.addDuckToCard(swimMasters.getId(), 8);  // splash (SWIMMING)
            service.addDuckToCard(swimMasters.getId(), 11); // wave (SWIMMING)

            service.addDuckToCard(skyFlyers.getId(), 5);  // daffy (FLYING)
            service.addDuckToCard(skyFlyers.getId(), 10); // swift (FLYING)

            service.addDuckToCard(hybridElite.getId(), 9);  // amphi (FLYING_AND_SWIMMING)
            service.addDuckToCard(hybridElite.getId(), 12); // combo (FLYING_AND_SWIMMING)

            // Create race event and subscribe users
            RaceEvent race1 = service.createRaceEvent("Spring Splash", 3);
            service.subscribeToEvent(race1.getId(), 1);  // Ana (person)
            service.subscribeToEvent(race1.getId(), 4);  // donald (duck)
            service.subscribeToEvent(race1.getId(), 9);  // amphi (duck)

            // Set distances and run race
            service.setRaceDistances(race1.getId(), new double[]{1.0, 1.2, 0.8});
            service.runRace(race1.getId());

            // Second race event
            RaceEvent race2 = service.createRaceEvent("Championship Finals", 4);
            service.subscribeToEvent(race2.getId(), 2);  // Bogdan (person)
            service.subscribeToEvent(race2.getId(), 8);  // splash (duck)
            service.subscribeToEvent(race2.getId(), 12); // combo (duck)

            // Set default distances
            service.setRaceDistances(race2.getId(), new double[]{1.0, 1.0, 1.0, 1.0});


            System.out.println("RaceEvent 200 a fost rulat cu distanțe predefinite; verifică notificările cu opțiunea 16 sau rulează alt eveniment cu opțiunea 15.");
        } catch (Exception e) {
            System.err.println("Eroare la încărcarea datelor de test: " + e.getMessage());
        }
    }
}
