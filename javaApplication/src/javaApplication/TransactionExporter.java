package javaApplication;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TransactionExporter {

    public static void exportToCSV(List<TransactionRecord> transactions, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.append("Bill Number,Item Code,Internal Price,Discount,Sale Price,Quantity,Line Total,Checksum\n");

            // Write each transaction
            for (TransactionRecord record : transactions) {
                writer.append(record.getBillNumber()).append(",")
                        .append(record.getItemCode()).append(",")
                        .append(record.getInternalPrice().toString()).append(",")
                        .append(record.getDiscount().toString()).append(",")
                        .append(record.getSalePrice().toString()).append(",")
                        .append(String.valueOf(record.getQuantity())).append(",")
                        .append(record.getLineTotal().toString()).append(",")
                        .append(String.valueOf(record.getOriginalChecksum())).append("\n");
            }
        }
    }
}
