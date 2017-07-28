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

import com.github.prasanthj.bloomfilter.Bloom1Filter;
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

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BenchmarkBloomFilter {
  @Param({"10000"})
  private int numEntries;
  private BloomFilter bf;
  private Bloom1Filter bf1;
  private int[] inp;
  private Random rand;

  @Setup
  public void setup() {
    bf = new BloomFilter(numEntries);
    bf1 = new Bloom1Filter(numEntries);
    inp = new int[numEntries];
    rand = new Random(123);
    for (int i = 0; i < numEntries; i++) {
      inp[i] = rand.nextInt(numEntries);
    }
  }

  @Benchmark
  public void bloomFilterAddLong() {
    for (int i : inp) {
      bf.addLong(i);
    }
  }

  @Benchmark
  public void bloomFilterTestLong() {
    for (int i : inp) {
      bf.testLong(i);
    }
  }

  @Benchmark
  public void bloom1FilterAddLong() {
    for (int i : inp) {
      bf1.addLong(i);
    }
  }

  @Benchmark
  public void bloom1FilterTestLong() {
    for (int i : inp) {
      bf1.testLong(i);
    }
  }

   /*
   * ============================== HOW TO RUN THIS TEST: ====================================
   *
   * You can run this test:
   *
   * a) Via the command line:
   *    $ mvn clean install
   *    $ java -jar target/benchmarks.jar BenchmarkBloomFilter -prof perf     -f 1 (Linux)
   *    $ java -jar target/benchmarks.jar BenchmarkBloomFilter -prof perfnorm -f 3 (Linux)
   *    $ java -jar target/benchmarks.jar BenchmarkBloomFilter -prof perfasm  -f 1 (Linux)
   *    $ java -jar target/benchmarks.jar BenchmarkBloomFilter -prof gc  -f 1 (allocation counting via gc)
   */

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder()
      .include(BenchmarkBloomFilter.class.getSimpleName())
      .addProfiler(LinuxPerfProfiler.class)
      .addProfiler(LinuxPerfNormProfiler.class)
      .addProfiler(LinuxPerfAsmProfiler.class)
      .forks(1)
      .build();

    new Runner(options).run();
  }
}