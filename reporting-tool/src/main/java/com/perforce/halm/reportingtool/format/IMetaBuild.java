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

package com.perforce.halm.reportingtool.format;

import com.perforce.halm.reportingtool.models.BuildMetadata;
import com.perforce.halm.rest.types.automation.build.AutomationBuild;

import java.util.List;

/**
 * Abstract class defining the interface for converting build artifacts into a Helix ALM AutomationBuild object.
 */
public abstract class IMetaBuild {
    /**
     * Function that processes the specified report files, and returns an automation build object that can be sent to Helix ALM
     *
     * @param buildNumber The build number to specify when submitting the build
     * @param reportFiles The report files to format the build results for
     * @param buildMetadata The metadata for the build
     * @return See description
     */
    public abstract AutomationBuild generateAutomationBuild(final String buildNumber, final List<String> reportFiles, final BuildMetadata buildMetadata);
}
