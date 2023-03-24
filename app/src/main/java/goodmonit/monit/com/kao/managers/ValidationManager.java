package goodmonit.monit.com.kao.managers;

import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.devices.BlePacketManager;

/**
 * Created by Jake on 2017-05-08.
 */

public class ValidationManager {
    private static final String TAG = Configuration.BASE_TAG + "ValidationManager";

    public static final String VALID_SPECIAL_CHARACTERS = ".!@#$%&^ *+=-(){}[]";
    private static final int MIN_LENGTH_NICKNAME    = 1;
    private static final int MAX_LENGTH_NICKNAME    = 12;
    private static final int MIN_LENGTH_PASSWORD    = 8;
    private static final int MAX_LENGTH_PASSWORD    = 64;

    public static final int PASSWORD_AVAILABLE              = 1;
    public static final int PASSWORD_NO_DIGIT               = 2;
    public static final int PASSWORD_NO_ALPHABET            = 3;
    public static final int PASSWORD_NO_SPECIAL_CHARACTER   = 4;
    public static final int PASSWORD_NO_NUMBER              = 5;
    public static final int PASSWORD_NO_LOWERCASE_ALPHABET  = 6;
    public static final int PASSWORD_NO_UPPERCASE_ALPHABET  = 7;

    private Context mContext;
    public ValidationManager(Context context) {
        mContext = context;
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.length() == 0) {
            return false;
        }

        String[] seperated = email.split("@");
        if (seperated.length != 2) {
            return false;
        } else {
            if (seperated[0].length() == 0 || seperated[1].length() == 0) {
                return false;
            }
        }

        return seperated[1].contains(".");
    }

    public boolean isValidAccountID(String accountId) {
        if (accountId == null || accountId.length() == 0) {
            return false;
        }

        if (accountId.length() > 3 && accountId.length() < 40) {
            return true;
        } else {
            return false;
        }
    }

    public int isValidPassword(String password) {
        if (password == null ||
                password.length() == 0 ||
                password.length() < MIN_LENGTH_PASSWORD ||
                password.length() > MAX_LENGTH_PASSWORD) {
            return PASSWORD_NO_DIGIT;
        }

        boolean containsUpperAlphabet = false;
        boolean containsLowerAlphabet = false;
        boolean containsSpecialCharacter = false;
        boolean containsNumber = false;
        for(int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c >= 'a' && c <= 'z') {
                containsLowerAlphabet = true;
            }

            if (c >= 'A' && c <= 'Z') {
                containsUpperAlphabet = true;
            }

            if (c >= '0' && c <= '9') {
                containsNumber = true;
            }

            if (!containsSpecialCharacter) {
                for (int j = 0; j < VALID_SPECIAL_CHARACTERS.length(); j++) {
                    char s = VALID_SPECIAL_CHARACTERS.charAt(j);
                    if (s == c) {
                        containsSpecialCharacter = true;
                        break;
                    }
                }
            }
        }

        if (!containsNumber) {
            return PASSWORD_NO_NUMBER;
        }

        if (!containsUpperAlphabet) {
            return PASSWORD_NO_UPPERCASE_ALPHABET;
        }

        if (!containsLowerAlphabet) {
            return PASSWORD_NO_LOWERCASE_ALPHABET;
        }

        if (!containsSpecialCharacter) {
            return PASSWORD_NO_SPECIAL_CHARACTER;
        }

        return PASSWORD_AVAILABLE;
    }

    public boolean isValidPasswordConfirmation(String password, String confirmation) {
        if (password == null || password.length() == 0) {
            return false;
        }

        return password.equals(confirmation);

    }

    public boolean isValidNickname(String nickname) {
        return !(nickname == null ||
                nickname.length() == 0 ||
                nickname.length() < MIN_LENGTH_NICKNAME ||
                nickname.length() > MAX_LENGTH_NICKNAME);
    }

    public boolean isValidShortId(String shortId) {
        if (shortId == null) return false;
        if (shortId.length() != 6) return false;
        shortId = shortId.toUpperCase();
        for (int i = 0; i < shortId.length(); i++) {
            char c = shortId.charAt(i);
            if ((c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9')) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean isValidBabyname(String name) {
        if (name == null || name.length() < 1) return false;

        byte[] byteName = null;
        try {
            byteName = name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            byteName = name.getBytes();
        }
        return byteName.length <= BlePacketManager.MAX_BYTE_LENGTH_NAME;
    }

    public boolean isValidRoomname(String name) {
        if (name == null || name.length() < 1) return false;

        byte[] byteName = null;
        try {
            byteName = name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            byteName = name.getBytes();
        }
        return byteName.length <= BlePacketManager.MAX_BYTE_LENGTH_NAME;
    }

    public boolean isValidRecommenderId(String id) {
        return id != null && id.length() == 6;
    }

    public boolean isValidProductNumber(String productNumber) {
        return productNumber != null && productNumber.length() == 12;
    }
    /**
     *  English + Number + Special Character Validation
     */
    public boolean textValidate(String str) {
        String Passwrod_PATTERN = "^(?=.*[a-zA-Z]+)(?=.*[0-9]+).{1,6}$";
        Pattern pattern = Pattern.compile(Passwrod_PATTERN);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}