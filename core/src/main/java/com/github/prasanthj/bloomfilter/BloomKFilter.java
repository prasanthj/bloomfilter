/**
 *   Copyright 2014 Prasanth Jayachandran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.prasanthj.bloomfilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * BloomFilter is a probabilistic data structure for set membership check. BloomFilters are
 * highly space efficient when compared to using a HashSet. Because of the probabilistic nature of
 * bloom filter false positive (element not present in bloom filter but test() says true) are
 * possible but false negatives are not possible (if element is present then test() will never
 * say false). The false positive probability is configurable (default: 5%) depending on which
 * storage requirement may increase or decrease. Lower the false positive probability greater
 * is the space requirement.
 * Bloom filters are sensitive to number of elements that will be inserted in the bloom filter.
 * During the creation of bloom filter expected number of entries must be specified. If the number
 * of insertions exceed the specified initial number of entries then false positive probability will
 * increase accordingly.
 * <p/>
 * Internally, this implementation of bloom filter uses Murmur3 fast non-cryptographic hash
 * algorithm. Although Murmur2 is slightly faster than Murmur3 in Java, it suffers from hash
 * collisions for specific sequence of repeating bytes. Check the following link for more info
 * https://code.google.com/p/smhasher/wiki/MurmurHash2Flaw
 */
public class BloomKFilter {
  private byte[] BYTE_ARRAY_4 = new byte[4];
  private byte[] BYTE_ARRAY_8 = new byte[8];
  private static final double DEFAULT_FPP = 0.05;
  private static final int DEFAULT_BLOCK_SIZE = 8;
  private static final int DEFAULT_BLOCK_SIZE_BITS = (int) (Math.log(DEFAULT_BLOCK_SIZE) / Math.log(2));
  private static final int DEFAULT_BLOCK_OFFSET_MASK = DEFAULT_BLOCK_SIZE - 1;
  private static final int DEFAULT_BIT_OFFSET_MASK = Long.SIZE - 1;
  private BitSet bitSet;
  private final long m;
  private final int k;
  private final double fpp;
  private final long n;
  private final long[] masks = new long[DEFAULT_BLOCK_SIZE];
  // spread k-1 bits to adjacent longs, default is 8
  // spreading hash bits within blockSize * longs will make bloom filter L1 cache friendly
  // default block size is set to 8 as most cache line sizes are 64 bytes and also AVX512 friendly
  private final int totalBlockCount;

  public BloomKFilter(long maxNumEntries) {
    this(maxNumEntries, DEFAULT_FPP);
  }

  public BloomKFilter(long maxNumEntries, double fpp) {
    assert maxNumEntries > 0 : "maxNumEntries should be > 0";
    assert fpp > 0.0 && fpp < 1.0 : "False positive percentage should be > 0.0 & < 1.0";
    this.fpp = fpp;
    this.n = maxNumEntries;
    long numBits = optimalNumOfBits(maxNumEntries, fpp);
    this.k = optimalNumOfHashFunctions(maxNumEntries, numBits);
    int nLongs = (int) Math.ceil((double) numBits / (double) Long.SIZE);
    // additional bits to pad long array to block size
    int padLongs = DEFAULT_BLOCK_SIZE - nLongs % DEFAULT_BLOCK_SIZE;
    this.m = (nLongs + padLongs) * Long.SIZE;
    this.bitSet = new BitSet(m);
    assert (bitSet.data.length % DEFAULT_BLOCK_SIZE) == 0 : "bitSet has to be block aligned";
    this.totalBlockCount = bitSet.data.length / DEFAULT_BLOCK_SIZE;
  }

  // deserialize bloomfilter. see serialize() for the format.
  public BloomKFilter(List<Long> serializedBloom) {
    this(serializedBloom.get(0), Double.longBitsToDouble(serializedBloom.get(1)));
    List<Long> bitSet = serializedBloom.subList(2, serializedBloom.size());
    long[] data = new long[bitSet.size()];
    for (int i = 0; i < bitSet.size(); i++) {
      data[i] = bitSet.get(i);
    }
    this.bitSet = new BitSet(data);
  }

  static int optimalNumOfHashFunctions(long n, long m) {
    return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
  }

  static long optimalNumOfBits(long n, double p) {
    if (p == 0) {
      p = Double.MIN_VALUE;
    }
    return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
  }

  public long sizeInBytes() {
    return bitSet.bitSize() / 8;
  }

  public void add(byte[] val) {
    addBytes(val);
  }

