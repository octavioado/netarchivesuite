/*
 * #%L
 * Netarchivesuite - archive - test
 * %%
 * Copyright (C) 2005 - 2017 The Royal Danish Library, 
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
package dk.netarkivet.archive.arcrepositoryadmin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import dk.netarkivet.common.exceptions.ArgumentNotValid;

public class FileListStatusTester {

    @Test(expected = ArgumentNotValid.class)
    public void testFromOrdinal() {
        assertEquals(FileListStatus.NO_FILELIST_STATUS, FileListStatus.fromOrdinal(0));
        assertEquals(FileListStatus.MISSING, FileListStatus.fromOrdinal(1));
        assertEquals(FileListStatus.OK, FileListStatus.fromOrdinal(2));

        FileListStatus.fromOrdinal(3);
        fail("Should throw ArgumentNotValid with argument > 2");
    }
}
