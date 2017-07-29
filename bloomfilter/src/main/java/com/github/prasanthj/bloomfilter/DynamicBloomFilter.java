/**
 * Copyright 2014 Prasanth Jayachandran
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.prasanthj.bloomfilter;

/**
 * Derived from Apache Hadoop's implementation of DynamicBloomFilter with underlying
 * BLoomFilter replaced with faster implementation.
 *
 * Implements a <i>dynamic Bloom filter</i>, as defined in the INFOCOM 2006 paper.
 * <p>
 * A dynamic Bloom filter (DBF) makes use of a <code>s * m</code> bit matrix but
 * each of the <code>s</code> rows is a standard Bloom filter. The creation
 * process of a DBF is iterative. At the start, the DBF is a <code>1 * m</code>
 * bit matrix, i.e., it is composed of a single standard Bloom filter.
 * It assumes that <code>n<sub>r</sub></code> elements are recorded in the
 * initial bit vector, where <code>n<sub>r</sub> <= n</code> (<code>n</code> is
 * the cardinality of the set <code>A</code> to record in the filter).
 * <p>
 * As the size of <code>A</code> grows during the execution of the application,
 * several keys must be inserted in the DBF.  When inserting a key into the DBF,
 * one must first get an active Bloom filter in the matrix.  A Bloom filter is
 * active when the number of recorded keys, <code>n<sub>r</sub></code>, is
 * strictly less than the current cardinality of <code>A</code>, <code>n</code>.
 * If an active Bloom filter is found, the key is inserted and
 * <code>n<sub>r</sub></code> is incremented by one. On the other hand, if there
 * is no active Bloom filter, a new one is created (i.e., a new row is added to
 * the matrix) according to the current size of <code>A</code> and the element
 * is added in this new Bloom filter and the <code>n<sub>r</sub></code> value of
 * this new Bloom filter is set to one.  A given key is said to belong to the
 * DBF if the <code>k</code> positions are set to one in one of the matrix rows.
 * <p>
 * Originally created by
 * <a href="http://www.one-lab.org">European Commission One-Lab Project 034819</a>.
 *
 * @see <a href="http://www.cse.fau.edu/~jie/research/publications/Publication_files/infocom2006.pdf">Theory and Network Applications of Dynamic Bloom Filters</a>
 */
public class DynamicBloomFilter {
  /**
   * Threshold for the maximum number of key to record in a dynamic Bloom filter row.
   */
  private long nr;

  /**
   * The number of keys recorded in the current standard active Bloom filter.
   */
  private int currentNbRecord;

  /**
   * The matrix of Bloom filter.
   */
  private BloomFilter[] matrix;
  private double falsePositivePercent;

  public DynamicBloomFilter(long maxNumEntries) {
    this(maxNumEntries, BloomFilter.DEFAULT_FPP);
  }

  public DynamicBloomFilter(long maxNumEntries, double fpp) {

    this.nr = maxNumEntries;
    this.currentNbRecord = 0;

    matrix = new BloomFilter[1];
    matrix[0] = new BloomFilter(nr, fpp);
  }

  public void addByte(byte val) {
    BloomFilter bf = getActiveStandardBF();

    if (bf == null) {
      addRow();
      bf = matrix[matrix.length - 1];
      currentNbRecord = 0;
    }

    bf.addByte(val);

    currentNbRecord++;
  }

  public void addInt(int val) {
    BloomFilter bf = getActiveStandardBF();

    if (bf == null) {
      addRow();
      bf = matrix[matrix.length - 1];
      currentNbRecord = 0;
    }

    bf.addInt(val);

    currentNbRecord++;
  }

  public void addLong(long val) {
    BloomFilter bf = getActiveStandardBF();

    if (bf == null) {
      addRow();
      bf = matrix[matrix.length - 1];
      currentNbRecord = 0;
    }

    bf.addLong(val);

    currentNbRecord++;
  }

  public void addFloat(float val) {
    BloomFilter bf = getActiveStandardBF();

    if (bf == null) {
      addRow();
      bf = matrix[matrix.length - 1];
      currentNbRecord = 0;
    }

    bf.addFloat(val);

    currentNbRecord++;
  }

  public void addDouble(double val) {
    BloomFilter bf = getActiveStandardBF();

    if (bf == null) {
      addRow();
      bf = matrix[matrix.length - 1];
      currentNbRecord = 0;
    }

    bf.addDouble(val);

    currentNbRecord++;
  }

  public void addString(String val) {
    BloomFilter bf = getActiveStandardBF();

    if (bf == null) {
      addRow();
      bf = matrix[matrix.length - 1];
      currentNbRecord = 0;
    }

    bf.addString(val);

    currentNbRecord++;
  }

  public void addBytes(byte[] val) {
    BloomFilter bf = getActiveStandardBF();

    if (bf == null) {
      addRow();
      bf = matrix[matrix.length - 1];
      currentNbRecord = 0;
    }

    bf.addBytes(val);

    currentNbRecord++;
  }

  public boolean testByte(byte val) {

    for (int i = 0; i < matrix.length; i++) {
      if (matrix[i].testByte(val)) {
        return true;
      }
    }

    return false;
  }

  public boolean testInt(int val) {

    for (int i = 0; i < matrix.length; i++) {
      if (matrix[i].testInt(val)) {
        return true;
      }
    }

    return false;
  }

  public boolean testLong(long val) {

    for (int i = 0; i < matrix.length; i++) {
      if (matrix[i].testLong(val)) {
        return true;
      }
    }

    return false;
  }

  public boolean testFloat(float val) {

    for (int i = 0; i < matrix.length; i++) {
      if (matrix[i].testFloat(val)) {
        return true;
      }
    }

    return false;
  }

  public boolean testDouble(double val) {

    for (int i = 0; i < matrix.length; i++) {
      if (matrix[i].testDouble(val)) {
        return true;
      }
    }

    return false;
  }

  public boolean testString(String val) {

    for (int i = 0; i < matrix.length; i++) {
      if (matrix[i].testString(val)) {
        return true;
      }
    }

    return false;
  }

  public boolean testBytes(byte[] val) {

    for (int i = 0; i < matrix.length; i++) {
      if (matrix[i].testBytes(val)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Adds a new row to <i>this</i> dynamic Bloom filter.
   */
  private void addRow() {
    BloomFilter[] tmp = new BloomFilter[matrix.length + 1];

    for (int i = 0; i < matrix.length; i++) {
      tmp[i] = matrix[i];
    }

    tmp[tmp.length-1] = new BloomFilter(nr);

    matrix = tmp;
  }

  /**
   * Returns the active standard Bloom filter in <i>this</i> dynamic Bloom filter.
   * @return BloomFilter The active standard Bloom filter.
   * 			 <code>Null</code> otherwise.
   */
  private BloomFilter getActiveStandardBF() {
    if (currentNbRecord >= nr) {
      return null;
    }

    return matrix[matrix.length - 1];
  }

  public long getBitsetSize() {
    long result = 0;
    for (BloomFilter bf : matrix) {
      result += bf.getBitSize();
    }
    return result;
  }

  // this is configured value, not actual value
  public double getFalsePositivePercent() {
    return matrix[0].getFalsePositivePercent();
  }

  public BloomFilter[] getMatrix() {
    return matrix;
  }
}
