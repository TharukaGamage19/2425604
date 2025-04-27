package javaApplication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//tax calculation
public class TaxProcessor {
    private List<TransactionRecord> transactions;
    private BigDecimal taxRate;

    public TaxProcessor() {
        this.transactions = new ArrayList<>();
        this.taxRate = BigDecimal.ZERO;
    }

    public void setTransactions(List<TransactionRecord> transactions) {
        this.transactions = transactions;
    }

    public List<TransactionRecord> getTransactions() {
        return transactions;
    }

    public List<TransactionRecord> getValidTransactions() {
        return transactions.stream()
                .filter(TransactionRecord::isValid)
                .collect(Collectors.toList());
    }

    public List<TransactionRecord> getInvalidTransactions() {
        return transactions.stream()
                .filter(record -> !record.isValid())
                .collect(Collectors.toList());
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }


    //to update records -Manager
    public void updateRecord(int index, TransactionRecord updatedRecord) {
        if (index >= 0 && index < transactions.size()) {
            updatedRecord.calculateLineTotal(); // lineTotal

            updatedRecord.calculateProfit(); //updated profit
            //calculating checksum again
            String transactionLine = updatedRecord.toTransactionLine();
            int newChecksum = TransactionValidator.calculateChecksum(transactionLine);
            updatedRecord.setOriginalChecksum(newChecksum);

            //record validating
            boolean isValid = TransactionValidator.validateTransaction(updatedRecord);
            updatedRecord.setValid(isValid);

            transactions.set(index, updatedRecord);
        }
    }


    //To delete invalid records
    public void deleteRecord(int index) {
        if (index >= 0 && index < transactions.size()) {
            transactions.remove(index);
        }
    }


    //deleting 0 profit transactions
    public void deleteZeroProfitRecords() {
        transactions.removeIf(record ->
                record.getProfit().compareTo(BigDecimal.ZERO) == 0);
    }

    //final tax
    public BigDecimal calculateFinalTax() {
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        for (TransactionRecord record : getValidTransactions()) {
            BigDecimal profit = record.getProfit();
            if (profit.compareTo(BigDecimal.ZERO) > 0) {
                totalProfit = totalProfit.add(profit);
            } else {
                totalLoss = totalLoss.add(profit.abs());
            }
        }

        BigDecimal taxableAmount = totalProfit.subtract(totalLoss);
        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
            taxableAmount = BigDecimal.ZERO;  // Can't have negative tax
        }

        return taxableAmount.multiply(taxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
    }
}