package com.ansvia.sensei.mt

import org.apache.log4j.Logger

/**
 * Copyright (C) 2011-2012 Ansvia Inc.
 * User: robin
 * Date: 11/21/12
 * Time: 6:10 PM
 * 
 */
trait Logging {
  protected lazy val log = Logger.getLogger(getClass);
}
