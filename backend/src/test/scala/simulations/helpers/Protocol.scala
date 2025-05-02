package simulations.helpers

import io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.core.config.GatlingConfiguration

object Protocol {

  def httpProtocol(implicit configuration: GatlingConfiguration) = {
    val base = sys.props.getOrElse("baseUrl", "http://localhost:8080")

    http
      .baseUrl(base)
      .acceptHeader("application/json")
      .contentTypeHeader("application/json")
  }
}
