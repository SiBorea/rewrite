/*
 * Copyright 2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.kotlin.tree;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.kotlin.Assertions.kotlin;
import static org.openrewrite.kotlin.tree.ParserAsserts.isFullyParsed;

public class MethodInvocationTest implements RewriteTest {

    @Test
    void implicitFunctionCall() {
        rewriteRun(
          kotlin("""
              fun plugins(input: () -> String) {
                println( input( ) )
              }
              """,
            isFullyParsed()
          ),
          kotlin("""
              fun main() {
                plugins {
                    "test"
                }
              }
            """,
            isFullyParsed()
          )
        );
    }

    @Test
    void buildGradle() {
        rewriteRun(
          kotlin("""
              class Spec {
                var id = ""
                fun id(arg: String): Spec {
                    return this
                }
              }
            """,
            isFullyParsed()
          ),
          kotlin("""
                class SpecScope  {
                    val delegate: Spec = Spec()
                    fun id(id: String): Spec = delegate.id(id)
                }
            """,
            isFullyParsed()
          ),
          kotlin("""
                class DSL  {
                    fun setScope(block: SpecScope.() -> Unit) {
                        block(SpecScope())
                    }
                }
            """,
            isFullyParsed()
          ),
          kotlin("""
                fun method() {
                    DSL().setScope {
                        id("someId")
                    }
                }
            """,
            isFullyParsed()
          )
        );
    }

    // Temp code that contains infix function decl and function call.
    @Disabled("Requires support of Kotlin modifier `infix`. Add support for infix function calls ` version \"10\"`")
    @Test
    void buildGradle2() {
        rewriteRun(
          kotlin("""
              class Spec {
                var id = ""
                fun id(arg: String): Spec {
                    return this
                }
                fun version(version: String): Spec {
                    return this
                }
              }
            """,
            isFullyParsed()
          ),
          kotlin("""
                class SpecScope  {
                    val delegate: Spec = Spec()
                    fun id(id: String): Spec = delegate.id(id)
                }
                infix fun Spec.version(version: String): Spec = version(version)
            """,
            isFullyParsed()
          ),
          kotlin("""
                class DSL  {
                    fun setScope(block: SpecScope.() -> Unit) {
                        block(SpecScope())
                    }
                }
            """,
            isFullyParsed()
          ),
          kotlin("""
                fun method() {
                    DSL().setScope {
                        id("someId") version "10"
                    }
                }
            """,
            isFullyParsed()
          )
        );
    }

    @Test
    void methodWithLambda() {
        rewriteRun(
          kotlin("""
              fun method(arg: Any) {
              }
            """,
            isFullyParsed()
          ),
          kotlin(
            """
              fun callMethodWithLambda() {
                  method {
                  }
              }
              """,
            isFullyParsed()
          )
        );
    }

    @Test
    void methodInvocation() {
        rewriteRun(
          kotlin(
            """
              fun method(arg: Any) {
                val l = listOf(1, 2, 3)
              }
              """,
            isFullyParsed()
          )
        );
    }

    @Test
    void multipleTypesOfMethodArguments() {
        rewriteRun(
          kotlin("""
              fun methodA(a: String, b: int, c: Double) {
              }
              """,
            isFullyParsed()
          ),
          kotlin(
            """
              fun methodB() {
                methodA("a", 1, 2.0)
              }
              """,
            isFullyParsed()
          )
        );
    }
}
