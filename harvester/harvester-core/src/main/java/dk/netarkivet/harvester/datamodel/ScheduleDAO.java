/*
 * #%L
 * Netarchivesuite - harvester
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
package dk.netarkivet.harvester.datamodel;

import java.util.Iterator;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.exceptions.PermissionDenied;
import dk.netarkivet.common.exceptions.UnknownID;

/**
 * A DAO for reading and writing schedules by name.
 */
public abstract class ScheduleDAO implements DAO, Iterable<Schedule> {

    /** The singleton instance. */
    private static ScheduleDAO instance;

    /**
     * Constructor made private to enforce singleton.
     */
    protected ScheduleDAO() {
    }

    /**
     * Gets the singleton instance of the ScheduleDAO.
     *
     * @return ScheduleDAO singleton
     */
    public static synchronized ScheduleDAO getInstance() {
        if (instance == null) {
            instance = new ScheduleDBDAO();
        }
        return instance;
    }

    /**
     * Create a new schedule.
     *
     * @param schedule The schedule to create
     * @throws ArgumentNotValid if schedule is null
     * @throws PermissionDenied if a schedule already exists
     */
    public abstract void create(Schedule schedule);

    /**
     * Returns whether a named schedule exists.
     *
     * @param scheduleName The name of a schedule
     * @return True if the schedule exists.
     * @throws ArgumentNotValid if the schedulename is null or empty
     */
    public abstract boolean exists(String scheduleName);

    /**
     * Read an existing schedule.
     *
     * @param scheduleName the name of the schedule
     * @return The schedule read
     * @throws ArgumentNotValid if schedulename is null or empty
     * @throws UnknownID if the schedule doesn't exist
     */
    public abstract Schedule read(String scheduleName);

    /**
     * Update a schedule in the DAO.
     *
     * @param schedule The schedule to update
     * @throws ArgumentNotValid If the schedule is null
     * @throws UnknownID If the schedule doesn't exist in the DAO
     * @throws IOFailure If the edition of the schedule to update is older than the DAO's
     */
    public abstract void update(Schedule schedule);

    /**
     * Get iterator to all available schedules.
     *
     * @return iterator to all available schedules
     */
    public abstract Iterator<Schedule> getAllSchedules();

    /**
     * Get an iterator over the schedules handled by this DAO. Implements the Iterable interface.
     *
     * @return Iterator of all current schedules.
     */
    public Iterator<Schedule> iterator() {
        return getAllSchedules();
    }

    /**
     * Get the number of defined schedules.
     *
     * @return The number of defined schedules
     */
    public abstract int getCountSchedules();

    /**
     * Reset the DAO. Only for use from within tests.
     */
    static void reset() {
        instance = null;
    }

}