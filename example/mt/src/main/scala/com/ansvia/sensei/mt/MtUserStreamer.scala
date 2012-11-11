package com.ansvia.sensei.mt

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Comparator
import java.util.List
import java.util.Map
import java.util.regex.Matcher
import java.util.regex.Pattern
import com.mongodb.casbah.Imports._

import org.apache.log4j.Logger
import org.json.JSONObject

import proj.zoie.api.DataConsumer.DataEvent
import proj.zoie.impl.indexing.StreamDataProvider
import java.util
import com.mongodb.casbah.MongoURI
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class MtUserStreamer(config:util.Map[String,String],versionComparator:Comparator[String])
  extends StreamDataProvider[JSONObject](versionComparator) {

  import com.mongodb.casbah.commons.conversions.scala._
  RegisterJodaTimeConversionHelpers()

  private lazy val con = {
	val host = config.get("mongodb")
	println("connecting to mongodb " + config.get("mongodb") + "...")
	val hp = host.split(":")
	MongoConnection(hp(0), hp(1).toInt)
  }
  private lazy val db = {
	println("dbname: " + config.get("dbname"))
	con(config.get("dbname"))
  }
  private lazy val col = db("user")
  private lazy val cursor = {
	val count = col.count(MongoDBObject("_deleted" -> false, "_closed" -> false))
	println("streaming from " + count + " datas...")
	col.find(MongoDBObject("_deleted" -> false, "_closed" -> false)).sort(MongoDBObject("_id" -> -1))
  }
  private var incrementalId = 0

  def next():DataEvent[JSONObject] = {
    var evData:DataEvent[JSONObject] = null
    try {

      if (!cursor.hasNext)
        return null

      val u = cursor.next()

      val json = new JSONObject()

      json.put("id", incrementalId)
      incrementalId += 1

      val oid = u.as[ObjectId]("_id").toString
      val name = u.as[String]("name")
      val sex = {
		u.as[Any]("sex") match {
			case s:Boolean => s match {
				case true => 1
				case _ => 0
			}
			case s:Integer => s
			case _ => 0
		}
      }
      val sdescsl = u.as[Seq[String]]("personal_descs")
      val birthDate = u.as[DateTime]("birth_date")
      val location = u.getAsOrElse[String]("location","").toLowerCase
      val joinDate = u.as[DateTime]("_creation_time")

      val fmt = DateTimeFormat.forPattern("yyyy/mm/dd")
      val birthDateF = fmt.print(birthDate)
      val joinDateF = fmt.print(joinDate)

      println("processing: " + name + " ...")

      json.put("oid", oid)
      json.put("name", name)
      json.put("sex", sex)
      json.put("sdescs", sdescsl.map(_.toLowerCase).foldLeft("")(_ + "," + _))
      json.put("birthdate", birthDateF)
      json.put("join_date", joinDateF)
      json.put("location", location)

      evData = new DataEvent[JSONObject](json, System.currentTimeMillis().toString)

    }catch {
      case e:Exception =>
        e.printStackTrace()
        return null
    }
    evData
  }

  def setStartingOffset(p1: String) {}

  def reset() {}
}
