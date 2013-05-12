package org.ratpackframework.test.groovy

import org.ratpackframework.groovy.templating.TemplateRenderer

class BasicGroovyDslSpec extends RatpackGroovyDslSpec {

  def "can use special Groovy dsl"() {
    given:
    file("public/foo.txt") << "bar"

    when:
    app {
      routing {
        get("a") {
          response.send "a handler"
        }
        post("b") {
          response.send "b handler"
        }
        path(":first") {
          get(":second") {
            response.send allPathTokens.toString()
          }
          handler("c/:second", ["get", "post"] as List) {
            response.send allPathTokens.toString()
          }
        }
        assets("public")
      }
    }

    then:
    urlGetText("a") == "a handler"
    urlGetConnection("b").responseCode == 415
    urlPostText("b") == "b handler"
    urlGetText("1/2") == "[first:1, second:2]"
    urlGetText("foo/c/bar") == "[first:foo, second:bar]"
    urlGetText("foo.txt") == "bar"
  }

  def "can use file method to access file contextual"() {
    given:
    file("foo/file.txt") << "foo"
    file("bar/file.txt") << "bar"

    when:
    app {
      routing {
        fsContext("foo") {
          get("foo") {
            response.send file("file.txt").text
          }
        }
        fsContext("bar") {
          get("bar") {
            response.send file("file.txt").text
          }
        }
      }
    }

    then:
    urlGetText("foo") == 'foo'
    urlGetText("bar") == 'bar'
  }
}