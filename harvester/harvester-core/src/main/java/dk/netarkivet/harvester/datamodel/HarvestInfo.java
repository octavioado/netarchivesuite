/*
 * #%L
 * Netarchivesuite - harvester
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
package dk.netarkivet.harvester.datamodel;

import java.util.Date;

import dk.netarkivet.common.exceptions.ArgumentNotValid;

/**
 * Summary information about a specific harvest of a domain. This class is immutable.
 */
public class HarvestInfo {

    /** The date the harvest information was created. */
    private final Date date;

    /** The identifier of the harvest. */
    private final Long harvestID;

    /** The total number of objects retrieved. */
    private final long countObjectRetrieved;

    /** The total size in bytes of the downloaded data. */
    private final long sizeDataRetrieved;

    /** The reason the harvest stopped. */
    private final StopReason stopReason;

    /** The name of the domain harvested. */
    private final String domainName;

    /** The configuration that the domain was harvested with. */
    private final String domainCfgName;

    /**
     * The job that was used to create this info, or null if it cannot be determined (for old harvestinfo only).
     */
    private final Long jobID;

    /** ID autogenerated by DB, ignored otherwise. */
    private Long id;

    /**
     * Create new harvest info instance.
     *
     * @param harvestID The id of the harvest
     * @param domainName The name of the Domain
     * @param domainCfgName The name of the Domain configuration
     * @param date The date of the harvest
     * @param sizeDataRetrieved The number of bytes retrieved for this Domain
     * @param countObjectRetrieved The number of objects retrieved for this Domain
     * @param stopReason The reason why the current harvest terminated
     */
    public HarvestInfo(Long harvestID, String domainName, String domainCfgName, Date date, long sizeDataRetrieved,
            long countObjectRetrieved, StopReason stopReason) {
        this(harvestID, null, domainName, domainCfgName, date, sizeDataRetrieved, countObjectRetrieved, stopReason);
    }

    /**
     * Create new harvest info instance.
     *
     * @param harvestID The id of the harvest
     * @param jobID The id of the job, if available
     * @param domainName The name of the Domain
     * @param domainCfgName The name of the Domain configuration
     * @param date The date of the harvest
     * @param sizeDataRetrieved The number of bytes retrieved for this Domain
     * @param countObjectRetrieved The number of objects retrieved for this Domain
     * @param stopReason The reason why the current harvest terminated
     */
    public HarvestInfo(Long harvestID, Long jobID, String domainName, String domainCfgName, Date date,
            long sizeDataRetrieved, long countObjectRetrieved, StopReason stopReason) {
        ArgumentNotValid.checkNotNull(harvestID, "harvestID");
        ArgumentNotValid.checkNotNull(date, "date");
        ArgumentNotValid.checkNotNullOrEmpty(domainName, "domainName");
        ArgumentNotValid.checkNotNullOrEmpty(domainCfgName, "domainCfgName");
        ArgumentNotValid.checkNotNegative(sizeDataRetrieved, "sizeDataRetrieved");
        ArgumentNotValid.checkNotNegative(countObjectRetrieved, "countObjectRetrieved");
        ArgumentNotValid.checkNotNull(stopReason, "stopReason");

        this.harvestID = harvestID;
        this.jobID = jobID;
        this.domainCfgName = domainCfgName;
        this.domainName = domainName;
        this.date = date;
        this.sizeDataRetrieved = sizeDataRetrieved;
        this.countObjectRetrieved = countObjectRetrieved;
        this.stopReason = stopReason;
    }

    /**
     * Get the total amount of data downloaded (bytes).
     *
     * @return the total amount of data downloaded (bytes)
     */
    public long getSizeDataRetrieved() {
        return sizeDataRetrieved;
    }

    /**
     * Get the total number of objects downloaded.
     *
     * @return the total number of objects downloaded
     */
    public long getCountObjectRetrieved() {
        return countObjectRetrieved;
    }

    /**
     * Get the reason the harvest stopped.
     *
     * @return the reason the harvest stopped
     */
    public StopReason getStopReason() {
        return stopReason;
    }

    /**
     * Get the date this harvest information was recorded.
     *
     * @return the date the harvest information was recorded
     */
    public Date getDate() {
        return date;
    }

    /**
     * Get the id of the harvest.
     *
     * @return the harvest ID
     */
    public Long getHarvestID() {
        return harvestID;
    }

    /**
     * Get the id of the job, if available.
     *
     * @return the job ID or null.
     */
    public Long getJobID() {
        return jobID;
    }

    /**
     * Get the domain name of the harvest.
     *
     * @return the domain name
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Get the domain configuration name of the harvest.
     *
     * @return the domain configuration name
     */
    public String getDomainConfigurationName() {
        return domainCfgName;
    }

    /**
     * Get the ID of this harvestinfo. Only for use by DBDAO.
     *
     * @return the ID of this harvestinfo
     */
    long getID() {
        return id;
    }

    /**
     * Set the ID of this harvestinfo. Only for use by DBDAO
     *
     * @param newid the new ID of this harvestinfo
     */
    void setID(long newid) {
        this.id = newid;
    }

    /**
     * Check if this harvestinfo has an ID set yet (doesn't happen until the DBDAO persists it).
     *
     * @return true, if this harvestinfo has an ID set
     */
    boolean hasID() {
        return id != null;
    }

    /**
     * Autogenerated from IDEA.
     *
     * @return true if object fields are equal
     * @see Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HarvestInfo)) {
            return false;
        }

        final HarvestInfo harvestInfo = (HarvestInfo) o;

        if (countObjectRetrieved != harvestInfo.countObjectRetrieved) {
            return false;
        }
        if (sizeDataRetrieved != harvestInfo.sizeDataRetrieved) {
            return false;
        }
        if (!date.equals(harvestInfo.date)) {
            return false;
        }
        if (!domainCfgName.equals(harvestInfo.domainCfgName)) {
            return false;
        }
        if (!domainName.equals(harvestInfo.domainName)) {
            return false;
        }
        if (!harvestID.equals(harvestInfo.harvestID)) {
            return false;
        }
        if (!stopReason.equals(harvestInfo.stopReason)) {
            return false;
        }

        return true;
    }

    /**
     * Autogenerated from IDEA.
     *
     * @return hashcode
     * @see Object#hashCode()
     */
    public int hashCode() {
        int result;
        result = date.hashCode();
        result = 29 * result + harvestID.hashCode();
        result = 29 * result + (int) (countObjectRetrieved ^ (countObjectRetrieved >>> 32));
        result = 29 * result + (int) (sizeDataRetrieved ^ (sizeDataRetrieved >>> 32));
        result = 29 * result + stopReason.hashCode();
        result = 29 * result + domainName.hashCode();
        result = 29 * result + domainCfgName.hashCode();
        return result;
    }

    /**
     * A human readable representation.
     *
     * @return A human readable representation
     */
    public String toString() {
        return "Harvest info for harvest #" + harvestID + " of " + domainName + "(" + domainCfgName + ")" + " on "
                + date + "\n" + "Status: " + stopReason + "\n" + countObjectRetrieved + "objects / "
                + sizeDataRetrieved + "bytes\n";
    }

}
