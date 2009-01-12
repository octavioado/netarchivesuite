/* $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2007 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *   USA
 */
package dk.netarkivet.deploy2;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/** 
 * The application that is run to generate install and start/stop scripts
 * for all physical locations, machines and applications.
 */
public class DeployApplication {
    /** The configuration for this deploy. */
    private static DeployConfiguration itConfig;
    /** Argument parameter. */
    private static ArgumentParameters ap = new ArgumentParameters();
    /** The it-config file. */
    private static File itConfigFile;
    /** The NetarchiveSuite file.*/
    private static File netarchiveSuiteFile;
    /** The security policy file.*/
    private static File secPolicyFile;
    /** The log property file.*/
    private static File logPropFile;

    /**
     * Run the new deploy.
     * 
     * @param args The Command-line arguments in no particular order:
     * 
     * -C  The it-configuration file (ends with .xml).
     * -Z  The NetarchiveSuite file to be unpacked (ends with .zip).
     * -S  The security policy file (ends with .policy).
     * -L  The logging property file (ends with .prop).
     * -O  [OPTIONAL] The output directory
     * -D  [OPTIONAL] The database
     * -T  [OPTIONAL] The test arguments (httpportoffset, port, 
     * 				environmentName, mailReciever) 
     */
    public static void main(String[] args) {
        try {
            // Make sure the arguments can be parsed.
            if(!ap.parseParameters(args)) {
                System.err.print(Constants.MSG_ERROR_PARSE_ARGUMENTS);
                System.out.println();
                System.exit(0);
            }

            // Check arguments
            if(ap.cmd.getOptions().length < Constants.ARGUMENTS_REQUIRED) {
                System.err.print(Constants.MSG_ERROR_NOT_ENOUGH_ARGUMENTS);
                System.out.println();
                System.out.println(
                        "Use DeployApplication with following arguments:");
                System.out.println(ap.listArguments());
                System.out.println(
                        "outputdir defaults to "
                        + "./environmentName (set in config file)");
                System.out.println(
                        "Database defaults to "
                        + "?? (from NetarchiveSuite.zip)");
                System.out.println("Example: ");
                System.out.println(
                        "DeployApplication "
                        + "-C./conf/it-config.xml "
                        + "-Z./NetarchiveSuite-1.zip "
                        + "-S./conf/security.policy "
                        + "-L./conf/log.prop");
                System.exit(0);
            }
            // test if more arguments than options is given 
            if (args.length > ap.options.getOptions().size()) {
                System.err.print(
                        Constants.MSG_ERROR_TOO_MANY_ARGUMENTS);
                System.out.println();
                System.out.println("Maximum " + ap.options.getOptions().size() 
                        + "arguments.");
                System.exit(0);
            }
           
            // Retrieving the configuration filename
            String itConfigFileName = ap.cmd.getOptionValue(
                    Constants.ARG_CONFIG_FILE);
            // Retrieving the NetarchiveSuite filename
            String netarchiveSuiteFileName = ap.cmd.getOptionValue(
                    Constants.ARG_NETARCHIVE_SUITE_FILE);
            // Retrieving the security policy filename
            String secPolicyFileName = ap.cmd.getOptionValue(
                    Constants.ARG_SECURITY_FILE);
            // Retrieving the log property filename
            String logPropFileName = ap.cmd.getOptionValue(
                    Constants.ARG_LOG_PROPERTY_FILE);
            // Retrieving the output directory name
            String outputDir = ap.cmd.getOptionValue(
                    Constants.ARG_OUTPUT_DIRECTORY);
            // Retrieving the database filename
            String databaseFileName = ap.cmd.getOptionValue(
                    Constants.ARG_DATABASE_FILE);
            // Retrieving the test arguments
            String testArguments = ap.cmd.getOptionValue(
        	    Constants.ARG_TEST);

            // check itConfigFileName and retrieve the file
            initConfigFile(itConfigFileName);
            
            // check netarchiveSuiteFileName and retrieve the file
            initNetarchiveSuiteFile(netarchiveSuiteFileName);

            // check sePolicyFileName and retrieve the file
            initSecPolicyFile(secPolicyFileName);

            // check logPropFileName and retrieve the file
            initLogPropFile(logPropFileName);

            // check database
            checkDatabase(databaseFileName);
            
            // check and apply the test arguments
            applyTestArguments(testArguments);
            
            // Make the configuration based on the input data
            itConfig = new DeployConfiguration(
                    itConfigFile,
                    netarchiveSuiteFile,
                    secPolicyFile,
                    logPropFile,
                    outputDir,
                    databaseFileName); 

            // Write the scripts, directories and everything
            itConfig.write();
        } catch (SecurityException e) {
            // This problem should only occur in tests -> thus not err message. 
            System.out.println("SECURITY ERROR: " + e);
        } catch (Exception e) {
            // handle other exceptions?
            System.err.println("DEPLOY APPLICATION ERROR: " + e);
        }
    }
    
