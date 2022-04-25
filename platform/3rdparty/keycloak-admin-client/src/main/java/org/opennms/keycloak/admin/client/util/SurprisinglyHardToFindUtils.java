package org.opennms.keycloak.admin.client.util;

import java.nio.charset.StandardCharsets;

/**
 * Please replace this utility class with a 3rd party implementation, if a good, reasonable one can be found.
 */
public class SurprisinglyHardToFindUtils {
    public static final SurprisinglyHardToFindUtils INSTANCE = new SurprisinglyHardToFindUtils();

    /**
     * Safely encode a single path segment for use in a URL path.  A path segment is one part of the path, separated
     * from other path segments by '/' characters.  A slash character on input will be converted to %2F.
     *
     * It appears URLEncoder.encode() is almost usable.  It is a superset with the exception of space which
     * URLEncoder().encode() converts to a `+` while `+` is a valid literal character in the path segment; therefore,
     * that method cannot be used.
     *
     * NOTE: this is NOT the encoding for query parameters nor form parameters.
     *
     * @param pathSegment
     * @return
     */
    public String encodeUrlPathSegment(String pathSegment) {
        StringBuilder result = new StringBuilder();

        for (byte oneByte : pathSegment.getBytes(StandardCharsets.UTF_8)) {
            char curChar = (char) oneByte;

            if (isValidUrlPathSegmentChar(curChar)) {
                result.append(curChar);
            } else {
                result.append('%');
                result.append(digitToHex(oneByte / 16));
                result.append(digitToHex(oneByte % 16));
            }
        }

        return result.toString();
    }

    public boolean isValidUrlPathSegmentChar(char candidate) {
        return Character.isAlphabetic(candidate) || Character.isDigit(candidate) || isValidUrlPathSegmentPunctuation(candidate);
    }

    public boolean isValidUrlPathSegmentPunctuation(char candidate) {
        switch (candidate) {
            case '-':
            case '.':
            case '_':
            case '~':
            case '!':
            case '$':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case ';':
            case '=':
                return true;
        }

        return false;
    }

    public char digitToHex(int digit) {
        switch (digit) {
            case 0: return '0';
            case 1: return '1';
            case 2: return '2';
            case 3: return '3';
            case 4: return '4';
            case 5: return '5';
            case 6: return '6';
            case 7: return '7';
            case 8: return '8';
            case 9: return '9';
            case 10: return 'A';
            case 11: return 'B';
            case 12: return 'C';
            case 13: return 'D';
            case 14: return 'E';
            case 15: return 'F';
        }

        throw new IllegalArgumentException("invalid digit, must be between 0 and 15");
    }
}
