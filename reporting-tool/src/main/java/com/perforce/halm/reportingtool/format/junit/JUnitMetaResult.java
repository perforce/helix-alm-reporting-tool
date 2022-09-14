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

import com.perforce.halm.reportingtool.format.IMetaResult;
import com.perforce.halm.reportingtool.format.UniqueNameTracker;
import com.perforce.halm.reportingtool.format.junit.parser.JUnitTestCase;
import com.perforce.halm.reportingtool.format.junit.parser.JUnitTestCaseError;
import com.perforce.halm.reportingtool.format.junit.parser.JUnitTestCaseFailure;
import com.perforce.halm.reportingtool.format.junit.parser.JUnitTestCaseSkipped;
import com.perforce.halm.reportingtool.format.junit.parser.JUnitTestSuite;
import com.perforce.halm.rest.types.IDLabelPair;
import com.perforce.halm.rest.types.NameValuePair;
import com.perforce.halm.rest.types.automation.build.AutomationResult;
import com.perforce.halm.rest.types.automation.build.AutomationResultStatus;

import java.util.Map;

/**
 * Formatter object for converting JUnit XML objects into Helix ALM build results.
 */
public class JUnitMetaResult implements IMetaResult {
    private JUnitTestCase testCase;

    private JUnitTestSuite testSuite;

    /**
     * Constructor
     */
    public JUnitMetaResult() {
    }

    /**
     * Private constructor, so the class cannot be created
     * @param testCase The test case to convert into a result
     * @param testSuite The test suite the test case belongs too
     */
    public JUnitMetaResult(final JUnitTestCase testCase,  final JUnitTestSuite testSuite) {
        this.testCase = testCase;
        this.testSuite = testSuite;
    }

