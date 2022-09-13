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

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Object representing a JUnit testsuite XML element
 */
@XmlType(name = "testsuite")
public class JUnitTestSuite {
    // Standard attributes
    @XmlAttribute(required = true)
    private String name;
    @XmlAttribute
    private String timestamp;
    @XmlAttribute
    private String hostname;
    @XmlAttribute(required = true)
    private int tests;
    @XmlAttribute
    private int failures;
    @XmlAttribute
    private int errors;
    @XmlAttribute
    private int skipped;
    @XmlAttribute
    private Double time;
    @XmlAttribute(name = "package")
    private String packageValue;
    @XmlAttribute
    private String id;

    @XmlAnyAttribute
    private Map<QName, String> nonStandardAttributes;

    // Child elements
    @XmlElement(name = "property")
    @XmlElementWrapper(name = "properties")
    private List<JUnitProperty> properties;
    @XmlElement(name = "testcase")
    private List<JUnitTestCase> testCases;
    @XmlElement(name = "system-out")
    private String systemOut;
    @XmlElement(name = "system-err")
    private String systemErr;

    public String getName() { return this.name; }
    public String getTimestamp() { return this.timestamp; }
    public String getHostname() { return this.hostname; }
    public int getTests() { return this.tests; }
    public int getFailures() { return this.failures; }
    public int getErrors() { return this.errors; }
    public int getSkipped() { return this.skipped; }
    public double getTime() { return this.time != null ? this.time : 0; }
    public Number getTimeInMS() { return (long)(this.getTime() * 1000); }
    public String getPackageValue() { return packageValue; }
    public String getId() { return this.id; }

    /**
     * @return Copy of the non-standard attributes map, but using only the local part of the QName key values.
     */
    public Map<String, String> getNonStandardLocalAttributes() {
        Map<String, String> attributes = new HashMap<>();
        if (this.nonStandardAttributes != null) {
            for (Entry<QName, String> entry : this.nonStandardAttributes.entrySet()) {
                attributes.put(entry.getKey().getLocalPart(), entry.getValue());
            }
        }
        return attributes;
    }

    public List<JUnitProperty> getProperties() { return this.properties; }
    public List<JUnitTestCase> getTestCases() { return this.testCases; }
    public String getSystemOut() { return this.systemOut; }
    public String getSystemErr() { return this.systemErr; }
}
