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

import com.perforce.halm.reportingtool.format.ReportFormatType;

import java.util.List;

/**
 * Object that encapsulates all the data and context for the report files being submitted as a build.
 */
public class ReportContext {
    private ReportFormatType reportFormatType;
    private List<String> reportFiles;

    /**
     * Default constructor
     */
    public ReportContext() {}

    /**
     * Constructor
     *
     * @param reportFormatType The type of format used by the report files
     * @param reportFiles The report files
     */
    public ReportContext(final ReportFormatType reportFormatType, final List<String> reportFiles) {
        this.reportFormatType = reportFormatType;
        this.reportFiles = reportFiles;
    }

    public ReportFormatType getReportFormatType() { return reportFormatType; }
    public void setReportFormatType(ReportFormatType reportFormatType) { this.reportFormatType = reportFormatType; }
    public List<String> getReportFiles() { return reportFiles; }
    public void setReportFiles(List<String> reportFiles) { this.reportFiles = reportFiles; }

    /**
     * @return Whether the context members are valid or not
     */
    public boolean isValidContext() {
        return this.reportFormatType != null && this.reportFiles != null && !this.reportFiles.isEmpty();
    }
}
