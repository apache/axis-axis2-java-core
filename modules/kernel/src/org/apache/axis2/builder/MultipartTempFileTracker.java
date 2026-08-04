/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.builder;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Deletes the temporary files that the multipart/form-data builder spills to
 * disk once nothing can read them any more.
 *
 * <p>A multipart part larger than the factory's threshold is written to a temp
 * file, and a file part is then handed to the service as a {@code DataHandler}
 * that reads it during invocation. That makes the file's useful life longer
 * than the build step, so the builder cannot simply delete it on the way out —
 * which is why these files previously accumulated for the lifetime of the JVM.
 *
 * <p>Cleanup is therefore tied to reachability: commons-io tracks each temp file
 * against its {@code DiskFileItem} through a phantom reference and deletes the
 * file once that item is collected. Form-field parts do not need to wait, and
 * the builder deletes those itself as soon as it has copied the text out.
 *
 * <p>The reaper is a daemon thread created on the first multipart request, so a
 * deployment that never receives one never starts it. {@link #shutdown()} stops
 * it, and is called when the Axis2 configuration is cleaned up so the thread
 * does not outlive a redeployment.
 */
public final class MultipartTempFileTracker {

    private static final Log log = LogFactory.getLog(MultipartTempFileTracker.class);

    private static FileCleaningTracker tracker;

    private MultipartTempFileTracker() {
    }

    /**
     * The tracker to register temp files with, started on first use.
     *
     * <p>A tracker that has been shut down cannot be restarted, so this creates
     * a fresh one rather than handing back a dead reaper if a configuration is
     * cleaned up and another is built in the same JVM.
     */
    public static synchronized FileCleaningTracker getTracker() {
        if (tracker == null) {
            tracker = new FileCleaningTracker();
            if (log.isDebugEnabled()) {
                log.debug("Started the multipart temporary file reaper");
            }
        }
        return tracker;
    }

    /**
     * Stop the reaper once the files it is still tracking have been deleted.
     */
    public static synchronized void shutdown() {
        if (tracker != null) {
            if (log.isDebugEnabled()) {
                log.debug("Stopping the multipart temporary file reaper with "
                        + tracker.getTrackCount() + " file(s) still tracked");
            }
            tracker.exitWhenFinished();
            tracker = null;
        }
    }

    /**
     * How many temp files are still awaiting deletion. Intended for tests and
     * diagnostics.
     */
    public static synchronized int getTrackedFileCount() {
        return tracker == null ? 0 : tracker.getTrackCount();
    }
}
