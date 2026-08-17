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

import com.perforce.halm.reportingtool.format.junit.JUnitUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that is responsible for parsing the specified report files into our Java JUnit classes.
 */
public class JUnitJAXBParser {
    private final List<String> reportFiles;

    /**
     * Constructor
     *
     * @param reportFiles The report files to parse
     */
    public JUnitJAXBParser(final List<String> reportFiles) {
        this.reportFiles = reportFiles;
    }

    /**
     * Function that processes the specified report files.
     * Returns JUnit testsuite objects that can be converted into a Perforce ALM automation build object.
     *
     * @return See description
     * @throws JAXBException Thrown when we encounter a JAXB parser exception
     * @throws IOException Thrown when we encounter an error reading or escaping the file
     */
    public List<JUnitTestSuite> parseReportFiles() throws JAXBException, IOException {
        List<JUnitTestSuite> testSuites = new ArrayList<>();
        JAXBContext context = JUnitUtils.getJAXBContext();
        if (context != null) {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            for (String reportFile : this.reportFiles) {
                // Each report file's root element could either be a JUnitTestSuitesWrapper or single JUnitTestSuite.
                // This code may look a bit messy, but it allows us to determine the type and proceed accordingly.
                JAXBElement rootObject = (JAXBElement)unmarshaller.unmarshal(Files.newInputStream(Paths.get(reportFile)));
                if (rootObject != null) {
                    Object baseElement = rootObject.getValue();
                    if (baseElement instanceof JUnitTestSuitesWrapper) {
                        JUnitTestSuitesWrapper wrapper = (JUnitTestSuitesWrapper) baseElement;
                        testSuites.addAll(wrapper.getTestSuites());
                    } else if (baseElement instanceof JUnitTestSuite) {
                        JUnitTestSuite suite = (JUnitTestSuite) baseElement;
                        testSuites.add(suite);
                    }
                }
            }
        }
        return testSuites;
    }
}
