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

package org.ratpackframework.internal;

import groovy.json.JsonBuilder;
import groovy.lang.Closure;
import org.ratpackframework.Request;
import org.ratpackframework.Response;
import org.ratpackframework.responder.FinalizedResponse;
import org.ratpackframework.templating.TemplateRenderer;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultResponse implements Response {

  private final Map<String, Object> headers = new LinkedHashMap<String, Object>();
  private final Request request;
  private int status = 200;

  private final TemplateRenderer templateRenderer;
  private final AsyncResultHandler<FinalizedResponse> finalHandler;

  public DefaultResponse(Request request, TemplateRenderer templateRenderer, AsyncResultHandler<FinalizedResponse> finalHandler) {
    this.request = request;
    this.templateRenderer = templateRenderer;
    this.finalHandler = finalHandler;
  }

  @Override
  public Request getRequest() {
    return this.request;
  }

  @Override
  public Map<String, ?> getHeaders() {
    return headers;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public void setStatus(int status) {
    this.status = status;
  }

  public void setContentType(String contentType) {
    headers.put(HttpHeader.CONTENT_TYPE, contentType);
  }

  @Override
  public Handler<Buffer> renderer() {
    return new Handler<Buffer>() {
      @Override
      public void handle(Buffer buffer) {
        finalHandler.handle(new AsyncResult<FinalizedResponse>(
            new FinalizedResponse(headers, status, buffer)
        ));
      }
    };
  }

  private void maybeSetContentType(String contentType) {
    if (!headers.containsKey(HttpHeader.CONTENT_TYPE)) {
      headers.put(HttpHeader.CONTENT_TYPE, contentType);
    }
  }

  public void render(String templateName) {
    render(Collections.<String, Object>emptyMap(), templateName);
  }

  public void render(Map<String, ?> model, String templateName) {
    maybeSetContentType(MimeType.TEXT_HTML);
    templateRenderer.renderFileTemplate(templateName, model, asyncErrorHandler(renderer()));
  }

  public void renderJson(final Object jsonObject) {
    maybeSetContentType(MimeType.APPLICATION_JSON);
    errorHandler(new Handler<Object>() {
      @Override
      public void handle(Object event) {
        renderString(new JsonBuilder(jsonObject).toString());
      }
    }).handle(jsonObject);
  }

  public void renderString(String str) {
    maybeSetContentType(MimeType.TEXT_PLAIN);
    renderer().handle(new Buffer(str));
  }

  public void sendRedirect(String location) {
    status = 301;
    headers.put(HttpHeader.LOCATION, location);
    renderer().handle(new Buffer());
  }

  @Override
  public <T> Handler<T> errorHandler(final Handler<T> handler) {
    return new Handler<T>() {
      @Override
      public void handle(T event) {
        try {
          handler.handle(event);
        } catch (Exception e) {
          error(e);
        }
      }
    };
  }

  @Override
  public <T> AsyncResultHandler<T> asyncErrorHandler(final Handler<T> handler) {
    return new AsyncResultHandler<T>() {
      @Override
      public void handle(AsyncResult<T> event) {
        if (event.failed()) {
          error(event.exception);
        } else {
          errorHandler(handler).handle(event.result);
        }
      }
    };
  }

  public void error(Exception e) {
    finalHandler.handle(new AsyncResult<FinalizedResponse>(e));
  }

  @Override
  public void handleErrors(Closure<?> closure) {
    try {
      closure.call();
    } catch (Exception e) {
      error(e);
    }
  }

}