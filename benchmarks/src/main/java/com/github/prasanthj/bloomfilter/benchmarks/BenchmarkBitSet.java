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
package com.github.prasanthj.bloomfilter.benchmarks;

import com.github.prasanthj.bloomfilter.BloomFilter;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.LinuxPerfAsmProfiler;
import org.openjdk.jmh.profile.LinuxPerfNormProfiler;
import org.openjdk.jmh.profile.LinuxPerfProfiler;
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
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BenchmarkBitSet {
  @Param({"10000"})
  public int size;
  private int[] indices;
  private BloomFilter.BitSet cbs;
  private java.util.BitSet jbs;
  private RoaringBitmap rbs;
  private FastBitSet fbs;

  @Setup
  public void setup() {
    Random rand = new Random(123);
    indices = new int[size];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = rand.nextInt(size);
    }
    cbs = new BloomFilter.BitSet(size);
    jbs = new java.util.BitSet(size);
    rbs = new RoaringBitmap();
    fbs = new FastBitSet();
  }

  @Benchmark
  public void customBitset() {
    for (int i : indices) {
      cbs.set(i);
    }
  }

  @Benchmark
  public void javaBitset() {
    for (int i : indices) {
      jbs.set(i);
    }
  }

  @Benchmark
  public void roaringBitset() {
    for (int i : indices) {
      rbs.add(i);
    }
  }

  @Benchmark
  public void fastutilBitset() {
    for (int i : indices) {
      fbs.set(i);
    }
  }

  /*
   * ============================== HOW TO RUN THIS TEST: ====================================
   *
   * You can run this test:
   *
   * a) Via the command line:
   *    $ mvn clean install
   *    $ java -jar target/benchmarks.jar BenchmarkBitSet -prof perf     -f 1 (Linux)
   *    $ java -jar target/benchmarks.jar BenchmarkBitSet -prof perfnorm -f 3 (Linux)
   *    $ java -jar target/benchmarks.jar BenchmarkBitSet -prof perfasm  -f 1 (Linux)
   *    $ java -jar target/benchmarks.jar BenchmarkBitSet -prof gc  -f 1 (allocation counting via gc)
   */
  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder()
      .include(BenchmarkBitSet.class.getSimpleName())
      .addProfiler(LinuxPerfProfiler.class)
      .addProfiler(LinuxPerfNormProfiler.class)
      .addProfiler(LinuxPerfAsmProfiler.class)
      .forks(1)
      .build();

    new Runner(options).run();
  }
}
