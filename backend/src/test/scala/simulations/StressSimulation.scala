package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios._
import simulations.helpers.Protocol
import scala.concurrent.duration._

/**
 * StressSimulation - Load Limit Test
 * Gradually increases the load until the application's breaking point is reached
 * or until the maximum expected load is reached
 */
class StressSimulation extends Simulation {

  // Configuring scalability
  val rampUpTime = 3.minutes
  val targetUsers = 2000

  val fullUserScenario = FullUserScenario.builder
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder
  val profileScenario = UserProfileScenario.builder

  setUp(
    // Gradual ramp-up of the main scenario
    fullUserScenario
      .inject(
        rampUsers(targetUsers / 2).during(rampUpTime)
      ),

    // Gradual scaling of authentications
    authScenario
      .inject(
        nothingFor(30.seconds),
        rampUsers(targetUsers / 4).during(rampUpTime.minus(30.seconds))
      ),

    // Gradual increase in exoplanet consultations
    exoScenario
      .inject(
        nothingFor(1.minute),
        rampUsers(targetUsers / 3).during(rampUpTime.minus(1.minute))
      ),

    // Gradual scaling of profile changes
    profileScenario
      .inject(
        nothingFor(1.5.minutes),
        rampUsers(targetUsers / 5).during(rampUpTime.minus(1.5.minutes))
      )
  )
    .protocols(Protocol.httpProtocol)
    .assertions(
      // Assertions suitable for stress testing
      global.responseTime.max.lt(10000),
      global.responseTime.mean.lt(5000),
      global.successfulRequests.percent.gte(80),

      // Specific checks for critical operations
      details("Login").responseTime.percentile3.lt(5000),
      details("GET summary").responseTime.percentile3.lt(4000),
      details("Verify OTP").successfulRequests.percent.gte(80)
    )

    // Configuration to monitor the evolution of response times
    .throttle(
      reachRps(100).in(1.minute),
      holdFor(30.seconds),
      reachRps(200).in(1.minute),
      holdFor(30.seconds),
      reachRps(300).in(1.minute)
    )
}