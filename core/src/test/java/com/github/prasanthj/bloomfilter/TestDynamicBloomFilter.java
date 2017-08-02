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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

/**
 *
 */
public class TestDynamicBloomFilter {
  // dynamic bloom is known to have higher fpp with higher misestimation of expected entries
  // to make tests pass give room for another 10%
  private final double deltaError = 0.1;

  @Test
  public void testFpp1K() {
    int size = 1000;
    DynamicBloomFilter bf = new DynamicBloomFilter(size);
    int fp = 0;
    for (int i = 0; i < size * 1.1; i++) {
      bf.addLong(i);
    }

    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int probe = random.nextInt();
      // out of range probes
      if ((probe > size) || (probe < 0)) {
        if (bf.testLong(probe)) {
          fp++;
        }
      }
    }

    double actualFpp = (double) fp / (double) size;
    double expectedFpp = bf.getFalsePositivePercent();
    if (actualFpp < expectedFpp) {
      assertTrue(actualFpp != 0.0);
    } else {
      assertEquals(expectedFpp, actualFpp, deltaError);
    }
  }

  @Test
  public void testFpp10K() {
    int size = 10_000;
    DynamicBloomFilter bf = new DynamicBloomFilter(size);
    int fp = 0;
    for (int i = 0; i < size * 1.2; i++) {
      bf.addLong(i);
    }

    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int probe = random.nextInt();
      // out of range probes
      if ((probe > size) || (probe < 0)) {
        if (bf.testLong(probe)) {
          fp++;
        }
      }
    }

    double actualFpp = (double) fp / (double) size;
    double expectedFpp = bf.getFalsePositivePercent();
    if (actualFpp < expectedFpp) {
      assertTrue(actualFpp != 0.0);
    } else {
      assertEquals(expectedFpp, actualFpp, deltaError);
    }
  }

  @Test
  public void testFpp1M() {
    int size = 1_000_000;
    DynamicBloomFilter bf = new DynamicBloomFilter(size);
    int fp = 0;
    for (int i = 0; i < size * 1.3; i++) {
      bf.addLong(i);
    }

    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int probe = random.nextInt();
      // out of range probes
      if ((probe > size) || (probe < 0)) {
        if (bf.testLong(probe)) {
          fp++;
        }
      }
    }

    double actualFpp = (double) fp / (double) size;
    double expectedFpp = bf.getFalsePositivePercent();
    if (actualFpp < expectedFpp) {
      assertTrue(actualFpp != 0.0);
    } else {
      assertEquals(expectedFpp, actualFpp, deltaError);
    }
  }

  @Test
  public void testFpp10M() {
    int size = 10_000_000;
    DynamicBloomFilter bf = new DynamicBloomFilter(size);
    int fp = 0;
    for (int i = 0; i < size * 1.4; i++) {
      bf.addLong(i);
    }

    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int probe = random.nextInt();
      // out of range probes
      if ((probe > size) || (probe < 0)) {
        if (bf.testLong(probe)) {
          fp++;
        }
      }
    }

    double actualFpp = (double) fp / (double) size;
    double expectedFpp = bf.getFalsePositivePercent();
    if (actualFpp < expectedFpp) {
      assertTrue(actualFpp != 0.0);
    } else {
      assertEquals(expectedFpp, actualFpp, deltaError);
    }
  }

  @Test
  public void testBitsetSize() {
    int size = 10_000;
    DynamicBloomFilter bf = new DynamicBloomFilter(size);
    for (int i = 0; i < size; i++) {
      bf.addLong(i);
    }
    assertEquals(1, bf.getMatrix().length);
    for (int i = 0; i < 100; i++) {
      bf.addLong(i);
    }
    assertEquals(2, bf.getMatrix().length);
    for (int i = 0; i < size; i++) {
      bf.addLong(size + i);
    }
    assertEquals(3, bf.getMatrix().length);
  }
}
