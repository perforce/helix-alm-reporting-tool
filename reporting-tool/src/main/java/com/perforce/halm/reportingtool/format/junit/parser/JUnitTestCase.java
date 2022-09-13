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

package com.perforce.halm.reportingtool.format.junit.parser;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * Object representing a JUnit testcase XML element
 */
public class JUnitTestCase {
    // Attributes
    @XmlAttribute(required = true)
    private String name;
    @XmlAttribute(name = "classname", required = true)
    private String className;
    @XmlAttribute
    private Double time;

    @XmlAnyAttribute
    private Map<QName, String> nonStandardAttributes;

    // Child elements
    @XmlElement
    private JUnitTestCaseSkipped skipped;
    @XmlElement
    private JUnitTestCaseError error;
    @XmlElement
    private JUnitTestCaseFailure failure;

    public String getName() { return this.name; }
    public String getClassName() { return this.className; }
    public Double getTime() { return this.time != null ? this.time : 0; }
    public Number getTimeInMS() { return (long)(this.getTime() * 1000); }

    /**
     * @return Copy of the non-standard attributes map, but using only the local part of the QName key values.
     */
    public Map<String, String> getNonStandardLocalAttributes() {
        Map<String, String> attributes = new HashMap<>();
        if (this.nonStandardAttributes != null) {
            for (Map.Entry<QName, String> entry : this.nonStandardAttributes.entrySet()) {
                attributes.put(entry.getKey().getLocalPart(), entry.getValue());
            }
        }
        return attributes;
    }

    public JUnitTestCaseSkipped getSkipped() { return this.skipped; }
    public JUnitTestCaseError getError() { return this.error; }
    public JUnitTestCaseFailure getFailure() {
        return this.failure;
    }
}
