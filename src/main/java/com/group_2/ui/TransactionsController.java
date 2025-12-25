package com.group_2.ui;

import com.group_2.service.TransactionService;
import com.group_2.util.SessionManager;
import com.model.User;
import com.model.WG;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;

@Component
public class TransactionsController extends Controller {

    private final TransactionService transactionService;
    private final SessionManager sessionManager;

    @Autowired
    private ApplicationContext applicationContext;

    // Header elements
    @FXML
    private Text headerUserName;
    @FXML
    private Text headerWgName;
    @FXML
    private Text headerAvatar;

    // Balance display
    @FXML
    private Text totalBalanceText;
    @FXML
    private Text balanceStatusText;

    // Balance table
    @FXML
    private TableView<BalanceEntry> balanceTable;
    @FXML
    private TableColumn<BalanceEntry, String> memberColumn;
    @FXML
    private TableColumn<BalanceEntry, String> balanceColumn;

    // Transaction dialog fields will be added here

    private DecimalFormat currencyFormat = new DecimalFormat("€#,##0.00");

    @Autowired
    public TransactionsController(TransactionService transactionService,
            SessionManager sessionManager) {
        this.transactionService = transactionService;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void initialize() {
        setupBalanceTable();
        // Dialog setup will be added here
    }

    public void initView() {
        sessionManager.refreshCurrentUser();
        updateHeader();
        updateBalanceDisplay();
        updateBalanceSheet();
    }

    private void updateHeader() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            String fullName = currentUser.getName() +
                    (currentUser.getSurname() != null ? " " + currentUser.getSurname() : "");
            headerUserName.setText(fullName);

            String initial = currentUser.getName() != null && !currentUser.getName().isEmpty()
                    ? currentUser.getName().substring(0, 1).toUpperCase()
                    : "?";
            headerAvatar.setText(initial);

            WG wg = currentUser.getWg();
            if (wg != null) {
                headerWgName.setText(wg.name);
            } else {
                headerWgName.setText("No WG");
            }
        }
    }

    private void setupBalanceTable() {
        memberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemberName()));

        balanceColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBalanceFormatted()));

        balanceColumn.setCellFactory(column -> new TableCell<BalanceEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    BalanceEntry entry = getTableView().getItems().get(getIndex());
                    double balance = entry.getBalance();

                    if (balance > 0) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if (balance < 0) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #64748b; -fx-font-weight: normal;");
                    }
                }
            }
        });
    }

    private void updateBalanceDisplay() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null)
            return;

        double totalBalance = transactionService.getTotalBalance(currentUser.getId());
        totalBalanceText.setText(currencyFormat.format(totalBalance));

        if (totalBalance > 0) {
            balanceStatusText.setText("(You are owed)");
        } else if (totalBalance < 0) {
            balanceStatusText.setText("(You owe)");
        } else {
            balanceStatusText.setText("(All settled)");
        }
    }

    private void updateBalanceSheet() {
        User currentUser = sessionManager.getCurrentUser();
        WG wg = currentUser != null ? currentUser.getWg() : null;

        balanceTable.getItems().clear();

        if (wg == null || wg.mitbewohner == null) {
            balanceTable.setPrefHeight(100);
            balanceTable.setMaxHeight(100);
            return;
        }

        Map<Long, Double> balances = transactionService.calculateAllBalances(currentUser.getId());

        for (User member : wg.mitbewohner) {
            if (!member.getId().equals(currentUser.getId())) {
                double balance = balances.getOrDefault(member.getId(), 0.0);
                String memberName = member.getName() +
                        (member.getSurname() != null ? " " + member.getSurname() : "");
                balanceTable.getItems().add(new BalanceEntry(memberName, balance));
            }
        }

        int rowCount = balanceTable.getItems().size();
        double tableHeight = (rowCount * 50) + 35;
        balanceTable.setPrefHeight(tableHeight);
        balanceTable.setMaxHeight(tableHeight);
        balanceTable.setMinHeight(tableHeight);
    }

    @FXML
    public void showAddTransactionDialog() {
        // New Splitwise-style dialog will be implemented here
        System.out.println("Add Transaction clicked - dialog not yet implemented");
    }

    @FXML
    public void backToHome() {
        loadScene(headerUserName.getScene(), "/main_screen.fxml");
        javafx.application.Platform.runLater(() -> {
            MainScreenController mainScreenController = applicationContext.getBean(MainScreenController.class);
            mainScreenController.initView();
        });
    }

    @FXML
    public void handleLogout() {
        sessionManager.clear();
        loadScene(headerUserName.getScene(), "/login.fxml");
    }

    public static class BalanceEntry {
        private final String memberName;
        private final double balance;
        private final DecimalFormat format = new DecimalFormat("€#,##0.00");

        public BalanceEntry(String memberName, double balance) {
            this.memberName = memberName;
            this.balance = balance;
        }

        public String getMemberName() {
            return memberName;
        }

        public double getBalance() {
            return balance;
        }

        public String getBalanceFormatted() {
            return format.format(balance);
        }
    }
}
