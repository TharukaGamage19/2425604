import static org.junit.jupiter.api.Assertions.*;

import javaApplication.TaxProcessor;
import javaApplication.TransactionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

class TaxProcessorTest {

    private TaxProcessor taxProcessor;
    private List<TransactionRecord> testTransactions;

    @BeforeEach
    void setUp() {
        taxProcessor = new TaxProcessor();
        testTransactions = new ArrayList<>();


        //Valid record with profit
        TransactionRecord record1 = new TransactionRecord(
                "B001", "ITEM123", new BigDecimal("100"),
                new BigDecimal("10"), new BigDecimal("150"), 2,
                new BigDecimal("290"), 23);
        record1.setValid(true);

        //Valid record with zero profit
        TransactionRecord record2 = new TransactionRecord(
                "B002", "ITEM456", new BigDecimal("75"),
                new BigDecimal("0"), new BigDecimal("75"), 1,
                new BigDecimal("75"), 40);
        record2.setValid(true);

        //Valid record with loss
        TransactionRecord record3 = new TransactionRecord(
                "B003", "ITEM789", new BigDecimal("200"),
                new BigDecimal("20"), new BigDecimal("150"), 1,
                new BigDecimal("130"), 45);
        record3.setValid(true);

        //Invalid record
        TransactionRecord record4 = new TransactionRecord(
                "B004", "ITEM@456", new BigDecimal("50"),
                new BigDecimal("5"), new BigDecimal("70"), 3,
                new BigDecimal("205"), 42);
        record4.setValid(true);

        testTransactions.add(record1);
        testTransactions.add(record2);
        testTransactions.add(record3);
        testTransactions.add(record4);

        taxProcessor.setTransactions(testTransactions);
    }

    @Test
    void updateRecord() {
        //original transaction
        TransactionRecord original = taxProcessor.getTransactions().get(0);
        assertEquals("B001", original.getBillNumber());


        TransactionRecord modified = new TransactionRecord(
                original.getBillNumber(), original.getItemCode(),
                original.getInternalPrice(), original.getDiscount(),
                original.getSalePrice(), original.getQuantity(),
                original.getLineTotal(), original.getOriginalChecksum());

        // Changing a field
        modified.setBillNumber("B001-MODIFIED");

        // Updating a record
        taxProcessor.updateRecord(0, modified);

        TransactionRecord updated = taxProcessor.getTransactions().get(0);
        assertEquals("B001-MODIFIED", updated.getBillNumber());

        assertNotEquals(original.getOriginalChecksum(), updated.getOriginalChecksum());
    }

    @Test
    void deleteRecord() {

        int originalSize = taxProcessor.getTransactions().size();

        // Delete record (index;1)
        taxProcessor.deleteRecord(1);

        assertEquals(originalSize - 1, taxProcessor.getTransactions().size());

        // Verifing the record deleted was the right one
        for (TransactionRecord record : taxProcessor.getTransactions()) {
            assertNotEquals("B002", record.getBillNumber());
        }
    }

    @Test
    void deleteZeroProfitRecords() {
        //original size
        int originalSize = taxProcessor.getTransactions().size();

        //count of zero profit records
        long zeroProfit = taxProcessor.getTransactions().stream()
                .filter(r -> r.getProfit().compareTo(BigDecimal.ZERO) == 0)
                .count();

        // Deleting zero profit records
        taxProcessor.deleteZeroProfitRecords();

        assertEquals(originalSize - zeroProfit, taxProcessor.getTransactions().size());

        for (TransactionRecord record : taxProcessor.getTransactions()) {
            assertNotEquals(0, record.getProfit().compareTo(BigDecimal.ZERO));
        }
    }

    @Test
    void calculateFinalTax() {
        // tax rate - 20%
        taxProcessor.setTaxRate(new BigDecimal("20"));


        // Only valid records are considered for tax
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        for (TransactionRecord record : taxProcessor.getValidTransactions()) {
            BigDecimal profit = record.getProfit();
            if (profit.compareTo(BigDecimal.ZERO) > 0) {
                totalProfit = totalProfit.add(profit);
            } else {
                totalLoss = totalLoss.add(profit.abs());
            }
        }

        BigDecimal taxableAmount = totalProfit.subtract(totalLoss);
        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
            taxableAmount = BigDecimal.ZERO;
        }

        BigDecimal expectedTax = taxableAmount.multiply(
                new BigDecimal("20").divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
        );

        // Calculate tax using the method
        BigDecimal actualTax = taxProcessor.calculateFinalTax();

        // Comparing
        assertEquals(expectedTax, actualTax);
    }

}