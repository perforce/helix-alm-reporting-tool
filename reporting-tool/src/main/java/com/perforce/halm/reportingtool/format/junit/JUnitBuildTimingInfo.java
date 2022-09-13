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

import com.perforce.halm.reportingtool.format.junit.parser.JUnitTestCase;
import com.perforce.halm.reportingtool.format.junit.parser.JUnitTestSuite;
import com.perforce.halm.rest.types.automation.build.AutomationBuild;

import java.time.Instant;

/**
 * Class that handles keeping track of build timing information while processing JUnit test suites and test cases
 */
public class JUnitBuildTimingInfo {
    private String earliestTimestampFormatted = "";
    private Instant earliestTimestamp = null;
    private Instant latestTimestamp = null;
    private Number totalDuration = 0;
    private Number latestDuration = 0;

    /**
     * Updates our timing information based on the passed in JUnit test suite
     *
     * @param suite The suite to update timing info for
     */
    public void updateTimeDataForTestSuite(final JUnitTestSuite suite) {
        if (suite != null && suite.getTimestamp() != null && !suite.getTimestamp().isEmpty()) {
            Instant startDate = this.getStartDate(suite);
            if (startDate != null) {
                if (this.earliestTimestamp == null || startDate.isBefore(this.earliestTimestamp)) {
                    this.earliestTimestamp = startDate;
                    this.earliestTimestampFormatted = suite.getTimestamp();
                }
                if (this.latestTimestamp == null || startDate.isAfter(this.latestTimestamp)) {
                    this.latestTimestamp = startDate;
                    this.latestDuration = suite.getTimeInMS().intValue();
                }
            }

            // If this is not set, and we have a time value, add it to our total duration value.
            if (this.earliestTimestamp == null) {
                this.totalDuration = this.totalDuration.intValue() + suite.getTimeInMS().intValue();
            }
        }
    }

    /**
     * Updates our timing information based on the passed in JUnit test case
     *
     * @param testCase The test case to update timing info for
     */
    public void updateTimeDataForTestCase(final JUnitTestCase testCase) {
        if (testCase != null && testCase.getTime() != null && testCase.getTime() > 0) {
            this.totalDuration = this.totalDuration.intValue() + testCase.getTimeInMS().intValue();
        }
    }

    /**
     * Sets our current timing information on the AutomationBuild object
     *
     * @param build The build object to set timing information on
     */
    public void setTimingInformationOnBuild(AutomationBuild build) {
        // If we have the earliest timestamp, set it now.
        if (!this.earliestTimestampFormatted.isEmpty()) {
            build.setStartDate(this.earliestTimestampFormatted);
        }
        // Either make a calculation for the determination, or if we have no way of telling just a sum of all durations.
        if (this.earliestTimestamp != null) {
            long diffSeconds = this.latestTimestamp.getEpochSecond() - this.earliestTimestamp.getEpochSecond();
            build.setDuration((diffSeconds * 1000) + this.latestDuration.intValue());
        } else {
            // No timestamps were provided, all we can do is return the sum, which will not be valid if tests are run in parallel.
            build.setDuration(this.totalDuration);
        }
    }

    /**
     * Gets the start date timestamp from the test suite, ignoring any exception we may encounter.
     *
     * @param suite The suite with the timestamp to parse
     * @return See description
     */
    private Instant getStartDate(JUnitTestSuite suite) {
        try {
            return JUnitUtils.parseISO8601StringToInstant(suite.getTimestamp());
        } catch (Exception e) {
            e.printStackTrace(); // We don't care if this throws an exception, we just want to ignore it if it does.
        }
        return null;
    }
}
