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

import com.perforce.halm.rest.types.automation.build.AutomationResult;

import java.util.HashSet;
import java.util.Set;

/**
 * As automated test results require a uniqueName be provided, using this tracker class helps ensure we generate unique names.
 */
public class UniqueNameTracker {
    private final Set<String> allUniqueNames = new HashSet<>();

    /**
     * Ensures a uniqueName is set, and is unique across all known values. If none is set, this will generate one.
     *
     * @param result The result to generate a unique name for
     */
    public void ensureUniqueNameForResult(AutomationResult result) {
        String uniqueName = result.getUniqueName();
        if (uniqueName == null || uniqueName.isEmpty()) {
            uniqueName = result.getName() != null ? result.getName() : "";
        }
        result.setUniqueName(ensureUniqueName(uniqueName));
        allUniqueNames.add(result.getUniqueName());
    }

    /**
     * Ensures that the specified name is unique across all known values
     *
     * @param name The base name to check
     * @return The unique name generated
     */
    private String ensureUniqueName(final String name) {
        int identifier = 1;
        String uniqueName = name;
        while (allUniqueNames.contains(uniqueName) && identifier <= 100000) { // Don't continue forever, we can only do so much to ensure uniqueness with this method.
            uniqueName = name + "." + identifier;
            identifier++;
        }

        // TODO: ENHANCEMENT: Implement better logging. If uniqueName != name, We should write out some kind of log message indicating that there is a name collision, and test results will not be reliable between two builds.
        return uniqueName;
    }
}
