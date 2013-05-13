/*
 * Copyright 2013 the original author or authors.
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

package org.ratpackframework.groovy.bootstrap;

import org.ratpackframework.groovy.RatpackScriptApp;

import java.io.File;

public class RatpackMain {

  public static void main(String[] args) throws Exception {
    File ratpackFile = args.length == 0 ? new File("ratpack.groovy") : new File(args[0]);
    if (!ratpackFile.exists()) {
      System.err.println("Ratpack file " + ratpackFile.getAbsolutePath() + " does not exist");
      System.exit(1);
    }


    RatpackScriptApp.ratpack(ratpackFile).startAndWait();
  }

}