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
package com.github.prasanthj.bloomfilter.benchmarks;

import com.github.prasanthj.bloomfilter.BloomFilter;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.roaringbitmap.RoaringBitmap;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javolution.util.FastBitSet;

/**
 *
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(3)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
public class BenchmarkBitSet {
  private int[] indices;

  @Setup
  public void setup() {
    Random rand = new Random(123);
    int size = 10000;
    indices = new int[size];
    for(int i = 0; i < indices.length; i++) {
      indices[i] = rand.nextInt(size);
    }
  }

  @Benchmark
  public void custom_bitset(Blackhole bh) {
    BloomFilter.BitSet bs = new BloomFilter.BitSet(10000);
    for(int i : indices) {
      bs.set(i);
    }
    bh.consume(bs);
  }

  @Benchmark
  public void java_bitset(Blackhole bh) {
    java.util.BitSet bs = new java.util.BitSet(10000);
    for(int i : indices) {
      bs.set(i);
    }
    bh.consume(bs);
  }

  @Benchmark
  public void roaring_bitset(Blackhole bh) {
    RoaringBitmap roaringBitmap = new RoaringBitmap();
    for(int i : indices) {
      roaringBitmap.add(i);
    }
    bh.consume(roaringBitmap);
  }

  @Benchmark
  public void fastutil_bitset(Blackhole bh) {
    FastBitSet fbs = new FastBitSet();
    for(int i : indices) {
      fbs.set(i);
    }
    bh.consume(fbs);
  }

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder()
        .include(BenchmarkBitSet.class.getSimpleName())
        .forks(1)
        .build();

    new Runner(options).run();
  }
}
