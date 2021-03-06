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

package ratpack;

import org.ratpackframework.handling.Chain;
import org.ratpackframework.handling.Exchange;
import org.ratpackframework.handling.Handler;
import org.ratpackframework.handling.Handlers;
import org.ratpackframework.util.Action;

public class Routing implements Action<Chain> {

  public void execute(Chain chain) {
    chain.add(Handlers.get("foo/bar", new Handler() {
      public void handle(Exchange exchange) {
        exchange.getResponse().send(exchange.getRequest().getUri());
      }
    }));
  }
}
