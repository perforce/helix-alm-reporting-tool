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

import com.perforce.halm.rest.ConnectionInfo;

/**
 * Object that encapsulates all the data and context for the Helix ALM project we are submitting a build to.
 */
public class HelixALMSuiteContext {
    private ConnectionInfo restAPIConnectionInfo;
    private String helixALMProjectID;
    private String helixALMSuiteID;

    /**
     * Default constructor
     */
    public HelixALMSuiteContext() {}

    /**
     * Constructor
     *
     * @param connectionInfo The connection information to use when connecting to the Helix ALM REST API
     * @param projectID The identifier for the Helix ALM project to connect to
     * @param suiteID The identifier for the Helix ALM automation suite to submit a build to
     */
    public HelixALMSuiteContext(final ConnectionInfo connectionInfo, final String projectID, final String suiteID) {
        this.restAPIConnectionInfo = connectionInfo;
        this.helixALMProjectID = projectID;
        this.helixALMSuiteID = suiteID;
    }

    public ConnectionInfo getRestAPIConnectionInfo() { return restAPIConnectionInfo; }
    public void setRestAPIConnectionInfo(ConnectionInfo connectionInfo) { this.restAPIConnectionInfo = connectionInfo; }
    public String getHelixALMProjectID() { return helixALMProjectID; }
    public void setHelixALMProjectID(String helixALMProjectID) { this.helixALMProjectID = helixALMProjectID; }
    public String getHelixALMSuiteID() { return helixALMSuiteID; }
    public void setHelixALMSuiteID(String helixALMSuiteID) { this.helixALMSuiteID = helixALMSuiteID; }

    /**
     * @return Whether the context members are valid or not. Does not validate the actual connection itself.
     */
    public boolean isValidContext() {
        return this.restAPIConnectionInfo != null && !this.helixALMProjectID.isEmpty() && !this.helixALMSuiteID.isEmpty();
    }
}
