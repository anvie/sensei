package com.ansvia.sensei.mt

import java.util.Comparator
import com.mongodb.casbah.Imports._

import org.json.JSONObject

import proj.zoie.api.DataConsumer.DataEvent
import proj.zoie.impl.indexing.StreamDataProvider
import java.util
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

//import com.mongodb.casbah.commons.TypeImports.ObjectId

class MtPostStreamer(config:util.Map[String,String],versionComparator:Comparator[String])
  extends StreamDataProvider[JSONObject](versionComparator) {

  import com.mongodb.casbah.commons.conversions.scala._
  RegisterJodaTimeConversionHelpers()

  private val dateFormater = DateTimeFormat.forPattern("yyyy/MM/dd")

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
  private lazy val col = db("user_post")
  private lazy val cursor = {
    val count = col.count(MongoDBObject("_deleted" -> false, "_closed" -> false))
    println("streaming from " + count + " datas...")
    col.find(MongoDBObject("_deleted" -> false, "_closed" -> false)).sort(MongoDBObject("_id" -> -1))
  }
  private var incrementalId = 0

  private def getUser(id:String) = db("user").findOne(MongoDBObject("_id" -> new ObjectId(id)))

  private val hashtagPatt = """#\w\w+""".r
  private val trimerPatt = """^\W+|\W+$""".r

  class ImplicitStringNormalizer(t:String) {
    def trimAll = {
      trimerPatt.replaceAllIn(t, "")
    }
    def normalize = {
      t.toLowerCase.trimAll
    }
  }
  implicit def stringTrimer(t:String):ImplicitStringNormalizer = 
	new ImplicitStringNormalizer(t)

  // trimerPatt.replaceAllIn(t, "")
  private def extractHashTags(msg:String):String = {
    hashtagPatt.findAllIn(msg).map(_.normalize).foldLeft("")(_ + "," + _).normalize
  }

  private def getResponseCount(id:String):Int = {
	val respCol = db("response")
	respCol.find(MongoDBObject("_object_id" -> id)).size
  }

  def next():DataEvent[JSONObject] = {
    var evData:DataEvent[JSONObject] = null
    try {

      if (!cursor.hasNext)
        return null

      val p = cursor.next()

      val json = new JSONObject()

      json.put("id", incrementalId)
      incrementalId += 1

      val oid = p.as[ObjectId]("_id").toString

      getUser(p.as[String]("_writer_id")) map { u =>
        val creator_name = u.as[String]("name").normalize
        val creator_sex:Int = u.as[Any]("sex") match {
          case s:Boolean => s match {
            case true => 1
            case _ => 0
          }
          case s:Int => s
          case s:java.lang.Integer => s.intValue()
          case _ => 0
        }
        val creatorSdescsl = u.as[Seq[String]]("personal_descs").map(_.normalize).foldLeft("")(_ + "," + _).normalize
        val creatorBd = dateFormater.print(u.as[DateTime]("birth_date"))
        val creatorLoc = u.getAsOrElse[String]("location","").normalize
        val creationDate = u.as[DateTime]("_creation_time")

        val likesCount = p.getAsOrElse[Int]("_liked_user_count", 0)
        val respCount = db("user_like").count(MongoDBObject("_object_id" -> oid))
        val originKind = p.getAsOrElse[String]("origin_class", "User")
        val originId = p.getAsOrElse[String]("_origin_id", "")
		val containsData:String = {
			if (p.getAsOrElse[Boolean]("contains_pic", false))
				"pic"
			else if (p.getAsOrElse[Boolean]("contains_link", false))
				"link"
			else if (p.getAsOrElse[Boolean]("contains_video_link", false))
				"video_link"
			else ""
		}

        val hashtags = extractHashTags(p.getAsOrElse[String]("message", ""))
		val responseCount = getResponseCount(oid)


        println("processing post id " + oid + " ...")
        println("hashtags: " + hashtags)

        json.put("oid", oid)
        json.put("creator_name", creator_name)
        json.put("creator_sex", creator_sex)
        json.put("creator_sdescs", creatorSdescsl)
        json.put("creator_birthdate", creatorBd)
        json.put("creator_location", creatorLoc)
        json.put("likes_count", likesCount)
        json.put("resp_count", respCount)
        json.put("origin_kind", originKind)
        json.put("origin_id", originId)
        json.put("hashtags", hashtags)
        json.put("creation_date", creationDate.getMillis)
		json.put("contains_data", containsData)
		json.put("response_count", responseCount)

        evData = new DataEvent[JSONObject](json, System.currentTimeMillis().toString)

      }

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
