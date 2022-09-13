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

package com.perforce.halm.reportingtool.models;

import com.perforce.halm.rest.types.IDLabelPair;
import com.perforce.halm.rest.types.NameValuePair;
import com.perforce.halm.rest.types.automation.build.AutomationBuildRunConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Object that encapsulates any optional build metadata that can be supplied when submitting a build.
 */
public class BuildMetadata {
    private String description;
    private String branch;
    private String externalURL;
    private IDLabelPair testRunSet;
    private AutomationBuildRunConfiguration runConfigurationInfo;
    private String sourceOverride = "reporting-tool";
    private String pendingRunID;
    private List<NameValuePair> properties;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getExternalURL() { return externalURL; }
    public void setExternalURL(String externalURL) { this.externalURL = externalURL; }
    public IDLabelPair getTestRunSet() { return this.testRunSet; }
    public void setTestRunSet(IDLabelPair testRunSet) { this.testRunSet = testRunSet; }
    public AutomationBuildRunConfiguration getRunConfigurationInfo() { return this.runConfigurationInfo; }
    public void setRunConfigurationInfo(AutomationBuildRunConfiguration runConfigurationInfo) { this.runConfigurationInfo = runConfigurationInfo; }
    public String getSourceOverride() { return sourceOverride; }
    public void setSourceOverride(String source) { this.sourceOverride = source; }
    public String getPendingRunID() { return pendingRunID; }
    public void setPendingRunID(String pendingRunID) { this.pendingRunID = pendingRunID; }
    public List<NameValuePair> getProperties() { return properties; }
    public void addProperty(NameValuePair property) {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        this.properties.add(property);
    }
    public void setProperties(List<NameValuePair> properties) { this.properties = properties; }
}
