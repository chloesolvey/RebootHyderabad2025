package com.lloyds.onboard.Util;

public class MaskUtil {

    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email; // No masking needed
        }
        String maskedPart = "*".repeat(atIndex - 1);
        return maskedPart + email.substring(atIndex - 1);
    }

    public static String maskMobileNumber(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.length() < 4) {
            return mobileNumber; // No masking needed
        }
        String lastFourDigits = mobileNumber.substring(mobileNumber.length() - 4);
        return "*".repeat(mobileNumber.length() - 4) + lastFourDigits;
    }
}
