package com.senseidb.ba.index1;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.tool.DataFileReadTool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.browseengine.bobo.facets.data.TermValueList;
import com.senseidb.ba.ForwardIndex;
import com.senseidb.ba.IndexSegmentImpl;
import com.senseidb.ba.index1.InMemoryAvroMapper;
import com.senseidb.ba.index1.SegmentPersistentManager;
import com.senseidb.util.SingleNodeStarter;

public class SegmentPersistentManagerTest extends TestCase{

    private File avroFile;
    private File indexDir;

    @Before
    public void setUp() throws Exception {
        indexDir = new File("testIndex");
        SingleNodeStarter.rmrf(indexDir);
        indexDir.mkdir();
        indexDir = new File(indexDir, "segment");
        indexDir.mkdir();
        avroFile = new File(getClass().getClassLoader().getResource("data/sample_data.avro").toURI());
        

    }

    @After
    public void tearDown() throws Exception {
        SingleNodeStarter.rmrf(indexDir);

    }
@Test
    public void test1LoadPersistReadAndCompareWithJson() throws Exception {
        FileInputStream avroFileStream = new FileInputStream(avroFile);
        /*dumpToJson(avroFileStream, new PrintStream( new FileOutputStream(new File("json.txt"))));
        avroFileStream = new FileInputStream(avroFile);*/
        InMemoryAvroMapper avroMapper = new InMemoryAvroMapper(avroFile);
        IndexSegmentImpl indexSegmentImpl = avroMapper.build();
       
        SegmentPersistentManager segmentPersistentManager = new SegmentPersistentManager();
        segmentPersistentManager.persist(indexDir, indexSegmentImpl);
        IndexSegmentImpl persistedIndexSegment = segmentPersistentManager.read(indexDir, false);
        System.out.println(FileUtils.readFileToString(segmentPersistentManager.getPropertiesMetadata(indexDir).getFile()));
        
        IOUtils.closeQuietly(avroFileStream);
        compareWithJsonFile(indexSegmentImpl);
        compareWithJsonFile(persistedIndexSegment);
    }

private void compareWithJsonFile(IndexSegmentImpl indexSegmentImpl) throws URISyntaxException, IOException, JSONException {
    File jsonFile = new File(getClass().getClassLoader().getResource("data/sample_data.json").toURI());
    int i = 1;
    for (String line : FileUtils.readLines(jsonFile)) {
        JSONObject json = new JSONObject(line);
       
        for (String column : indexSegmentImpl.getColumnTypes().keySet()) {
            ForwardIndex forwardIndex = indexSegmentImpl.getForwardIndex(column);
            TermValueList<?> dictionary = indexSegmentImpl.getDictionary(column);
            
                String jsonValue = json.get(column).toString();
                int valueIndex = forwardIndex.getValueIndex(i);
               
                String value = dictionary.get(valueIndex);
                value = value.replaceFirst("^0+(?!$)", "");
                value = value.replaceFirst("^-0+(?!$)", "-");
                assertEquals(jsonValue, value);
        }
        i++;
    }
}
public void dumpToJson(InputStream fileStream, PrintStream out) throws Exception {
     

      GenericDatumReader<Object> reader = new GenericDatumReader<Object>();
      DataFileStream fileReader =
        new DataFileStream(fileStream, reader);
      try {
        Schema schema = fileReader.getSchema();
        DatumWriter<Object> writer = new GenericDatumWriter<Object>(schema);
        Encoder encoder = new JsonEncoder(schema, (JsonGenerator)null);
        for (Object datum : fileReader) {
          // init() recreates the internal Jackson JsonGenerator
          encoder.init(out);
          writer.write(datum, encoder);
          encoder.flush();
          out.println();
        }
        out.flush();
      } finally {
        fileReader.close();
      }
      //return 0;
    }

}
