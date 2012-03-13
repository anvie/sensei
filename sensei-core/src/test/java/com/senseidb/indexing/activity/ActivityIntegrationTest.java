package com.senseidb.indexing.activity;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;

import proj.zoie.impl.indexing.ZoieConfig;

import com.senseidb.gateway.file.FileDataProviderWithMocks;
import com.senseidb.test.SenseiStarter;
import com.senseidb.test.TestSensei;

public class ActivityIntegrationTest extends TestCase {

  private static final Logger logger = Logger.getLogger(ActivityIntegrationTest.class);
 
  static {
    SenseiStarter.start("test-conf/node1", "test-conf/node2");  
  }
  public void test1SendUpdatesAndSort() throws Exception {
    for (int i = 0; i < 10; i++) {
      FileDataProviderWithMocks.add(new JSONObject().put("id", 10L + i).put("_type", "update").put("likes", "+" + (10 + i)));
    }
    final CompositeActivityValues inMemoryColumnData1 = CompositeActivityManager.cachedInstances.get(1).activityValues;
    final CompositeActivityValues inMemoryColumnData2 = CompositeActivityManager.cachedInstances.get(2).activityValues;
    Wait.until(10000, "The activity value wasn't updated", new Wait.Condition() {
      public boolean evaluate() {
        return inMemoryColumnData1.getVersion().equals(String.valueOf(15009)) || inMemoryColumnData2.getVersion().equals(String.valueOf(15009));
      }
    }); 
    String req = "{\"selections\": [{\"range\": {\"likes\": {\"from\": 18, \"include_lower\": true}}}], \"sort\":[{\"likes\":\"desc\"}]}";
    JSONObject res = TestSensei.search(new JSONObject(req));
    JSONArray hits = res.getJSONArray("hits");
    assertEquals(hits.getJSONObject(0).getJSONArray("likes").toString(), "[\"20\"]");
    assertEquals(hits.getJSONObject(1).getJSONArray("likes").toString(), "[\"19\"]");
    assertEquals(hits.getJSONObject(2).getJSONArray("likes").toString(), "[\"18\"]");
   System.out.println("!!!" + res.toString(1));
  }
  
  public void test2SendUpdateAndCheckIfItsPersisted() throws Exception {
    for (int i = 0; i < 5; i++) {
      FileDataProviderWithMocks.add(new JSONObject().put("id", 1L).put("_type", "update").put("likes", "+5").put("color", "blue"));
    }
    final CompositeActivityValues inMemoryColumnData1 = CompositeActivityManager.cachedInstances.get(1).activityValues;
    final CompositeActivityValues inMemoryColumnData2 = CompositeActivityManager.cachedInstances.get(2).activityValues;
    Wait.until(10000, "The activity value wasn't updated", new Wait.Condition() {
      public boolean evaluate() {
        return inMemoryColumnData1.getValueByUID(1L, "likes") == 26 || inMemoryColumnData2.getValueByUID(1L, "likes") == 26;
      }
    });
    for (int i = 0; i < 5; i++) {
      FileDataProviderWithMocks.add(new JSONObject().put("id", 1L).put("_type", "update").put("likes", "+5"));
    }
    Wait.until(10000, "The activity value wasn't updated", new Wait.Condition() {
      public boolean evaluate() {
        return inMemoryColumnData1.getValueByUID(1L, "likes") == 51 || inMemoryColumnData2.getValueByUID(1L, "likes") == 51;
      }
    });
  }
  @Ignore
  public void test3OpeningTheNewActivityFieldValues() throws Exception {
    final CompositeActivityValues inMemoryColumnData1 = CompositeActivityManager.cachedInstances.get(1).activityValues;
    inMemoryColumnData1.flush();
    inMemoryColumnData1.syncWithPersistentVersion(String.valueOf(15019));
    FileDataProviderWithMocks.add(new JSONObject().put("id", 0).put("_type", "update").put("likes", "+5"));
    String absolutePath = SenseiStarter.IndexDir + "/test/node1/" + "activity/";
    CompositeActivityValues compositeActivityValues = CompositeActivityValues.readFromFile(absolutePath, java.util.Arrays.asList("likes"), ZoieConfig.DEFAULT_VERSION_COMPARATOR);
    
    assertEquals(51, inMemoryColumnData1.getValueByUID(1L, "likes"));
  }
}
