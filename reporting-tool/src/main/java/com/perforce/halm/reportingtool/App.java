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

package com.perforce.halm.reportingtool;

import com.perforce.halm.reportingtool.models.HelixALMSuiteContext;
import com.perforce.halm.rest.*;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Application object for handling command line arguments
 */
public class App {
    /**
     * Application main function. Handles command line arguments.
     *
     * @param args Command line arguments to process
     */
    public static void main(String[] args) {
        ReportingToolArgs parsedArgs = new ReportingToolArgs();
        try {
            CommandLine cmdLine = new CommandLine(parsedArgs);
            cmdLine.parseArgs(args);
            if (cmdLine.isUsageHelpRequested()) {
                CommandLine.usage(parsedArgs, System.out);
            }
            else if (cmdLine.isVersionHelpRequested()) {
                System.out.println(App.class.getPackage().getImplementationVersion());
            } else {
                // If we got here, we should be ready to try and generate the report and submit.
                BuildSubmitter submitter = new BuildSubmitter(parsedArgs.buildNumber, parsedArgs.getReportContext(),
                        App.getHelixALMSuiteContext(parsedArgs), parsedArgs.getBuildMetadata());
                submitter.submitAutomationBuild();
            }
        } catch (Exception e) {
            // If we run into an exception, print out help.
            CommandLine.usage(parsedArgs, System.out);
        }
    }

    /**
     * Function that will create a Helix ALM suite context from the parsed CLI arguments,
     * filling in any default values necessary from a config.properties file if found.
     *
     * @param parsedArgs Parsed command line arguments
     * @return See description
     */
    private static HelixALMSuiteContext getHelixALMSuiteContext(ReportingToolArgs parsedArgs) throws IOException {
        // Start with the parsed arguments, only use defaults if arguments were not specified.
        final HelixALMSuiteContext context = parsedArgs.getHelixALMSuiteContext();

        // See if a config.properties file even exists. If not, then skip this section.
        Properties config = App.loadConfigProperties();
        if (config != null) {
            // Apply default parameters if the values were not provided via the CLI and defaults exist.
            App.applyDefaultStringPropertyIfNeeded(config, "REST_API_BASE_URL", () -> context.getRestAPIConnectionInfo().getUrl(), url -> context.getRestAPIConnectionInfo().setUrl(url));
            App.applyDefaultStringPropertyIfNeeded(config, "HALM_PROJECT_ID", context::getHelixALMProjectID, context::setHelixALMProjectID);
            App.applyDefaultStringPropertyIfNeeded(config, "HALM_SUITE_ID", context::getHelixALMSuiteID, context::setHelixALMSuiteID);

            // For username/password/APIkey, it will be easier to just check the CLI params directly.
            // Assume if no username/api key ID CLI parameter was provided, that we should use the defaults.
            // Note that the system defaults to using API key over username/password if both are in the config.
            if (parsedArgs.restAPIContext.username.isEmpty()) {
                IAuthInfo defaultAuthInfo = App.buildDefaultAuthInfoFromConfig(config);
                if (defaultAuthInfo != null) {
                    context.getRestAPIConnectionInfo().setAuthInfo(defaultAuthInfo);
                }
            }
        }

        // Check the certificate information for our connection. If the certificate status returns INVALID_DOWNLOADABLE and also returns
        // pem certificates, then we need to see if we have a matching SSL fingerprint provided either via the CLI or configuration file.
        // If we have a matching SSL fingerprint, then we will take that as authorization to accept the certificates and proceed.
        CertificateInfo certInfo = CertUtils.getServerCertStatus(context.getRestAPIConnectionInfo());
        if (certInfo.getStatus() == CertificateStatus.INVALID_DOWNLOADABLE && !certInfo.getPemCertificates().isEmpty()) {
            String sslFingerprint = parsedArgs.restAPIContext.sslFingerprint;
            if (sslFingerprint.isEmpty() && config != null) {
                sslFingerprint = config.getProperty("SSL_FINGERPRINT", "");
            }
            if (!sslFingerprint.isEmpty() && certInfo.getFingerprints().contains(sslFingerprint)) {
                // SSL fingerprint matches, accept the certificates.
                context.getRestAPIConnectionInfo().setPemCertContents(certInfo.getPemCertificates());
            } else if (!certInfo.getFingerprints().isEmpty()) {
                System.out.println("SSL fingerprint was not provided, or does not match the fingerprint provided by the Helix ALM REST API: " + certInfo.getFingerprints().get(0));
            }
        }

        return context;
    }

    /**
     * Loads the config.properties file, which contains default parameters, into a Properties object
     * @return See description
     */
    private static Properties loadConfigProperties() {
        Properties config = null;
        final File fileExtConfig = new File(System.getenv("APP_HOME") + File.separator + "config.properties");
        if (fileExtConfig.exists()) {
            try (FileInputStream in = new FileInputStream(fileExtConfig)) {
                config = new Properties();
                config.load(in);
            } catch (Exception ignored) {}
        }
        return config;
    }

    /**
     * Convenience function for applying default config properties if they were not explicitly specified on the command line.
     *
     * @param config The config.properties contents
     * @param propertyKey The property key to lookup
     * @param getterFn The getter function to see if the value is already set
     * @param setterFn The setter function to use if we want to apply the default value
     */
    private static void applyDefaultStringPropertyIfNeeded(final Properties config, final String propertyKey,
                                                           Supplier<String> getterFn, Consumer<String> setterFn) {
        if (getterFn.get() == null || getterFn.get().isEmpty()) {
            String propertyValue = config.getProperty(propertyKey, "");
            if (!propertyValue.isEmpty()) {
                setterFn.accept(propertyValue);
            }
        }
    }

    /**
     * Function that will build an IAuthInfo object based on the specified default configuration options.
     * Will return null if no configuration properties or default parameters exist.
     *
     * @param config The default configuration properties
     * @return See description
     */
    private static IAuthInfo buildDefaultAuthInfoFromConfig(Properties config) {
        IAuthInfo authInfo = null;

        if (config != null) {
            String apiKey = config.getProperty("REST_API_APIKEY", "");
            if (!apiKey.isEmpty()) {
                authInfo = new AuthInfoAPIKey(apiKey);
            } else {
                String user = config.getProperty("REST_API_USERNAME", "");
                String pass = config.getProperty("REST_API_PASSWORD", "");
                if (!user.isEmpty()) {
                    authInfo = new AuthInfoBasic(user, pass);
                }
            }
        }

        return authInfo;
    }
}
