package UI;

import domain.Duck;
import domain.Persoana;
import domain.TipRata;
import domain.User;
import errors.RepoError;
import errors.ValidationError;
import service.NetworkService;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private NetworkService service;
    private Scanner scanner;

    public ConsoleUI(NetworkService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        addTestData();
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

    private void printMenu(){
        System.out.println("1. Adaugă utilizator");
        System.out.println("2. Șterge utilizator");
        System.out.println("3. Adaugă prietenie");
        System.out.println("4. Șterge prietenie");
        System.out.println("5. Afișează toți utilizatorii");
        System.out.println("6. Afișează numărul de comunități");
        System.out.println("7. Afișează cea mai sociabilă comunitate");
        System.out.println("0. Ieșire");
    }

    private void addUser(){
        System.out.print("Tip (persoana/duck): ");
        String type=scanner.nextLine();

        System.out.print("ID (int): ");
        int id=Integer.parseInt(scanner.nextLine());
        System.out.print("Username: ");
        String username=scanner.nextLine();
        System.out.print("Email: ");
        String email=scanner.nextLine();
        System.out.print("Password (min 8 characters): ");
        String password=scanner.nextLine();

        User user;
        if(type.equalsIgnoreCase("persoana")){
            System.out.print("Nume: ");
            String nume=scanner.nextLine();
            System.out.print("Prenume: ");
            String prenume=scanner.nextLine();
            System.out.print("Ocupatie: ");
            String ocupatie=scanner.nextLine();
            System.out.print("Data nasterii (YYYY-MM-DD): ");
            LocalDate dataNasterii=LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ISO_LOCAL_DATE);
            System.out.print("Nivel empatie (1-10): ");
            int nivelEmpatie=Integer.parseInt(scanner.nextLine());
            user=new Persoana(id, username, email, password, nume, prenume, ocupatie, dataNasterii, nivelEmpatie);
        } else if(type.equalsIgnoreCase("duck")){
            System.out.print("Tip rata (SWIMMING/FLYING/FLYING_AND_SWIMMING): ");
            TipRata tipRata=TipRata.valueOf(scanner.nextLine().toUpperCase());
            System.out.print("Viteza (double): ");
            double viteza=Double.parseDouble(scanner.nextLine());
            System.out.print("Rezistenta (double): ");
            double rezistenta=Double.parseDouble(scanner.nextLine());
            user=new Duck(id, username, email, password, tipRata, viteza, rezistenta);
        } else {
            System.out.println("Tip necunoscut!");
            return;
        }
        service.addUser(user);
        System.out.println("Utilizator adăugat cu succes.");
    }

    private void removeUser(){
        System.out.print("ID-ul utilizatorului de șters: ");
        int id=Integer.parseInt(scanner.nextLine());
        User removed=service.removeUser(id);
        System.out.println("Utilizatorul " +removed.getUsername()+" a fost șters cu succes.");
    }

    private void addFriend(){
        System.out.print("ID-ul primului utilizator: ");
        int id1=Integer.parseInt(scanner.nextLine());
        System.out.print("ID-ul celui de-al doilea utilizator: ");
        int id2=Integer.parseInt(scanner.nextLine());
        service.addFriendship(id1, id2);
        System.out.println("Prietenie adăugată cu succes între utilizatorii " + id1 + " și " + id2 + ".");
    }

    private void removeFriend(){
        System.out.print("ID-ul primului utilizator: ");
        int id1=Integer.parseInt(scanner.nextLine());
        System.out.print("ID-ul celui de-al doilea utilizator: ");
        int id2=Integer.parseInt(scanner.nextLine());
        service.removeFriendship(id1, id2);
        System.out.println("Prietenia dintre utilizatorii " + id1 + " și " + id2 + " a fost ștearsă cu succes.");
    }

    private void showAllUsers(){
        System.out.println("Utilizatorii retelei:");
        for (User u: service.getAllUsers()) {
            System.out.println(u.getId()+": "+ u.getUsername()+" "+u.getFriends());
        }
    }

    private void showCommunities(){
        System.out.println("Numarul de comunitati (componente conexe): "+service.getNumberOfCommunities());
    }

    private void showMostSocial(){
        System.out.println("Cea mai sociabila comunitate este:");
        List<User> community=service.getMostSocialCommunity();
        for (User u: community) {
            System.out.println(" - "+ u.getUsername());
        }
    }

    private void addTestData(){
        try{
            // Comunitatea 1 (linie)
            // Constructor Persoana: (id, username, email, password, nume, prenume, ocupatie, dataNasterii, nivelEmpatie)
            service.addUser(new Persoana(1, "ana", "a@g.com", "pass1231", "Ana", "Pop", "Student", LocalDate.of(2000, 5, 15), 8));
            service.addUser(new Persoana(2, "bogdan", "b@g.com", "pass4561", "Bogdan", "Ion", "Inginer", LocalDate.of(1995, 10, 20), 5));
            service.addUser(new Persoana(3, "cipi", "c@g.com", "pass7891", "Cipi", "Vlad", "Artist", LocalDate.of(1998, 2, 1), 9));

            service.addFriendship(1, 2);
            service.addFriendship(2, 3);

            // Comunitatea 2 (triunghi)
            // Constructor Duck: (id, username, email, password, tipRata, viteza, rezistenta)
            service.addUser(new Duck(4, "donald", "d1@g.com", "quack111", TipRata.SWIMMING, 15.5, 20.0));
            service.addUser(new Duck(5, "daffy", "d2@g.com", "quack111", TipRata.FLYING, 50.0, 10.5));
            service.addUser(new Persoana(6, "elena", "e@g.com", "pass1011", "Elena", "Turc", "Medic", LocalDate.of(1990, 7, 30), 7));

            service.addFriendship(4, 5);
            service.addFriendship(5, 6);
            service.addFriendship(4, 6);

            // Comunitatea 3 (un singur nod)
            service.addUser(new Persoana(7, "singur", "s@g.com", "passSolitar", "Singur", "Singurel", "Gardian", LocalDate.of(1985, 1, 1), 3));

            System.out.println("Date de test (complete) încărcate.");
        } catch (Exception e) {
            System.err.println("Eroare la încărcarea datelor de test: " + e.getMessage());
        }
    }
}
