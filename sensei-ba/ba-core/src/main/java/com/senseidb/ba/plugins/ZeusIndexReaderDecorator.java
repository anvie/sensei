package com.senseidb.ba.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.DocIdSet;

import proj.zoie.api.ZoieIndexReader;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.facets.FacetHandler;
import com.senseidb.ba.SegmentToZoieReaderAdapter;
import com.senseidb.ba.facet.BaFacetHandler;
import com.senseidb.ba.gazelle.IndexSegment;
import com.senseidb.plugin.SenseiPlugin;
import com.senseidb.plugin.SenseiPluginRegistry;
import com.senseidb.search.node.SenseiIndexReaderDecorator;

public class ZeusIndexReaderDecorator extends SenseiIndexReaderDecorator implements SenseiPlugin {
  final static String[] emptyString = new String[0];
  private final List customFacetHandlers;
  @SuppressWarnings("rawtypes")
  public ZeusIndexReaderDecorator(List<FacetHandler> customFacetHandlers) {
    this.customFacetHandlers = customFacetHandlers;
  }
  public ZeusIndexReaderDecorator() {
    this(Collections.EMPTY_LIST);
  }
  @Override
public BoboIndexReader decorate(ZoieIndexReader<BoboIndexReader> zoieReader) throws IOException {
  SegmentToZoieReaderAdapter adapter = (SegmentToZoieReaderAdapter<?>)zoieReader;
  final IndexSegment offlineSegment = adapter.getOfflineSegment();
  List facetHandlers = new ArrayList(offlineSegment.getColumnTypes().size() + 1);
  
 
  for (String column : offlineSegment.getColumnTypes().keySet()) {
    facetHandlers.add(new BaFacetHandler(column, column, IndexSegment.class.getSimpleName()));
  }
  if (customFacetHandlers != null) {
    facetHandlers.addAll((List) customFacetHandlers);
  }
  BoboIndexReader indexReader =  new BoboIndexReader(adapter,  facetHandlers, Collections.EMPTY_LIST, new BoboIndexReader.WorkArea(), false) {
    public void facetInit() throws IOException {
      putFacetData(IndexSegment.class.getSimpleName(), offlineSegment);
      super.facetInit();
    }
    {facetInit();}
  };
 
  return indexReader;
  }
  @Override
  public void setDeleteSet(BoboIndexReader reader, DocIdSet docIds) {
    // TODO Auto-generated method stub
    
  }
  @Override
  public void init(Map<String, String> config, SenseiPluginRegistry pluginRegistry) {
    customFacetHandlers.addAll(pluginRegistry.resolveBeansByListKey(SenseiPluginRegistry.FACET_CONF_PREFIX, FacetHandler.class));
    
  }
  @Override
  public void start() {
    // TODO Auto-generated method stub
    
  }
  @Override
  public void stop() {
    // TODO Auto-generated method stub
    
  }
 
}
