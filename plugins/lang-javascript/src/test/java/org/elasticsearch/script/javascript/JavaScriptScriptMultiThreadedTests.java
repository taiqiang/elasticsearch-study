/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.script.javascript;

import org.elasticsearch.common.Randomness;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.script.CompiledScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.test.ESTestCase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.equalTo;

public class JavaScriptScriptMultiThreadedTests extends ESTestCase {
    public void testExecutableNoRuntimeParams() throws Exception {
        final JavaScriptScriptEngineService se = new JavaScriptScriptEngineService(Settings.Builder.EMPTY_SETTINGS);
        final Object compiled = se.compile(null, "x + y", Collections.emptyMap());
        final AtomicBoolean failed = new AtomicBoolean();

        Thread[] threads = new Thread[between(3, 12)];
        final CountDownLatch latch = new CountDownLatch(threads.length);
        final CyclicBarrier barrier = new CyclicBarrier(threads.length + 1);
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        long x = Randomness.get().nextInt();
                        long y = Randomness.get().nextInt();
                        long addition = x + y;
                        Map<String, Object> vars = new HashMap<String, Object>();
                        vars.put("x", x);
                        vars.put("y", y);
                        ExecutableScript script = se.executable(new CompiledScript(ScriptType.INLINE, "testExecutableNoRuntimeParams", "js", compiled), vars);
                        for (int i = 0; i < between(100, 1000); i++) {
                            long result = ((Number) script.run()).longValue();
                            assertThat(result, equalTo(addition));
                        }
                    } catch (Exception e) {
                        failed.set(true);
                        logger.error("failed", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        barrier.await();
        latch.await();
        assertThat(failed.get(), equalTo(false));
        assertWarnings("[javascript] scripts are deprecated, use [painless] scripts instead");
    }

    public void testExecutableWithRuntimeParams() throws Exception {
        final JavaScriptScriptEngineService se = new JavaScriptScriptEngineService(Settings.Builder.EMPTY_SETTINGS);
        final Object compiled = se.compile(null, "x + y", Collections.emptyMap());
        final AtomicBoolean failed = new AtomicBoolean();

        Thread[] threads = new Thread[between(3, 12)];
        final CountDownLatch latch = new CountDownLatch(threads.length);
        final CyclicBarrier barrier = new CyclicBarrier(threads.length + 1);
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        long x = Randomness.get().nextInt();
                        Map<String, Object> vars = new HashMap<String, Object>();
                        vars.put("x", x);
                        ExecutableScript script = se.executable(new CompiledScript(ScriptType.INLINE, "testExecutableNoRuntimeParams", "js", compiled), vars);
                        for (int i = 0; i < between(100, 1000); i++) {
                            long y = Randomness.get().nextInt();
                            long addition = x + y;
                            script.setNextVar("y", y);
                            long result = ((Number) script.run()).longValue();
                            assertThat(result, equalTo(addition));
                        }
                    } catch (Exception e) {
                        failed.set(true);
                        logger.error("failed", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        barrier.await();
        latch.await();
        assertThat(failed.get(), equalTo(false));
        assertWarnings("[javascript] scripts are deprecated, use [painless] scripts instead");
    }

    public void testExecute() throws Exception {
        final JavaScriptScriptEngineService se = new JavaScriptScriptEngineService(Settings.Builder.EMPTY_SETTINGS);
        final Object compiled = se.compile(null, "x + y", Collections.emptyMap());
        final AtomicBoolean failed = new AtomicBoolean();

        Thread[] threads = new Thread[between(3, 12)];
        final CountDownLatch latch = new CountDownLatch(threads.length);
        final CyclicBarrier barrier = new CyclicBarrier(threads.length + 1);
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        Map<String, Object> runtimeVars = new HashMap<String, Object>();
                        for (int i = 0; i < between(100, 1000); i++) {
                            long x = Randomness.get().nextInt();
                            long y = Randomness.get().nextInt();
                            long addition = x + y;
                            runtimeVars.put("x", x);
                            runtimeVars.put("y", y);
                            long result = ((Number) se.executable(new CompiledScript(ScriptType.INLINE, "testExecutableNoRuntimeParams", "js", compiled), runtimeVars).run()).longValue();
                            assertThat(result, equalTo(addition));
                        }
                    } catch (Exception e) {
                        failed.set(true);
                        logger.error("failed", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        barrier.await();
        latch.await();
        assertThat(failed.get(), equalTo(false));
        assertWarnings("[javascript] scripts are deprecated, use [painless] scripts instead");
    }
}
