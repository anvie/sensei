package com.ansvia.sensei.mt

import java.util.Comparator
import com.mongodb.casbah.Imports._

import org.json.JSONObject

import proj.zoie.api.DataConsumer.DataEvent
import proj.zoie.impl.indexing.StreamDataProvider
import java.util
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import concurrent.Lock
import implicits.StringNormalizer._
import org.apache.commons.configuration.{PropertiesConfiguration, Configuration}
import java.io.File


class MtPostStreamer(config:util.Map[String,String],versionComparator:Comparator[String])
  extends MtBaseStreamer(config, versionComparator) {

  import com.mongodb.casbah.commons.conversions.scala._
  RegisterJodaTimeConversionHelpers()

  val VERSION = "0.0.2"
  val DB_COLLECTION = "user_post"

//  private val dateFormater = DateTimeFormat.forPattern("yyyy/MM/dd")

//  private var incrementalId = 0
//  private var streamEnd = true
  override protected val CACHE_FILE = "mt-post.cache"

  private val hashtagPatt = """#[\w\.\-_][\w\.\-_]+""".r
//  private val lock = new Lock()
//  private var cursor:MongoCursor = null

//  private lazy val con = {
//    val host = config.get("mongodb")
//    log.info("connecting to mongodb " + config.get("mongodb") + "...")
//    val hp = host.split(":")
//    MongoConnection(hp(0), hp(1).toInt)
//  }
//  private lazy val db = {
//    log.info("dbname: " + config.get("dbname"))
//    con(config.get("dbname"))
//  }
//  private lazy val col = db("user_post")
//  private def getConf = {
//    val fset = new File(CACHE_FILE)
//
//    if (!fset.exists())
//      fset.createNewFile()
//
//    new PropertiesConfiguration(fset)
//  }
//
//  object dataSyncher extends Thread with Logging {
//    private var _stoped = false
//    def setStop(state:Boolean){
//      log.info("stoping...")
//      _stoped = state
//    }
//    override def run() {
//      log.info("Started.")
//      while (!_stoped){
//        log.info("synching...")
//
//        val lastId = {
//          val conf = getConf
//          if (conf.containsKey("index.last_id"))
//            conf.getProperty("index.last_id").toString
//          else ""
//        }
//
//        if (lastId.length > 0)
//          log.info("last indexed id: " + lastId + ", starting from this id.")
//
//        if (streamEnd == true){
//          lock.acquire
//
//          try {
//
//            val query = {
//              if (lastId.length > 0){
//                ("_id" $gt new ObjectId(lastId)) ++ MongoDBObject("_deleted" -> false, "_closed" -> false)
//              }else{
//                MongoDBObject("_deleted" -> false, "_closed" -> false)
//              }
//            }
//
//            val count = col.count(query)
//            streamEnd = count == 0
//
//            if (!streamEnd){
//              log.info("streaming from " + count + " datas...")
//              cursor = col.find(query).sort(MongoDBObject("_id" -> 1))
//            }else{
//              log.info("no more data to stream.")
//            }
//
//          }finally{
//            lock.release
//          }
//
//        }
//
//        Thread.sleep(10000L)
//      }
//      log.info("stoped.")
//    }
//  }
//
//  dataSyncher.start()
//
//  Runtime.getRuntime.addShutdownHook(new Thread(){
//    override def run() {
//      dataSyncher.setStop(state = true)
//    }
//  })
//
//  log.info(VERSION + " loaded.")

//  private def getUser(id:String) = db("user").findOne(MongoDBObject("_id" -> new ObjectId(id)))


  private def extractHashTags(msg:String):String = {
    hashtagPatt.findAllIn(msg).map(_.normalize).foldLeft("")(_ + "," + _).normalize
  }

  private def getResponseCount(id:String):Int = {
    val respCol = db("response")
    respCol.find(MongoDBObject("_object_id" -> id)).size
  }

//  private var lastOid:String = ""

//  def next():DataEvent[JSONObject] = {
//
//    if (cursor == null || streamEnd == true)
//      return null
//
//    var evData:DataEvent[JSONObject] = null
//
//    lock.acquire
//    try {
//
//      if (!cursor.hasNext){
//        if (!streamEnd){
//          log.info("saving last index id: " + lastOid)
//          val conf = getConf
//          conf.setProperty("index.last_id", lastOid)
//          conf.save()
//          streamEnd = true
//        }
//        lock.release
//        return null
//      }
//
//      val p = cursor.next()
//
//      val json = new JSONObject()
//
//      json.put("id", incrementalId)
//      incrementalId += 1
//
//      val oid = p.as[ObjectId]("_id").toString
//
//      getUser(p.as[String]("_writer_id")) map { u =>
//        val creator_name = u.as[String]("name").normalize
//        val creator_sex:Int = u.as[Any]("sex") match {
//          case s:Boolean => s match {
//            case true => 1
//            case _ => 0
//          }
//          case s:Int => s
//          case s:java.lang.Integer => s.intValue()
//          case _ => 0
//        }
//        val creatorSdescsl = u.as[Seq[String]]("personal_descs").map(_.normalize).foldLeft("")(_ + "," + _).normalize
//        val creatorBd = dateFormater.print(u.as[DateTime]("birth_date"))
//        val creatorLoc = u.getAsOrElse[String]("location","").normalize
//        val creationDate = u.as[DateTime]("_creation_time")
//
//        val likesCount = p.getAsOrElse[Int]("_liked_user_count", 0)
//        val respCount = db("user_like").count(MongoDBObject("_object_id" -> oid))
//        val originKind = p.getAsOrElse[String]("origin_class", "User")
//        val originId = p.getAsOrElse[String]("_origin_id", "")
//        val containsData:String = {
//          if (p.getAsOrElse[Boolean]("contains_pic", false))
//            "pic"
//          else if (p.getAsOrElse[Boolean]("contains_link", false))
//            "link"
//          else if (p.getAsOrElse[Boolean]("contains_video_link", false))
//            "video_link"
//          else ""
//        }
//
//        val hashtags = extractHashTags(p.getAsOrElse[String]("message", ""))
//		    val responseCount = getResponseCount(oid)
//
//
//        log.info("processing post id " + oid + ", hashtags: " + hashtags)
//
//        json.put("oid", oid)
//        json.put("creator_name", creator_name)
//        json.put("creator_sex", creator_sex)
//        json.put("creator_sdescs", creatorSdescsl)
//        json.put("creator_birthdate", creatorBd)
//        json.put("creator_location", creatorLoc)
//        json.put("likes_count", likesCount)
//        json.put("resp_count", respCount)
//        json.put("origin_kind", originKind)
//        json.put("origin_id", originId)
//        json.put("hashtags", hashtags)
//        json.put("creation_date", creationDate.getMillis)
//		    json.put("contains_data", containsData)
//		    json.put("response_count", responseCount)
//
//        lastOid = oid
//
//        evData = new DataEvent[JSONObject](json, System.currentTimeMillis().toString)
//
//      }
//
//    }catch {
//      case e:Exception =>
//        e.printStackTrace()
//        return null
//    }finally{
//      lock.release
//    }
//    evData
//  }

  def buildDataEvent(json: JSONObject, doc:MongoDBObject):DataEvent[JSONObject] = {

    var evData:DataEvent[JSONObject] = null

    val oid = doc.as[ObjectId]("_id").toString

    getUser(doc.as[String]("_writer_id")) map { u =>
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

      val likesCount = doc.getAsOrElse[Int]("_liked_user_count", 0)
      val respCount = db("user_like").count(MongoDBObject("_object_id" -> oid))
      val originKind = doc.getAsOrElse[String]("origin_class", "User")
      val originId = doc.getAsOrElse[String]("_origin_id", "")
      val containsData:String = {
        if (doc.getAsOrElse[Boolean]("contains_pic", false))
          "pic"
        else if (doc.getAsOrElse[Boolean]("contains_link", false))
          "link"
        else if (doc.getAsOrElse[Boolean]("contains_video_link", false))
          "video_link"
        else ""
      }

      val hashtags = extractHashTags(doc.getAsOrElse[String]("message", ""))
      val responseCount = getResponseCount(oid)


      log.info("processing post id " + oid + ", hashtags: " + hashtags)

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

      setLastOid(oid)

      evData = new DataEvent[JSONObject](json, System.currentTimeMillis().toString)

    }

    evData
  }
}
