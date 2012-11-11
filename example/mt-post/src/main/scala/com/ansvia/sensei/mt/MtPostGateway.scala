package com.ansvia.sensei.mt


import org.json.JSONObject

import com.senseidb.gateway.SenseiGateway
import com.senseidb.indexing.DataSourceFilter
import com.senseidb.indexing.ShardingStrategy
import java.util

class MtPostGateway extends SenseiGateway[JSONObject] {
			

  def buildDataProvider(dataFilter:DataSourceFilter[JSONObject], 
			oldSinceKey:String, 
			shardingStrategy:ShardingStrategy,
      partitions:util.Set[java.lang.Integer]) = {
				
    new MtPostStreamer(config, SenseiGateway.DEFAULT_VERSION_COMPARATOR)
  }

  def getVersionComparator = {
  	SenseiGateway.DEFAULT_VERSION_COMPARATOR
  }
}
