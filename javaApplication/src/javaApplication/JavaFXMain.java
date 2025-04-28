package javaApplication;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class JavaFXMain extends Application {

    private Stage primaryStage;
    private TableView<TransactionRecord> transactionTable;
    private TaxProcessor taxProcessor;
    private Label summaryTotalLabel;
    private Label summaryValidLabel;
    private Label summaryInvalidLabel;
    private Label taxRateLabel;
    private Label totalTaxLabel;
    private TextField taxRateField;

    // Updated color palette
    private static final String PRIMARY_BG = "#F1EFEC";
    private static final String SECTION_BG = "#D4C9BE";
    private static final String ACCENT_COLOR = "#123458";
    private static final String TEXT_COLOR = "#030303";
    private static final String SUCCESS_COLOR = "#1DB954";

    // Path to the imported CSV file (will be set on import)
    private String importedFilePath = null;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.taxProcessor = new TaxProcessor();
        primaryStage.setTitle("Government Tax Department System");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(createHeader());
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createCenterContent());
        mainLayout.setBottom(createStatusBar());

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + ACCENT_COLOR + ";");
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Government Tax Department System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(PRIMARY_BG));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exitButton = createStyledButton("Exit", ACCENT_COLOR);
        exitButton.setOnAction(e -> primaryStage.close());

        header.getChildren().addAll(titleLabel, spacer, exitButton);
        return header;
    }

    // Sidebar is now empty and very narrow, matches section background
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(0));
        sidebar.setPrefWidth(40);
        sidebar.setStyle("-fx-background-color: " + SECTION_BG + ";");
        return sidebar;
    }

    private VBox createCenterContent() {
        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        centerContent.setStyle("-fx-background-color: " + PRIMARY_BG + ";");

        // Import section
        HBox importSection = new HBox(10);
        importSection.setAlignment(Pos.CENTER_LEFT);
        Label importLabel = new Label("Import Tax Transaction File:");
        importLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        importLabel.setTextFill(Color.web(TEXT_COLOR));
        TextField filePathField = new TextField();
        filePathField.setPrefWidth(500);
        HBox.setHgrow(filePathField, Priority.ALWAYS);

        Button browseButton = createStyledButton("Browse", ACCENT_COLOR);
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Tax File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                filePathField.setText(file.getAbsolutePath());
            }
        });

        Button importButton = createStyledButton("Import", ACCENT_COLOR);
        importButton.setOnAction(e -> {
            String filePath = filePathField.getText();
            if (filePath != null && !filePath.isEmpty()) {
                importTaxFile(filePath);
            } else {
                showAlert(Alert.AlertType.WARNING, "Import Warning",
                        "No File Selected", "Please select a file to import.");
            }
        });

        importSection.getChildren().addAll(importLabel, filePathField, browseButton, importButton);

        // Table section
        VBox tableSection = new VBox(10);
        Label tableLabel = new Label("Transaction Records");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tableLabel.setTextFill(Color.web(TEXT_COLOR));
        transactionTable = createTransactionTable();
        VBox.setVgrow(transactionTable, Priority.ALWAYS);
        tableSection.getChildren().addAll(tableLabel, transactionTable);

        // Operation buttons
        HBox operationButtons = new HBox(10);
        operationButtons.setAlignment(Pos.CENTER);
        Button editButton = createStyledButton("Edit Selected Transaction", ACCENT_COLOR);
        editButton.setOnAction(e -> editSelectedRecord());
        Button deleteButton = createStyledButton("Delete Selected Transaction", ACCENT_COLOR);
        deleteButton.setOnAction(e -> deleteSelectedRecord());
        Button deleteZeroButton = createStyledButton("Delete Zero Profit Transactions", ACCENT_COLOR);
        deleteZeroButton.setOnAction(e -> deleteZeroProfitRecords());
        operationButtons.getChildren().addAll(editButton, deleteButton, deleteZeroButton);

        // Tax calculation section (summary left, tax right)
        HBox taxCalculationSection = createTaxCalculationSection();

        centerContent.getChildren().addAll(importSection, tableSection, operationButtons, taxCalculationSection);
        return centerContent;
    }

    // Summary left, tax calculation right
    private HBox createTaxCalculationSection() {
        HBox taxSection = new HBox(20);
        taxSection.setPadding(new Insets(15));
        taxSection.setStyle(
                "-fx-background-color: " + SECTION_BG + ";" +
                        "-fx-border-radius: 5px; -fx-border-color: " + ACCENT_COLOR + ";"
        );

        // Left side - Summary
        VBox summaryBox = new VBox(8);
        summaryBox.setPrefWidth(300);
        Label summaryTitle = new Label("Summary");
        summaryTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        summaryTitle.setTextFill(Color.web(TEXT_COLOR));
        summaryTotalLabel = new Label("Total Records: 0");
        summaryTotalLabel.setTextFill(Color.web(TEXT_COLOR));
        summaryValidLabel = new Label("Valid Records: 0");
        summaryValidLabel.setTextFill(Color.web(TEXT_COLOR));
        summaryInvalidLabel = new Label("Invalid Records: 0");
        summaryInvalidLabel.setTextFill(Color.web(TEXT_COLOR));
        summaryBox.getChildren().addAll(summaryTitle, summaryTotalLabel, summaryValidLabel, summaryInvalidLabel);

        // Right side - Tax Calculation
        VBox taxCalculationBox = new VBox(8);
        taxCalculationBox.setPrefWidth(300);
        Label taxCalcTitle = new Label("Tax Calculation");
        taxCalcTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        taxCalcTitle.setTextFill(Color.web(TEXT_COLOR));
        HBox taxRateBox = new HBox(10);
        taxRateBox.setAlignment(Pos.CENTER_LEFT);
        Label taxRateTextLabel = new Label("Tax Rate (%):");
        taxRateTextLabel.setTextFill(Color.web(TEXT_COLOR));
        taxRateField = new TextField();
        taxRateField.setPrefWidth(100);
        Button calculateButton = createStyledButton("Calculate Tax", ACCENT_COLOR);
        calculateButton.setOnAction(e -> calculateTax());
        taxRateBox.getChildren().addAll(taxRateTextLabel, taxRateField, calculateButton);

        taxRateLabel = new Label("Tax Rate: 0.0%");
        taxRateLabel.setTextFill(Color.web(TEXT_COLOR));
        totalTaxLabel = new Label("Total Tax: LKR 0.00");
        totalTaxLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        totalTaxLabel.setTextFill(Color.web(SUCCESS_COLOR));

        taxCalculationBox.getChildren().addAll(taxCalcTitle, taxRateBox, taxRateLabel, totalTaxLabel);

        // Add spacer between the two sections
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        taxSection.getChildren().addAll(summaryBox, spacer, taxCalculationBox);

        return taxSection;
    }

    // Helper for styled buttons
    private Button createStyledButton(String text, String bgColor) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + PRIMARY_BG + ";" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-background-radius: 4;"
        );
        return button;
    }

    private TableView<TransactionRecord> createTransactionTable() {
        TableView<TransactionRecord> table = new TableView<>();
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<TransactionRecord, String> billNumberCol = new TableColumn<>("Bill Number");
        billNumberCol.setCellValueFactory(new PropertyValueFactory<>("billNumber"));
        billNumberCol.setCellFactory(TextFieldTableCell.forTableColumn());
        billNumberCol.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            TransactionRecord record = e.getTableView().getItems().get(row);
            record.setBillNumber(e.getNewValue());
            updateRecord(row, record);
        });

        TableColumn<TransactionRecord, String> itemCodeCol = new TableColumn<>("Item Code");
        itemCodeCol.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        itemCodeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        itemCodeCol.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            TransactionRecord record = e.getTableView().getItems().get(row);
            record.setItemCode(e.getNewValue());
            updateRecord(row, record);
        });

        TableColumn<TransactionRecord, BigDecimal> internalPriceCol = new TableColumn<>("Internal Price");
        internalPriceCol.setCellValueFactory(new PropertyValueFactory<>("internalPrice"));
        internalPriceCol.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        internalPriceCol.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            TransactionRecord record = e.getTableView().getItems().get(row);
            record.setInternalPrice(e.getNewValue());
            updateRecord(row, record);
        });

        TableColumn<TransactionRecord, BigDecimal> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));
        discountCol.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        discountCol.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            TransactionRecord record = e.getTableView().getItems().get(row);
            record.setDiscount(e.getNewValue());
            updateRecord(row, record);
        });

        TableColumn<TransactionRecord, BigDecimal> salePriceCol = new TableColumn<>("Sale Price");
        salePriceCol.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
        salePriceCol.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        salePriceCol.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            TransactionRecord record = e.getTableView().getItems().get(row);
            record.setSalePrice(e.getNewValue());
            updateRecord(row, record);
        });

        TableColumn<TransactionRecord, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityCol.setOnEditCommit(e -> {
            int row = e.getTablePosition().getRow();
            TransactionRecord record = e.getTableView().getItems().get(row);
            record.setQuantity(e.getNewValue());
            updateRecord(row, record);
        });

        TableColumn<TransactionRecord, BigDecimal> lineTotalCol = new TableColumn<>("Total Value");
        lineTotalCol.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        TableColumn<TransactionRecord, Integer> checksumCol = new TableColumn<>("Checksum");
        checksumCol.setCellValueFactory(new PropertyValueFactory<>("originalChecksum"));

        TableColumn<TransactionRecord, BigDecimal> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));

        TableColumn<TransactionRecord, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("valid"));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean valid, boolean empty) {
                super.updateItem(valid, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    if (valid) {
                        setText("Valid");
                        setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
                    } else {
                        setText("Invalid");
                        setStyle("-fx-text-fill: " + ACCENT_COLOR + "; -fx-font-weight: bold;");
                    }
                }
            }
        });

        table.getColumns().addAll(
                billNumberCol, itemCodeCol, internalPriceCol, discountCol,
                salePriceCol, quantityCol, lineTotalCol, checksumCol, profitCol, statusCol
        );
        return table;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 15, 5, 15));
        statusBar.setStyle("-fx-background-color: " + SECTION_BG + "; -fx-border-color: " + ACCENT_COLOR + "; -fx-border-width: 1 0 0 0;");
        Label statusLabel = new Label("Ready");
        statusLabel.setTextFill(Color.web(TEXT_COLOR));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label versionLabel = new Label("Version 1.0.0");
        versionLabel.setTextFill(Color.web(TEXT_COLOR));
        statusBar.getChildren().addAll(statusLabel, spacer, versionLabel);
        return statusBar;
    }

    private void importTaxFile(String filePath) {
        try {
            List<TransactionRecord> transactions = TransactionImporter.importFromCSV(filePath);
            taxProcessor.setTransactions(transactions);
            updateTransactionTable();
            updateSummaryLabels();
            importedFilePath = filePath;
            showSuccessDialog("File Import Success", "The tax transaction file was imported successfully.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Import Error",
                    "Failed to import tax file", e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTransactionTable() {
        ObservableList<TransactionRecord> data =
                FXCollections.observableArrayList(taxProcessor.getTransactions());
        transactionTable.setItems(data);
        transactionTable.refresh();
    }

    private void updateSummaryLabels() {
        int[] counts = TransactionImporter.getRecordCounts(taxProcessor.getTransactions());
        summaryTotalLabel.setText("Total Records: " + counts[0]);
        summaryValidLabel.setText("Valid Records: " + counts[1]);
        summaryInvalidLabel.setText("Invalid Records: " + counts[2]);
    }

    private void editSelectedRecord() {
        TransactionRecord selectedRecord = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedRecord != null) {
            showEditDialog(selectedRecord);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "No Record Selected", "Please select a record to edit.");
        }
    }

    private void showEditDialog(TransactionRecord record) {
        Dialog<TransactionRecord> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction Record");
        dialog.setHeaderText("Edit the selected transaction record");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField billNumberField = new TextField(record.getBillNumber());
        TextField itemCodeField = new TextField(record.getItemCode());
        TextField internalPriceField = new TextField(record.getInternalPrice().toString());
        TextField discountField = new TextField(record.getDiscount().toString());
        TextField salePriceField = new TextField(record.getSalePrice().toString());
        TextField quantityField = new TextField(String.valueOf(record.getQuantity()));

        grid.add(new Label("Bill Number:"), 0, 0);
        grid.add(billNumberField, 1, 0);
        grid.add(new Label("Item Code:"), 0, 1);
        grid.add(itemCodeField, 1, 1);
        grid.add(new Label("Internal Price:"), 0, 2);
        grid.add(internalPriceField, 1, 2);
        grid.add(new Label("Discount:"), 0, 3);
        grid.add(discountField, 1, 3);
        grid.add(new Label("Sale Price:"), 0, 4);
        grid.add(salePriceField, 1, 4);
        grid.add(new Label("Quantity:"), 0, 5);
        grid.add(quantityField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    TransactionRecord updatedRecord = new TransactionRecord(
                            billNumberField.getText(),
                            itemCodeField.getText(),
                            new BigDecimal(internalPriceField.getText()),
                            new BigDecimal(discountField.getText()),
                            new BigDecimal(salePriceField.getText()),
                            Integer.parseInt(quantityField.getText()),
                            record.getLineTotal(),
                            record.getOriginalChecksum()
                    );
                    return updatedRecord;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Input Error",
                            "Invalid Input", "Please enter valid numeric values.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedRecord -> {
            int index = transactionTable.getSelectionModel().getSelectedIndex();
            updateRecord(index, updatedRecord);
        });
    }

    private void deleteSelectedRecord() {
        TransactionRecord selectedRecord = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedRecord != null) {
            int index = transactionTable.getSelectionModel().getSelectedIndex();
            taxProcessor.deleteRecord(index);
            updateTransactionTable();
            updateSummaryLabels();
            saveToFile();
            showSuccessDialog("Record Deleted", "The selected record was successfully deleted.");
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "No Record Selected", "Please select a record to delete.");
        }
    }

    private void deleteZeroProfitRecords() {
        taxProcessor.deleteZeroProfitRecords();
        updateTransactionTable();
        updateSummaryLabels();
        saveToFile();
        showSuccessDialog("Operation Complete", "All records with zero profit have been deleted.");
    }

    private void updateRecord(int index, TransactionRecord record) {
        taxProcessor.updateRecord(index, record);
        updateTransactionTable();
        updateSummaryLabels();
        saveToFile();
    }

    private void calculateTax() {
        try {
            BigDecimal taxRate = new BigDecimal(taxRateField.getText());
            taxProcessor.setTaxRate(taxRate);
            BigDecimal finalTax = taxProcessor.calculateFinalTax();
            taxRateLabel.setText("Tax Rate: " + taxRate + "%");
            totalTaxLabel.setText("Total Tax: LKR " + finalTax.toString());
            showTaxSummary(taxRate, finalTax);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error",
                    "Invalid Tax Rate", "Please enter a valid tax rate percentage.");
        }
    }

    private void showTaxSummary(BigDecimal taxRate, BigDecimal finalTax) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Tax Calculation Results");
        dialog.setHeaderText("Tax Summary Information");
        dialog.initModality(Modality.APPLICATION_MODAL);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        Label taxRateLabel = new Label("Tax Rate: " + taxRate + "%");
        taxRateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label finalTaxLabel = new Label("Final Tax Amount: LKR " + finalTax.toString());
        finalTaxLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + SUCCESS_COLOR + ";");
        content.getChildren().addAll(taxRateLabel, finalTaxLabel);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 10px;");
        dialog.showAndWait();
    }

    // Success dialog disappears after 4 seconds and can be closed by X
    private void showSuccessDialog(String title, String message) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label iconLabel = new Label("✓");
        iconLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-background-color: " + SUCCESS_COLOR + ";" +
                        "-fx-background-radius: 50px;" +
                        "-fx-padding: 10px 17px;" +
                        "-fx-font-size: 24px;"
        );
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px 0;");
        content.getChildren().addAll(iconLabel, messageLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        dialog.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> dialog.close());
        delay.play();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        alert.showAndWait();
    }

    // Save to the imported file
    private void saveToFile() {
        if (importedFilePath == null) {
            return;
        }
        try {
            TransactionExporter.exportToCSV(taxProcessor.getTransactions(), importedFilePath);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error",
                    "Failed to save tax data", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
