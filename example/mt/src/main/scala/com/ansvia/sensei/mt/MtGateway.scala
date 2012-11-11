package com.ansvia.sensei.mt

import java.util.Comparator
import java.util.Set

import org.json.JSONObject

import proj.zoie.impl.indexing.StreamDataProvider

import com.senseidb.gateway.SenseiGateway
import com.senseidb.indexing.DataSourceFilter
import com.senseidb.indexing.ShardingStrategy
import java.util

class MtGateway() extends SenseiGateway[JSONObject] {
			

  def buildDataProvider(dataFilter:DataSourceFilter[JSONObject], 
			oldSinceKey:String, 
			shardingStrategy:ShardingStrategy,
      partitions:util.Set[java.lang.Integer]) = {
				
    new MtUserStreamer(config, SenseiGateway.DEFAULT_VERSION_COMPARATOR)
  }

  def getVersionComparator = {
  	SenseiGateway.DEFAULT_VERSION_COMPARATOR
  }
}
