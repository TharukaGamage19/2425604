package javaApplication;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**     JavaFX Application for Government Tax Department System       */
public class JavaFXMain extends Application {

    private Stage primaryStage;
    private TableView<TransactionRecord> transactionTable;
    private TaxProcessor taxProcessor;
    private Label statusLabel;
    private Label taxSummaryLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.taxProcessor = new TaxProcessor();

        primaryStage.setTitle("Government Tax Department System");

        // Create the main layout
        BorderPane borderPane = new BorderPane();

        // Create menu
        MenuBar menuBar = createMenuBar();
        borderPane.setTop(menuBar);

        // Create table
        transactionTable = createTransactionTable();
        VBox tableContainer = new VBox(10);
        tableContainer.setPadding(new Insets(10));
        tableContainer.getChildren().add(new Label("Transaction Records:"));
        tableContainer.getChildren().add(transactionTable);

        // Status label
        statusLabel = new Label("No file imported");
        tableContainer.getChildren().add(statusLabel);

        // Tax summary label
        taxSummaryLabel = new Label("Tax information will appear here");
        tableContainer.getChildren().add(taxSummaryLabel);

        borderPane.setCenter(tableContainer);

        // Create toolbar
        HBox toolbar = createToolbar();
        borderPane.setBottom(toolbar);

        Scene scene = new Scene(borderPane, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem importItem = new MenuItem("Import Tax File");
        importItem.setOnAction(e -> importTaxFile());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(importItem, new SeparatorMenuItem(), exitItem);

        // Operations menu
        Menu operationsMenu = new Menu("Operations");
        MenuItem deleteInvalidItem = new MenuItem("Delete Selected Record");
        deleteInvalidItem.setOnAction(e -> deleteSelectedRecord());
        MenuItem deleteZeroProfitItem = new MenuItem("Delete Zero Profit Records");
        deleteZeroProfitItem.setOnAction(e -> deleteZeroProfitRecords());
        MenuItem calculateTaxItem = new MenuItem("Calculate Tax");
        calculateTaxItem.setOnAction(e -> showTaxRateDialog());
        operationsMenu.getItems().addAll(deleteInvalidItem, deleteZeroProfitItem, calculateTaxItem);

        menuBar.getMenus().addAll(fileMenu, operationsMenu);
        return menuBar;
    }

    private TableView<TransactionRecord> createTransactionTable() {
        TableView<TransactionRecord> table = new TableView<>();

        // Make table editable
        table.setEditable(true);

        // Create columns
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

        TableColumn<TransactionRecord, BigDecimal> lineTotalCol = new TableColumn<>("Line Total");
        lineTotalCol.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        TableColumn<TransactionRecord, Integer> checksumCol = new TableColumn<>("Checksum");
        checksumCol.setCellValueFactory(new PropertyValueFactory<>("originalChecksum"));

        TableColumn<TransactionRecord, Boolean> validCol = new TableColumn<>("Valid");
        validCol.setCellValueFactory(new PropertyValueFactory<>("valid"));

        TableColumn<TransactionRecord, BigDecimal> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));

        table.getColumns().addAll(
                billNumberCol, itemCodeCol, internalPriceCol, discountCol,
                salePriceCol, quantityCol, lineTotalCol, checksumCol, validCol, profitCol);

        return table;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));

        Button importButton = new Button("Import Tax File");
        importButton.setOnAction(e -> importTaxFile());

        Button deleteButton = new Button("Delete Selected Record");
        deleteButton.setOnAction(e -> deleteSelectedRecord());

        Button calculateTaxButton = new Button("Calculate Tax");
        calculateTaxButton.setOnAction(e -> showTaxRateDialog());

        toolbar.getChildren().addAll(importButton, deleteButton, calculateTaxButton);
        return toolbar;
    }

    private void importTaxFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Tax File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                List<TransactionRecord> transactions = TransactionImporter.importFromCSV(file.getAbsolutePath());
                taxProcessor.setTransactions(transactions);

                // Update table
                updateTransactionTable();

                // Update status
                int[] counts = TransactionImporter.getRecordCounts(transactions);
                statusLabel.setText(String.format(
                        "Total records: %d, Valid records: %d, Invalid records: %d",
                        counts[0], counts[1], counts[2]));

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Import Error",
                        "Failed to import tax file", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateTransactionTable() {
        ObservableList<TransactionRecord> data =
                FXCollections.observableArrayList(taxProcessor.getTransactions());
        transactionTable.setItems(data);
        transactionTable.refresh();
    }

    private void deleteSelectedRecord() {
        TransactionRecord selectedRecord = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedRecord != null) {
            int index = transactionTable.getSelectionModel().getSelectedIndex();
            taxProcessor.deleteRecord(index);
            updateTransactionTable();

            // Update status
            int[] counts = TransactionImporter.getRecordCounts(taxProcessor.getTransactions());
            statusLabel.setText(String.format(
                    "Total records: %d, Valid records: %d, Invalid records: %d",
                    counts[0], counts[1], counts[2]));
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "No Record Selected", "Please select a record to delete.");
        }
    }

    private void deleteZeroProfitRecords() {
        taxProcessor.deleteZeroProfitRecords();
        updateTransactionTable();

        // Update status
        int[] counts = TransactionImporter.getRecordCounts(taxProcessor.getTransactions());
        statusLabel.setText(String.format(
                "Total records: %d, Valid records: %d, Invalid records: %d",
                counts[0], counts[1], counts[2]));

        showAlert(Alert.AlertType.INFORMATION, "Operation Complete",
                "Zero Profit Records Deleted", "All records with zero profit have been deleted.");
    }

    private void updateRecord(int index, TransactionRecord record) {
        taxProcessor.updateRecord(index, record);
        updateTransactionTable();

        // Update status
        int[] counts = TransactionImporter.getRecordCounts(taxProcessor.getTransactions());
        statusLabel.setText(String.format(
                "Total records: %d, Valid records: %d, Invalid records: %d",
                counts[0], counts[1], counts[2]));
    }

    private void showTaxRateDialog() {
        Dialog<BigDecimal> dialog = new Dialog<>();
        dialog.setTitle("Tax Rate");
        dialog.setHeaderText("Enter Tax Rate Percentage");

        ButtonType calculateButtonType = new ButtonType("Calculate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(calculateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField taxRateField = new TextField();
        taxRateField.setPromptText("Tax Rate %");

        grid.add(new Label("Tax Rate (%):"), 0, 0);
        grid.add(taxRateField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == calculateButtonType) {
                try {
                    return new BigDecimal(taxRateField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(taxRate -> {
            taxProcessor.setTaxRate(taxRate);
            BigDecimal finalTax = taxProcessor.calculateFinalTax();
            taxSummaryLabel.setText(String.format(
                    "Tax Rate: %s%%, Final Tax: $%s",
                    taxRate.toString(), finalTax.toString()));
            taxSummaryLabel.setStyle("-fx-font-weight: bold; -fx-background-color: lightyellow; -fx-padding: 5px;");

            showTaxSummary(taxRate, finalTax);
        });
    }

    private void showTaxSummary(BigDecimal taxRate, BigDecimal finalTax) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Tax Calculation Results");
        dialog.setHeaderText("Tax Summary Information");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label taxRateLabel = new Label("Tax Rate: " + taxRate + "%");
        taxRateLabel.setStyle("-fx-font-weight: bold");

        Label finalTaxLabel = new Label("Final Tax Amount: $" + finalTax.toString());
        finalTaxLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: darkgreen");

        content.getChildren().addAll(taxRateLabel, finalTaxLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}