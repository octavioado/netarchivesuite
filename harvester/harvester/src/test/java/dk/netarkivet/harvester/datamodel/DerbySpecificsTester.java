/* File:    $Id$
 * Version: $Revision$
 * Date:    $Date$
 * Author:  $Author$
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2010 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dk.netarkivet.harvester.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dk.netarkivet.common.utils.DBUtils;

/**
 *
 * Unit test testing the DerbySpecifics class.
 *
 */
public class DerbySpecificsTester extends DataModelTestCase {
    public DerbySpecificsTester(String s) {
        super(s);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }
    public void testGetTemporaryTable() {
        Connection c = HarvestDBConnection.get();

        try {
            String statement = "SELECT config_name, domain_name "
                + "FROM session.jobconfignames";
            PreparedStatement s = null;
            try {
                s = c.prepareStatement(statement);
                s.execute();
                fail("Should have failed query before table is made");
            } catch (SQLException e) {
                // expected
            } finally {
                DBUtils.closeStatementIfOpen(s);
            }

            try {
                c.setAutoCommit(false);
                String tmpTable =
                    DBSpecifics.getInstance().getJobConfigsTmpTable(c);
                assertEquals("Should have given expected name for Derby temp table",
                        "session.jobconfignames", tmpTable);
                s = c.prepareStatement(statement);
                s.execute();
                s.close();
                s = c.prepareStatement("INSERT INTO " + tmpTable
                        + " VALUES ( ?, ? )");
                s.setString(1, "foo");
                s.setString(2, "bar");
                s.executeUpdate();
                s.close();
                String domain =
                    DBUtils.selectStringValue(
                            c,
                            "SELECT domain_name FROM " + tmpTable
                            + " WHERE config_name = ?", "bar");
                assertEquals("Should get expected domain name", "foo", domain);
                c.commit();
                c.setAutoCommit(true);
                DBSpecifics.getInstance().dropJobConfigsTmpTable(c, tmpTable);
            } catch (SQLException e) {
                fail("Should not have had SQL exception " + e);
            } finally {
                DBUtils.closeStatementIfOpen(s);
            }

            try {
                s = c.prepareStatement(statement);
                s.execute();
                String domain =
                    DBUtils.selectStringValue(c,
                            "SELECT domain_name "
                            + "FROM session.jobconfignames "
                            + "WHERE config_name = 'foo'");
                fail("Should have failed query after table is dead, "
                        + "but return domain= " + domain);
            } catch (SQLException e) {
                // expected
            } finally {
                DBUtils.closeStatementIfOpen(s);
            }

            // Should be possible to get another temporary table.
            try {
                c.setAutoCommit(false);
                String tmpTable =
                    DBSpecifics.getInstance().getJobConfigsTmpTable(c);
                assertEquals("Should have given expected name for Derby temp table",
                        "session.jobconfignames", tmpTable);
                s = c.prepareStatement(statement);
                s.execute();
                s.close();
                s = c.prepareStatement("INSERT INTO " + tmpTable
                        + " VALUES ( ?, ? )");
                s.setString(1, "foo");
                s.setString(2, "bar");
                s.executeUpdate();
                s.close();
                String domain =
                    DBUtils.selectStringValue(c,
                            "SELECT domain_name FROM "
                            + tmpTable
                            + " WHERE config_name = ?", "bar");
                assertEquals("Should get expected domain name", "foo", domain);
                c.commit();
                c.setAutoCommit(true);
                DBSpecifics.getInstance().dropJobConfigsTmpTable(c, tmpTable);
            } catch (SQLException e) {
                fail("Should not have had SQL exception " + e);
            } finally {
                DBUtils.closeStatementIfOpen(s);
            }
        } finally {
            HarvestDBConnection.release(c);
        }
    }
}