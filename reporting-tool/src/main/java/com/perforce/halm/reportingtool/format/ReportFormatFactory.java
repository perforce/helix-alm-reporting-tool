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

import com.perforce.halm.reportingtool.format.junit.JUnitMetaBuild;
import com.perforce.halm.reportingtool.format.junit.JUnitMetaResult;

import java.security.InvalidParameterException;

/**
 * Factory that builds report formatters based on the specified type.
 */
public final class ReportFormatFactory {
    /**
     * Creates a meta build object of the specified type. Throws an exception for invalid types.
     *
     * @param type The type of build formatter to create
     * @return A build formatter object of the specified type
     * @throws InvalidParameterException When the specified format type is not supported
     */
    public static IMetaBuild createBuildFormatterForType(final ReportFormatType type) throws InvalidParameterException {
        switch (type) {
            case JUnit:
            case xUnit: // Treat xUnit the same as JUnit. The XML schemas are close enough that we shouldn't need a different parser.
                return new JUnitMetaBuild();
            default:
                throw new InvalidParameterException("Report format type is not supported.");
        }
    }

    /**
     * Creates a meta report object of the specified type. Throws an exception for invalid types.
     *
     * @param type The type of report formatter to create
     * @return A report formatter object of the specified type
     * @throws InvalidParameterException When the specified format type is not supported
     */
    public static IMetaResult createResultFormatterForType(final ReportFormatType type) throws InvalidParameterException {
        switch (type) {
            case JUnit:
            case xUnit: // Treat xUnit the same as JUnit. The XML schemas are close enough that we shouldn't need a different parser.
                return new JUnitMetaResult();
            default:
                throw new InvalidParameterException("Report format type is not supported.");
        }
    }

    /**
     * Private constructor so this cannot be initialized
     */
    private ReportFormatFactory() {}
}
