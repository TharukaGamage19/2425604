import static org.junit.jupiter.api.Assertions.*;

import javaApplication.TransactionImporter;
import javaApplication.TransactionRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

class TransactionImporterTest {

    @TempDir
    Path tempDir;

    @Test
    void getRecordCounts() throws IOException {
        //test CSV file
        File csvFile = tempDir.resolve("test_transactions_counts.csv").toFile();

        try (FileWriter writer = new FileWriter(csvFile)) {
            // Header line
            writer.write("BillNumber,ItemCode,InternalPrice,Discount,SalePrice,Quantity,LineTotal,Checksum\n");

            //valid transactions
            writer.write("B001,ITEM123,100,10,150,2,290,50\n");
            writer.write("B002,ITEM456,75,0,75,1,75,40\n");

            //invalid transactions
            writer.write("B003,ITEM@789,200,20,150,1,130,45\n"); // Invalid item code
            writer.write("B004,ITEM123,50,5,-70,3,-215,42\n"); // Negative sale price
            writer.write("B005,ITEM456,30,0,40,2,80,999\n"); // Invalid checksum
        }

        //Importing the transactions
        List<TransactionRecord> transactions = TransactionImporter.importFromCSV(csvFile.getAbsolutePath());

        int[] counts = TransactionImporter.getRecordCounts(transactions);

        // Verify counts
        assertEquals(5, counts[0]); // Total records

        assertEquals(counts[0], counts[1] + counts[2]);
    }
}