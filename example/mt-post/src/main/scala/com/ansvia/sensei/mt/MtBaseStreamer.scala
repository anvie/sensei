package com.ansvia.sensei.mt

import java.util.Comparator
import com.mongodb.casbah.Imports._

import org.json.JSONObject

import proj.zoie.api.DataConsumer.DataEvent
import proj.zoie.impl.indexing.StreamDataProvider
import java.util
import org.joda.time.format.DateTimeFormat
import concurrent.Lock
import org.apache.commons.configuration.PropertiesConfiguration
import java.io.File


abstract class MtBaseStreamer(config:util.Map[String,String],versionComparator:Comparator[String])
  extends StreamDataProvider[JSONObject](versionComparator) with Logging {

  import com.mongodb.casbah.commons.conversions.scala._
  RegisterJodaTimeConversionHelpers()

  val VERSION:String
  val DB_COLLECTION:String
  val SYNCHING_DELAY = 60000L

  protected val dateFormater = DateTimeFormat.forPattern("yyyy/MM/dd")

  protected var incrementalId = 0
  protected var streamEnd = true
  protected val CACHE_FILE:String = "mt-streamer.cache"


  protected val lock = new Lock()
  protected var cursor:MongoCursor = null

  protected lazy val con = {
    val host = config.get("mongodb")
    log.info("connecting to mongodb " + config.get("mongodb") + "...")
    val hp = host.split(":")
    MongoConnection(hp(0), hp(1).toInt)
  }
  protected lazy val db = {
    log.info("dbname: " + config.get("dbname"))
    con(config.get("dbname"))
  }
  protected lazy val col = db(DB_COLLECTION)

  protected def getConfig = {
    val fset = new File(CACHE_FILE)

    if (!fset.exists())
      fset.createNewFile()

    new PropertiesConfiguration(fset)
  }

  object dataSyncher extends Thread with Logging {
    private var _stoped = false
    def setStop(state:Boolean){
      log.info("stoping...")
      _stoped = state
    }

    override def run() {
      log.info("Started.")

      while (!_stoped){

        val conf = getConfig

        if(lastOid.length > 0){
          log.info("saving last index id: " + lastOid)
          conf.setProperty("index.last_id", lastOid)
          conf.save()
        }

        val lastId = {
          if (conf.containsKey("index.last_id"))
            conf.getProperty("index.last_id").toString
          else ""
        }

        if (streamEnd == true){
          lock.acquire

          log.info("synching...")

          if (lastId.length > 0)
            log.info("last indexed id: " + lastId + ", starting from this id.")

          try {

            val (query, sortBy) = buildSyncQuery(lastId)

            val count = col.count(query)
            streamEnd = count == 0

            if (!streamEnd){
              log.info("streaming from " + count + " datas...")
              cursor = col.find(query).sort(sortBy)
            }else{
              log.info("no more data to stream.")
            }

          }finally{
            lock.release
          }

        }

        Thread.sleep(SYNCHING_DELAY)
      }
      log.info("stoped.")
    }
  }

  dataSyncher.start()

  Runtime.getRuntime.addShutdownHook(new Thread(){
    override def run() {
      dataSyncher.setStop(state = true)
    }
  })

  log.info(VERSION + " loaded.")

  protected def getUser(id:String) = db("user").findOne(MongoDBObject("_id" -> new ObjectId(id)))

  protected def getResponseCount(id:String):Int = {
    val respCol = db("response")
    respCol.find(MongoDBObject("_object_id" -> id)).size
  }

  def buildSyncQuery(lastId: String):(DBObject, DBObject) = {
    if (lastId.length > 0){
      (("_id" $gt new ObjectId(lastId)) ++ MongoDBObject("_deleted" -> false, "_closed" -> false), MongoDBObject("_id" -> 1))
    }else{
      (MongoDBObject("_deleted" -> false, "_closed" -> false), MongoDBObject("_id" -> 1))
    }
  }

  private var lastOid:String = ""

  def buildDataEvent(json: JSONObject, doc:MongoDBObject):DataEvent[JSONObject]

  def setLastOid(oid:String){
    lastOid = oid
  }

  def getLastOid = lastOid

  def next():DataEvent[JSONObject] = {

    if (cursor == null || streamEnd == true)
      return null

    var evData:DataEvent[JSONObject] = null

    lock.acquire
    try {

      if (!cursor.hasNext){
        if (!streamEnd){
          log.info("saving last index id: " + lastOid)
          val conf = getConfig
          conf.setProperty("index.last_id", lastOid)
          conf.save()
          streamEnd = true
        }
        lock.release
        return null
      }

      val p = cursor.next()

      val json = new JSONObject()

      json.put("id", incrementalId)
      incrementalId += 1

      val oid = p.as[ObjectId]("_id").toString

      evData = buildDataEvent(json, p)

      lastOid = oid

    }catch {
      case e:Exception =>
        e.printStackTrace()
        return null
    }finally{
      lock.release
    }
    evData
  }

  def setStartingOffset(p1: String){}

  def reset() {}
}
