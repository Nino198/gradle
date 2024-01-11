/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.restricteddsl.project

import com.h0tk3y.kotlin.staticObjectNotation.Adding
import com.h0tk3y.kotlin.staticObjectNotation.Configuring
import com.h0tk3y.kotlin.staticObjectNotation.Restricted
import org.gradle.api.Action
import org.gradle.api.artifacts.ProjectDependency


internal
interface ProjectTopLevelReceiver {
    @Restricted
    val dependencies: RestrictedDependenciesHandler

    @Configuring
    fun dependencies(configure: Action<in RestrictedDependenciesHandler>)

    @Restricted
    fun project(path: String): ProjectDependency
}


interface RestrictedDependenciesHandler {
    @Adding
    fun api(dependency: ProjectDependency)

    @Adding
    fun compileOnly(dependency: ProjectDependency)

    @Adding
    fun implementation(dependency: ProjectDependency)

    @Adding
    fun testImplementation(dependency: ProjectDependency)

    @Adding
    fun androidTestImplementation(dependency: ProjectDependency)
}
