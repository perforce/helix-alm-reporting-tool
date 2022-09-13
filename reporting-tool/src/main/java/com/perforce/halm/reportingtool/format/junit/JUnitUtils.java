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

package com.perforce.halm.reportingtool.format.junit;

import com.perforce.halm.reportingtool.format.junit.parser.ObjectFactory;
import com.perforce.halm.rest.types.NameValuePair;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class to hold common static functions used by the JUnit formatting objects.
 */
public final class JUnitUtils {
    private static JAXBContext cachedJAXBContext;

    /**
     * Creating a new instance of JAXBContext is expensive. We will create and cache our JUnit JAXBContext the first time it's requested.
     * @return See description
     */
    public static JAXBContext getJAXBContext() {
        if (cachedJAXBContext == null) {
            try {
                cachedJAXBContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), ObjectFactory.class.getClassLoader());
            } catch (JAXBException e) {
                e.printStackTrace();
                return null;
            }
        }
        return cachedJAXBContext;
    }

    /**
     * If a standard JUnit attribute is unmappable to one of our Helix ALM object fields, and we want to add it to properties
     * if it exists; this function will check to see if the attribute is set, and if so will add it to the properties array.
     *
     * @param attributeName The attribute name
     * @param attributeValue The attribute value
     * @param addPropertyFn The add property function to call to consume the NameValuePair created
     */
    public static void addPropertyForUnmappableAttribute(final String attributeName, final String attributeValue, Consumer<NameValuePair> addPropertyFn) {
        if (attributeValue != null && !attributeValue.isEmpty()) {
            addPropertyFn.accept(new NameValuePair(attributeName, attributeValue));
        }
    }

    /**
     * Converts an iso8601 string into an Instant
     *
     * @param iso8601 The string timestamp to parse
     * @return See description
     */
    public static Instant parseISO8601StringToInstant(final String iso8601) {
        return DatatypeConverter.parseDateTime(iso8601).toInstant();
    }

    /**
     * Safety function that will transfer a string value if it's valid
     *
     * @param getterFn The getter function
     * @param setterFn The setter function
     */
    public static void setStringValueIfValid(Supplier<String> getterFn, Consumer<String> setterFn) {
        String getterFnResult = getterFn.get();
        if (getterFnResult != null && !getterFnResult.isEmpty()) {
            setterFn.accept(getterFnResult);
        }
    }

    /**
     * Safety function that will ensure we have a string value.
     *
     * @param getterFn The getter function
     * @return A string value. Either the result of the getter function, or an empty string.
     */
    public static String getStringValueIfValid(Supplier<String> getterFn) {
        String result = getterFn.get();
        if (result == null) {
            result = "";
        }
        return result;
    }


    /**
     * If a non-standard attribute value with the specified name exists, it will be passed to the specified consumer function.
     *
     * @param attributes The parsed non-standard JUnit XML attributes
     * @param attributeName The attribute name to look for
     * @param setterFn The function that will consume the value if found
     */
    public static void setStringPropertyFromNonStandardAttribute(Map<String, String> attributes, final String attributeName, Consumer<String> setterFn) {
        String value = consumeNonStandardAttribute(attributes, attributeName);
        if (value != null && !value.isEmpty()) {
            setterFn.accept(value);
        }
    }

    /**
     * If a non-standard attribute value with the specified name exists, it will be passed to the specified consumer function after splitting the delimited value.
     *
     * @param attributes The parsed non-standard JUnit XML attributes
     * @param attributeName The attribute name to look for
     * @param delimiter The string delimiter separating different values
     * @param setterFn The setter function
     */
    public static void setStringListPropertyFromNonStandardAttribute(Map<String, String> attributes, final String attributeName,
                                                                     final String delimiter, Consumer<List<String>> setterFn) {
        String value = consumeNonStandardAttribute(attributes, attributeName);
        if (value != null && !value.isEmpty()) {
            List<String> values = Stream.of(value.split(delimiter))
                    .map(String::trim)
                    .filter((String trimmed) -> !trimmed.isEmpty())
                    .collect(Collectors.toList());
            if (!values.isEmpty()) {
                setterFn.accept(values);
            }
        }
    }

    /**
     * If an attribute with the specified name exists, we will return the value for that attribute and delete it
     * from the attributes map. If it does not exist or is an empty value, an empty string will be returned instead.
     *
     * @param attributes The parsed non-standard JUnit XML attributes
     * @param attributeName The attribute name to look for
     * @return See description
     */
    private static String consumeNonStandardAttribute(Map<String, String> attributes, final String attributeName) {
        if (attributes.containsKey(attributeName)) {
            String value = attributes.get(attributeName);
            attributes.remove(attributeName);
            return value;
        }
        return null;
    }

    /**
     * Private constructor, so the class cannot be created
     */
    private JUnitUtils() {}
}