    /** 
     * Checks the configuration file argument and retrieves the file.
     * 
     * @param itConfigFileName The configuration file argument.
     */
    private static void initConfigFile(String itConfigFileName) {
        // check whether it-config file name is given as argument
        if(itConfigFileName == null) {
            System.err.print(
                    Constants.MSG_ERROR_NO_CONFIG_FILE_ARG);
            System.out.println();
            System.exit(0);
        }
        // check whether it-config file has correct extensions
        if(!itConfigFileName.endsWith(".xml")) {
            System.err.print(
                    Constants.MSG_ERROR_CONFIG_EXTENSION);
            System.out.println();
            System.exit(0);
        }
        // get the file
        itConfigFile = new File(itConfigFileName);
        // check whether the it-config file exists.
        if(!itConfigFile.exists()) {
            System.err.print(
                    Constants.MSG_ERROR_NO_CONFIG_FILE_FOUND);
            System.out.println();
            System.exit(0);
        }
    }

    /** 
     * Checks the NetarchiveSuite file argument and retrieves the file.
     * 
     * @param netarchiveSuiteFileName The NetarchiveSuite argument.
     */
    private static void initNetarchiveSuiteFile(String 
            netarchiveSuiteFileName) {
        // check whether NetarchiveSuite file name is given as argument
        if(netarchiveSuiteFileName == null) {
            System.err.print(
                    Constants.MSG_ERROR_NO_NETARCHIVESUITE_FILE_ARG);
            System.out.println();
            System.exit(0);
        }
        // check whether the NetarchiveSuite file has correct extensions
        if(!netarchiveSuiteFileName.endsWith(".zip")) {
            System.err.print(
                    Constants.MSG_ERROR_NETARCHIVESUITE_EXTENSION);
            System.out.println();
            System.exit(0);
        }
        // get the file
        netarchiveSuiteFile = new File(netarchiveSuiteFileName);
        // check whether the NetarchiveSuite file exists.
        if(!netarchiveSuiteFile.exists()) {
            System.err.print(
                    Constants.MSG_ERROR_NO_NETARCHIVESUITE_FILE_FOUND);
            System.out.println();
            System.exit(0);
        }
    }
    
    /** 
     * Checks the security policy file argument and retrieves the file.
     * 
     * @param secPolicyFileName The security policy argument.
     */
    private static void initSecPolicyFile(String secPolicyFileName) {
        // check whether security policy file name is given as argument
        if(secPolicyFileName == null) {
            System.err.print(
                    Constants.MSG_ERROR_NO_SECURITY_FILE_ARG);
            System.out.println();
            System.exit(0);
        }
        // check whether security policy file has correct extensions
        if(!secPolicyFileName.endsWith(".policy")) {
            System.err.print(
                    Constants.MSG_ERROR_SECURITY_EXTENSION);
            System.out.println();
            System.exit(0);
        }
        // get the file
        secPolicyFile = new File(secPolicyFileName);
        // check whether the security policy file exists.
        if(!secPolicyFile.exists()) {
            System.err.print(
                    Constants.MSG_ERROR_NO_SECURITY_FILE_FOUND);
            System.out.println();
            System.exit(0);
        }
    }
    
    /** 
     * Checks the log property file argument and retrieves the file.
     * 
     * @param logPropFileName The log property argument.
     */
    private static void initLogPropFile(String logPropFileName) {
        // check whether log property file name is given as argument
        if(logPropFileName == null) {
            System.err.print(
                    Constants.MSG_ERROR_NO_LOG_PROPERTY_FILE_ARG);
            System.out.println();
            System.exit(0);
        }
        // check whether the log property file has correct extensions
        if(!logPropFileName.endsWith(".prop")) {
            System.err.print(
                    Constants.MSG_ERROR_LOG_PROPERTY_EXTENSION);
            System.out.println();
            System.exit(0);
        }
        // get the file
        logPropFile = new File(logPropFileName);
        // check whether the log property file exists.
        if(!logPropFile.exists()) {
            System.err.print(
                    Constants.MSG_ERROR_NO_LOG_PROPERTY_FILE_FOUND);
            System.out.println();
            System.exit(0);
        }
    }
    
