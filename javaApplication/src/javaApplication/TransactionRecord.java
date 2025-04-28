package javaApplication;

import java.math.BigDecimal;

//representing the imported transaction record
public class TransactionRecord {
    private String billNumber;
    private String itemCode;
    private BigDecimal internalPrice;
    private BigDecimal discount;
    private BigDecimal salePrice;
    private int quantity;
    private BigDecimal lineTotal;
    private int originalChecksum;
    private boolean isValid;
    private BigDecimal profit;

    public TransactionRecord(String billNumber, String itemCode, BigDecimal internalPrice,
                             BigDecimal discount, BigDecimal salePrice, int quantity,
                             BigDecimal lineTotal, int checksum) {
        this.billNumber = billNumber;
        this.itemCode = itemCode;
        this.internalPrice = internalPrice;
        this.discount = discount;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.originalChecksum = checksum;
        this.isValid = true; //setted "true" as default
        calculateProfit();
    }



     //Profit = (sale price * quantity-(discount * quantity)) – (internal price * quantity)
    public void calculateProfit() {
        BigDecimal internalTotal = internalPrice.multiply(new BigDecimal(quantity));
        BigDecimal discountTotal = discount.multiply(new BigDecimal(quantity));
        BigDecimal saleTotal = salePrice.multiply(new BigDecimal(quantity)).subtract(discountTotal);
        this.profit = saleTotal.subtract(internalTotal);
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public BigDecimal getInternalPrice() {
        return internalPrice;
    }

    public void setInternalPrice(BigDecimal internalPrice) {
        this.internalPrice = internalPrice;
        calculateLineTotal();
        calculateProfit();
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
        calculateLineTotal();
        calculateProfit();
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
        calculateLineTotal();
        calculateProfit();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateLineTotal();
        calculateProfit();
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public int getOriginalChecksum() {
        return originalChecksum;
    }

    public void setOriginalChecksum(int originalChecksum) {
        this.originalChecksum = originalChecksum;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "billNumber='" + billNumber + '\'' +
                ", itemCode='" + itemCode + '\'' +
                ", internalPrice=" + internalPrice +
                ", discount=" + discount +
                ", salePrice=" + salePrice +
                ", quantity=" + quantity +
                ", lineTotal=" + lineTotal +
                ", checksum=" + originalChecksum +
                ", isValid=" + isValid +
                ", profit=" + profit +
                '}';
    }

    //Creates a transaction line for checksum calculation
    public String toTransactionLine() {
        return billNumber + "," + itemCode + "," + internalPrice + "," + discount + "," +
                salePrice + "," + quantity + "," + lineTotal;
    }

    // Line Total = (sale price * quantity) - (discount * quantity)
    public void calculateLineTotal() {
        BigDecimal totalSalePrice = this.salePrice.multiply(new BigDecimal(quantity));
        BigDecimal totalDiscount = this.discount.multiply(new BigDecimal(quantity));
        this.lineTotal = totalSalePrice.subtract(totalDiscount);
    }

}