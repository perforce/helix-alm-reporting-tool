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

import com.perforce.halm.reportingtool.models.BuildMetadata;
import com.perforce.halm.rest.types.NameValuePair;
import com.perforce.halm.rest.types.automation.build.AutomationBuild;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class JUnitMetaTestCommon {
    private String tempPath = "";
    private final List<String> testXMLFilenames;

    protected JUnitMetaTestCommon(List<String> testXMLFilenames) {
        this.testXMLFilenames = testXMLFilenames;
    }

    @BeforeAll
    void setup(@TempDir Path tempDir) {
        try {
            for (String fileName : this.testXMLFilenames) {
                Path tempFile = tempDir.resolve(fileName);
                InputStream input = getClass().getResourceAsStream("/junit/" + fileName);
                FileUtils.copyToFile(input, tempFile.toFile());
            }
            this.tempPath = tempDir + "/"; // Save off the temp file path, so it can be used to build full paths in the tests below.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a report file list from the specified filename strings
     *
     * @param fileNames The temporary file names
     * @return See description
     */
    protected List<String> buildReportFileList(String ... fileNames) {
        List<String> reportFiles = new ArrayList<>();
        for (String file : fileNames) {
            reportFiles.add(this.tempPath + file);
        }
        return reportFiles;
    }

    /**
     * Generates an automation build from the specified data
     *
     * @param buildNumber The build number to supply to the build
     * @param metadata Any build metadata to use
     * @param fileNames The report filenames to load
     * @return See description
     */
    protected AutomationBuild generateAutomationBuild(String buildNumber, BuildMetadata metadata, String ... fileNames) {
        List<String> reportFiles = this.buildReportFileList(fileNames);
        JUnitMetaBuild build = new JUnitMetaBuild();
        return assertDoesNotThrow(() -> build.generateAutomationBuild(buildNumber, reportFiles, metadata),
                "Unexpected exception encountered when parsing invalid XML.");
    }

    /**
     * Asserts that the specified properties match the expected properties
     *
     * @param expected The expected properties data
     * @param properties The properties list to check
     */
    protected void assertPropertiesMatch(final Map<String, String> expected, final List<NameValuePair> properties) {
        int found = 0;
        for (NameValuePair property : properties) {
            assertTrue(expected.containsKey(property.getName()));
            assertEquals(expected.get(property.getName()), property.getValue());
            if (expected.containsKey(property.getName())) {
                found++;
            }
        }
        assertEquals(properties.size(), found);
    }
}
