package org.opennms.keycloak.admin.client.util;

import junit.framework.TestCase;
import org.junit.Test;

public class SurprisinglyHardToFindUtilsTest extends TestCase {

    private SurprisinglyHardToFindUtils target = new SurprisinglyHardToFindUtils();

    public void testEncodeAlphaUrlPathSegment() {
        String result = target.encodeUrlPathSegment("abcdefghijklmnopqrstuvwxyz");
        assertEquals("abcdefghijklmnopqrstuvwxyz", result);
    }

    public void testEncodeNumericUrlPathSegment() {
        String result = target.encodeUrlPathSegment("0123456789");
        assertEquals("0123456789", result);
    }

    public void testEncodeValidPunctuationUrlPathSegment() {
        String result = target.encodeUrlPathSegment("-._.-");
        assertEquals("-._.-", result);
    }

    public void testEncodeInvalidPunctuationUrlPathSegment() {
        String result = target.encodeUrlPathSegment("?/");
        assertEquals("%3F%2F", result.toUpperCase());
    }

    public void testDigitToHex() {
        int cur = 0;
        while (cur < 10) {
            assertEquals(cur + '0', target.digitToHex(cur));
            cur++;
        }

        assertEquals('A', target.digitToHex(10));
        assertEquals('B', target.digitToHex(11));
        assertEquals('C', target.digitToHex(12));
        assertEquals('D', target.digitToHex(13));
        assertEquals('E', target.digitToHex(14));
        assertEquals('F', target.digitToHex(15));
    }

    public void testInvalidHexDigit() {
        try {
            target.digitToHex(16);
            fail("missing expected exception");
        } catch (Exception exc) {
            assertEquals("invalid digit, must be between 0 and 15", exc.getMessage());
        }
    }
}