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

import com.perforce.halm.reportingtool.format.IMetaBuild;
import com.perforce.halm.reportingtool.format.UniqueNameTracker;
import com.perforce.halm.reportingtool.format.junit.parser.JUnitJAXBParser;
import com.perforce.halm.reportingtool.format.junit.parser.JUnitTestCase;
import com.perforce.halm.reportingtool.format.junit.parser.JUnitTestSuite;
import com.perforce.halm.reportingtool.models.BuildMetadata;
import com.perforce.halm.rest.types.automation.build.AutomationBuild;

import java.util.List;

/**
 * Build formatter object for handling JUnit XML build assets.
 * Responsible for initiating the parser, then creating Helix ALM automation build objects from the results.
 */
public class JUnitMetaBuild extends IMetaBuild {
    /**
     * Function that processes the specified report files, and returns an automation build object that can be sent to Helix ALM
     *
     * @param buildNumber The build number to specify when submitting the build
     * @param reportFiles The report files to format the build results for
     * @param buildMetadata The metadata for the build
     * @return See description
     */
    public AutomationBuild generateAutomationBuild(final String buildNumber, final List<String> reportFiles, final BuildMetadata buildMetadata) {
        try {
            JUnitJAXBParser parser = new JUnitJAXBParser(reportFiles);
            return convertTestSuitesIntoBuild(parser.parseReportFiles(), buildNumber, buildMetadata);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the JUnit test suites that were parsed from the XML report files into an AutomationBuild object
     *
     * @param testSuites The suites to convert
     * @param buildNumber The build number to specify when submitting the build
     * @param buildMetadata The metadata for the build
     * @return See description
     */
    private AutomationBuild convertTestSuitesIntoBuild(final List<JUnitTestSuite> testSuites,
                                                       final String buildNumber, final BuildMetadata buildMetadata) {
        if (!testSuites.isEmpty()) {
            // Create the build, and set unrelated metadata on the build object.
            AutomationBuild build = new AutomationBuild();
            setBuildMetadata(build, buildNumber, buildMetadata);

            // We want to keep track of the oldest and newest timestamps given, for setting startDate and duration values.
            // If timestamps are not provided, then we will have to assume sequential testing and just total the duration values.
            JUnitBuildTimingInfo timingInfo = new JUnitBuildTimingInfo();
            UniqueNameTracker uniqueNameTracker = new UniqueNameTracker();
            for (final JUnitTestSuite suite : testSuites) {
                // Update timing information
                timingInfo.updateTimeDataForTestSuite(suite);

                // Loop through the test cases in this suite, adding each one to the build.
                if (suite.getTestCases() != null) {
                    for (final JUnitTestCase testCase : suite.getTestCases()) {
                        // Before adding the result, if we don't have a duration from the suite update timings based on the result.
                        if (suite.getTime() <= 0) {
                            timingInfo.updateTimeDataForTestCase(testCase);
                        }
                        build.addResult(new JUnitMetaResult(testCase, suite).formatResult(uniqueNameTracker));
                    }
                }
            }

            // Now that we've processed all the suites and test cases, we can finally set our timing information on the build.
            timingInfo.setTimingInformationOnBuild(build);

            return build;
        }
        return null;
    }

    /**
     * Sets metadata on the build that is not related to the test suite or test case JUnit data.
     *
     * @param build The build to set data on
     * @param buildNumber The build number to specify when submitting the build
     * @param buildMetadata The metadata for the build
     */
    private void setBuildMetadata(AutomationBuild build, final String buildNumber, final BuildMetadata buildMetadata) {
        build.setNumber(buildNumber);
        if (buildMetadata != null) {
            JUnitUtils.setStringValueIfValid(buildMetadata::getDescription, build::setDescription);
            JUnitUtils.setStringValueIfValid(buildMetadata::getBranch, build::setBranch);
            JUnitUtils.setStringValueIfValid(buildMetadata::getExternalURL, build::setExternalURL);
            JUnitUtils.setStringValueIfValid(buildMetadata::getSourceOverride, build::setSourceOverride);
            JUnitUtils.setStringValueIfValid(buildMetadata::getPendingRunID, build::setPendingRunID);
            if (buildMetadata.getTestRunSet() != null) {
                build.setTestRunSet(buildMetadata.getTestRunSet());
            }
            if (buildMetadata.getRunConfigurationInfo() != null) {
                build.setRunConfigurationInfo(buildMetadata.getRunConfigurationInfo());
            }
            if (buildMetadata.getProperties() != null) {
                build.setProperties(buildMetadata.getProperties());
            }
        }
    }
}
