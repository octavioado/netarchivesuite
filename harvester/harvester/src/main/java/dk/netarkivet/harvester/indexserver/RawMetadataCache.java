/* File:        $Id$
 * Revision:    $Revision$
 * Author:      $Author$
 * Date:        $Date$
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
 *  USA
 */
package dk.netarkivet.harvester.indexserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.Constants;
import dk.netarkivet.common.distribute.arcrepository.ArcRepositoryClientFactory;
import dk.netarkivet.common.distribute.arcrepository.BatchStatus;
import dk.netarkivet.common.distribute.arcrepository.Replica;
import dk.netarkivet.common.distribute.arcrepository.ReplicaType;
import dk.netarkivet.common.distribute.arcrepository.ViewerArcRepositoryClient;
import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.common.utils.archive.ArchiveBatchJob;
import dk.netarkivet.common.utils.archive.ArchiveHeaderBase;
import dk.netarkivet.common.utils.archive.ArchiveRecordBase;
import dk.netarkivet.harvester.HarvesterSettings;

/**
 * This is an implementation of the RawDataCache specialized for data out
 * of metadata files.  It uses regular expressions for matching URL and
 * mime-type of ARC entries for the kind of metadata we want.
 *
 */
public class RawMetadataCache extends FileBasedCache<Long>
        implements RawDataCache {
    /** A regular expression object that matches everything. */
    private static final Pattern MATCH_ALL_PATTERN = Pattern.compile(".*");
    /** The prefix (cache name) that this cache uses. */
    private final String prefix;
    /** The arc repository interface.
     * This does not need to be closed, it is a singleton.
     */
    private ViewerArcRepositoryClient arcrep
        = ArcRepositoryClientFactory.getViewerInstance();

    /** The job that we use to dig through metadata files. */
    private final ArchiveBatchJob job;

    /** The logger for this class. */
    private final Log log = LogFactory.getLog(getClass());

    /** Create a new RawMetadataCache.  For a given job ID, this will fetch
     * and cache selected content from metadata files
     * (&lt;ID&gt;-metadata-[0-9]+.arc).  Any entry in a metadata file that
     * matches both patterns will be returned.  The returned data does not
     * directly indicate which file they were from, though parts intrinsic to
     * the particular format might.
     *
     * @param prefix A prefix that will be used to distinguish this cache's
     * files from other caches'.  It will be used for creating a directory,
     * so it must not contain characters not legal in directory names.
     * @param urlMatcher A pattern for matching URLs of the desired entries.
     * If null, a .* pattern will be used.
     * @param mimeMatcher A pattern for matching mime-types of the desired
     * entries.  If null, a .* pattern will be used.
     */
    public RawMetadataCache(String prefix, Pattern urlMatcher,
                            Pattern mimeMatcher) {
        super(prefix);
        this.prefix = prefix;
        Pattern urlMatcher1;
        if (urlMatcher != null) {
            urlMatcher1 = urlMatcher;
        } else {
            urlMatcher1 = MATCH_ALL_PATTERN;
        }
        Pattern mimeMatcher1;
        if (mimeMatcher != null) {
            mimeMatcher1 = mimeMatcher;
        } else {
            mimeMatcher1 = MATCH_ALL_PATTERN;
        }
        log.info("Metadata cache for '" + prefix + "' is fetching"
                 + " metadata with urls matching '" + urlMatcher1.toString()
                 + "' and mimetype matching '" + mimeMatcher1 + "'");
        job = new GetMetadataArchiveBatchJob(urlMatcher1, mimeMatcher1);
    }

    /** Get the file potentially containing (cached) data for a single job.
     *
     * @see FileBasedCache#getCacheFile(Object)
     * @param id The job to find data for.
     * @return The file where cache data for the job can be stored.
     */
    @Override
    public File getCacheFile(Long id) {
        ArgumentNotValid.checkNotNull(id, "job ID");
        ArgumentNotValid.checkNotNegative(id, "job ID");
        return new File(getCacheDir(), prefix + "-" + id + "-cache");
    }

    /** Actually cache data for the given ID.
     *
     * @see FileBasedCache#cacheData(Object)
     * @param id A job ID to cache data for.
     * @return A File containing the data.  This file will be the same as
     * getCacheFile(ID);
     */
    protected Long cacheData(Long id) {
        final String replicaUsed = Settings.get(CommonSettings.USE_REPLICA_ID);
        log.debug("Extract using a batchjob of type '"
                + job.getClass().getName()
                + "' cachedata from files matching '"
                + id + Constants.METADATA_FILE_PATTERN_SUFFIX
                + "' on replica '" 
                + replicaUsed + "'");
        job.processOnlyFilesMatching(id
                + Constants.METADATA_FILE_PATTERN_SUFFIX);
        BatchStatus b = arcrep.batch(job, replicaUsed);
        
        // This check ensures that we got data from at least one file.
        // Mind you, the data may be empty, but at least one file was
        // successfully processed.
        if (b.hasResultFile()
                && b.getNoOfFilesProcessed() > b.getFilesFailed().size()) {
            File cacheFileName = getCacheFile(id);
            b.copyResults(cacheFileName);
            log.debug("Cached data for job '" + id
                    + "' for '" + prefix + "'");
            return id;
        } else {
            // Look for data in other bitarchive replicas, if this option is enabled
            if (!Settings.getBoolean(
                    HarvesterSettings.INDEXSERVER_INDEXING_LOOKFORDATAINOTHERBITARCHIVEREPLICAS)) {
                log.info("No data found for job '" + id + "' for '" + prefix 
                        + "' in local bitarchive '" + replicaUsed + "'. ");
                return null;
            } else {
                log.info("No data found for job '" + id + "' for '" + prefix 
                    + "' in local bitarchive '" + replicaUsed + "'. "
                    + "Trying other replicas.");
                for(Replica rep : Replica.getKnown()) {
                    // Only use different bitarchive replicas than replicaUsed
                    if(rep.getType().equals(ReplicaType.BITARCHIVE) 
                        && !rep.getId().equals(replicaUsed)) {
                        log.debug("Trying to retrieve index data for job '"
                            + id + "' from '" + rep.getId() + "'.");
                        b = arcrep.batch(job, rep.getId());
                    
                        // Perform same check as for the batchresults from
                        // the default replica.
                        if (b.hasResultFile() && (b.getNoOfFilesProcessed() 
                            > b.getFilesFailed().size())) {
                            File cacheFileName = getCacheFile(id);
                            b.copyResults(cacheFileName);
                            log.info("Cached data for job '" + id
                                + "' for '" + prefix + "' from '" + rep 
                                + " instead of " + replicaUsed);
                            return id;
                        } else {
                            log.trace("No data found for job '" + id + "' for '" 
                                + prefix + "' in bitarchive '" + rep + "'. ");
                        }
                    }
                }
                log.info("No data found for job '" + id + "' for '" + prefix 
                        + "' in all bitarchive replicas");
                return null;
            }
        }
    }

    /** A batch job that extracts metadata. */
    private static class GetMetadataArchiveBatchJob extends ArchiveBatchJob {
        /** The logger for this class. */
        private final Log log = LogFactory.getLog(getClass());
        /** The pattern for matching the urls.*/
        private final Pattern urlMatcher;
        /** The pattern for the mimetype matcher.*/
        private final Pattern mimeMatcher;
        /**
         * Constructor.
         * 
         * @param urlMatcher A pattern for matching URLs of the desired entries.
         * If null, a .* pattern will be used.
         * @param mimeMatcher A pattern for matching mime-types of the desired
         * entries.  If null, a .* pattern will be used.
         */
        public GetMetadataArchiveBatchJob(Pattern urlMatcher, Pattern mimeMatcher) {
            this.urlMatcher = urlMatcher;
            this.mimeMatcher = mimeMatcher;
            /**
            * one week in milliseconds.
            */
            batchJobTimeout = Constants.ONE_DAY_IN_MILLIES;
        }

        /**
         * Initialize method. Run before the arc-records are being processed.
         * Currently does nothing.
         * 
         * @param os The output stream to print any pre-processing data.
         */
        @Override
        public void initialize(OutputStream os) { }

        /**
         * The method for processing the arc-records.
         * 
         * @param record The arc-record to process.
         * @param os The output stream to write the results of the processing.
         * @throws IOFailure In an IOException is caught during handling of 
         * the arc record.
         */
        @Override
        public void processRecord(ArchiveRecordBase record, OutputStream os) 
                throws IOFailure {
            ArchiveHeaderBase header = record.getHeader();
            InputStream in = record.getInputStream();

            log.info(header.getUrl() + " - " + header.getMimetype());

            if (urlMatcher.matcher(header.getUrl()).matches()
                    && mimeMatcher.matcher(
                            header.getMimetype()).matches()) {
                try {
                        byte[] buf = new byte[Constants.IO_BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = in.read(buf)) != -1) {
                            os.write(buf, 0, bytesRead);
                        }
                } catch (IOException e) {
                	// TODO is getOffset() correct using the IA archiveReader?
                    String message = "Error writing body of Archive entry '"
                            + header.getArchiveFile() + "' offset '"
                            + header.getOffset() + "'";
                    throw new IOFailure(message, e);
                }
            }

            try {
                in.close();
            } catch (IOException e) {
                String message = "Error closing Archive input stream";
                throw new IOFailure(message, e);
            }
        }

        /**
         * Method for post-processing the data.
         * Currently does nothing.
         * 
         * @param os The output stream to write the results of the 
         * post-processing data.
         */
        @Override
        public void finish(OutputStream os) { }
        
        /**
         * Humanly readable description of this instance.
         * 
         * @return The human readable description of this instance.
         */
        @Override
        public String toString() {
            return getClass().getName() + ", with arguments: URLMatcher = " 
            + urlMatcher + ", mimeMatcher = " + mimeMatcher;
        }
    }
}