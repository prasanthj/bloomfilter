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

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.LinuxPerfAsmProfiler;
import org.openjdk.jmh.profile.LinuxPerfNormProfiler;
import org.openjdk.jmh.profile.LinuxPerfProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.github.prasanthj.bloomfilter.Bloom1Filter;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BenchmarkBloom1FilterProbe {
  @Param({"10000", "10000000"})
  private int size;
  private Bloom1Filter bf;

  @Setup
  public void setup() {
    bf = new Bloom1Filter(size);
    for (int i = 0; i < size; i++) {
      bf.addLong(i);
    }
  }

  @Benchmark
  @OperationsPerInvocation(1)
  public void testProbe1Bloom1Filter() {
    for (int i = 1; i >= 0 ; i--) {
      bf.testLong(i);
    }
  }

  @Benchmark
  @OperationsPerInvocation(20000)
  public void testProbe20KBloom1Filter() {
    for (int i = 20000; i >= 0 ; i--) {
      bf.testLong(i);
    }
  }

  @Benchmark
  @OperationsPerInvocation(20000000)
  public void testProbe20MBloom1Filter() {
    for (int i = 20000000; i >= 0 ; i--) {
      bf.testLong(i);
    }
  }

  /*
   * ============================== HOW TO RUN THIS TEST: ====================================
   *
   * You can run this test:
   *
   * a) Via the command line:
   *    $ mvn clean install
   *    $ java -jar target/benchmarks.jar BenchmarkBloom1FilterProbe -prof perf     -f 1 (Linux)
   *    $ java -jar target/benchmarks.jar BenchmarkBloom1FilterProbe -prof perfnorm -f 3 (Linux)
   *    $ java -jar target/benchmarks.jar BenchmarkBloom1FilterProbe -prof perfasm  -f 1 (Linux)
s   */
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
      .include(BenchmarkBloom1FilterProbe.class.getSimpleName())
      .addProfiler(LinuxPerfProfiler.class)
      .addProfiler(LinuxPerfNormProfiler.class)
      .addProfiler(LinuxPerfAsmProfiler.class)
      .build();

    new Runner(opt).run();
  }
}