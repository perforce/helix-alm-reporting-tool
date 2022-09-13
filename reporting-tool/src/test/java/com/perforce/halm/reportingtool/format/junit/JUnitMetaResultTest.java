/*
 * *****************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2022, Perforce Software, Inc.  
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of 
 * this software and associated documentation files (the "Software"), to deal in 
 * the Software without restriction, including without limitation the rights to use, 
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
 * Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 * SOFTWARE.
 * *****************************************************************************
 */

package com.perforce.halm.reportingtool.format.junit;

import com.perforce.halm.rest.types.automation.build.AutomationBuild;
import com.perforce.halm.rest.types.automation.build.AutomationResult;
import com.perforce.halm.rest.types.automation.build.AutomationResultStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for verifying JUnitMetaResult loading of XML data and metadata to an AutomationResult
 */
class JUnitMetaResultTest extends JUnitMetaTestCommon {
    JUnitMetaResultTest() {
        super(Arrays.asList(
            "single_case_invalid.xml",
            "single_case_metadata.xml"
        ));
    }

    @Test void processCaseInvalid() {
        // The AutomationBuild should be created, but have no results
        AutomationBuild build = this.generateAutomationBuild("Build 1", null, "single_case_invalid.xml");
        assertNull(build.getResults());
    }

    @Test void processCaseWithMetadata() {
        // The AutomationBuild should be created, and have one result with expected test result metadata
        AutomationBuild build = this.generateAutomationBuild("Build 2", null, "single_case_metadata.xml");
        assertEquals(1, build.getResults().size());
        AutomationResult result = build.getResults().get(0);
        assertNotNull(result);
        assertEquals("Login", result.getName());
        assertEquals("login_validation", result.getUniqueName());
        assertEquals(AutomationResultStatus.PASSED.id(), result.getStatus().getId());
        assertEquals("", result.getStatus().getLabel());
        assertEquals(3, result.getTags().size());
        assertTrue(result.getTags().contains("TC-1"));
        assertTrue(result.getTags().contains("TC-2"));
        assertTrue(result.getTags().contains("TC-3"));
        assertEquals("laptop", result.getDevice());
        assertEquals("Dell", result.getManufacturer());
        assertEquals("G15", result.getModel());
        assertEquals("Windows", result.getOS());
        assertEquals("10", result.getOSVersion());
        assertEquals("2022-05-14T04:36:20", result.getStartDate());
        assertEquals((long)123, result.getDuration());
        assertEquals("https://mybuilder/jenkins", result.getExternalURL());
        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put("classname", "login");
        expectedProperties.put("parameter", "xyz");
        this.assertPropertiesMatch(expectedProperties, result.getProperties());
    }
}
