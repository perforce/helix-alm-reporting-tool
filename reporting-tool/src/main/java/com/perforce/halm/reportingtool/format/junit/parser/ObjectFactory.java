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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * JUnit XML files can either have a root element of 'testsuites' or 'testsuite', depending on whether multiple suites
 * are reported in a single file or not. This object factory allows JAXB to handle determining the root element used.
 */
@XmlRegistry
public class ObjectFactory {
    private final static QName _suiteQName = new QName("testsuite");
    private final static QName _suitesWrapperQName = new QName("testsuites");

    /**
     * Constructor
     */
    public ObjectFactory() {}

    /**
     * Creates a JAXBElement of the type JUnitTestSuite
     * @param suite The suite object
     * @return See description
     */
    @XmlElementDecl(name = "testsuite")
    public JAXBElement<JUnitTestSuite> createTestSuite(JUnitTestSuite suite) {
        return new JAXBElement<JUnitTestSuite>(_suiteQName, JUnitTestSuite.class, suite);
    }

    /**
     * Creates a JAXBElement of the type JUnitTestSuitesWrapper
     * @param wrapper The suite wrapper object
     * @return See description
     */
    @XmlElementDecl(name = "testsuites")
    public JAXBElement<JUnitTestSuitesWrapper> createTestSuitesWrapper(JUnitTestSuitesWrapper wrapper) {
        return new JAXBElement<JUnitTestSuitesWrapper>(_suitesWrapperQName, JUnitTestSuitesWrapper.class, wrapper);
    }
}
