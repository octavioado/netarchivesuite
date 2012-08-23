/* $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2012 The Royal Danish Library, the Danish State and
 * University Library, the National Library of France and the Austrian
 * National Library.
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
package dk.netarkivet.deploy;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.archive.crawler.Heritrix;
import org.dom4j.Element;

import dk.netarkivet.common.exceptions.IOFailure;

/**
 * A LinuxMachine is the instance of the abstract machine class, which runs 
 * the operating system Linux or another Unix dependent operation system.
 * This class only contains the operating system specific functions.
 */
public class LinuxMachine extends Machine {
    /**
     * The constructor. 
     * Starts by initialising the parent abstract class, then sets the 
     * operating system dependent variables.
     * 
     * @param subTreeRoot The XML root element.
     * @param parentSettings The Settings to be inherited from the 
     * PhysicalLocation, where this machine is placed.
     * @param param The machine parameters to be inherited from the 
     * PhysicalLocation.
     * @param netarchiveSuiteSource The name of the NetarchiveSuite package 
     * file. Must end with '.zip'.
     * @param logProp The logging property file, to be copied into 
     * machine directory.
     * @param securityPolicy The security policy file, to be copied into
     * machine directory.
     * @param dbFile The name of the database file.
     * @param arcdbFile The name of the archive file.
     * @param resetDir Whether the temporary directory should be reset.
     * @param externalJarFolder The folder containing the external jar 
     * library files.
     */
    public LinuxMachine(Element subTreeRoot, XmlStructure parentSettings, 
            Parameters param, String netarchiveSuiteSource,
            File logProp, File securityPolicy, File dbFile,
            File arcdbFile, boolean resetDir, File externalJarFolder) {
        super(subTreeRoot, parentSettings, param, netarchiveSuiteSource,
                logProp, securityPolicy, dbFile, arcdbFile, resetDir,
                externalJarFolder);
        // set operating system
        operatingSystem = Constants.OPERATING_SYSTEM_LINUX_ATTRIBUTE;
        scriptExtension = Constants.SCRIPT_EXTENSION_LINUX;
    }

