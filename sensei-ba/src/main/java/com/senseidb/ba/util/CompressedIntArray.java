package com.senseidb.ba.util;

import java.nio.ByteBuffer;

public class CompressedIntArray {
  private final ByteBuffer buf;
  private int capacity;
  private final int numOfBitsPerElement;
private byte[] tempBuf;

  public CompressedIntArray(int numOfElements, int numOfBitsPerElement) {
    this.numOfBitsPerElement = numOfBitsPerElement;
    capacity = numOfElements;
    buf = ByteBuffer.allocateDirect(getRequiredBufferSize(numOfElements, numOfBitsPerElement));
    tempBuf = getByteBuf();
  }

  public static int getRequiredBufferSize(int numOfElements, int numOfBitsPerElement) {
    return (int) Math.ceil((float) numOfElements / 8 * numOfBitsPerElement);
  }
  public static int getNumOfBits(int dictionarySize) {
	    return  (int) Math.ceil(Math.log(dictionarySize)/Math.log(2));
	  }

  public CompressedIntArray(int numOfElements, int numOfBitsPerElement, ByteBuffer byteBuffer) {
    this.numOfBitsPerElement = numOfBitsPerElement;
    capacity = numOfElements;
    buf = byteBuffer;
    tempBuf = getByteBuf();
  }

  /**
   * We can reuse this buffer for multiple calls
   */
  public byte[] getByteBuf() {
    return new byte[numOfBitsPerElement / 8 + (numOfBitsPerElement % 8 < 2 ? 1 : 2)];
  }
  /**
   * This method is not threadsafe
 * @param position
 * @param number
 */
public void addInt(int position, int number) {
	addInt(position, number, tempBuf);
  }
  public void addInt(int position, int number, byte[] tempBuf) {
    int bytePosition = position * numOfBitsPerElement / 8;
    int startBitOffset = (position * numOfBitsPerElement) % 8;
    int endBitOffset = (8 - ((startBitOffset + numOfBitsPerElement) % 8)) % 8;
    int numberOfBytesUsed = (startBitOffset + numOfBitsPerElement) / 8
        + ((startBitOffset + numOfBitsPerElement) % 8 != 0 ? 1 : 0);
    buf.position(bytePosition);
    buf.get(tempBuf, 0, numberOfBytesUsed);
    long newNumber = tempBuf[0];
    if (startBitOffset > 0) {
      newNumber >>= 8 - startBitOffset;
    }
    newNumber <<= numOfBitsPerElement;
    newNumber |= number;
    if (endBitOffset != 0) {
      newNumber <<= endBitOffset;
      newNumber |= tempBuf[numberOfBytesUsed - 1] & 0xFF >>> (8 - endBitOffset);
    }
    for (int i = numberOfBytesUsed - 1; i >= 0; i--) {
      tempBuf[i] = (byte) (newNumber & 0xFF);

      newNumber = newNumber >> 8;
    }
    buf.position(bytePosition);
    buf.put(tempBuf, 0, numberOfBytesUsed);
  }

  public int readInt(int position) {

    /*
     * int bytePosition = position * numOfBitsPerElement / 8; int startBitOffset
     * = (position * numOfBitsPerElement) % 8; int endBitOffset = (8 -
     * ((startBitOffset + numOfBitsPerElement) % 8)) % 8; int numberOfBytesUsed
     * = (startBitOffset + numOfBitsPerElement) / 8 + ((startBitOffset +
     * numOfBitsPerElement) % 8 != 0 ? 1 : 0);buf.position(bytePosition);
     */
    int mult = position * numOfBitsPerElement;
    int bytePosition = mult >>> 3;
    int startBitOffset = mult & 7;
    int sum = startBitOffset + numOfBitsPerElement;
    int endBitOffset = (8 - (sum & 7)) & 7;

    // int numberOfBytesUsed = (sum >>> 3) + ((sum & 7) != 0 ? 1 : 0);
    int numberOfBytesUsed = ((sum + 7) >>> 3);
    int i = 0;

    buf.position(bytePosition);

    long number = 0;
    i = -1;
    while (true) {
      number |= (buf.get()) & 0xFF;
      i++;
      if (i == numberOfBytesUsed - 1) {
        break;
      }
      number <<= 8;
    }
    number >>= endBitOffset;
    number &= (0xFFFFFFFF >>> (32 - numOfBitsPerElement));
    return (int) number;
  }

public int getCapacity() {
	return capacity;
}

public ByteBuffer getStorage() {
	return buf;
}
  
}
