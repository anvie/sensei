package com.ansvia.sensei.mt.implicits

/**
 * Copyright (C) 2011-2012 Ansvia Inc.
 * User: robin
 * Date: 11/21/12
 * Time: 7:05 PM
 * 
 */
object StringNormalizer {
  private val trimerPatt = """^\W+|\W+$""".r

  implicit def strToStrNormalizer(t:String):StringNormalizer = new StringNormalizer(t)
}

class StringNormalizer(t:String) {

  import StringNormalizer.strToStrNormalizer

  def trimAll = {
    StringNormalizer.trimerPatt.replaceAllIn(t, "")
  }

  def normalize = {
    t.toLowerCase.trimAll
  }
}

