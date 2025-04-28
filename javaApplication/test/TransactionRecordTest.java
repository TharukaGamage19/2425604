import static org.junit.jupiter.api.Assertions.*;

import javaApplication.TransactionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class TransactionRecordTest {

    private TransactionRecord record;

    @BeforeEach
    void setUp() {
        // Create a sample transaction record
        record = new TransactionRecord(
                "B001", "ITEM123", new BigDecimal("100"),
                new BigDecimal("10"), new BigDecimal("150"), 2,
                new BigDecimal("290"), 50);
    }

    @Test
    void calculateProfit() {
        //Profit = (sale price * quantity-(discount * quantity)) – (internal price * quantity)
        // = (150 * 2 - (10 *2))- (100 * 2)
        // = 280 - 200 = 80

        //calculating the expected profit
        BigDecimal internalTotal = new BigDecimal("100").multiply(new BigDecimal("2"));
        BigDecimal discountTotal = new BigDecimal("10").multiply(new BigDecimal("2"));
        BigDecimal saleTotal = new BigDecimal("150").multiply(new BigDecimal("2")).subtract(discountTotal);
        BigDecimal expectedProfit = saleTotal.subtract(internalTotal);

        //recalculation
        record.calculateProfit();

        // Comparing
        assertEquals(expectedProfit, record.getProfit());
        assertEquals(new BigDecimal("80"), record.getProfit());
    }

    @Test
    void isValid() {
        // By default, isValid - true
        assertTrue(record.isValid());

        //setting it to false
        record.setValid(false);
        assertFalse(record.isValid());

        //setting it back to true
        record.setValid(true);
        assertTrue(record.isValid());
    }

    @Test
    void toTransactionLine() {
        // Expected format: billNumber,itemCode,internalPrice,discount,salePrice,quantity,lineTotal
        String expected = "B001,ITEM123,100,10,150,2,290";
        assertEquals(expected, record.toTransactionLine());

        // Changing values and test again
        record.setBillNumber("B999");
        record.setItemCode("NEWITEM");

        String newExpected = "B999,NEWITEM,100,10,150,2,290";
        assertEquals(newExpected, record.toTransactionLine());
    }

    @Test
    void calculateLineTotal() {
        // Line Total = (sale price * quantity) - (discount * quantity)
        // = (150 * 2) - (10 * 2) = 280

        // Reset line total
        record.setLineTotal(BigDecimal.ZERO);

        //recalculation
        record.calculateLineTotal();

        // Comparing
        assertEquals(new BigDecimal("280"), record.getLineTotal());

        // Change sale price
        record.setSalePrice(new BigDecimal("200"));
        record.calculateLineTotal();

        //so line total should be: (200 * 2) - (10 * 2) = 380
        assertEquals(new BigDecimal("380"), record.getLineTotal());

        // Change quantity
        record.setQuantity(3);
        record.calculateLineTotal();

        //so line total should be: (200 * 3) - (10 * 3) = 570
        assertEquals(new BigDecimal("570"), record.getLineTotal());

        // Change discount
        record.setDiscount(new BigDecimal("50"));
        record.calculateLineTotal();

        //so line total should be: (200 * 3) - (50 * 3) = 450
        assertEquals(new BigDecimal("450"), record.getLineTotal());
    }


    @Test
    void calculateProfitWithSetters() {
        // profit = 80 (from calculateProfit test)
        assertEquals(new BigDecimal("80"), record.getProfit());

        // Change internal price and verify profit updates
        record.setInternalPrice(new BigDecimal("80"));
        //  (150 * 2 - (10 *2))- (80 * 2) = 120
        assertEquals(new BigDecimal("120"), record.getProfit());

        // Change sale price and verify profit updates
        record.setSalePrice(new BigDecimal("200"));
        // = (200 * 2 - (10 *2))- (80 * 2)
        assertEquals(new BigDecimal("220"), record.getProfit());

        // Change quantity and verify profit updates
        record.setQuantity(3);
        // = (200 * 3 - (10 * 3))- (80 * 3)
        assertEquals(new BigDecimal("330"), record.getProfit());

        // Change discount and verify profit updates
        record.setDiscount(new BigDecimal("50"));
        // = (200 * 3 - (50 * 3))- (80 * 3)
        assertEquals(new BigDecimal("210"), record.getProfit());
    }
}