    /**
     * @param testCase The test case we need to format
     */
    public void setTestCase(JUnitTestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * @param testSuite The test suite the test case belongs too
     */
    public void setTestSuite(JUnitTestSuite testSuite) {
        this.testSuite = testSuite;
    }

    /**
     * Formats a result object from the JUnitTestCase specified
     *
     * @param uniqueNameTracker The uniqueName tracker that ensures uniqueNames are actually unique across all results
     * @return See description
     */
    public AutomationResult formatResult(UniqueNameTracker uniqueNameTracker) {
        AutomationResult result = new AutomationResult();

        // Map known attributes to the automation result
        result.setName(this.testCase.getName());
        result.setDuration(this.testCase.getTimeInMS());

        // Map any non-standard attributes that match our result JSON properties
        Map<String, String> nonStandardAttributes = this.testCase.getNonStandardLocalAttributes();
        if (nonStandardAttributes != null) {
            // Set uniqueName and tags if they are specified
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "uniqueName", result::setUniqueName);
            JUnitUtils.setStringListPropertyFromNonStandardAttribute(nonStandardAttributes, "tags", ",", result::setTags);

            // Set other defined test result properties if they have matching XML attributes
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "device", result::setDevice);
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "manufacturer", result::setManufacturer);
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "model", result::setModel);
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "os", result::setOS);
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "osVersion", result::setOSVersion);
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "browser", result::setBrowser);
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "browserVersion", result::setBrowserVersion);
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "externalURL", result::setExternalURL);
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "errorMessage", result::setErrorMessage);
            JUnitUtils.setStringPropertyFromNonStandardAttribute(nonStandardAttributes, "startDate", result::setStartDate);

            // Map any remaining non-standard attributes over to the build properties list
            for (Map.Entry<String, String> attribute : nonStandardAttributes.entrySet()) {
                JUnitUtils.addPropertyForUnmappableAttribute(attribute.getKey(), attribute.getValue(), result::addProperty);
            }
        }

        // If no uniqueName attribute was supplied, this will generate a unique name for the automated test result as well.
        this.setUniqueName(result, uniqueNameTracker);

        // We want to specifically handle status last. If an errorMessage was already set, we want to store the status message as part of properties.
        this.setResultStatusFromTestCase(result);

        return result;
    }



    /**
     * Handles setting the result status values from the specified test case.
     *
     * @param result The result to set data on
     */
    protected void setResultStatusFromTestCase(AutomationResult result) {
        if (this.testCase.getError() != null) {
            this.setErrorResultStatus(result, this.testCase.getError());
        }
        else if (this.testCase.getFailure() != null) {
            this.setFailureResultStatus(result, this.testCase.getFailure());
        }
        else if (this.testCase.getSkipped() != null) {
            this.setSkippedResultStatus(result, this.testCase.getSkipped());
        }
        else {
            result.setStatus(new IDLabelPair(AutomationResultStatus.PASSED.id(), ""));
        }
    }

    /**
     * Sets the result status values from the specified error object
     *
     * @param result The result to set data on
     * @param error The error object to get data from
     */
    protected void setErrorResultStatus(AutomationResult result, JUnitTestCaseError error) {
        result.setStatus(new IDLabelPair(AutomationResultStatus.FAILED.id(), ""));
        if (error.getMessage() != null && !error.getMessage().isEmpty()) {
            if (result.getErrorMessage() == null || result.getErrorMessage().isEmpty()) {
                result.setErrorMessage(error.getMessage());
            }
            else {
                result.addProperty(new NameValuePair("errorMessage", error.getMessage()));
            }
        }
        if (error.getType() != null && !error.getType().isEmpty()) {
            result.addProperty(new NameValuePair("errorType", error.getType()));
        }
        if (error.getValue() != null && !error.getValue().isEmpty()) {
            result.addProperty(new NameValuePair("errorValue", error.getValue()));
        }
    }

    /**
     * Sets the result status values from the specified failure object
     *
     * @param result The result to set data on
     * @param failure The failure object to get data from
     */
    protected void setFailureResultStatus(AutomationResult result, JUnitTestCaseFailure failure) {
        result.setStatus(new IDLabelPair(AutomationResultStatus.FAILED.id(), ""));
        if (failure.getMessage() != null && !failure.getMessage().isEmpty()) {
            if (result.getErrorMessage() == null || result.getErrorMessage().isEmpty()) {
                result.setErrorMessage(failure.getMessage());
            }
            else {
                result.addProperty(new NameValuePair("failureMessage", failure.getMessage()));
            }
        }
        if (failure.getType() != null && !failure.getType().isEmpty()) {
            result.addProperty(new NameValuePair("failureType", failure.getType()));
        }
        if (failure.getValue() != null && !failure.getValue().isEmpty()) {
            result.addProperty(new NameValuePair("failureValue", failure.getValue()));
        }
    }

    /**
     * Sets the result status values from the specified skipped object
     *
     * @param result The result to set data on
     * @param skipped The skipped object to get data from
     */
    protected void setSkippedResultStatus(AutomationResult result, JUnitTestCaseSkipped skipped) {
        result.setStatus(new IDLabelPair(AutomationResultStatus.SKIPPED.id(), ""));
        if (skipped.getMessage() != null && !skipped.getMessage().isEmpty()) {
            if (result.getErrorMessage() == null || result.getErrorMessage().isEmpty()) {
                result.setErrorMessage(skipped.getMessage());
            }
            else {
                result.addProperty(new NameValuePair("skippedMessage", skipped.getMessage()));
            }
        }
    }

    /**
     * If a uniqueName was not provided in the JUnit XML, then we need to generate our own 'unique' identifier
     * for this test result.
     *
     * @param result Result that might need a unique name set.
     * @param uniqueNameTracker Unique name tracker ot ensure we don't assign two results the same unique name
     */
    protected void setUniqueName(AutomationResult result, UniqueNameTracker uniqueNameTracker) {
        if (result.getUniqueName() == null || result.getUniqueName().isEmpty()) {
            String suiteName = JUnitUtils.getStringValueIfValid(this.testSuite::getName);
            String className = JUnitUtils.getStringValueIfValid(this.testCase::getClassName);
            String testName = JUnitUtils.getStringValueIfValid(this.testCase::getName);

            result.setUniqueName(String.format("%s:%s:%s", suiteName, className, testName));
        }

        // We need to call into the unique name tracker no matter what, so we can ensure names are actually unique across all automation results.
        // If the uniqueNameTracker has to modify the result's 'uniqueName', that will kind of break our cross build result tracking if the
        // test results are written in a different order, or new tests are added that cause us to start appending numbers to unique names.
        uniqueNameTracker.ensureUniqueNameForResult(result);
    }
}