    /**
     * Checks the database argument (if any) for extension and existence.
     * 
     * @param databaseFileName The name of the database file.
     */
    private static void checkDatabase(String databaseFileName) {
        // check the extension on the database, if it is given as argument 
        if(databaseFileName != null) {
            if(!databaseFileName.endsWith(".jar") 
                    && !databaseFileName.endsWith(".zip")) {
                System.err.print(
                        Constants.MSG_ERROR_DATABASE_EXTENSION);
                System.out.println();
                System.exit(0);
            }
            
            // get the file
            File databaseFile = new File(databaseFileName);
            // check whether the database file exists.
            if(!databaseFile.exists()) {
                System.err.print(
                            Constants.MSG_ERROR_NO_DATABASE_FILE_FOUND);
                System.out.println();
                System.exit(0);
            }
        }
    }
    
    /**
     * Applies the test arguments.
     * 
     * @param testArguments The test arguments.
     */
    private static void applyTestArguments(String testArguments) {
	if(testArguments == null || testArguments.equalsIgnoreCase("")) {
	    System.out.println("No test arguments!");
	    return;
	}
	
	String[] changes = testArguments.split("[,]");
	if(changes.length != Constants.TEST_ARGUMENTS_REQUIRED) {
	    System.err.print(
		    Constants.MSG_ERROR_TEST_ARGUMENTS);
	    System.exit(0);
	}
	
	try {
            CreateTestInstance cti = new CreateTestInstance(itConfigFile);

            // apply the arguments
            cti.applyTestArguments(changes[0], changes[1], changes[2], 
        	    changes[3]);

            // replace ".xml" with "_test.xml"
            String tmp = itConfigFile.getPath();
            String[] configFile = tmp.split("[.]");
            String nameOfNewConfig =  configFile[0] 
                    + Constants.TEST_CONFIG_FILE_REPLACE_ENDING;

            cti.createSettingsFile(nameOfNewConfig);
            itConfigFile = new File(nameOfNewConfig);
	} catch (IOException e) {
	    System.out.println("Error in test arguments: " + e);
	    System.exit(0);
	}
    }
    
    /**
     * Handles the incoming arguments.
     * 
     */
    private static class ArgumentParameters {
        /** Options object for parameters.*/
        public Options options = new Options();
        /** Parser for parsing the command line arguments.*/
        private CommandLineParser parser = new PosixParser();
        /** The command line.*/
        public CommandLine cmd;
         
        /**
         * Initialise options by setting legal parameters for batch jobs.
         */
        ArgumentParameters() {
            options.addOption(Constants.ARG_CONFIG_FILE, 
                    true, "Config file.");
            options.addOption(Constants.ARG_NETARCHIVE_SUITE_FILE, 
                    true, "The NetarchiveSuite package file.");
            options.addOption(Constants.ARG_SECURITY_FILE, 
                    true, "Security property file.");
            options.addOption(Constants.ARG_LOG_PROPERTY_FILE, 
                    true, "Log property file.");
            options.addOption(Constants.ARG_OUTPUT_DIRECTORY, 
                    true, "[OPTIONAL] output directory.");
            options.addOption(Constants.ARG_DATABASE_FILE, 
                    true, "[OPTIONAL] Database.");
            options.addOption(Constants.ARG_TEST, 
        	    true, "[OPTIONAL] Tests.");
        }
        
        /**
         * Parsing the input arguments.
         * 
         * @param args The input arguments.
         * @return Whether it parsed correctly or not.
         */
        Boolean parseParameters(String[] args) {
            try {
                // parse the command line arguments
                cmd = parser.parse(options, args);
            } catch(ParseException exp) {
                return false;
            }
            return true;
        }
        
        /**
         * Get the list of possible arguments with their description.
         * 
         * @return The list describing the possible arguments.
         */
        String listArguments() {
            StringBuilder res = new StringBuilder("\n");
            res.append("Arguments:");
            // add options
            for (Object o: options.getOptions()) {
                Option op = (Option) o;
                res.append("\n");
                res.append("-");
                res.append(op.getOpt());
                res.append(" ");
                res.append(op.getDescription());
            }
            return res.toString();
        }
    }
}
