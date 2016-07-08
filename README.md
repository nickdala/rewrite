# Distributing Java Source Refactoring

![Support Status](https://img.shields.io/badge/nebula-incubating-yellow.svg)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/nebula-plugins/java-source-refactor?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Apache 2.0](https://img.shields.io/github/license/nebula-plugins/java-source-refactor.svg)](http://www.apache.org/licenses/LICENSE-2.0)

## Purpose

The Java Source Refactoring plugin is a pluggable and distributed refactoring tool for Java source code.

## Usage

Clone and build with:

    ./gradlew publishToMavenLocal

To apply this plugin:

    buildscript {
        repositories { mavenLocal() }
        dependencies {
            classpath 'com.netflix.nebula:java-source-refactor:latest.release'
        }

        configurations.classpath.resolutionStrategy.cacheDynamicVersionsFor 0, 'minutes'
    }

    apply plugin: 'nebula.source-refactor'

# License

Copyright 2015-2016 Netflix, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.