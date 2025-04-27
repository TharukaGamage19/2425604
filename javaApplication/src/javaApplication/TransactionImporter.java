package javaApplication;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//Imports and parses transactions
public class TransactionImporter {

    public static List<TransactionRecord> importFromCSV(String filePath) throws IOException {
        List<TransactionRecord> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skipped header
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                if (fields.length >= 8) {
                    String billNumber = fields[0];
                    String itemCode = fields[1];
                    BigDecimal internalPrice = new BigDecimal(fields[2]);
                    BigDecimal discount = new BigDecimal(fields[3]);
                    BigDecimal salePrice = new BigDecimal(fields[4]);
                    int quantity = Integer.parseInt(fields[5]);
                    BigDecimal lineTotal = new BigDecimal(fields[6]);
                    int checksum = Integer.parseInt(fields[7]);

                    TransactionRecord record = new TransactionRecord(
                            billNumber, itemCode, internalPrice, discount, salePrice, quantity, lineTotal, checksum);

                    record.calculateLineTotal();

                    // Validating the record
                    boolean isValid = TransactionValidator.validateTransaction(record);
                    record.setValid(isValid);

                    transactions.add(record);
                }
            }
        }

        return transactions;
    }


    //Counts the number of valid and invalid records in a list
    public static int[] getRecordCounts(List<TransactionRecord> transactions) {
        int totalRecords = transactions.size();
        int validRecords = 0;

        for (TransactionRecord record : transactions) {
            if (record.isValid()) {
                validRecords++;
            }
        }

        int invalidRecords = totalRecords - validRecords;

        return new int[] {totalRecords, validRecords, invalidRecords};
    }
}