package UI;

import domain.*;
import domain.Message;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import service.NetworkService;
import util.PageResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Controller {
    private static final int PAGE_SIZE = 10;
    private final ObservableList<Duck> ducks = FXCollections.observableArrayList();
    private final ObservableList<Persoana> persons = FXCollections.observableArrayList();

    private TableView<Duck> duckTable;
    private TableView<Persoana> personTable;
    private ComboBox<TipRata> typeFilter;
    private Label pageInfoLabel;
    private Button prevPageButton;
    private Button nextPageButton;

    private ComboBox<String> newUserTypeCombo;
    private TextField usernameField;
    private TextField emailField;
    private TextField passwordField;
    private TextField numeField;
    private TextField prenumeField;
    private TextField ocupatieField;
    private TextField birthDateField;
    private TextField empatieField;
    private TextField speedField;
    private TextField enduranceField;
    private Button removeUserButton;

    private Label communityCountLabel;
    private TextArea socialCommunityArea;

    private TableView<User> friendsTable;
    private TextField friendEmailField;
    private TextArea chatArea;
    private TextField chatTargetField;
    private TextArea chatMessageField;
    private ComboBox<User> chatFriendCombo;
    private final ObservableList<User> chatPartners = FXCollections.observableArrayList();
    private boolean suppressChatComboEvents;

    private NetworkService service;
    private int currentPage = 0;
    private TipRata currentFilter = null;
    private User loggedInUser;

    public void setService(NetworkService service) {
        this.service = service;
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        refreshFriends();
        if (!friendsTable.getItems().isEmpty()) {
            friendsTable.getSelectionModel().selectFirst();
            loadConversation(friendsTable.getSelectionModel().getSelectedItem());
        }
    }

    public BorderPane buildView() {
        duckTable = new TableView<>();
        duckTable.setItems(ducks);
        duckTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_NEXT_COLUMN);

        TableColumn<Duck, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        TableColumn<Duck, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));

        TableColumn<Duck, Double> speedCol = new TableColumn<>("Viteza");
        speedCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getViteza()).asObject());

        TableColumn<Duck, Double> staminaCol = new TableColumn<>("Rezistenta");
        staminaCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getRezistenta()).asObject());

        TableColumn<Duck, String> typeCol = new TableColumn<>("Tip");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipRata().name()));

        duckTable.getColumns().addAll(emailCol, usernameCol, speedCol, staminaCol, typeCol);

        // simple Persoana table underneath (no paging yet)
        personTable = new TableView<>();
        personTable.setItems(persons);
        personTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_NEXT_COLUMN);

        TableColumn<Persoana, String> pEmailCol = new TableColumn<>("Email");
        pEmailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        TableColumn<Persoana, String> pUserCol = new TableColumn<>("Username");
        pUserCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        TableColumn<Persoana, String> pNumeCol = new TableColumn<>("Nume");
        pNumeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNume()));
        TableColumn<Persoana, String> pPrenumeCol = new TableColumn<>("Prenume");
        pPrenumeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenume()));

        personTable.getColumns().addAll(pEmailCol, pUserCol, pNumeCol, pPrenumeCol);

        typeFilter = new ComboBox<>();
        typeFilter.getItems().add(null);
        typeFilter.getItems().addAll(TipRata.values());
        typeFilter.setPromptText("Toate tipurile");
        typeFilter.valueProperty().addListener((obs, old, val) -> {
            currentFilter = val;
            currentPage = 0;
            loadPage();
        });

        prevPageButton = new Button("◀");
        nextPageButton = new Button("▶");
        prevPageButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadPage();
            }
        });
        nextPageButton.setOnAction(e -> {
            currentPage++;
            loadPage();
        });

        pageInfoLabel = new Label("Pagina 0/0");

        HBox paginationBar = new HBox(10,
                new Label("Filtru tip:"),
                typeFilter,
                prevPageButton,
                pageInfoLabel,
                nextPageButton
        );
        paginationBar.setAlignment(Pos.CENTER_LEFT);
        paginationBar.setPadding(new Insets(10));

        VBox tablesBox = new VBox(10, duckTable, new Label("Persoane"), personTable);
        tablesBox.setPadding(new Insets(0, 10, 10, 10));

        VBox rightControls = new VBox(15,
                buildUserSection(),
                buildFriendshipSection(),
                buildCommunitySection(),
                buildChatSection()
        );
        rightControls.setPadding(new Insets(10));
        rightControls.setPrefWidth(340);

        BorderPane root = new BorderPane();
        root.setTop(paginationBar);
        root.setCenter(tablesBox);
        ScrollPane scrollPane = new ScrollPane(rightControls);
        scrollPane.setFitToWidth(true);
        root.setRight(scrollPane);

        loadPage();
        loadPersons();
        return root;
    }

    private VBox buildUserSection() {
        newUserTypeCombo = new ComboBox<>();
        newUserTypeCombo.getItems().addAll("Persoana", "SwimmingDuck", "FlyingDuck", "AmphibiousDuck");
        newUserTypeCombo.setPromptText("Tip utilizator");
        newUserTypeCombo.valueProperty().addListener((obs, old, type) -> updateVisibleUserFields(type));

        usernameField = new TextField();
        usernameField.setPromptText("username");
        emailField = new TextField();
        emailField.setPromptText("email");
        passwordField = new TextField();
        passwordField.setPromptText("parola");

        numeField = new TextField();
        numeField.setPromptText("nume");
        prenumeField = new TextField();
        prenumeField.setPromptText("prenume");
        ocupatieField = new TextField();
        ocupatieField.setPromptText("ocupatie");
        birthDateField = new TextField();
        birthDateField.setPromptText("data nasterii YYYY-MM-DD");
        empatieField = new TextField();
        empatieField.setPromptText("nivel empatie 1-10");

        speedField = new TextField();
        speedField.setPromptText("viteza");
        enduranceField = new TextField();
        enduranceField.setPromptText("rezistenta");

        Button addUserButton = new Button("Adauga utilizator");
        addUserButton.setOnAction(e -> handleAddUser());

        removeUserButton = new Button("Sterge utilizator selectat");
        removeUserButton.setOnAction(e -> handleRemoveUser());

        VBox box = new VBox(6,
                new Label("Utilizatori"),
                newUserTypeCombo,
                usernameField,
                emailField,
                passwordField,
                numeField,
                prenumeField,
                ocupatieField,
                birthDateField,
                empatieField,
                speedField,
                enduranceField,
                addUserButton,
                new Separator(),
                removeUserButton
        );
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-width: 1;");

        // initialize visibility: nothing selected yet
        updateVisibleUserFields(null);
        return box;
    }

    private void updateVisibleUserFields(String type) {
        boolean isPersoana = "Persoana".equals(type);
        boolean isDuck = type != null && !isPersoana;

        // Common credentials always visible when a type is selected
        setFieldVisible(usernameField, type != null);
        setFieldVisible(emailField, type != null);
        setFieldVisible(passwordField, type != null);

        // Persoana-specific fields
        setFieldVisible(numeField, isPersoana);
        setFieldVisible(prenumeField, isPersoana);
        setFieldVisible(ocupatieField, isPersoana);
        setFieldVisible(birthDateField, isPersoana);
        setFieldVisible(empatieField, isPersoana);

        // Duck-specific fields
        setFieldVisible(speedField, isDuck);
        setFieldVisible(enduranceField, isDuck);
    }

    private void setFieldVisible(Control control, boolean visible) {
        control.setVisible(visible);
        control.setManaged(visible);
    }

    private VBox buildFriendshipSection() {
        friendEmailField = new TextField();
        friendEmailField.setPromptText("email prieten");
        Button addFriendButton = new Button("Adauga prietenie");
        addFriendButton.setOnAction(e -> handleFriendAdd());

        friendsTable = new TableView<>();
        friendsTable.setPlaceholder(new Label("Fara prieteni"));
        friendsTable.setPrefHeight(150);
        friendsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                chatTargetField.setText(newSelection.getEmail());
                loadConversation(newSelection);
            }
        });

        TableColumn<User, String> friendName = new TableColumn<>("Username");
        friendName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        TableColumn<User, String> friendEmail = new TableColumn<>("Email");
        friendEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        TableColumn<User, Void> removeCol = new TableColumn<>("Actiuni");
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button("Sterge");
            {
                removeButton.setOnAction(event -> {
                    User friend = getTableView().getItems().get(getIndex());
                    handleFriendRemove(friend.getEmail());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        friendsTable.getColumns().addAll(friendName, friendEmail, removeCol);

        VBox box = new VBox(6,
                new Label("Prietenii"),
                new Label("Adauga dupa email"),
                new HBox(8, friendEmailField, addFriendButton),
                friendsTable
        );
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-width: 1;");
        return box;
    }

    private VBox buildChatSection() {
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefRowCount(10);
        chatTargetField = new TextField();
        chatTargetField.setPromptText("email destinatar");
        chatMessageField = new TextArea();
        chatMessageField.setPromptText("Scrie mesajul...");
        chatMessageField.setPrefRowCount(3);
        Button sendButton = new Button("Trimite");
        sendButton.setOnAction(e -> handleSendMessage(chatTargetField.getText(), chatMessageField.getText()));
        chatFriendCombo = new ComboBox<>();
        chatFriendCombo.setPromptText("Selecteaza prieten");
        chatFriendCombo.setItems(chatPartners);
        chatFriendCombo.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getEmail());
            }
        });
        chatFriendCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getEmail());
            }
        });
        chatFriendCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressChatComboEvents) {
                return;
            }
            if (newVal != null) {
                chatTargetField.setText(newVal.getEmail());
                loadConversation(newVal);
            }
        });
        VBox box = new VBox(6,
                new Label("Chat"),
                new Label("Conversatii"),
                chatArea,
                chatTargetField,
                chatMessageField,
                sendButton,
                chatFriendCombo
        );
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-width: 1;");
        return box;
    }

    private VBox buildCommunitySection() {
        communityCountLabel = new Label("Comunitati: ?");
        socialCommunityArea = new TextArea();
        socialCommunityArea.setEditable(false);
        socialCommunityArea.setPrefRowCount(6);

        Button refreshCommunitiesButton = new Button("Calculeaza comunitati");
        refreshCommunitiesButton.setOnAction(e -> updateCommunityCount());

        Button showSocialButton = new Button("Cea mai sociabila");
        showSocialButton.setOnAction(e -> showMostSocialCommunity());

        VBox box = new VBox(6,
                new Label("Statistici comunitati"),
                communityCountLabel,
                new HBox(10, refreshCommunitiesButton, showSocialButton),
                socialCommunityArea
        );
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-width: 1;");
        return box;
    }

    private void loadPage() {
        if (service == null) {
            return;
        }
        try {
            PageResult<Duck> page = service.getDucksPage(currentPage, PAGE_SIZE, currentFilter);
            ducks.setAll(page.getItems());
            int totalPages = Math.max(page.getTotalPages(), 1);
            currentPage = Math.min(currentPage, totalPages - 1);
            pageInfoLabel.setText(String.format("Pagina %d / %d (total %d)", currentPage + 1, totalPages, page.getTotalItems()));
            prevPageButton.setDisable(!page.hasPrevious());
            nextPageButton.setDisable(!page.hasNext());
            refreshFriends();
        } catch (Exception e) {
            showError("Nu am putut incarca rațele", e.getMessage());
        }
    }

    private void loadPersons() {
        if (service == null) {
            return;
        }
        try {
            List<User> all = service.getAllUsers(Persoana.class);
            persons.setAll(all.stream().map(Persoana.class::cast).toList());
        } catch (Exception e) {
            showError("Nu am putut incarca persoanele", e.getMessage());
        }
    }

    private void handleAddUser() {
        if (service == null) {
            return;
        }
        String type = newUserTypeCombo.getValue();
        if (type == null) {
            showError("Tip lipsa", "Selecteaza tipul utilizatorului");
            return;
        }
        try {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            User newUser;
            if ("Persoana".equals(type)) {
                LocalDate birthDate = LocalDate.parse(birthDateField.getText());
                int emp = Integer.parseInt(empatieField.getText());
                newUser = new Persoana(username, email, password, numeField.getText(), prenumeField.getText(),
                        ocupatieField.getText(), birthDate, emp);
            } else {
                double speed = Double.parseDouble(speedField.getText());
                double endurance = Double.parseDouble(enduranceField.getText());
                newUser = switch (type) {
                    case "SwimmingDuck" -> new SwimmingDuck(username, email, password, speed, endurance);
                    case "FlyingDuck" -> new FlyingDuck(username, email, password, speed, endurance);
                    case "AmphibiousDuck" -> new AmphibiousDuck(username, email, password, speed, endurance);
                    default -> throw new IllegalArgumentException("Tip necunoscut");
                };
            }
            service.addUser(newUser);
            showInfo("Succes", "Utilizator adaugat");
            loadPage();
            loadPersons();
        } catch (Exception ex) {
            showError("Nu am putut adauga utilizatorul", ex.getMessage());
        }
    }

    private void handleRemoveUser() {
        if (service == null) {
            return;
        }
        Optional<User> selected = getSelectedUser();
        if (selected.isEmpty()) {
            showError("Selectie lipsa", "Selecteaza un utilizator din tabel pentru a-l sterge");
            return;
        }
        try {
            service.removeUser(selected.get().getId());
            showInfo("Succes", "Utilizator sters");
            loadPage();
            loadPersons();
        } catch (Exception ex) {
            showError("Nu am putut sterge utilizatorul", ex.getMessage());
        }
    }

    private void handleFriendAdd() {
        if (service == null || loggedInUser == null) {
            showError("Autentificare necesara", "Logheaza-te pentru a gestiona prietenii");
            return;
        }
        try {
            service.addFriendshipByEmail(loggedInUser.getEmail(), friendEmailField.getText());
            showInfo("Succes", "Prieten adaugat");
            refreshFriends();
        } catch (Exception ex) {
            showError("Nu am putut adauga", ex.getMessage());
        }
    }

    private void handleFriendRemove(String friendEmail) {
        if (service == null || loggedInUser == null) {
            showError("Autentificare necesara", "Logheaza-te pentru a gestiona prietenii");
            return;
        }
        try {
            service.removeFriendshipByEmail(loggedInUser.getEmail(), friendEmail);
            showInfo("Succes", "Prietenie stearsa");
            refreshFriends();
        } catch (Exception ex) {
            showError("Nu am putut sterge", ex.getMessage());
        }
    }

    private void refreshFriends() {
        if (service == null || loggedInUser == null) {
            friendsTable.getItems().clear();
            chatArea.clear();
            chatTargetField.clear();
            chatPartners.clear();
            return;
        }
        suppressChatComboEvents = true;
        friendsTable.getItems().setAll(service.getFriendsFor(loggedInUser.getId()));
        chatPartners.setAll(friendsTable.getItems());
        if (!friendsTable.getItems().isEmpty()) {
            User first = friendsTable.getItems().get(0);
            friendsTable.getSelectionModel().select(first);
            chatFriendCombo.getSelectionModel().select(first);
            loadConversation(first);
        } else {
            chatArea.clear();
            chatTargetField.clear();
            chatFriendCombo.getSelectionModel().clearSelection();
        }
        suppressChatComboEvents = false;
    }

    private void handleSendMessage(String targetEmail, String messageText) {
        if (service == null || loggedInUser == null) {
            showError("Autentificare necesara", "Logheaza-te pentru a trimite mesaje");
            return;
        }
        if (targetEmail == null || targetEmail.isBlank() || messageText == null || messageText.isBlank()) {
            showError("Date lipsa", "Completeaza email si mesaj");
            return;
        }
        try {
            User recipient = service.reloadUser(service.requireUserByEmail(targetEmail).getId());
            service.sendMessage(loggedInUser, List.of(recipient), messageText);
            if (chatPartners.stream().noneMatch(u -> u.getId() == recipient.getId())) {
                chatPartners.add(recipient);
            }
            suppressChatComboEvents = true;
            chatFriendCombo.getSelectionModel().select(recipient);
            suppressChatComboEvents = false;
            chatTargetField.setText(recipient.getEmail());
            chatMessageField.clear();
            loadConversation(recipient);
            showInfo("Trimis", "Mesaj trimis");
        } catch (Exception ex) {
            showError("Nu am putut trimite", ex.getMessage());
        }
    }

    private void loadConversation(User other) {
        if (service == null || loggedInUser == null || other == null) {
            chatArea.clear();
            return;
        }
        StringBuilder conversation = new StringBuilder();
        service.getConversation(loggedInUser.getId(), other.getId()).forEach(msg -> {
            String author = msg.getFrom().getId() == loggedInUser.getId() ? "Tu" : msg.getFrom().getUsername();
            conversation.append(author).append(": ").append(msg.getMessage()).append("\n");
        });
        chatArea.setText(conversation.toString());
    }

    private void updateCommunityCount() {
        if (service == null) {
            return;
        }
        try {
            int count = service.getNumberOfCommunities();
            communityCountLabel.setText("Comunitati: " + count);
        } catch (Exception ex) {
            showError("Nu am putut calcula comunitatile", ex.getMessage());
        }
    }

    private void showMostSocialCommunity() {
        if (service == null) {
            return;
        }
        try {
            List<User> community = service.getMostSocialCommunity();
            if (community.isEmpty()) {
                socialCommunityArea.setText("(fara utilizatori)");
            } else {
                StringBuilder sb = new StringBuilder();
                community.forEach(u -> sb.append(u.getId()).append(" - ").append(u.getUsername()).append('\n'));
                socialCommunityArea.setText(sb.toString());
            }
        } catch (Exception ex) {
            showError("Nu am putut incarca comunitatea", ex.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private Optional<User> getSelectedUser() {
        User duckSelection = duckTable.getSelectionModel().getSelectedItem();
        if (duckSelection != null) {
            return Optional.of(duckSelection);
        }
        User personSelection = personTable.getSelectionModel().getSelectedItem();
        if (personSelection != null) {
            return Optional.of(personSelection);
        }
        return Optional.empty();
    }
}