  public void addBytes(byte[] val) {

    // We use the trick mentioned in "Less Hashing, Same Performance: Building a Better Bloom Filter"
    // by Kirsch et.al. From abstract 'only two hash functions are necessary to effectively
    // implement a Bloom filter without any loss in the asymptotic false positive probability'

    // Lets split up 64-bit hashcode into two 32-bit hashcodes and employ the technique mentioned
    // in the above paper
    long hash64 = Murmur3.hash64(val);
    int hash1 = (int) hash64;
    int hash2 = (int) (hash64 >>> 32);

    int firstHash = hash1 + hash2;
    // hashcode should be positive, flip all the bits if it's negative
    if (firstHash < 0) {
      firstHash = ~firstHash;
    }

    // first hash is used to locate start of the block (blockBaseOffset)
    // subsequent K hashes are used to generate K bits within a block of words
    final int blockIdx = firstHash % totalBlockCount;
    final int blockBaseOffset = blockIdx << DEFAULT_BLOCK_SIZE_BITS;
    for (int i = 1; i <= k; i++) {
      int combinedHash = hash1 + ((i + 1) * hash2);
      // hashcode should be positive, flip all the bits if it's negative
      if (combinedHash < 0) {
        combinedHash = ~combinedHash;
      }
      // LSB 3 bits is used to locate offset within the block
      final int absOffset = blockBaseOffset + (combinedHash & DEFAULT_BLOCK_OFFSET_MASK);
      // Next 6 bits are used to locate offset within a long/word
      final int bitPos = (combinedHash >>> DEFAULT_BLOCK_SIZE_BITS) & DEFAULT_BIT_OFFSET_MASK;
      bitSet.data[absOffset] |= (1L << bitPos);
    }
  }

  public void addString(String val) {
    addBytes(val.getBytes());
  }

  public void addByte(byte val) {
    addBytes(new byte[]{val});
  }

  public void addInt(int val) {
    // puts int in little endian order
    addBytes(intToByteArrayLE(val));
  }


  public void addLong(long val) {
    // puts long in little endian order
    addBytes(longToByteArrayLE(val));
  }

  public void addFloat(float val) {
    addInt(Float.floatToIntBits(val));
  }

  public void addDouble(double val) {
    addLong(Double.doubleToLongBits(val));
  }

  public boolean test(byte[] val) {
    return testBytes(val);
  }

  public boolean testBytes(byte[] val) {
    long hash64 = Murmur3.hash64(val);
    int hash1 = (int) hash64;
    int hash2 = (int) (hash64 >>> 32);

    int firstHash = hash1 + hash2;
    // hashcode should be positive, flip all the bits if it's negative
    if (firstHash < 0) {
      firstHash = ~firstHash;
    }

    // first hash is used to locate start of the block (blockBaseOffset)
    // subsequent K hashes are used to generate K bits within a block of words
    // To avoid branches during probe, a separate masks array is used for each longs/words within a block.
    // data array and masks array are then traversed together and checked for corresponding set bits.
    final int blockIdx = firstHash % totalBlockCount;
    final int blockBaseOffset = blockIdx << DEFAULT_BLOCK_SIZE_BITS;

    // iterate and update masks array
    for (int i = 1; i <= k; i++) {
      int combinedHash = hash1 + ((i + 1)  * hash2);
      // hashcode should be positive, flip all the bits if it's negative
      if (combinedHash < 0) {
        combinedHash = ~combinedHash;
      }
      // LSB 3 bits is used to locate offset within the block
      final int wordOffset = combinedHash & DEFAULT_BLOCK_OFFSET_MASK;
      // Next 6 bits are used to locate offset within a long/word
      final int bitPos = (combinedHash >>> DEFAULT_BLOCK_SIZE_BITS) & DEFAULT_BIT_OFFSET_MASK;
      masks[wordOffset] |= (1L << bitPos);
    }

    // traverse data and masks array together, check for set bits
    long expected = 0;
    for (int i = 0; i < DEFAULT_BLOCK_SIZE; i++) {
      final long mask = masks[i];
      expected |= (bitSet.data[blockBaseOffset + i] & mask) ^ mask;
    }

    // clear the mask for array reuse (this is to avoid masks array allocation in inner loop)
    Arrays.fill(masks, 0);

    // if all bits are set, expected should be 0
    return expected == 0;
  }

  public boolean testString(String val) {
    return testBytes(val.getBytes());
  }

