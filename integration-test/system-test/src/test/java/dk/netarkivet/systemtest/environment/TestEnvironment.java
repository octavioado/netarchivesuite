/*
 * #%L
 * NetarchiveSuite System test
 * %%
 * Copyright (C) 2005 - 2014 The Royal Danish Library, the Danish State and University Library,
 *             the National Library of France and the Austrian National Library.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package dk.netarkivet.systemtest.environment;

public interface TestEnvironment {
    /**
     * Possibly in a better world these would be values retrieved
     * by method calls rather than constants.
     */
    public static String DEPLOYMENT_SERVER = "kb-prod-udv-001.kb.dk";
    public static String JOB_ADMIN_SERVER = "kb-test-adm-001.kb.dk";
    public static String ARCHIVE_ADMIN_SERVER = "kb-test-adm-001.kb.dk";
    public static String CHECKSUM_SERVER = "kb-test-acs-001.kb.dk";

    /**
     * The NAS environment (settings.common.environmentName) in which
     * the test is to be run.
     * @return
     */
    public String getTESTX();

    /**
     * The port on the host where the GUI is to be deployed.
     * @return
     */
    public String getGuiHost();

    /**
     * The port where the GUI is to be deployed.
     * @return
     */
    public int getGuiPort();

    /**
     * The timestamp of the NAS software version to be run in the test. This does not
     * have to be an actual timestamp. The software in unpacked from the file
     * Netarchivesuite-<timestamp>.zip in the directory release_software_dist/releases
     * on the deployment server.
     * @return
     */
    public String getTimestamp();

    /**
     * A comma-separated list of addresses to receive mail from failed tests.
     * @return
     */
    public String getMailreceivers();

}
