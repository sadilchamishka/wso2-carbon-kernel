/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.ldap;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.util.LDAPUtil;

import java.time.format.DateTimeParseException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.dateAndTimePattern;

/**
 * Unit tests for UniqueIDReadOnlyLDAPUserStoreManager class.
 */
public class UniqueIDReadOnlyLDAPUserStoreManagerTest {

    @Mock
    private RealmConfiguration realmConfig;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test directly the convertToStandardTimeFormat method with all supported formats.
     */
    @Test
    public void testConvertToStandardTimeFormat() throws Exception {

        // Configure realm config for testing - no custom pattern.
        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn(null);

        // Test all supported LDAP timestamp formats:
        // "uuuuMMddHHmmss,SSSX" - 14 digits + ,3digits + timezone.
        assertEquals("2025-08-13T14:56:07.123Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,123Z"));
        assertEquals("2025-08-13T14:56:07.123Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,123+0000"));
        assertEquals("2025-08-13T12:56:07.123Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,123+0200"));

        // "uuuuMMddHHmmss.SSSX" - 14 digits + .3digits + timezone.
        assertEquals("2025-08-13T14:56:07.456Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.456Z"));
        assertEquals("2025-08-13T14:56:07.456Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.456+0000"));
        assertEquals("2025-08-13T12:56:07.456Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.456+0200"));

        // "uuuuMMddHHmmss,SSX" - 14 digits + ,2digits + timezone.
        assertEquals("2025-08-13T14:56:07.120Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,12Z"));
        assertEquals("2025-08-13T14:56:07.120Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,12+0000"));
        assertEquals("2025-08-13T12:56:07.120Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,12+0200"));

        // "uuuuMMddHHmmss.SSX" - 14 digits + .2digits + timezone.
        assertEquals("2025-08-13T14:56:07.780Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.78Z"));
        assertEquals("2025-08-13T14:56:07.780Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.78+0000"));
        assertEquals("2025-08-13T12:56:07.780Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.78+0200"));

        // "uuuuMMddHHmmss,SX" - 14 digits + ,1digit + timezone.
        assertEquals("2025-08-13T14:56:07.900Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,9Z"));
        assertEquals("2025-08-13T14:56:07.900Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,9+0000"));
        assertEquals("2025-08-13T12:56:07.900Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,9+0200"));

        // "uuuuMMddHHmmss.SX" - 14 digits + .1digit + timezone.
        assertEquals("2025-08-13T14:56:07.800Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.8Z"));
        assertEquals("2025-08-13T14:56:07.800Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.8+0000"));
        assertEquals("2025-08-13T12:56:07.800Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.8+0200"));

        // Test null and empty.
        assertNull(LDAPUtil.convertToStandardTimeFormat(realmConfig, null));
        assertEquals("", LDAPUtil.convertToStandardTimeFormat(realmConfig, ""));

        // Test basic format without fractional seconds.
        assertEquals("2025-08-13T14:56:07Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607Z"));
        assertEquals("2025-08-13T14:56:07Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607+0000"));
        assertEquals("2025-08-13T12:56:07Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607+0200"));
    }

    /**
     * Test convertToStandardTimeFormat with custom date pattern configured.
     */
    @Test
    public void testConvertToStandardTimeFormat_WithCustomPattern() throws Exception {

        // Configure realm config with custom pattern.
        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn("uuuuMMddHHmmssX");

        // Test with custom pattern - should use configured pattern instead of inference.
        assertEquals("2025-08-13T14:56:07Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607Z"));
    }

    /**
     * Test convertToStandardTimeFormat with the RFC 4517 minute-precision fraction (12-digit base).
     * When the minute is the last time component, a trailing fraction is a fraction of a MINUTE
     * (one digit d => d * 6 whole seconds), not a fraction of a second.
     */
    @Test
    public void testConvertToStandardTimeFormat_MinuteFraction() throws Exception {

        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn(null);

        assertEquals("2025-08-13T14:56:30Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "202508131456,5Z"));
        assertEquals("2025-08-13T14:56:30Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "202508131456.5Z"));
        // Offset must be preserved through the fraction-to-seconds splice (14:56:30 at +05:30 => 09:26:30Z).
        assertEquals("2025-08-13T09:26:30Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "202508131456,5+0530"));
        assertEquals("2025-08-13T14:56:54Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "202508131456,9Z"));
        assertEquals("2025-08-13T14:56:00Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "202508131456,0Z"));
    }

    /**
     * Test convertToStandardTimeFormat with 2-digit (hour-only) timezone offsets, which RFC 4517 permits.
     * These must be accepted across all supported shapes.
     */
    @Test
    public void testConvertToStandardTimeFormat_TwoDigitOffset() throws Exception {

        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn(null);

        // 14:56:07 at +05:00 => 09:56:07Z.
        assertEquals("2025-08-13T09:56:07Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607+05"));
        // 14:56:07.123 at +05:00 => 09:56:07.123Z.
        assertEquals("2025-08-13T09:56:07.123Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.123+05"));
        // Minute-fraction with 2-digit offset: 14:56:30 at +05:00 => 09:56:30Z.
        assertEquals("2025-08-13T09:56:30Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "202508131456,5+05"));
    }

    /**
     * Test that convertToStandardTimeFormat throws a DateTimeParseException for an unsupported timestamp format,
     * so the failure is surfaced (and aborts retrieval) rather than being silently swallowed.
     */
    @Test
    public void testConvertToStandardTimeFormat_UnsupportedFormatThrows() throws Exception {

        // Configure realm config for testing - no custom pattern.
        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn(null);

        try {
            LDAPUtil.convertToStandardTimeFormat(realmConfig, "invalid-timestamp");
            throw new AssertionError("Expected DateTimeParseException was not thrown");
        } catch (DateTimeParseException e) {
            assertTrue(e.getMessage().contains("Unsupported LDAP timestamp format"),
                    "Error message should mention unsupported timestamp format");
        }

        try {
            LDAPUtil.convertToStandardTimeFormat(realmConfig, "202508131456075Z");
            throw new AssertionError("Expected DateTimeParseException was not thrown");
        } catch (DateTimeParseException e) {
            assertTrue(e.getMessage().contains("Unsupported LDAP timestamp format"),
                    "Error message should mention unsupported timestamp format");
        }
    }

    /**
     * Test that convertToStandardTimeFormat throws a DateTimeParseException when the value does not match the
     * configured custom pattern.
     */
    @Test
    public void testConvertToStandardTimeFormat_InvalidCustomPatternThrows() throws Exception {

        // Configure realm config with custom pattern.
        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn("uuuuMMddHHmmssX");

        try {
            LDAPUtil.convertToStandardTimeFormat(realmConfig, "invalid-format-for-custom-pattern");
            throw new AssertionError("Expected DateTimeParseException was not thrown");
        } catch (DateTimeParseException e) {
            assertTrue(e.getMessage().contains("could not be parsed"),
                    "Error message should mention the timestamp could not be parsed");
        }
    }
}
