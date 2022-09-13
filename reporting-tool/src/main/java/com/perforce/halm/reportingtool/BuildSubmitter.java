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

package com.perforce.halm.reportingtool;

import com.perforce.halm.reportingtool.format.IMetaBuild;
import com.perforce.halm.reportingtool.format.ReportFormatFactory;
import com.perforce.halm.reportingtool.models.BuildMetadata;
import com.perforce.halm.reportingtool.models.HelixALMSuiteContext;
import com.perforce.halm.reportingtool.models.ReportContext;
import com.perforce.halm.rest.Client;
import com.perforce.halm.rest.responses.SubmitAutomationBuildResponse;
import com.perforce.halm.rest.types.automation.build.AutomationBuild;

/**
 * Object that generates a build to report based on the specified information
 */
public class BuildSubmitter {
    private final String buildNumber;
    private final ReportContext reportContext;
    private final HelixALMSuiteContext helixALMContext;
    private final BuildMetadata buildMetadata;

    /**
     * Constructor
     *
     * @param buildNumber The build number that will be specified in Helix ALM
     * @param reportContext The context object containing information about the report being submitted
     * @param helixALMContext The Helix ALM REST API automation suite context to use when submitting
     * @param buildMetadata The metadata for the build
     */
    public BuildSubmitter(final String buildNumber, final ReportContext reportContext,
                          final HelixALMSuiteContext helixALMContext, final BuildMetadata buildMetadata) {
        this.buildNumber = buildNumber;
        this.reportContext = reportContext;
        this.helixALMContext = helixALMContext;
        this.buildMetadata = buildMetadata;
    }

    /**
     * Submits the automation build report
     * @return The response object
     */
    public SubmitAutomationBuildResponse submitAutomationBuild() {
        SubmitAutomationBuildResponse response = new SubmitAutomationBuildResponse();
        if (this.validateParameters()) {
            // Ensure we can build the necessary formatter.
            IMetaBuild metaBuild = ReportFormatFactory.createBuildFormatterForType(this.reportContext.getReportFormatType());

            try {
                // Attempt to create the Helix ALM Rest API client connection.
                Client restAPIClient = new Client(this.helixALMContext.getRestAPIConnectionInfo());
                if (restAPIClient.getAuthToken(this.helixALMContext.getHelixALMProjectID()) != null) {
                    // We know we are connected. Now we need to format the report files into an automation build to submit.
                    AutomationBuild build = metaBuild.generateAutomationBuild(this.buildNumber, this.reportContext.getReportFiles(), this.buildMetadata);
                    if (build != null) {
                        response = restAPIClient.submitAutomationBuild(build, this.helixALMContext.getHelixALMProjectID(), this.helixALMContext.getHelixALMSuiteID());
                    } else {
                        response.setErrorMessage("Unable to create an automation build object from the specified data.");
                    }
                } else {
                    response.setErrorMessage("Unable to get authentication token from the Helix ALM REST API.");
                }
            } catch (Exception e) {
                //todo: ENHANCEMENT - Implement actual error logging.
                response.setErrorMessage(e.getLocalizedMessage());
                e.printStackTrace();
            }
        } else {
            response.setErrorMessage("Invalid parameters were passed when submitting the build.");
        }
        return response;
    }

    /**
     * Validates our parameters to ensure we have enough information to continue
     *
     * @return Returns true if parameters may be valid, false if we know they are not
     */
    private boolean validateParameters() {
        return !this.buildNumber.isEmpty() && this.reportContext != null && this.reportContext.isValidContext() &&
                this.helixALMContext != null && this.helixALMContext.isValidContext();
    }
}
