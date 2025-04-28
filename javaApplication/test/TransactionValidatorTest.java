import static org.junit.jupiter.api.Assertions.*;

import javaApplication.TransactionRecord;
import javaApplication.TransactionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

class TransactionValidatorTest {

    //method to create TransactionRecord with an auto calculated checksum
    private TransactionRecord createRecord(String batchId, String itemCode,
                                           BigDecimal purchasePrice, BigDecimal discount,
                                           BigDecimal salePrice, int quantity,
                                           BigDecimal lineTotal) {
        TransactionRecord record = new TransactionRecord(batchId, itemCode, purchasePrice, discount, salePrice, quantity, lineTotal, 0);
        int checksum = TransactionValidator.calculateChecksum(record.toTransactionLine());
        record.setOriginalChecksum(checksum);
        return record;
    }

    @Test
    void validateTransaction() {
        // Creating a valid transaction record
        TransactionRecord validRecord = createRecord(
                "B001", "ITEM123", new BigDecimal("100"),
                new BigDecimal("10"), new BigDecimal("150"), 2,
                new BigDecimal("290")
        );

        // Test validation
        assertTrue(TransactionValidator.validateTransaction(validRecord));

        // Creating an invalid record (@- symbol)
        TransactionRecord invalidItemCodeRecord = createRecord(
                "B002", "ITEM@456", new BigDecimal("75"),
                new BigDecimal("0"), new BigDecimal("75"), 1,
                new BigDecimal("75")
        );

        // Should be invalid
        assertFalse(TransactionValidator.validateTransaction(invalidItemCodeRecord));

        // creating an invalid record (- sale price)
        TransactionRecord negativeSalePriceRecord = createRecord(
                "B003", "ITEM789", new BigDecimal("200"),
                new BigDecimal("20"), new BigDecimal("-150"), 1,
                new BigDecimal("-170")
        );

        // Should be invalid
        assertFalse(TransactionValidator.validateTransaction(negativeSalePriceRecord));

        // Create an invalid record (incorrect checksum)
        TransactionRecord invalidChecksumRecord = new TransactionRecord(
                "B004", "ITEM456", new BigDecimal("50"),
                new BigDecimal("5"), new BigDecimal("70"), 3,
                new BigDecimal("205"), 999 // Incorrect on purpose
        );

        // Should be invalid
        assertFalse(TransactionValidator.validateTransaction(invalidChecksumRecord));
    }

    @Test
    void calculateChecksum() {
        //simple string
        String simple = "ABC123";
        // 3 uppercase + 0 lowercase + 3 digits = 6
        assertEquals(6, TransactionValidator.calculateChecksum(simple));

        //mixed case
        String mixed = "Abc123";
        // 1 uppercase + 2 lowercase + 3 digits = 6
        assertEquals(6, TransactionValidator.calculateChecksum(mixed));

        //decimal points
        String withDecimal = "123.45,6.7";
        // 0 uppercase + 0 lowercase + 7 digits + 2 decimals = 9
        assertEquals(9, TransactionValidator.calculateChecksum(withDecimal));

        // real transaction line
        String transactionLine = "B001,ITEM123,100,10,150,2,290";
        // B(1) + ITEM(4) = 5 uppercase, digits = 18 (count each digit), total = 23
        assertEquals(23, TransactionValidator.calculateChecksum(transactionLine));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ITEM123", "item456", "Item_789", "A1B2C3"})
    void isValidItemCode_valid(String itemCode) {
        assertTrue(TransactionValidator.isValidItemCode(itemCode));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ITEM@123", "item-456", "Item 789", "A1B2C3!"})
    void isValidItemCode_invalid(String itemCode) {
        assertFalse(TransactionValidator.isValidItemCode(itemCode));
    }

    @Test
    void validateTransaction_allRules() {
        // Create a transaction that violates all rules
        TransactionRecord invalidRecord = new TransactionRecord(
                "B999", "ITEM@123", new BigDecimal("100"),
                new BigDecimal("10"), new BigDecimal("-50"), 2,
                new BigDecimal("-110"), 999 // Incorrect checksum
        );

        // Should be invalid
        assertFalse(TransactionValidator.validateTransaction(invalidRecord));

        // Fixing negative sale price
        invalidRecord.setSalePrice(new BigDecimal("50"));
        invalidRecord.calculateLineTotal();

        // Still invalid because of the invalid item code and checksum
        assertFalse(TransactionValidator.validateTransaction(invalidRecord));

        // Fix invalid item code
        invalidRecord.setItemCode("ITEM123");

        // Still invalid due to incorrect checksum
        assertFalse(TransactionValidator.validateTransaction(invalidRecord));

        // Fix checksum
        int correctChecksum = TransactionValidator.calculateChecksum(invalidRecord.toTransactionLine());
        invalidRecord.setOriginalChecksum(correctChecksum);

        // Now is valid
        assertTrue(TransactionValidator.validateTransaction(invalidRecord));
    }
}