  public boolean testByte(byte val) {
    return testBytes(new byte[]{val});
  }

  public boolean testInt(int val) {
    return testBytes(intToByteArrayLE(val));
  }

  public boolean testLong(long val) {
    return testBytes(longToByteArrayLE(val));
  }

  public boolean testFloat(float val) {
    return testInt(Float.floatToIntBits(val));
  }

  public boolean testDouble(double val) {
    return testLong(Double.doubleToLongBits(val));
  }

  private byte[] intToByteArrayLE(int val) {
    BYTE_ARRAY_4[0] = (byte) (val >> 0);
    BYTE_ARRAY_4[1] = (byte) (val >> 8);
    BYTE_ARRAY_4[2] = (byte) (val >> 16);
    BYTE_ARRAY_4[3] = (byte) (val >> 24);
    return BYTE_ARRAY_4;
  }

  private byte[] longToByteArrayLE(long val) {
    BYTE_ARRAY_8[0] = (byte) (val >> 0);
    BYTE_ARRAY_8[1] = (byte) (val >> 8);
    BYTE_ARRAY_8[2] = (byte) (val >> 16);
    BYTE_ARRAY_8[3] = (byte) (val >> 24);
    BYTE_ARRAY_8[4] = (byte) (val >> 32);
    BYTE_ARRAY_8[5] = (byte) (val >> 40);
    BYTE_ARRAY_8[6] = (byte) (val >> 48);
    BYTE_ARRAY_8[7] = (byte) (val >> 56);
    return BYTE_ARRAY_8;
  }

  public long getBitSize() {
    return m;
  }

  public int getNumHashFunctions() {
    return k;
  }

  public double getFalsePositivePercent() {
    return fpp;
  }

  public long getExpectedNumEntries() {
    return n;
  }

  /**
   * First 2 entries are expected entries (n) and false positive percentage (fpp). fpp which is a
   * double is serialized as long. The entries following first 2 entries are the actual bit set.
   *
   * @return bloom filter as list of long
   */
  public List<Long> serialize() {
    List<Long> serialized = new ArrayList<Long>();
    serialized.add(n);
    serialized.add(Double.doubleToLongBits(fpp));
    for (long l : bitSet.getData()) {
      serialized.add(l);
    }
    return serialized;
  }

  /**
   * Check if the specified bloom filter is compatible with the current bloom filter.
   *
   * @param that - bloom filter to check compatibility
   * @return true if compatible false otherwise
   */
  public boolean isCompatible(BloomKFilter that) {
    return this != that &&
        this.getBitSize() == that.getBitSize() &&
        this.getNumHashFunctions() == that.getNumHashFunctions();
  }

  /**
   * Merge the specified bloom filter with current bloom filter.
   * NOTE: Merge does not check for incompatibility. Use isCompatible() before calling merge().
   *
   * @param that - bloom filter to merge
   */
  public void merge(BloomKFilter that) {
    this.bitSet.putAll(that.bitSet);
  }

  public long getNumBits() {
    return m;
  }

  public long[] getBitSet() {
    return bitSet.getData();
  }
  /**
   * Bare metal bitset implementation. For performance reasons, this implementation does not check
   * for index bounds nor expand the bitset size if the specified index is greater than the size.
   */
  public static class BitSet {
    final long[] data;

    public BitSet(long bits) {
      this(new long[(int) Math.ceil((double) bits / (double) Long.SIZE)]);
    }

    /**
     * Deserialize long array as bitset.
     *
     * @param data
     */
    BitSet(long[] data) {
      assert data.length > 0 : "data length is zero!";
      this.data = data;
    }

    /**
     * Sets the bit at specified index.
     *
     * @param index
     */
    public void set(long index) {
      data[(int) (index >>> 6)] |= (1L << index);
    }

    /**
     * Returns true if the bit is set in the specified index.
     *
     * @param index
     * @return
     */
    boolean get(long index) {
      return (data[(int) (index >>> 6)] & (1L << index)) != 0;
    }

    /**
     * Number of bits
     */
    long bitSize() {
      return (long) data.length * Long.SIZE;
    }

    public long[] getData() {
      return data;
    }

    /**
     * Combines the two BitArrays using bitwise OR.
     */
    void putAll(BitSet array) {
      assert data.length == array.data.length :
          "BitArrays must be of equal length (" + data.length + "!= " + array.data.length + ")";
      for (int i = 0; i < data.length; i++) {
        data[i] |= array.data[i];
      }
    }
  }
}
