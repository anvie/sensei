package com.senseidb.ba.index1;

import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.browseengine.bobo.facets.data.TermValueList;
import com.senseidb.ba.ColumnType;
import com.senseidb.ba.util.CompressedIntArray;

public class InMemoryAvroMapper extends Avro2ForwardIndexMapper {
	private long startOffset = 0;
	public InMemoryAvroMapper(InputStream avroFile) {
		super(avroFile);
		
	}

	@Override
	public synchronized ColumnMetadata getColumnMetadata(TermValueList dictionary,
			int count, String columnName, ColumnType columnType) {
		int numOfBits = CompressedIntArray.getNumOfBits(dictionary.size());
		int bufferSize = CompressedIntArray.getRequiredBufferSize(count, numOfBits); 
		ColumnMetadata columnMetadata = new ColumnMetadata();
		columnMetadata.setBitsPerElement(numOfBits);
		columnMetadata.setByteLength(bufferSize);
		columnMetadata.setColumn(columnName);
		columnMetadata.setNumberOfDictionaryValues(dictionary.size());
		columnMetadata.setNumberOfElements(count);
		columnMetadata.setSorted(false);
		columnMetadata.setStartOffset(startOffset);
		columnMetadata.setColumnType(columnType);
		startOffset += bufferSize;
		return columnMetadata;
	}

	@Override
	public ByteBuffer getByteBuffer(int numOfElements, int dictionarySize) {
		return ByteBuffer.allocate(CompressedIntArray.getRequiredBufferSize(numOfElements, CompressedIntArray.getNumOfBits(dictionarySize)));
	}
  
}
