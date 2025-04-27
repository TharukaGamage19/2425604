package javaApplication;

import java.util.regex.Pattern;


public class TransactionValidator {
    public static boolean validateTransaction(TransactionRecord record) {
        // Rule 1- Checksum
        int calculatedChecksum = calculateChecksum(record.toTransactionLine());
        if (calculatedChecksum != record.getOriginalChecksum()) {
            return false;
        }

        // Rule 2- Item code with no special characters
        if (!isValidItemCode(record.getItemCode())) {
            return false;
        }

        // Rule 3: price > 0
        if (record.getSalePrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return false;
        }

        return true;
    }

    //checksum
    public static int calculateChecksum(String transactionLine) {
        int uppercaseCount = 0;
        int lowercaseCount = 0;
        int numberCount = 0;

        for (char c : transactionLine.toCharArray()) {
            if (Character.isUpperCase(c)) {
                uppercaseCount++;
            } else if (Character.isLowerCase(c)) {
                lowercaseCount++;
            } else if (Character.isDigit(c) || c == '.') {
                numberCount++;
            }
        }

        return uppercaseCount + lowercaseCount + numberCount;
    }


    //check if the itemcode is valid
    public static boolean isValidItemCode(String itemCode) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$"); //numbers,letters or "_"
        return pattern.matcher(itemCode).matches();
    }
}