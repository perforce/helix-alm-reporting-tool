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

import com.perforce.halm.reportingtool.format.ReportFormatType;
import com.perforce.halm.reportingtool.models.BuildMetadata;
import com.perforce.halm.reportingtool.models.HelixALMSuiteContext;
import com.perforce.halm.reportingtool.models.ReportContext;
import com.perforce.halm.rest.AuthInfoAPIKey;
import com.perforce.halm.rest.ConnectionInfo;
import com.perforce.halm.rest.types.IDLabelPair;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Reporting tool argument object. Used when interacting with the command line interface.
 */
@CommandLine.Command(name = "reporting-tool.jar", mixinStandardHelpOptions = true, version = "Hello, my version.",
    description = "Assists with submitting automated testing results to the Helix ALM Server.")
public class ReportingToolArgs {
    // Required arguments
    @Parameters(description = "Report files", arity = "1..")
    public List<String> reportFiles = new ArrayList<>();

    @Option(names = {"--format", "-f"}, description = "Report file format. Valid values: ${COMPLETION-CANDIDATES}", required = true)
    public ReportFormatType reportFileFormat;

    @Option(names = {"--number", "-n"}, description = "Build number", required = true)
    public String buildNumber;

    @Option(names = {"--project", "-p"}, description = "Helix ALM Project")
    public String almProjectID;

    @Option(names = {"--suite", "-s"}, description = "Helix ALM Automation Suite")
    public String almSuiteID;

    @CommandLine.ArgGroup(validate = false, heading = "%nHelix ALM REST API configuration%n")
    public RESTAPIContext restAPIContext = new RESTAPIContext();

    /**
     * Context parameters for connecting to the Helix ALM REST API
     */
    static class RESTAPIContext {
        @CommandLine.Option(names = {"--host", "-H"}, description = "Helix ALM REST API URL.")
        public String host = "";

        @CommandLine.Option(names = {"--username", "-U"}, description = "Helix ALM REST API username or API key ID")
        public String username = "";

        @CommandLine.Option(names = {"--password", "-P"}, description = "Helix ALM REST API password or API key secret", interactive = true,
                arity = "0..1")
        public String password = "";

        @CommandLine.Option(names = {"--authType", "-A"},
                description = "Helix ALM REST API authentication method. Valid values: ${COMPLETION-CANDIDATES}")
        public APIAuthType authType = APIAuthType.basic;

        @CommandLine.Option(names = {"--fingerprint", "-F"}, description = "SSL fingerprint for certificates to accept")
        public String sslFingerprint = "";

        /**
         * @return Converts our data into a ConnectionInfo object
         */
        public ConnectionInfo getConnectionInfo() {
            return this.authType == APIAuthType.apiKey
                    ? new ConnectionInfo(this.host, new AuthInfoAPIKey(this.username, this.password))
                    : new ConnectionInfo(this.host, this.username, this.password);
        }
    }

    // Optional arguments
    @Option(names = {"--description", "-d"}, description = "Build description")
    public String buildDescription;

    @Option(names = {"--branch", "-b"}, description = "Branch")
    public String buildBranch;

    @Option(names = {"--externalURL", "-x"}, description = "External URL")
    public String externalURL;

    @Option(names = {"--testRunSetID", "-i"}, description = "Test Run Set identifier")
    public Number testRunSetID;

    @Option(names = {"--testRunSetLabel", "-l"}, description = "Test Run Set label")
    public String testRunSetLabel;

    // Help information
    @CommandLine.ArgGroup(validate = false, heading = "%nReporting Tool Info%n")
    public ReportingToolInfo reportingToolInfo;

    /**
     * Defines the help and version information to display in the CLI.
     */
    static class ReportingToolInfo {
        @Option(names = {"--help", "-h"}, description = "Print the help info", usageHelp = true)
        public boolean help;

        @Option(names = {"--version", "-v"}, description = "Print reporting tool version information.",
            versionHelp = true)
        public boolean version;
    }

    /**
     * Returns a report context object from the args
     * @return See description
     */
    public ReportContext getReportContext() {
        return new ReportContext(this.reportFileFormat, this.reportFiles);
    }

    /**
     * Returns a context object from the args defining the Helix ALM suite to submit the build to
     * @return See description
     */
    public HelixALMSuiteContext getHelixALMSuiteContext() {
        return new HelixALMSuiteContext(this.restAPIContext.getConnectionInfo(), this.almProjectID, this.almSuiteID);
    }

    /**
     * Returns a build metadata object from the args
     * @return See description
     */
    public BuildMetadata getBuildMetadata() {
        BuildMetadata metadata = new BuildMetadata();
        metadata.setDescription(this.buildDescription);
        metadata.setBranch(this.buildBranch);
        metadata.setExternalURL(this.externalURL);
        if (this.testRunSetID != null || (this.testRunSetLabel != null && !this.testRunSetLabel.isEmpty())) {
            metadata.setTestRunSet(new IDLabelPair(this.testRunSetID != null ? this.testRunSetID : 0, this.testRunSetLabel));
        }
        return metadata;
    }
}
