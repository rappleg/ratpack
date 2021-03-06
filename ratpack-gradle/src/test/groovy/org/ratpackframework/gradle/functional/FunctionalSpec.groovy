/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ratpackframework.gradle.functional

import org.gradle.GradleLauncher
import org.gradle.StartParameter
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.ratpackframework.gradle.RatpackGroovyPlugin
import spock.lang.Specification
import org.ratpackframework.gradle.RatpackPlugin
import org.gradle.BuildResult
import org.gradle.testfixtures.ProjectBuilder

abstract class FunctionalSpec extends Specification {
  @Rule TemporaryFolder dir

  static class ExecutedTask {
    Task task
    TaskState state
  }

  List<ExecutedTask> executedTasks = []

  GradleLauncher launcher(String... args) {
    StartParameter startParameter = GradleLauncher.createStartParameter(args)
    startParameter.setProjectDir(dir.root)
    GradleLauncher launcher = GradleLauncher.newInstance(startParameter)
    executedTasks.clear()
    launcher.addListener(new TaskExecutionListener() {
      void beforeExecute(Task task) {
        getExecutedTasks() << new ExecutedTask(task: task)
      }

      void afterExecute(Task task, TaskState taskState) {
        getExecutedTasks().last().state = taskState
        taskState.metaClass.upToDate = taskState.skipMessage == "UP-TO-DATE"
      }
    })
    launcher
  }

  BuildResult run(String... args) {
    def launcher = launcher(*args)
    def result = launcher.run()
    result.rethrowFailure()
    result
  }

  File getBuildFile() {
    makeFile("build.gradle")
  }

  File makeFile(String path) {
    def f = file(path)
    if (!f.exists()) {
      def parts = path.split("/")
      if (parts.size() > 1) {
        dir.newFolder(*parts[0..-2])
      }
      dir.newFile(path)
    }
    f
  }

  File file(String path) {
    def file = new File(dir.root, path)
    assert file.parentFile.mkdirs() || file.parentFile.exists()
    file
  }
  
  ExecutedTask task(String name) {
    executedTasks.find { it.task.name == name }
  }

  def setup() {
    file("settings.gradle") << "rootProject.name = 'test-app'"
    buildFile << """
      ext.RatpackGroovyPlugin = project.class.classLoader.loadClass('${RatpackGroovyPlugin.name}')
      apply plugin: RatpackGroovyPlugin
      archivesBaseName = "functional-test"
      version = "1.0"
      repositories {
        maven { url "file://${localRepo.absolutePath}" }
        mavenCentral()
      }
    """
  }

  def unzip(File source, File destination) {
    def project = ProjectBuilder.builder().withProjectDir(dir.root).build()
    project.copy {
      from project.zipTree(source)
      into destination
    }
  }
  
  File getWarFile() {
    def f = file("build/libs/functional-test-1.0.war")
    assert f.exists()
    f
  }
  
  def unpackedWarFile(path) {
    def unpacked = file("build/unpacked-war")
    if (!unpacked.exists()) {
      unzip(warFile, unpacked)
    }

    new File(unpacked, path)
  }

  File getLocalRepo() {
    def rootRelative = new File("build/localrepo")
    rootRelative.directory ? rootRelative : new File(new File(System.getProperty("user.dir")).parentFile, "build/localrepo")
  }
}