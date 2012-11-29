package com.senseidb.search.node;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;

import java.util.Collection;

/**
 * Copyright (C) 2011-2012 Ansvia Inc.
 * User: robin
 * Date: 11/29/12
 * Time: 9:05 PM
 */
class SenseiIndexReaderFinishedListener implements IndexReader.ReaderFinishedListener {
    private static final Logger log = Logger.getLogger(SenseiIndexReaderFinishedListener.class);
    private final Collection<SenseiIndexReaderDecorator.BoboListener> boboListeners;

    SenseiIndexReaderFinishedListener(Collection<SenseiIndexReaderDecorator.BoboListener> boboListeners) {
        this.boboListeners = boboListeners;
    }

    public void finished(IndexReader indexReader) {
        for (SenseiIndexReaderDecorator.BoboListener boboListener : boboListeners) {
            boboListener.indexDeleted(indexReader);
        }
    }
}