    @Override
    protected String osInstallScript() {
        StringBuilder res = new StringBuilder();
        // echo copying null.zip to:kb-test-adm-001.kb.dk
        res.append(ScriptConstants.ECHO_COPYING + Constants.SPACE);
        res.append(netarchiveSuiteFileName);
        res.append(Constants.SPACE + ScriptConstants.TO + Constants.COLON);
        res.append(name);
        res.append(Constants.NEWLINE);
        // scp null.zip dev@kb-test-adm-001.kb.dk:/home/dev
        res.append(ScriptConstants.SCP + Constants.SPACE);
        res.append(netarchiveSuiteFileName);
        res.append(Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.COLON);
        res.append(machineParameters.getInstallDirValue());
        res.append(Constants.NEWLINE);
        // echo deleting kb-test-adm-001.kb.dk:/home/dev/TEST/lib
        res.append(ScriptConstants.ECHO_DELETING + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.COLON);
        res.append(getInstallDirPath() + getLocalLibDirPath());
        res.append(Constants.NEWLINE);
        // ssh dev@kb-test-adm-001.kb.dk rm -rf /home/dev/TEST/lib
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE);
        res.append(ScriptConstants.LINUX_FORCE_RECURSIVE_DELETE);
        res.append(Constants.SPACE);
        res.append(getInstallDirPath() + getLocalLibDirPath());
        res.append(Constants.NEWLINE);
        // echo unzipping null.zip at:kb-test-adm-001.kb.dk
        res.append(ScriptConstants.ECHO_UNZIPPING + Constants.SPACE);
        res.append(netarchiveSuiteFileName);
        res.append(Constants.SPACE + ScriptConstants.AT + Constants.COLON);
        res.append(name);
        res.append(Constants.NEWLINE);
        // ssh dev@kb-test-adm-001.kb.dk unzip -q -o /home/dev/null.zip -d 
        // /home/dev/TEST
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + ScriptConstants.LINUX_UNZIP_COMMAND
                + Constants.SPACE);
        res.append(machineParameters.getInstallDirValue());
        res.append(Constants.SLASH);
        res.append(netarchiveSuiteFileName);
        res.append(Constants.SPACE + ScriptConstants.SCRIPT_DIR 
                + Constants.SPACE);
        res.append(getInstallDirPath());
        res.append(Constants.NEWLINE);
        // create other directories.
        res.append(osInstallScriptCreateDir());
        // echo preparing for copying of settings and scripts
        res.append(ScriptConstants.ECHO_PREPARING_FOR_COPY);
        res.append(Constants.NEWLINE);
        // For overriding jmxremote.password give user all rights.
        // ssh machine: "if [ -e conf/jmxremote.password ]; 
        // then chmod u+rwx conf/jmxremote.password; fi; "
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + Constants.QUOTE_MARK + Constants.SPACE
                + ScriptConstants.LINUX_HOME_DIR + Constants.SEMICOLON 
                + Constants.SPACE + ScriptConstants.LINUX_IF_EXIST
                + Constants.SPACE);
        res.append(getConfDirPath());
        res.append(Constants.JMX_PASSWORD_FILE_NAME);
        res.append(Constants.SPACE + ScriptConstants.LINUX_THEN 
                + Constants.SPACE + ScriptConstants.LINUX_USER_ONLY 
                + Constants.SPACE);
        res.append(getConfDirPath());
        res.append(Constants.JMX_PASSWORD_FILE_NAME + Constants.SEMICOLON 
                + Constants.SPACE + ScriptConstants.FI + Constants.SEMICOLON
                + Constants.SPACE + Constants.QUOTE_MARK);
        res.append(Constants.NEWLINE);
        // For overriding jmxremote.access give user all rights.
        // ssh machine: "if [ -e conf/jmxremote.access ]; 
        // then chmod u+rwx conf/jmxremote.access; fi; "
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + Constants.QUOTE_MARK + Constants.SPACE
                + ScriptConstants.LINUX_HOME_DIR + Constants.SEMICOLON 
                + Constants.SPACE + ScriptConstants.LINUX_IF_EXIST
                + Constants.SPACE);
        res.append(getConfDirPath());
        res.append(Constants.JMX_ACCESS_FILE_NAME);
        res.append(Constants.SPACE + ScriptConstants.LINUX_THEN 
                + Constants.SPACE + ScriptConstants.LINUX_USER_ONLY 
                + Constants.SPACE);
        res.append(getConfDirPath());
        res.append(Constants.JMX_ACCESS_FILE_NAME + Constants.SEMICOLON 
                + Constants.SPACE + ScriptConstants.FI + Constants.SEMICOLON
                + Constants.SPACE + Constants.QUOTE_MARK);
        res.append(Constants.NEWLINE);
        // echo copying settings and scripts
        res.append(ScriptConstants.ECHO_COPY_SETTINGS_AND_SCRIPTS);
        res.append(Constants.NEWLINE);
        // scp -r kb-test-adm-001.kb.dk/* 
        // dev@kb-test-adm-001.kb.dk:/home/dev/TEST/conf/
        res.append(ScriptConstants.SCP + Constants.SPACE
                + ScriptConstants.SCRIPT_REPOSITORY + Constants.SPACE);
        res.append(name);
        res.append(Constants.SLASH + Constants.STAR + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.COLON);
        res.append(getConfDirPath());
        res.append(Constants.NEWLINE);
        // INSTALL EXTERNAL JAR FILES.
        res.append(osInstallExternalJarFiles());
        // APPLY HARVEST DEFINITION DATABASE!
        res.append(osInstallDatabase());
        // APPLY ARCHIVE DATABASE!
        res.append(osInstallArchiveDatabase());
        // echo make scripts executable
        res.append(ScriptConstants.ECHO_MAKE_EXECUTABLE);
        res.append(Constants.NEWLINE);
        // Allow only user to be able to deal with these files 
        // (go=-rwx,u=+rwx) = 700.
        // ssh dev@kb-test-adm-001.kb.dk "chmod 700 /home/dev/TEST/conf/*.sh "
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + Constants.QUOTE_MARK 
                + ScriptConstants.LINUX_USER_700 + Constants.SPACE); 
        res.append(getConfDirPath());
        res.append(Constants.STAR + Constants.SCRIPT_EXTENSION_LINUX 
                + Constants.SPACE + Constants.QUOTE_MARK);
        res.append(Constants.NEWLINE);
        // HANDLE JMXREMOTE PASSWORD AND ACCESS FILE.
        res.append(getJMXremoteFilesCommand());
        // END OF SCRIPT 
        return res.toString();
    }

    @Override
    protected String osKillScript() {
        StringBuilder res = new StringBuilder();
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + Constants.QUOTE_MARK + Constants.DOT
                + Constants.SPACE + ScriptConstants.ETC_PROFILE 
                + Constants.SEMICOLON + Constants.SPACE);
        res.append(getConfDirPath());
        res.append(Constants.SCRIPT_NAME_KILL_ALL);
        res.append(scriptExtension);
        res.append(Constants.QUOTE_MARK + Constants.SEMICOLON);
        res.append(Constants.NEWLINE);
        return res.toString();
    }

    /**
     * Creates the operation system specific starting script for this machine.
     * 
     * pseudo code:
     * - ssh maclogin ". /etc/profile; conf/startall.sh; sleep 5; 
     * cat install/*.log"
     * 
     * where:
     * maclogin = login for machine (username@machine).
     * conf = path to /conf directory.
     * install = path to install directory.
     * 
     * @return Operation system specific part of the startscript.
     */
    @Override
    protected String osStartScript() {
        StringBuilder res = new StringBuilder();
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + Constants.QUOTE_MARK + Constants.DOT
                + Constants.SPACE + ScriptConstants.ETC_PROFILE 
                + Constants.SEMICOLON + Constants.SPACE);
        res.append(getConfDirPath());
        res.append(Constants.SCRIPT_NAME_START_ALL);
        res.append(scriptExtension);
        res.append(Constants.SEMICOLON + Constants.SPACE 
                + ScriptConstants.SLEEP_5 + Constants.SEMICOLON 
                + Constants.SPACE + ScriptConstants.CAT + Constants.SPACE);
        res.append(getInstallDirPath());
        res.append(Constants.SLASH + ScriptConstants.STAR_LOG 
                + Constants.QUOTE_MARK);
        res.append(Constants.NEWLINE);
        return res.toString();
    }

    @Override
    protected String getInstallDirPath() {
        return machineParameters.getInstallDirValue() + Constants.SLASH 
                + getEnvironmentName();
    }

    @Override
    protected String getConfDirPath() {
        return getInstallDirPath() + Constants.CONF_DIR_LINUX;
    }
   
    /**
     * Creates the local path to the lib dir.
     * 
     * @return The path to the lib dir for ssh.
     */
    protected String getLocalLibDirPath() {
        return Constants.LIB_DIR_LINUX;
    }
    
    
    /**
     * This function creates the script to kill all applications on this 
     * machine.
     * The scripts calls all the kill script for each application. 
     * It also runs the script for killing any external database.
     * 
     * pseudo code:
     * - echo Killing all applications at machine: mac
     * - if [ -e ./kill_app.sh ]
     * -    ./kill_app.sh
     * - fi
     * - ...
     * 
     * where:
     * mac = machine name.
     * app = application name.
     * ... = the same for other applications.
     * 
     * @param directory The directory for this machine (use global variable?).
     * @throws IOFailure If an error occurred during the creation of the local
     * killall script.
     */
    @Override
    protected void createOSLocalKillAllScript(File directory) throws IOFailure {
        // create the kill all script file
        File killAllScript = new File(directory, 
                Constants.SCRIPT_NAME_KILL_ALL + scriptExtension);
        try {
            // Initialise script
            PrintWriter killPrinter = new PrintWriter(killAllScript);
            try {
                killPrinter.println(ScriptConstants.ECHO_KILL_ALL_APPS
                        + Constants.COLON + Constants.SPACE 
                        + Constants.APOSTROPHE + name + Constants.APOSTROPHE);
                killPrinter.println(ScriptConstants.BIN_BASH_COMMENT);
                killPrinter.println(ScriptConstants.CD + Constants.SPACE
                        + getConfDirPath());
                // insert path to kill script for all applications
                for(Application app : applications) {
                    // Constructing filename
                    String appScript = Constants.DOT + Constants.SLASH
                            + Constants.SCRIPT_NAME_LOCAL_KILL
                            + app.getIdentification() + scriptExtension;
                    // check if file exists
                    killPrinter.println(ScriptConstants.LINUX_IF_EXIST
                            + Constants.SPACE + appScript + Constants.SPACE
                            + ScriptConstants.LINUX_THEN + Constants.SPACE);
                    killPrinter.println(ScriptConstants.MULTI_SPACE_6
                            + appScript);
                    killPrinter.println(ScriptConstants.FI);
                }
                
                // kill the harvest database, if any, after the applications
                killPrinter.print(callKillHarvestDatabase());
                // kill the admin database, if any, after the applications
                killPrinter.print(callKillArchiveDatabase());
            } finally {
                // close script
                killPrinter.close();
            }
        } catch (IOException e) {
            String msg = "Problems creating local kill all script. ";
            log.error(msg, e);
            throw new IOFailure(msg, e);
        }
    }

    /**
     * This function creates the script to start all applications on this 
     * machine.
     * The scripts calls all the start script for each application. 
     * It also runs the script for starting any external database.
     * 
     * pseudo code:
     * - echo Starting all applications at machine: mac
     * - if [ -e ./start_app.sh ]
     * -    ./start_app.sh
     * - fi
     * - ...
     * 
     * where:
     * mac = machine name.
     * app = application name.
     * ... = the same for other applications.
     * 
     * @param directory The directory for this machine (use global variable?).
     * @throws IOFailure If an error occurred during the creation of the local
     * startall script.
     */
    @Override
    protected void createOSLocalStartAllScript(File directory) 
            throws IOFailure {
        // create the start all script file
        File startAllScript = new File(directory, 
                Constants.SCRIPT_NAME_START_ALL + scriptExtension);
        try {
            // Initialise script
            PrintWriter startPrinter = new PrintWriter(startAllScript);
            try {
                startPrinter.println(ScriptConstants.BIN_BASH_COMMENT);
                startPrinter.println(ScriptConstants.CD + Constants.SPACE
                        + getConfDirPath());

                // start the harvest database, if any, before the applications.
                startPrinter.print(callStartHarvestDatabase());
                // start the admin database, if any, before the applications.
                startPrinter.print(callStartArchiveDatabase());

                startPrinter.println(ScriptConstants.ECHO_START_ALL_APPS
                        + Constants.COLON + Constants.SPACE 
                        + Constants.APOSTROPHE + name + Constants.APOSTROPHE);

                // insert path to kill script for all applications
                for(Application app : applications) {
                    // make name of file
                    String appScript = Constants.DOT + Constants.SLASH
                            + Constants.SCRIPT_NAME_LOCAL_START
                            + app.getIdentification() + scriptExtension;
                    // check if file exists
                    startPrinter.println(ScriptConstants.LINUX_IF_EXIST
                            + Constants.SPACE + appScript + Constants.SPACE
                            + ScriptConstants.LINUX_THEN + Constants.SPACE);
                    startPrinter.println(ScriptConstants.MULTI_SPACE_6
                            + appScript);
                    startPrinter.println(ScriptConstants.FI);
                }
            } finally {
                // close script
                startPrinter.close();
            }
        } catch (IOException e) {
            String msg = "Problems creating local start all script. ";
            log.trace(msg, e);
            throw new IOFailure(msg, e);
        }
    }

    /**
     * Creates the kill scripts for all the applications.
     * 
     * The script starts by finding all running processes of the application.
     * If it finds any processes, it kills them. 
     * 
     * The kill_app.sh should have the following structure:
     * 
     * - echo Killing linux application.
     * - #!/bin/bash
     * - PIDS = $(ps -wwfe | grep fullapp | grep -v grep | grep 
     * path\settings_app.xml | awk "{print \\$2}")
     * - if [ -n "$PIDS" ]; then
     * -     kill -9 $PIDS;
     * - fi
     * 
     * Also, if a heritrix process is started, the following is added:
     * - PIDS = $(ps -wwfe | grep heritrix | grep -v grep | grep 
     * path\settings_app.xml | awk "{print \\$2}")
     * - if [ -n "$PIDS" ]; then
     * -     kill -9 $PIDS;
     * - fi
     * 
     * where:
     * path = the path to the ./conf directory.
     * fullapp = the full application name with class path.
     * app = the id of the application (name + instanceId).
     * heritrix = the heritrix class path.
     * 
     * @param directory The directory for this machine (use global variable?).
     * @throws IOFailure If an error occured during the creation of the kill
     * application script file.
     */
    @Override
    protected void createApplicationKillScripts(File directory) 
            throws IOFailure {
        // go through all applications and create their kill script
        for(Application app : applications) {
            File appKillScript = new File(directory, 
                    Constants.SCRIPT_NAME_LOCAL_KILL + app.getIdentification() 
                    + scriptExtension);
            try {
                // make print writer for writing to file
                PrintWriter appPrint = new PrintWriter(appKillScript);
                try {
                    // echo Killing linux application.
                    appPrint.println(ScriptConstants.ECHO_KILL_LINUX_APPLICATION
                            + Constants.COLON + Constants.SPACE
                            + app.getIdentification());
                    // #!/bin/bash
                    appPrint.println(ScriptConstants.BIN_BASH_COMMENT);
                    
                    // First try an ordinary kill, wait 2 seconds. Then test, 
                    // if process is still around. If it is make a hard kill 
                    // (-9)
                  
                    
                    // PIDS = $(ps -wwfe | grep fullapp | grep -v grep | grep 
                    // path\settings_app.xml | awk "{print \\$2}")
                    appPrint.println(ScriptConstants.getLinuxPIDS(
                            app.getTotalName(), getConfDirPath(), 
                            app.getIdentification()));
                    // if [ -n "$PIDS" ]; then
                    appPrint.println(ScriptConstants.LINUX_IF_N_EXIST 
                            + Constants.SPACE + Constants.QUOTE_MARK
                            + ScriptConstants.PIDS + Constants.QUOTE_MARK
                            + Constants.SPACE + ScriptConstants.LINUX_N_THEN);
                    
                    appPrint.println(ScriptConstants.KILL_PIDS 
                            + Constants.SEMICOLON);
                    // fi
                    appPrint.println(ScriptConstants.FI);
                    appPrint.println(); 
                    appPrint.println(ScriptConstants.SLEEP_2); 
                            
                    appPrint.println();
                    // Set PIDS
                    appPrint.println(ScriptConstants.getLinuxPIDS(
                            app.getTotalName(), getConfDirPath(), 
                            app.getIdentification()));
                    // IF
                    appPrint.println(ScriptConstants.LINUX_IF_N_EXIST 
                            + Constants.SPACE + Constants.QUOTE_MARK
                            + ScriptConstants.PIDS + Constants.QUOTE_MARK
                            + Constants.SPACE + ScriptConstants.LINUX_N_THEN);
                    
                    //     kill -9 $PIDS;
                    appPrint.println(ScriptConstants.KILL_9_PIDS 
                            + Constants.SEMICOLON);
                    // fi
                    appPrint.println(ScriptConstants.FI);
                    
                    
                    
                    // If the application contains a heritrix instance,
                    // then make script for killing the heritrix process.
                    String[] heritrixJmxPort = app.getSettingsValues(
                            Constants.SETTINGS_HARVEST_HERITRIX_JMX_PORT);
                    if(heritrixJmxPort != null && heritrixJmxPort.length > 0) {
                        // log if more than one jmx port defined for heritrix.
                        if(heritrixJmxPort.length > 1) {
                            log.trace(heritrixJmxPort.length 
                                    + " number of jmx-ports for a heritrix "
                                    + "harvester.");
                        }
                        appPrint.println();
                        // - PIDS = $(ps -wwfe | grep heritrix | grep -v grep 
                        // | grep path\settings_app.xml | awk "{print \\$2}")
                        appPrint.println(ScriptConstants.getLinuxPIDS(
                                Heritrix.class.getName(), getConfDirPath(), 
                                app.getIdentification()));
                        // - if [ -n "$PIDS" ]; then
                        appPrint.println(ScriptConstants.LINUX_IF_N_EXIST
                                + Constants.SPACE + Constants.QUOTE_MARK 
                                + ScriptConstants.PIDS + Constants.QUOTE_MARK
                                + Constants.SPACE 
                                + ScriptConstants.LINUX_N_THEN);
                        // first make a ordinary kill, wait 2 seconds, then make a 
                        // hard kill -9 
                        appPrint.println(ScriptConstants.KILL_PIDS + Constants.SEMICOLON);
                        // - fi
                        appPrint.println(ScriptConstants.FI);
                        appPrint.println();
                        // wait 2 seconds
                        appPrint.println(ScriptConstants.SLEEP_2);
                        appPrint.println();
                        // See if Process is still around
                        appPrint.println(ScriptConstants.getLinuxPIDS(
                                Heritrix.class.getName(), getConfDirPath(), 
                                app.getIdentification()));
                        //if still around
                        appPrint.println(ScriptConstants.LINUX_IF_N_EXIST
                                + Constants.SPACE + Constants.QUOTE_MARK 
                                + ScriptConstants.PIDS + Constants.QUOTE_MARK
                                + Constants.SPACE 
                                + ScriptConstants.LINUX_N_THEN);
                        // -     kill -9 $PIDS;
                        appPrint.println(ScriptConstants.KILL_9_PIDS
                                + Constants.SEMICOLON);
                        // - fi
                        appPrint.println(ScriptConstants.FI);
                    }
                } finally {
                    // close file
                    appPrint.close();
                }
            } catch (IOException e) {
                String msg = "Problems creating application kill script: ";
                log.error(msg, e);
                throw new IOFailure(msg, e);
            }
        }
    }

    /**
     * Creates the start scripts for all the applications.
     * 
     * The application should only be started, if it is not running already.
     * The script starts by finding all running processes of the application.
     * If any processes are found, a new application should not be started.
     * Otherwise start the application.
     * 
     * The start_app.sh should have the following structure:
     * 
     * - echo Starting linux application: app
     * - cd path
     * - #!/bin/bash
     * - PIDS = $(ps -wwfe | grep fullapp | grep -v grep | grep 
     * path\settings_app.xml | awk "{print \\$2}")
     * - if [ -n "$PIDS" ]; then
     * -     echo Application already running.
     * - else
     * -     export CLASSPATH = cp:$CLASSPATH;
     * -     JAVA
     * - fi
     * 
     * where:
     * path = the path to the install directory.
     * fullapp = the full name application with java path.
     * app = the name of the application.
     * cp = the classpaths for the application.
     * JAVA = the command to run the java application.
     * 
     * @param directory The directory for this machine (use global variable?).
     * @throws IOFailure If an error occurred during the creation of the start 
     * application script file. 
     */
    @Override
    protected void createApplicationStartScripts(File directory) 
            throws IOFailure {
        // go through all applications and create their start script
        for(Application app : applications) {
            File appStartScript = new File(directory, 
                    Constants.SCRIPT_NAME_LOCAL_START + app.getIdentification() 
                    + scriptExtension);
            try {
                // make print writer for writing to file
                PrintWriter appPrint = new PrintWriter(appStartScript);
                try {
                    // #!/bin/bash
                    appPrint.println(ScriptConstants.ECHO_START_LINUX_APP
                            + Constants.COLON + Constants.SPACE
                            + app.getIdentification());
                    // cd path
                    appPrint.println(ScriptConstants.CD + Constants.SPACE
                            + app.installPathLinux());
                    // PIDS = $(ps -wwfe | grep fullapp | grep -v grep | grep 
                    //    path\settings_app.xml | awk "{print \\$2}")
                    appPrint.println(ScriptConstants.getLinuxPIDS(
                            app.getTotalName(), getConfDirPath(), 
                            app.getIdentification()));
                    // if [ -n "$PIDS" ]; then
                    appPrint.println(ScriptConstants.LINUX_IF_N_EXIST 
                            + Constants.SPACE + Constants.QUOTE_MARK
                            + ScriptConstants.PIDS + Constants.QUOTE_MARK
                            + Constants.SPACE + ScriptConstants.LINUX_N_THEN);
                    //     echo Application already running.
                    appPrint.println(ScriptConstants.ECHO_APP_ALREADY_RUNNING);
                    // else
                    appPrint.println(ScriptConstants.ELSE);
                    //     export CLASSPATH = cp;
                    appPrint.println(ScriptConstants.MULTI_SPACE_4
                            + ScriptConstants.EXPORT_CLASSPATH
                            + osGetClassPath(app)
                            + ScriptConstants.VALUE_OF_CLASSPATH 
                            + Constants.SEMICOLON);
                    //     JAVA
                    appPrint.println(ScriptConstants.MULTI_SPACE_4
                            + ScriptConstants.JAVA + Constants.SPACE
                            + app.getMachineParameters().writeJavaOptions()
                            + Constants.SPACE + Constants.DASH 
                            + ScriptConstants.OPTION_SETTINGS 
                            + getConfDirPath() + Constants.PREFIX_SETTINGS
                            + app.getIdentification() 
                            + Constants.EXTENSION_XML_FILES + Constants.SPACE
                            + Constants.DASH 
                            + ScriptConstants.OPTION_LOG_COMPLETE
                            + Constants.SPACE + Constants.DASH 
                            + ScriptConstants.OPTION_LOG_CONFIG
                            + getConfDirPath() + Constants.LOG_PREFIX
                            + app.getIdentification() 
                            + Constants.EXTENSION_LOG_PROPERTY_FILES
                            + Constants.SPACE + Constants.DASH 
                            + ScriptConstants.OPTION_SECURITY_MANAGER
                            + Constants.SPACE + Constants.DASH 
                            + ScriptConstants.OPTION_SECURITY_POLICY
                            + getConfDirPath() 
                            + Constants.SECURITY_POLICY_FILE_NAME 
                            + Constants.SPACE + app.getTotalName()
                            + Constants.SPACE + ScriptConstants.LINUX_DEV_NULL
                            + Constants.SPACE 
                            + Constants.SCRIPT_NAME_LOCAL_START
                            + app.getIdentification() 
                            + Constants.EXTENSION_LOG_FILES
                            + Constants.SPACE 
                            + ScriptConstants.LINUX_ERROR_MESSAGE_TO_1);
                    // fi
                    appPrint.println(ScriptConstants.FI);
                } finally {
                    // close file
                    appPrint.close();
                }
            } catch (IOException e) {
                String msg = "Problems creating application start script. ";
                log.trace(msg, e);
                throw new IOFailure(msg, e);
            }
        }
    }

    @Override
    protected String osGetClassPath(Application app) {
        StringBuilder res = new StringBuilder();
        // get all the classpaths
        for(Element cp : app.getMachineParameters().getClassPaths()) {
            res.append(getInstallDirPath() + Constants.SLASH + cp.getText() 
                    + Constants.COLON);
        }
        return res.toString();
    }

    @Override
    protected String osInstallDatabase() {
        StringBuilder res = new StringBuilder();

        String databaseDir = machineParameters.getHarvestDatabaseDirValue();
        // Do not install if no proper database directory.
        if(databaseDir == null || databaseDir.isEmpty()) {
            return Constants.EMPTY;
        }

        // copy to final destination if database argument.
        if(databaseFile != null) {
            // echo Copying database
            res.append(ScriptConstants.ECHO_COPYING_DATABASE);
            res.append(Constants.NEWLINE);
            // scp database.jar user@machine:dbDir/db
            res.append(ScriptConstants.SCP + Constants.SPACE);
            res.append(databaseFile.getPath());
            res.append(Constants.SPACE);
            res.append(machineUserLogin());
            res.append(Constants.COLON);
            res.append(getInstallDirPath());
            res.append(Constants.SLASH);
            res.append(Constants.HARVEST_DATABASE_BASE_PATH);
            res.append(Constants.NEWLINE);
        }
        // unzip database.
        res.append(ScriptConstants.ECHO_UNZIPPING_DATABASE);
        res.append(Constants.NEWLINE);
        // ssh user@machine "
        // cd dir; if [ -d databaseDir ]; then echo ; 
        // else mkdir databaseDir; fi; if [ $(ls -A databaseDir) ]; 
        // then echo ERROR MESSAGE: DIR NOT EMPTY; 
        // else unzip -q -o dbDir/db -d databaseDir/.; fi; exit;
        // "
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + Constants.QUOTE_MARK
                + ScriptConstants.CD + Constants.SPACE);
        res.append(getInstallDirPath());
        res.append(Constants.SEMICOLON + Constants.SPACE 
                + ScriptConstants.LINUX_IF_DIR_EXIST + Constants.SPACE);
        res.append(databaseDir);
        res.append(Constants.SPACE + ScriptConstants.LINUX_THEN 
                + Constants.SPACE + ScriptConstants.ECHO + Constants.SPACE);
        res.append(ScriptConstants.DATABASE_ERROR_PROMPT_DIR_NOT_EMPTY);
        res.append(Constants.SEMICOLON + Constants.SPACE + ScriptConstants.ELSE
                + Constants.SPACE + ScriptConstants.LINUX_UNZIP_COMMAND
                + Constants.SPACE);
        res.append(Constants.HARVEST_DATABASE_BASE_PATH);
        res.append(Constants.SPACE + ScriptConstants.SCRIPT_DIR 
                + Constants.SPACE);
        res.append(databaseDir);
        res.append(Constants.SEMICOLON + Constants.SPACE + ScriptConstants.FI
                + Constants.SEMICOLON + Constants.SPACE + ScriptConstants.EXIT
                + Constants.SEMICOLON + Constants.SPACE + Constants.QUOTE_MARK);
        res.append(Constants.NEWLINE);

        return res.toString();
    }
    
    @Override
    protected String osInstallExternalJarFiles() {
        if(jarFolder == null) {
            return Constants.EMPTY;
        }
        
        StringBuilder res = new StringBuilder();
        
        // Comment about copying files.
        res.append(ScriptConstants.ECHO_INSTALLING_EXTERNAL_JAR_FILES);
        res.append(Constants.NEWLINE);
        // if [ -d folder ]; then scp folder machine:installdir/external; fi;
        res.append(ScriptConstants.LINUX_IF_DIR_EXIST + Constants.SPACE);
        res.append(jarFolder.getPath());
        res.append(Constants.SPACE + ScriptConstants.LINUX_THEN 
                + Constants.SPACE + ScriptConstants.SCP + Constants.SPACE 
                + ScriptConstants.DASH_R + Constants.SPACE);
        res.append(jarFolder.getPath());
        res.append(Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.COLON);
        res.append(getInstallDirPath());
        res.append(Constants.SLASH + Constants.EXTERNAL_JAR_DIRECTORY 
                + Constants.SEMICOLON + Constants.SPACE + ScriptConstants.FI 
                + Constants.SEMICOLON);
        res.append(Constants.NEWLINE);

        return res.toString();
    }
    
    @Override
    protected String osInstallArchiveDatabase() {
        String adminDatabaseDir = 
            machineParameters.getArchiveDatabaseDirValue();
        // Do not install if no proper archive database directory.
        if(adminDatabaseDir == null || adminDatabaseDir.isEmpty()) {
            return Constants.EMPTY;
        }

        // Initialise the StringBuilder containing the resulting script.
        StringBuilder res = new StringBuilder();

        // copy to final destination if database argument.
        if(arcDatabaseFile != null) {
            // echo Copying database
            res.append(ScriptConstants.ECHO_COPYING_ARCHIVE_DATABASE);
            res.append(Constants.NEWLINE);
            // scp database.jar user@machine:dbDir/bpdb
            res.append(ScriptConstants.SCP + Constants.SPACE);
            res.append(arcDatabaseFile.getPath());
            res.append(Constants.SPACE);
            res.append(machineUserLogin());
            res.append(Constants.COLON);
            res.append(getInstallDirPath());
            res.append(Constants.SLASH);
            // Now the two databases are in different directories
            res.append(Constants.ARCHIVE_DATABASE_BASE_PATH);
            res.append(Constants.NEWLINE);
        }
        // unzip database.
        res.append(ScriptConstants.ECHO_UNZIPPING_ARCHIVE_DATABASE);
        res.append(Constants.NEWLINE);
        // ssh user@machine "
        // cd dir; if [ -d bpDatabaseDir ]; then echo ; 
        // else mkdir bpDatabaseDir; fi; if [ $(ls -A bpDatabaseDir) ]; 
        // then echo ERROR MESSAGE: DIR NOT EMPTY; 
        // else unzip -q -o dbDir/bpdb -d databaseDir/.; fi; exit;
        // "
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + Constants.QUOTE_MARK
                + ScriptConstants.CD + Constants.SPACE);
        res.append(getInstallDirPath());
        res.append(Constants.SEMICOLON + Constants.SPACE 
                + ScriptConstants.LINUX_IF_DIR_EXIST + Constants.SPACE);
        res.append(adminDatabaseDir);
        res.append(Constants.SPACE + ScriptConstants.LINUX_THEN 
                + Constants.SPACE + ScriptConstants.ECHO + Constants.SPACE);
        res.append(ScriptConstants.DATABASE_ERROR_PROMPT_DIR_NOT_EMPTY);
        res.append(Constants.SEMICOLON + Constants.SPACE + ScriptConstants.ELSE
                + Constants.SPACE + ScriptConstants.LINUX_UNZIP_COMMAND
                + Constants.SPACE);
        res.append(Constants.ARCHIVE_DATABASE_BASE_PATH);
        res.append(Constants.SPACE + ScriptConstants.SCRIPT_DIR 
                + Constants.SPACE);
        res.append(adminDatabaseDir);
        res.append(Constants.SEMICOLON + Constants.SPACE + ScriptConstants.FI
                + Constants.SEMICOLON + Constants.SPACE + ScriptConstants.EXIT
                + Constants.SEMICOLON + Constants.SPACE + Constants.QUOTE_MARK);
        res.append(Constants.NEWLINE);

        return res.toString();
    }


    /**
     * Creates the specified directories in the deploy-configuration file.
     * 
     * Structure
     * - ssh login cd path; DIRS; CLEANDIR; exit;
     * 
     * where:
     * login = username@machine.
     * path = path to install directory.
     * DIRS = the way to create directories. 
     * CLEANDIR = the command to clean the tempDir (if chosen as optional)
     * 
     * The install creation of DIR has the following structure 
     * for directory dir:
     * if [ ! -d dir ]; then mkdir dir; fi;  
     * 
     * @return The script for creating the directories.
     */
    @Override
    protected String osInstallScriptCreateDir() {
        StringBuilder res = new StringBuilder();
        res.append(ScriptConstants.ECHO_CREATING_DIRECTORIES);
        res.append(Constants.NEWLINE);
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + Constants.QUOTE_MARK 
                + ScriptConstants.CD + Constants.SPACE);
        res.append(getInstallDirPath());
        res.append(Constants.SEMICOLON + Constants.SPACE);
        
        // go through all directories.
        String dir;
        
        // get archive.bitpresevation.baseDir directory.
        dir = settings.getLeafValue(
                Constants.SETTINGS_ARCHIVE_BP_BASEDIR_LEAF);
        if(dir != null && !dir.isEmpty() 
                && !dir.equalsIgnoreCase(Constants.DOT)) {
            res.append(createPathToDir(dir));
            res.append(scriptCreateDir(dir, false));
        }
        
        // get archive.arcrepository.baseDir directory.
        dir = settings.getLeafValue(
                Constants.SETTINGS_ARCHIVE_ARC_BASEDIR_LEAF);
        if(dir != null && !dir.isEmpty()
                && !dir.equalsIgnoreCase(Constants.DOT)) {
            res.append(scriptCreateDir(dir, false));
        }
        
        // get tempDir directory.
        dir = settings.getLeafValue(
                Constants.SETTINGS_TEMPDIR_LEAF);
        if(dir != null && !dir.isEmpty()
                && !dir.equalsIgnoreCase(Constants.DOT)) {
            res.append(createPathToDir(dir));
            res.append(scriptCreateDir(dir, resetTempDir));
        }

        // get the application specific directories.
        res.append(getAppDirectories());
        
        res.append(ScriptConstants.EXIT + Constants.SEMICOLON + Constants.SPACE
                + Constants.QUOTE_MARK + Constants.NEWLINE);
        
        return res.toString();
    }
    
    @Override
    protected String scriptCreateDir(String dir, boolean clean) {
        StringBuilder res = new StringBuilder();
        res.append(ScriptConstants.LINUX_IF_NOT_DIR_EXIST + Constants.SPACE);
        res.append(dir);
        res.append(Constants.SPACE + ScriptConstants.LINUX_THEN
                + Constants.SPACE + ScriptConstants.MKDIR + Constants.SPACE);
        res.append(dir);
        if(clean) {
            res.append(Constants.SEMICOLON + Constants.SPACE
                    + ScriptConstants.ELSE_REMOVE + Constants.SPACE);
            res.append(dir);
            res.append(Constants.SEMICOLON + Constants.SPACE 
                    + ScriptConstants.MKDIR + Constants.SPACE);
            res.append(dir);
        }
        res.append(Constants.SEMICOLON + Constants.SPACE + ScriptConstants.FI
                + Constants.SEMICOLON + Constants.SPACE);

        return res.toString();
    }
    
    /**
     * Function for creating the directories along the path
     * until the end directory. Does not create the end directory.
     * 
     * @param dir The path to the directory.
     * @return The script for creating the directory.
     */
    protected String createPathToDir(String dir) {
        StringBuilder res = new StringBuilder();

        String[] pathDirs = dir.split(Constants.REGEX_SLASH_CHARACTER);
        StringBuilder path = new StringBuilder();

        // only make directories along path to last directory, 
        // don't create end directory.
        for(int i = 0; i < pathDirs.length-1; i++) {
            // don't make directory of empty path.
            if(!pathDirs[i].isEmpty()) {
                path.append(pathDirs[i]);
                res.append(scriptCreateDir(path.toString(), false));
            }
            path.append(Constants.SLASH);
        }

        return res.toString();
    }
    
    @Override
    protected String getAppDirectories() {
        StringBuilder res = new StringBuilder();
        String[] dirs;

        for(Application app : applications) {
            // get archive.fileDir directories.
            dirs = app.getSettingsValues(
                    Constants.SETTINGS_BITARCHIVE_BASEFILEDIR_LEAF);
            if(dirs != null && dirs.length > 0) {
                for(String dir : dirs) {
                    res.append(createPathToDir(dir));   
                    res.append(scriptCreateDir(dir, false));
                    for(String subdir : Constants.BASEFILEDIR_SUBDIRECTORIES) {
                        res.append(scriptCreateDir(
                                dir + Constants.SLASH + subdir, false));
                    }
                }
            }

            // get harvester.harvesting.serverDir directories.
            dirs = app.getSettingsValues(
                    Constants.SETTINGS_HARVEST_SERVERDIR_LEAF);
            if(dirs != null && dirs.length > 0) {
                for(String dir : dirs) {
                    res.append(createPathToDir(dir));
                    res.append(scriptCreateDir(dir, false));
                }
            }
            
            // get the viewerproxy.baseDir directories.
            dirs = app.getSettingsValues(
                    Constants.SETTINGS_VIEWERPROXY_BASEDIR_LEAF);
            if(dirs != null && dirs.length > 0) {
                for(String dir : dirs) {
                    res.append(createPathToDir(dir));
                    res.append(scriptCreateDir(dir, false));
                }
            }
            
            // get the common.tempDir directories. But only those, 
            // which are not the same as the machine common.tempDir.
            dirs = app.getSettingsValues(Constants.SETTINGS_TEMPDIR_LEAF);
            if(dirs != null && dirs.length > 0) {
                String machineDir = settings.getLeafValue(
                        Constants.SETTINGS_TEMPDIR_LEAF);
                for(String dir : dirs) {
                    // Don't make machine temp dir twice.
                    if(!dir.equals(machineDir)) {
                        res.append(createPathToDir(dir));
                        res.append(scriptCreateDir(dir, resetTempDir));
                    }
                }
            }
        }

        return res.toString();
    }

    /**
     * Dummy function on linux machine.
     * This is only used for windows machines!
     * 
     * @param dir The directory to put the file.
     */
    @Override
    protected void createInstallDirScript(File dir) {
        // Do nothing. Dummy function on linux machine.
    }

    @Override
    protected String changeFileDirPathForSecurity(String path) {
        path += Constants.SLASH + Constants.SECURITY_FILE_DIR_TAG 
                + Constants.SLASH;
        return path.replace(Constants.SLASH, 
                ScriptConstants.SECURITY_DIR_SEPARATOR);
    }

    @Override
    protected String getJMXremoteFilesCommand() {
        String accessFilePath;
        String passwordFilePath;
        String[] options;

        // retrieve the access file path.
        options = settings.getLeafValues(Constants
                .SETTINGS_COMMON_JMX_ACCESSFILE);

        // extract the path, if any. Else set default.
        if(options.length == 0) {
            accessFilePath = Constants.JMX_ACCESS_FILE_PATH_DEFAULT;
        } else {
            accessFilePath = options[0];
            // warn if more than one access file is defined.
            if(options.length > 1) {
                log.debug(Constants.MSG_WARN_TOO_MANY_JMXREMOTE_FILE_PATHS);
            }
        }

        // retrieve the password file path.
        options = settings.getLeafValues(Constants
                .SETTINGS_COMMON_JMX_PASSWORDFILE);

        // extract the path, if any. Else set default.
        if(options.length == 0) {
            passwordFilePath = Constants.JMX_PASSWORD_FILE_PATH_DEFAULT;
        } else {
            passwordFilePath = options[0];
            // warn if more than one access file is defined.
            if(options.length > 1) {
                log.debug(Constants.MSG_WARN_TOO_MANY_JMXREMOTE_FILE_PATHS);
            }
        }

        // initialise the resulting command string.
        StringBuilder res = new StringBuilder();

        // echo make password files readonly
        res.append(ScriptConstants.ECHO_MAKE_PASSWORD_FILES);
        res.append(Constants.NEWLINE);

        // IF NOT DEFAULT PATHS, THEN MAKE SCRIPT TO MOVE THE FILES.
        if(!accessFilePath.equals(Constants.JMX_ACCESS_FILE_PATH_DEFAULT)) {
            // ssh dev@kb-test-adm-001.kb.dk "mv -f
            // installpath/conf/jmxremote.access installpath/accessFilePath"
            res.append(ScriptConstants.SSH + Constants.SPACE);
            res.append(machineUserLogin());
            res.append(Constants.SPACE + Constants.QUOTE_MARK);
            res.append(ScriptConstants.LINUX_FORCE_MOVE);
            res.append(Constants.SPACE);
            res.append(getInstallDirPath());
            res.append(Constants.SLASH);
            res.append(Constants.JMX_ACCESS_FILE_PATH_DEFAULT);
            res.append(Constants.SPACE);
            res.append(getInstallDirPath());
            res.append(Constants.SLASH);
            res.append(accessFilePath);
            res.append(Constants.QUOTE_MARK);
            res.append(Constants.NEWLINE);
        }

        if(!passwordFilePath.equals(Constants.JMX_PASSWORD_FILE_PATH_DEFAULT)) {
            // ssh dev@kb-test-adm-001.kb.dk "mv -f
            // installpath/conf/jmxremote.access installpath/accessFilePath"
            res.append(ScriptConstants.SSH + Constants.SPACE);
            res.append(machineUserLogin());
            res.append(Constants.SPACE + Constants.QUOTE_MARK);
            res.append(ScriptConstants.LINUX_FORCE_MOVE);
            res.append(Constants.SPACE);
            res.append(getInstallDirPath());
            res.append(Constants.SLASH);
            res.append(Constants.JMX_PASSWORD_FILE_PATH_DEFAULT);
            res.append(Constants.SPACE);
            res.append(getInstallDirPath());
            res.append(Constants.SLASH);
            res.append(passwordFilePath);
            res.append(Constants.QUOTE_MARK);
            res.append(Constants.NEWLINE);
        }

        // Allow only user to be able to only read jmxremote.password 
        // (a=-rwx,u=+r) = 400.
        // ssh dev@kb-test-adm-001.kb.dk "chmod 400 
        // /home/dev/TEST/conf/jmxremote.password"
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + Constants.QUOTE_MARK
                + ScriptConstants.LINUX_USER_400 + Constants.SPACE);
        res.append(getInstallDirPath());
        res.append(Constants.SLASH);
        res.append(passwordFilePath);
        res.append(Constants.QUOTE_MARK);
        res.append(Constants.NEWLINE);
        // ssh dev@kb-test-adm-001.kb.dk "chmod 400 
        // /home/dev/TEST/conf/jmxremote.access"
        res.append(ScriptConstants.SSH + Constants.SPACE);
        res.append(machineUserLogin());
        res.append(Constants.SPACE + Constants.QUOTE_MARK
                + ScriptConstants.LINUX_USER_400 + Constants.SPACE);
        res.append(getInstallDirPath());
        res.append(Constants.SLASH);
        res.append(accessFilePath);
        res.append(Constants.QUOTE_MARK);
        res.append(Constants.NEWLINE);

        return res.toString();
    }

    /**
     * Creates script for restarting all the applications on a machine.
     * This script should start by killing all the existing processes, and then
     * starting them again.
     * 
     * First the killall scripts is called, then wait for 5 seconds for the 
     * applications to be fully terminated, and finally the startall 
     * script is called.
     * 
     * @param dir The directory where the script file will be placed.
     * @throws IOFailure If the restart script cannot be created.
     */
    @Override
    protected void createRestartScript(File dir) throws IOFailure {
        try {
            // initialise the script file.
            File restartScript = new File(dir, 
                    Constants.SCRIPT_NAME_RESTART + scriptExtension);
            
            // make print writer for writing to file
            PrintWriter restartPrint = new PrintWriter(restartScript);
            try {
                // init, go to directory
                restartPrint.println(ScriptConstants.BIN_BASH_COMMENT);
                restartPrint.println(ScriptConstants.CD + Constants.SPACE
                        + getConfDirPath());
                
                // call killall script.
                restartPrint.print(Constants.DOT + Constants.SLASH
                        + Constants.SCRIPT_NAME_KILL_ALL
                        + scriptExtension);
                restartPrint.print(Constants.NEWLINE);
                
                // call wait script.
                restartPrint.print(ScriptConstants.SLEEP);
                restartPrint.print(Constants.SPACE);
                restartPrint.print(Constants.WAIT_TIME_DURING_RESTART);
                restartPrint.print(Constants.NEWLINE);
                
                // call startall script.
                restartPrint.print(Constants.DOT + Constants.SLASH
                        + Constants.SCRIPT_NAME_START_ALL
                        + scriptExtension);
                restartPrint.print(Constants.NEWLINE);
            } finally {
                // close file
                restartPrint.close();
            }
        } catch (IOException e) {
            // Log the error and throw an IOFailure.
            log.trace(Constants.MSG_ERROR_RESTART_FILE, e);
            throw new IOFailure(Constants.MSG_ERROR_RESTART_FILE, e);
        }
    }
    
    /**
     * Creates a script for starting the archive database on a given machine.
     * This is only created if the &lt;globalArchiveDatabaseDir&gt; parameter
     * is defined on the machine level.
     * 
     * <br/> &gt; #!/bin/bash
     * <br/> &gt; cd InstallDir
     * <br/> &gt; java -cp 'DB-CLASSPATH' 
     * org.apache.derby.drda.NetworkServerControl start < /dev/null > 
     * start_external_database.log 2>&1 &
     * 
     * @param dir The directory where the script will be placed.
     * @throws IOFailure If the script cannot be written.
     */
    @Override
    protected void createArchiveDatabaseStartScript(File dir) throws IOFailure {
        
        // Ignore if no archive database directory has been defined.
        String dbDir = machineParameters.getArchiveDatabaseDirValue();
        if(dbDir.isEmpty()) {
            return;
        }

        try {
            // initialise the script file.
            File startArcDBScript = new File(dir, 
                    Constants.SCRIPT_NAME_ADMIN_DB_START + scriptExtension);
            
            // retrieve the port
            String port = settings.getLeafValue(
                    Constants.SETTINGS_ARCHIVE_DATABASE_PORT);
            
            // make print writer for writing to file
            PrintWriter startDBPrint = new PrintWriter(startArcDBScript);
            try {
                // - #!/bin/bash
                startDBPrint.println(ScriptConstants.BIN_BASH_COMMENT);
                // - cd InstallDir
                startDBPrint.print(ScriptConstants.CD + Constants.SPACE);
                startDBPrint.println(getInstallDirPath());
                // - java -Dderby.system.home=$INSTALLDIR/archivedatabasedir 
                //  -cp 'DB-CLASSPATH' 
                // org.apache.derby.drda.NetworkServerControl start  
                // < /dev/null > start_external_database.log 2>&1 &
                startDBPrint.print(ScriptConstants.JAVA + Constants.SPACE);
                // FIXME: Incomplete implementation of NAS-2030
                //startDBPrint.print("-Dderby.system.home="
                //        + getInstallDirPath() + Constants.SLASH
                //        + dbDir
                //        + Constants.SPACE);
                
                startDBPrint.print(machineParameters.writeJavaOptions());
                startDBPrint.print(Constants.SPACE);
                startDBPrint.print(ScriptConstants.JAVA_CLASSPATH);
                startDBPrint.print(Constants.SPACE + getDbClasspaths());
                startDBPrint.print(ScriptConstants.DERBY_ACCESS_METHOD);
                // insert the PORT if any specified.
                if(port != null && !port.isEmpty()) {
                    startDBPrint.print(Constants.SPACE);
                    startDBPrint.print(ScriptConstants.DATABASE_PORT_ARGUMENT);
                    startDBPrint.print(Constants.SPACE);
                    startDBPrint.print(port);
                } 
                startDBPrint.print(Constants.SPACE);
                startDBPrint.print(ScriptConstants.DERBY_COMMAND_START);
                startDBPrint.print(Constants.SPACE);
                startDBPrint.print(ScriptConstants.LINUX_DEV_NULL);
                startDBPrint.print(Constants.SPACE);
                startDBPrint.print(Constants.SCRIPT_NAME_ADMIN_DB_START);
                startDBPrint.print(Constants.EXTENSION_LOG_FILES);
                startDBPrint.print(Constants.SPACE);
                startDBPrint.println(ScriptConstants.LINUX_ERROR_MESSAGE_TO_1);
            } finally {
                // close file
                startDBPrint.close();
            }
        } catch (IOException e) {
            // Log the error and throw an IOFailure.
            log.trace(Constants.MSG_ERROR_DB_START_FILE, e);
            throw new IOFailure(Constants.MSG_ERROR_DB_START_FILE, e);
        }
    }
    
    /**
     * Method for generating the command for running the 
     * external_admin_database_start script. This should be called before the 
     * application on the machines have been started.
     * 
     * <br/> &gt; echo Starting external database
     * <br/> &gt; if [ -e ./start_external_admin_database.sh ]; then
     * <br/> &gt;     ./start_external_admin_database.sh &
     * <br/> &gt;     sleep 5
     * <br/> &gt; fi
     * 
     * @return The command for running external_admin_database_start script.
     */
    protected String callStartArchiveDatabase() {
        // Ignore if no archive database directory has been defined.
        String dbDir = machineParameters.getArchiveDatabaseDirValue();
        if(dbDir.isEmpty()) {
            return "";
        }
        
        // Constructing filename
        String appScript = Constants.DOT + Constants.SLASH
                + Constants.SCRIPT_NAME_ADMIN_DB_START + scriptExtension;
        
        StringBuilder res = new StringBuilder();
        // echo Starting external database
        res.append(ScriptConstants.ECHO_START_EXTERNAL_ADMIN_DATABASE);
        res.append(Constants.NEWLINE);
        // if [ -e ./start_external_database.sh ]; then
        res.append(ScriptConstants.LINUX_IF_EXIST + Constants.SPACE);
        res.append(appScript + Constants.SPACE + ScriptConstants.LINUX_THEN);
        res.append(Constants.NEWLINE);
        //    ./start_external_database.sh
        res.append(ScriptConstants.MULTI_SPACE_6 + appScript);
        res.append(ScriptConstants.LINUX_RUN_BACKGROUND + Constants.NEWLINE);
        //    sleep 5
        res.append(ScriptConstants.MULTI_SPACE_6 + ScriptConstants.SLEEP_5);
        res.append(Constants.NEWLINE);
        // fi
        res.append(ScriptConstants.FI + Constants.NEWLINE);        

        return res.toString();
    }

    /**
     * Creates a script for killing the archive database on a given machine.
     * This is only created if the &lt;globalArchiveDatabaseDir&gt; parameter
     * is defined on the machine level.
     * 
     * The output is appended to the log, thus the '>>' instead of the standard
     * '>' when redirecting the output.
     * 
     * <br/> &gt; #!/bin/bash
     * <br/> &gt; cd InstallDir
     * <br/> &gt; java -cp 'DB-CLASSPATH' 
     * org.apache.derby.drda.NetworkServerControl shutdown < /dev/null >> 
     * start_external_database.log 2>&1 &
     * 
     * <br/>
     * where 'PORT' is in the setting: settings.archive.admin.database.port
     * 
     * @param dir The directory where the script will be placed.
     * @throws IOFailure If the script cannot be created.
     */
    @Override
    protected void createArchiveDatabaseKillScript(File dir) throws IOFailure {
        // Ignore if no archive database directory has been defined.
        String dbDir = machineParameters.getArchiveDatabaseDirValue();
        if(dbDir.isEmpty()) {
            return;
        }
        
        try {
            // initialise the script file.
            File killArcDBScript = new File(dir, 
                    Constants.SCRIPT_NAME_ADMIN_DB_KILL + scriptExtension);
            
            // retrieve the port for the database.
            String port = settings.getLeafValue(
                    Constants.SETTINGS_ARCHIVE_DATABASE_PORT);
            
            // make print writer for writing to file
            PrintWriter killDBPrint = new PrintWriter(killArcDBScript);
            try {
                // - #!/bin/bash
                killDBPrint.println(ScriptConstants.BIN_BASH_COMMENT);

                // - cd InstallDir
                killDBPrint.print(ScriptConstants.CD + Constants.SPACE);
                killDBPrint.println(getInstallDirPath());
                // - java -cp 'DB-CLASSPATH' 
                // org.apache.derby.drda.NetworkServerControl shutdown  
                // < /dev/null >> start_external_database.log 2>&1 &
                killDBPrint.print(ScriptConstants.JAVA + Constants.SPACE);
                killDBPrint.print(ScriptConstants.JAVA_CLASSPATH);
                killDBPrint.print(Constants.SPACE + getDbClasspaths());
                killDBPrint.print(ScriptConstants.DERBY_ACCESS_METHOD);
                // insert the PORT if any specified.
                if(port != null && !port.isEmpty()) {
                    killDBPrint.print(Constants.SPACE);
                    killDBPrint.print(ScriptConstants.DATABASE_PORT_ARGUMENT);
                    killDBPrint.print(Constants.SPACE);
                    killDBPrint.print(port);
                } 
                killDBPrint.print(Constants.SPACE);
                killDBPrint.print(ScriptConstants.DERBY_COMMAND_KILL);
                killDBPrint.print(Constants.SPACE);
                killDBPrint.print(ScriptConstants.LINUX_DEV_NULL);
                killDBPrint.print(Constants.GREATER_THAN);
                killDBPrint.print(Constants.SPACE);
                killDBPrint.print(Constants.SCRIPT_NAME_ADMIN_DB_START);
                killDBPrint.print(Constants.EXTENSION_LOG_FILES);
                killDBPrint.print(Constants.SPACE);
                killDBPrint.println(ScriptConstants.LINUX_ERROR_MESSAGE_TO_1);
            } finally {
                // close file
                killDBPrint.close();
            }
        } catch (IOException e) {
            // Log the error and throw an IOFailure.
            log.trace(Constants.MSG_ERROR_DB_KILL_FILE, e);
            throw new IOFailure(Constants.MSG_ERROR_DB_KILL_FILE, e);
        }
    }
    
    
    /**
     * Method for generating the command for running the 
     * external_database_kill script. This should be called when the 
     * application on the machines have been killed.
     * 
     * <br/> &gt; echo Killing external database
     * <br/> &gt; if [ -e ./kill_external_database.sh ]; then
     * <br/> &gt;     ./kill_external_database.sh
     * <br/> &gt; fi
     * 
     * @return The command for running external_database_kill script.
     */
    protected String callKillArchiveDatabase() {
        // Ignore if no archive database directory has been defined.
        String dbDir = machineParameters.getArchiveDatabaseDirValue();
        if(dbDir.isEmpty()) {
            return "";
        }

        // Constructing filename
        String appScript = Constants.DOT + Constants.SLASH
                + Constants.SCRIPT_NAME_ADMIN_DB_KILL + scriptExtension;

        StringBuilder res = new StringBuilder();
        // echo Killing external database
        res.append(ScriptConstants.ECHO_KILL_EXTERNAL_ADMIN_DATABASE);
        res.append(Constants.NEWLINE);
        // if [ -e ./kill_external_database.sh ]; then
        res.append(ScriptConstants.LINUX_IF_EXIST + Constants.SPACE);
        res.append(appScript + Constants.SPACE + ScriptConstants.LINUX_THEN);
        res.append(Constants.NEWLINE);
        //    ./kill_external_database.sh
        res.append(ScriptConstants.MULTI_SPACE_6 + appScript);
        res.append(Constants.NEWLINE);
        // fi
        res.append(ScriptConstants.FI + Constants.NEWLINE);        
        
        return res.toString();
    }
    
    /**
     * Creates a script for starting the harvest database on a given machine.
     * This is only created if the &lt;deployHarvestDatabaseDir&gt; parameter
     * is defined on the machine level.
     * 
     * <br/> &gt; #!/bin/bash
     * <br/> &gt; cd InstallDir
     * <br/> &gt; java -cp 'DB-CLASSPATH' 
     * org.apache.derby.drda.NetworkServerControl start < /dev/null > 
     * start_external_harvest_database.log 2>&1 &
     * 
     * @param dir The directory where the script will be placed.
     * @throws IOFailure If the script cannot be written.
     */
    @Override
    protected void createHarvestDatabaseStartScript(File dir) throws IOFailure {
        // Ignore if no harvest database directory has been defined.
        String dbDir = machineParameters.getHarvestDatabaseDirValue();
        if(dbDir.isEmpty()) {
            return;
        }

        try {
            // initialise the script file.
            File startHarvestDBScript = new File(dir, 
                    Constants.SCRIPT_NAME_HARVEST_DB_START + scriptExtension);
            
            // retrieve the port
            String port = settings.getLeafValue(
                    Constants.SETTINGS_HARVEST_DATABASE_PORT);
            
            // make print writer for writing to file
            PrintWriter startDBPrint = new PrintWriter(startHarvestDBScript);
            try {
                // - #!/bin/bash
                startDBPrint.println(ScriptConstants.BIN_BASH_COMMENT);
                // - cd InstallDir
                startDBPrint.print(ScriptConstants.CD + Constants.SPACE);
                startDBPrint.println(getInstallDirPath());
                // - java -cp 'DB-CLASSPATH' 
                // org.apache.derby.drda.NetworkServerControl start  
                // < /dev/null > start_external_harvest_database.log 2>&1 &
                startDBPrint.print(ScriptConstants.JAVA + Constants.SPACE);
 // FIXME: Incomplete implementation of NAS-2030               
 //               startDBPrint.print("-Dderby.system.home="
 //                       + getInstallDirPath() + Constants.SLASH
 //                       + dbDir
 //                       + Constants.SPACE);

                startDBPrint.print(machineParameters.writeJavaOptions());
                startDBPrint.print(Constants.SPACE);
                startDBPrint.print(ScriptConstants.JAVA_CLASSPATH);
                startDBPrint.print(Constants.SPACE + getDbClasspaths());
                startDBPrint.print(ScriptConstants.DERBY_ACCESS_METHOD);
                // insert the PORT if any specified.
                if(port != null && !port.isEmpty()) {
                    startDBPrint.print(Constants.SPACE);
                    startDBPrint.print(ScriptConstants.DATABASE_PORT_ARGUMENT);
                    startDBPrint.print(Constants.SPACE);
                    startDBPrint.print(port);
                } 
                startDBPrint.print(Constants.SPACE);
                startDBPrint.print(ScriptConstants.DERBY_COMMAND_START);
                startDBPrint.print(Constants.SPACE);
                startDBPrint.print(ScriptConstants.LINUX_DEV_NULL);
                startDBPrint.print(Constants.SPACE);
                startDBPrint.print(Constants.SCRIPT_NAME_HARVEST_DB_START);
                startDBPrint.print(Constants.EXTENSION_LOG_FILES);
                startDBPrint.print(Constants.SPACE);
                startDBPrint.println(ScriptConstants.LINUX_ERROR_MESSAGE_TO_1);
            } finally {
                // close file
                startDBPrint.close();
            }
        } catch (IOException e) {
            // Log the error and throw an IOFailure.
            log.trace(Constants.MSG_ERROR_DB_START_FILE, e);
            throw new IOFailure(Constants.MSG_ERROR_DB_START_FILE, e);
        }
    }
    
    /**
     * Method for generating the command for running the 
     * external_harvest_database_start script. This should be called before the 
     * application on the machines have been started.
     * 
     * <br/> &gt; echo Starting external harvest database
     * <br/> &gt; if [ -e ./start_external_harvest_database.sh ]; then
     * <br/> &gt;     ./start_external_harvest_database.sh &
     * <br/> &gt;     sleep 5
     * <br/> &gt; fi
     * 
     * @return The command for running external_harvest_database_start script.
     */
    protected String callStartHarvestDatabase() {
        // Ignore if no archive database directory has been defined.
        String dbDir = machineParameters.getHarvestDatabaseDirValue();
        if(dbDir.isEmpty()) {
            return "";
        }
        
        // Constructing filename
        String appScript = Constants.DOT + Constants.SLASH
                + Constants.SCRIPT_NAME_HARVEST_DB_START + scriptExtension;
        
        String app2Script = Constants.DOT + Constants.SLASH
                + Constants.SCRIPT_NAME_HARVEST_DB_UPDATE + scriptExtension;;
        
        StringBuilder res = new StringBuilder();
        // echo Starting external harvest database
        res.append(ScriptConstants.ECHO_START_EXTERNAL_HARVEST_DATABASE);
        res.append(Constants.NEWLINE);
        // if [ -e ./start_external_harvest_database.sh ]; then
        res.append(ScriptConstants.LINUX_IF_EXIST + Constants.SPACE);
        res.append(appScript + Constants.SPACE + ScriptConstants.LINUX_THEN);
        res.append(Constants.NEWLINE);
        //    ./start_external_harvest_database.sh
        res.append(ScriptConstants.MULTI_SPACE_6 + appScript);
        res.append(ScriptConstants.LINUX_RUN_BACKGROUND + Constants.NEWLINE);
        //    sleep 5
        res.append(ScriptConstants.MULTI_SPACE_6 + ScriptConstants.SLEEP_5);
        // fi
        res.append(Constants.NEWLINE + ScriptConstants.FI); 
        res.append(Constants.NEWLINE);
        // echo Updating external harvest database
        res.append(ScriptConstants.ECHO_UPDATE_EXTERNAL_HARVEST_DATABASE);
        res.append(Constants.NEWLINE);
        // if [ -e ./start_external_harvest_database.sh ]; then
        res.append(ScriptConstants.LINUX_IF_EXIST + Constants.SPACE);
        res.append(app2Script + Constants.SPACE + ScriptConstants.LINUX_THEN);
        res.append(Constants.NEWLINE);
        res.append(ScriptConstants.MULTI_SPACE_6 + app2Script);
	res.append(Constants.NEWLINE);
        // fi
        res.append(ScriptConstants.FI + Constants.NEWLINE);        

        return res.toString();
    }

    /**
     * Creates a script for killing the harvest database on a given machine.
     * This is only created if the &lt;globalHarvestDatabaseDir&gt; parameter
     * is defined on the machine level.
     * 
     * The output is appended to the log, thus the '>>' instead of the standard
     * '>' when redirecting the output.
     * 
     * <br/> &gt; #!/bin/bash
     * <br/> &gt; cd InstallDir
     * <br/> &gt; java -cp 'DB-CLASSPATH' 
     * org.apache.derby.drda.NetworkServerControl shutdown < /dev/null >> 
     * start_external_harvest_database.log 2>&1 &
     * 
     * <br/>
     * where 'PORT' is in the setting: settings.common.database.port
     * 
     * @param dir The directory where the script will be placed.
     * @throws IOFailure If the script cannot be created.
     */
    @Override
    protected void createHarvestDatabaseKillScript(File dir) throws IOFailure {
        // Ignore if no harvest database directory has been defined.
        String dbDir = machineParameters.getHarvestDatabaseDirValue();
        if(dbDir.isEmpty()) {
            return;
        }
        
        try {
            // initialise the script file.
            File killHarvestDBScript = new File(dir, 
                    Constants.SCRIPT_NAME_HARVEST_DB_KILL + scriptExtension);
            
            // retrieve the port for the database.
            String port = settings.getLeafValue(
                    Constants.SETTINGS_HARVEST_DATABASE_PORT);
            
            // make print writer for writing to file
            PrintWriter killDBPrint = new PrintWriter(killHarvestDBScript);
            try {
                // - #!/bin/bash
                killDBPrint.println(ScriptConstants.BIN_BASH_COMMENT);

                // - cd InstallDir
                killDBPrint.print(ScriptConstants.CD + Constants.SPACE);
                killDBPrint.println(getInstallDirPath());
                // - java -cp 'DB-CLASSPATH' 
                // org.apache.derby.drda.NetworkServerControl shutdown  
                // < /dev/null >> start_external_harvest_database.log 2>&1 &
                killDBPrint.print(ScriptConstants.JAVA + Constants.SPACE);
                killDBPrint.print(ScriptConstants.JAVA_CLASSPATH);
                killDBPrint.print(Constants.SPACE + getDbClasspaths());
                killDBPrint.print(ScriptConstants.DERBY_ACCESS_METHOD);
                // insert the PORT if any specified.
                if(port != null && !port.isEmpty()) {
                    killDBPrint.print(Constants.SPACE);
                    killDBPrint.print(ScriptConstants.DATABASE_PORT_ARGUMENT);
                    killDBPrint.print(Constants.SPACE);
                    killDBPrint.print(port);
                }
                killDBPrint.print(Constants.SPACE);
                killDBPrint.print(ScriptConstants.DERBY_COMMAND_KILL);
                killDBPrint.print(Constants.SPACE);
                killDBPrint.print(ScriptConstants.LINUX_DEV_NULL);
                killDBPrint.print(Constants.GREATER_THAN);
                killDBPrint.print(Constants.SPACE);
                killDBPrint.print(Constants.SCRIPT_NAME_HARVEST_DB_START);
                killDBPrint.print(Constants.EXTENSION_LOG_FILES);
                killDBPrint.print(Constants.SPACE);
                killDBPrint.println(ScriptConstants.LINUX_ERROR_MESSAGE_TO_1);
            } finally {
                // close file
                killDBPrint.close();
            }
        } catch (IOException e) {
            // Log the error and throw an IOFailure.
            log.trace(Constants.MSG_ERROR_DB_KILL_FILE, e);
            throw new IOFailure(Constants.MSG_ERROR_DB_KILL_FILE, e);
        }
    }
    
    
    /**
     * Method for generating the command for running the 
     * external_database_kill script. This should be called when the 
     * application on the machines have been killed.
     * 
     * <br/> &gt; echo Killing external harvest database
     * <br/> &gt; if [ -e ./kill_external_harvest_database.sh ]; then
     * <br/> &gt;     ./kill_external_harvest_database.sh
     * <br/> &gt; fi
     * 
     * @return The command for running external_harvest_database_kill script.
     */
    protected String callKillHarvestDatabase() {
        // Ignore if no harvest database directory has been defined.
        String dbDir = machineParameters.getHarvestDatabaseDirValue();
        if(dbDir.isEmpty()) {
            return "";
        }

        // Constructing filename
        String appScript = Constants.DOT + Constants.SLASH
                + Constants.SCRIPT_NAME_HARVEST_DB_KILL + scriptExtension;

        StringBuilder res = new StringBuilder();
        // echo Killing external harvest database
        res.append(ScriptConstants.ECHO_KILL_EXTERNAL_HARVEST_DATABASE);
        res.append(Constants.NEWLINE);
        // if [ -e ./kill_external_harvest_database.sh ]; then
        res.append(ScriptConstants.LINUX_IF_EXIST + Constants.SPACE);
        res.append(appScript + Constants.SPACE + ScriptConstants.LINUX_THEN);
        res.append(Constants.NEWLINE);
        //    ./kill_external_harvest_database.sh
        res.append(ScriptConstants.MULTI_SPACE_6 + appScript);
        res.append(Constants.NEWLINE);
        // fi
        res.append(ScriptConstants.FI + Constants.NEWLINE);        
        
        return res.toString();
    }

    /**
     * Method for combining the classpaths for the database access. <br/>
     * E.g. /home/test/NAS/lib/db/derby.jar:/home/test/NAS/lib/db/derbynet.jar
     * 
     * @return The combined classpaths for accessing the database.
     */
    private String getDbClasspaths() {
        StringBuilder res = new StringBuilder();
        
        for(int i = 0; i < ScriptConstants.DERBY_ACCESS_CLASSPATH.length; i++) {
            // ignore colon at the first classpath.
            if(i != 0) {
                res.append(Constants.COLON);
            }
            res.append(getInstallDirPath() + Constants.SLASH);
            res.append(ScriptConstants.DERBY_ACCESS_CLASSPATH[i]);
        }
        res.append(Constants.SPACE);
        return res.toString();
    }

    @Override
    protected void createHarvestDatabaseUpdateScript(File dir) {
        // Ignore if no harvest database directory has been defined.
        // Only update database on machine, where harvestdatabase is running
        String dbDir = machineParameters.getHarvestDatabaseDirValue();
        if(dbDir.isEmpty()) {
            return;
        }
        
        try {
            // initialise the script file.
            File updateHarvestDBScript = new File(dir, 
                    Constants.SCRIPT_NAME_HARVEST_DB_UPDATE + scriptExtension);
            
            File updateHarvestDBSettingsFile = new File(dir, 
                    Constants.SETTINGS_PREFIX + Constants.SCRIPT_NAME_HARVEST_DB_UPDATE +
                    Constants.EXTENSION_XML_FILES);
            PrintWriter updateDBSettings = new PrintWriter(updateHarvestDBSettingsFile);
            updateDBSettings.println(settings.getXML());
            updateDBSettings.close();
            
            // make print writer for writing to file
            PrintWriter updateDBPrint = new PrintWriter(updateHarvestDBScript);
            try {
                // - #!/bin/bash
                updateDBPrint.println(ScriptConstants.BIN_BASH_COMMENT);

                // - cd InstallDir
                updateDBPrint.print(ScriptConstants.CD + Constants.SPACE);
                updateDBPrint.println(getInstallDirPath());
                
                // - java -cp  
                // org.apache.derby.drda.NetworkServerControl shutdown  
                // < /dev/null >> start_external_harvest_database.log 2>&1 &
                
                updateDBPrint.print(ScriptConstants.EXPORT_CLASSPATH);
                updateDBPrint.print(getDefaultMachineClasspath() + ScriptConstants.NEWLINE);
                
                updateDBPrint.print(ScriptConstants.JAVA + Constants.SPACE + "-" 
                        +  ScriptConstants.OPTION_SETTINGS 
                        +  getConfDirPath() + updateHarvestDBSettingsFile.getName()
                        + Constants.SPACE);
                updateDBPrint.print(ScriptConstants.HARVEST_DATABASE_UPDATE_APP);
                
                updateDBPrint.print(Constants.SPACE);
                updateDBPrint.print(ScriptConstants.LINUX_DEV_NULL);
                updateDBPrint.print(Constants.GREATER_THAN);
                updateDBPrint.print(Constants.SPACE);
                updateDBPrint.print(Constants.SCRIPT_NAME_HARVEST_DB_UPDATE);
                updateDBPrint.print(Constants.EXTENSION_LOG_FILES);
                updateDBPrint.print(Constants.SPACE);
                updateDBPrint.println(ScriptConstants.LINUX_ERROR_MESSAGE_TO_1);
            } finally {
                // close file
                updateDBPrint.close();
            }
        } catch (IOException e) {
            // Log the error and throw an IOFailure.
            log.trace(Constants.MSG_ERROR_DB_KILL_FILE, e);
            throw new IOFailure(Constants.MSG_ERROR_DB_KILL_FILE, e);
        }
    }
    private String getDefaultMachineClasspath() { 
        StringBuilder res = new StringBuilder();
        // get all the classpaths 
        for(Element cp : machineParameters.getClassPaths()) {
            res.append(getInstallDirPath() + Constants.SLASH + cp.getText() 
                + Constants.COLON);
        }
        return res.toString();
    }

    @Override
    protected String getLibDirPath() {
        return getInstallDirPath() + Constants.LIB_DIR_LINUX;
    }
    
}
