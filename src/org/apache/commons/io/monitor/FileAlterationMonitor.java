/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.monitor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

/**
 * A runnable that spawns a monitoring thread triggering any
 * registered {@link FileAlterationObserver} at a specified interval.
 *
 * @version $Id: FileAlterationMonitor.java 1304052 2012-03-22 20:55:29Z ggregory $
 * @see FileAlterationObserver
 * @since 2.0
 */
public final class FileAlterationMonitor implements Runnable {

    private final long interval;
    private final List<FileAlterationObserver> observers = new CopyOnWriteArrayList<FileAlterationObserver>();
    private ThreadFactory threadFactory;
    private volatile boolean running = false;

    /**
     * Construct a monitor with the specified interval.
     *
     * @param interval      The amount of time in miliseconds to wait between
     *                      checks of the file system
     * @param threadFactory Some factory
     */
    public FileAlterationMonitor(long interval, ThreadFactory threadFactory) {
        this.interval = interval;
        this.threadFactory = threadFactory;
    }

    /**
     * Start monitoring.
     *
     * @throws Exception if an error occurs initializing the observer
     */
    public synchronized void start() throws Exception {
        if (running) {
            throw new IllegalStateException("Monitor is already running");
        }
        for (FileAlterationObserver observer : observers) {
            observer.initialize();
        }
        running = true;
        Thread thread;
        if (threadFactory != null) {
            thread = threadFactory.newThread(this);
        } else {
            thread = new Thread(this);
        }
        thread.start();
    }

    /**
     * Run.
     */
    public void run() {
        while (running) {
            for (FileAlterationObserver observer : observers) {
                observer.checkAndNotify();
            }
            if (!running) {
                break;
            }
            try {
                Thread.sleep(interval);
            } catch (final InterruptedException ignored) {
            }
        }
    }
}