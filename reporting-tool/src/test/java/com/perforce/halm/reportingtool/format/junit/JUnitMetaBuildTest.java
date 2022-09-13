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

import com.perforce.halm.reportingtool.format.junit.parser.JUnitJAXBParser;
import com.perforce.halm.reportingtool.models.BuildMetadata;
import com.perforce.halm.rest.types.IDLabelPair;
import com.perforce.halm.rest.types.NameValuePair;
import com.perforce.halm.rest.types.automation.build.AutomationBuild;
import com.perforce.halm.rest.types.automation.jenkins.*;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintStream;
import java.util.*;

/**
 * Unit test class for verifying JUnitMetaBuild loading of XML data and metadata to an AutomationBuild
 */
class JUnitMetaBuildTest extends JUnitMetaTestCommon {
    JUnitMetaBuildTest() {
        super(Arrays.asList(
            "invalid.xml",
            "single_suite_metadata.xml",
            "single_suite_no_tests.xml",
            "wrapper_two_suites_no_tests.xml"
        ));
    }

    @Test void processInvalidFile() {
        // The JUnitMetaBuild should throw an exception parsing it
        List<String> reportFiles = this.buildReportFileList("invalid.xml");
        JUnitJAXBParser parser = new JUnitJAXBParser(reportFiles);
        // Suppress expected exception output
        PrintStream systemErrStream = System.err;
        System.setErr(new java.io.PrintStream(new java.io.OutputStream(){public void write(int i){}}));
        assertThrows(JAXBException.class, parser::parseReportFiles, "Exception expected when parsing invalid XML.");
        // Restore exception output
        System.setErr(systemErrStream);
    }

    @Test void processSuiteWithMetadata() {
        // Generate some basic metadata
        String description = "This is the best build ever.";
        String branch = "Mainline";
        String url = "https://www.perforce.com";
        String runSetLabel = "My Run Set";
        BuildMetadata metadata = new BuildMetadata();
        metadata.setDescription(description);
        metadata.setBranch(branch);
        metadata.setExternalURL(url);
        metadata.setTestRunSet(new IDLabelPair(0, runSetLabel));

        // Generate some Jenkins-specific metadata
        String jenkinsProjectName = "Automation Tests";
        String jenkinsAuthToken = "token123ABC";
        String textBuildParameter = "Text Build Parameter";
        String passBuildParameter = "Password123";
        AutomationBuildRunConfigurationJenkins jenkinsRunConfig = new AutomationBuildRunConfigurationJenkins();
        List<JenkinsBuildParameter> jenkinsBuildParams = new ArrayList<>();
        JenkinsBuildParameterText textParam = new JenkinsBuildParameterText();
        textParam.setText(textBuildParameter);
        jenkinsBuildParams.add(textParam);
        JenkinsBuildParameterPassword passwordParam = new JenkinsBuildParameterPassword();
        passwordParam.setPassword(passBuildParameter);
        jenkinsBuildParams.add(passwordParam);
        jenkinsBuildParams.add(new JenkinsBuildParameterIgnore());
        jenkinsRunConfig.getJenkins().setBuildParameters(jenkinsBuildParams);
        metadata.setRunConfigurationInfo(jenkinsRunConfig);

        // Generate build properties to store
        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put("releaseVersion", "2023.1");
        expectedProperties.put("releaseBuild", "12");
        expectedProperties.put("packageID", "MetadataSuite/package");
        expectedProperties.put("botID", "automation1");
        for (Map.Entry<String, String> property : expectedProperties.entrySet()) {
            metadata.addProperty(new NameValuePair(property.getKey(), property.getValue()));
        }

        // The AutomationBuild should be created with our expected metadata in place
        AutomationBuild build = this.generateAutomationBuild("Build 1", metadata, "single_suite_metadata.xml");
        assertEquals("2022-05-14T04:40:51", build.getStartDate());
        assertEquals(123, build.getDuration().intValue());
        assertEquals(description, build.getDescription());
        assertEquals(branch, build.getBranch());
        assertEquals(url, build.getExternalURL());
        assertEquals(runSetLabel, build.getTestRunSet().getLabel());
        assertEquals(AutomationBuildRunConfigurationJenkins.TYPE_VALUE, build.getRunConfigurationInfo().getType());
        AutomationBuildRunConfigurationJenkins runConfig = (AutomationBuildRunConfigurationJenkins)build.getRunConfigurationInfo();
        assertJenkinsRunConfigurationsMatch(jenkinsRunConfig, runConfig);
        this.assertPropertiesMatch(expectedProperties, build.getProperties());

        // We are going to verify the content of the Jenkins default build parameters here, since we know what we added.
        if (runConfig != null) {
            assertNotNull(runConfig.getJenkins().getBuildParameters());
            for (JenkinsBuildParameter param : runConfig.getJenkins().getBuildParameters()) {
                switch (param.getType()) {
                    case JenkinsBuildParameterText.TYPE_VALUE:
                        JenkinsBuildParameterText tmpParamText = (JenkinsBuildParameterText)param;
                        assertNotNull(tmpParamText);
                        assertEquals(textBuildParameter, tmpParamText.getText());
                        break;
                    case JenkinsBuildParameterPassword.TYPE_VALUE:
                        JenkinsBuildParameterPassword tmpParamPass = (JenkinsBuildParameterPassword)param;
                        assertNotNull(tmpParamPass);
                        assertEquals(passBuildParameter, tmpParamPass.getPassword());
                        break;
                    case JenkinsBuildParameterIgnore.TYPE_VALUE:
                        JenkinsBuildParameterIgnore tmpParamIgnore = (JenkinsBuildParameterIgnore)param;
                        assertNotNull(tmpParamIgnore);
                        break;
                }
            }
        }
    }

    void assertJenkinsRunConfigurationsMatch(final AutomationBuildRunConfigurationJenkins expected, final AutomationBuildRunConfigurationJenkins config) {
        assertNotNull(expected);
        assertNotNull(config);
        assertEquals(expected.getJenkins().getBuildParameters() != null, config.getJenkins().getBuildParameters() != null);
        assertEquals(expected.getJenkins().getBuildParameters().size(), config.getJenkins().getBuildParameters().size());
    }

    @Test void processSuiteNoMetadata() {
        // The AutomationBuild should be created with no metadata in place
        AutomationBuild build = this.generateAutomationBuild("Build 2", null, "single_suite_no_tests.xml");
        assertNull(build.getDescription());
        assertNull(build.getBranch());
        assertNull(build.getExternalURL());
        assertNull(build.getTestRunSet());
        assertNull(build.getRunConfigurationInfo());
        assertNull(build.getProperties());
    }

    @Test void processSuiteNoTests() {
        // The AutomationBuild should be created, but have no results
        AutomationBuild build = this.generateAutomationBuild("Build 2", null, "single_suite_no_tests.xml");
        assertNull(build.getResults());
    }

    @Test void processWrapperTwoSuitesNoTests() {
        // Multiple AutomationBuilds should be created, but none of them should have results
        AutomationBuild build = this.generateAutomationBuild("Build 3", null, "wrapper_two_suites_no_tests.xml");
        assertNull(build.getResults());
    }

    @Test void processSuiteAndWrapperNoTests() {
        // This combines the above two tests, ensuring we can handle multiple files and multiple types of files.
        AutomationBuild build = this.generateAutomationBuild("Build 4", null, "single_suite_no_tests.xml", "wrapper_two_suites_no_tests.xml");
        assertNull(build.getResults());
        assertEquals("2022-05-14T04:36:20", build.getStartDate());
        assertEquals(60000, build.getDuration().longValue());
    }
}
