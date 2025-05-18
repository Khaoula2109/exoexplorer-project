package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios.{FullUserScenario, AuthenticationScenario, ExoplanetScenario, UserProfileScenario}
import simulations.helpers.Protocol
import scala.concurrent.duration._

/**
 * BaselineSimulation - Baseline simulation to establish a performance baseline
 * Uses a light constant load to verify normal application behavior
 */
class BaselineSimulation extends Simulation {

  // Definition of scenarios to be tested
  val fullUserScenario = FullUserScenario.builder
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder
  val profileScenario = UserProfileScenario.builder

  // User simulation with constant load
  setUp(
    // Complete scenario with full user journey
    fullUserScenario
      .inject(
        constantUsersPerSec(1).during(2.minutes) // 1 user per second for 2 minutes
      ),

    // Authentication test only
    authScenario
      .inject(
        constantUsersPerSec(2).during(1.minute) // 2 users per second for 1 minute
      ),

    // Navigating among exoplanets
    exoScenario
      .inject(
        nothingFor(30.seconds), // Wait for users to be authenticated
        constantUsersPerSec(2).during(1.minute)
      ),

    // User profile management
    profileScenario
      .inject(
        nothingFor(40.seconds),
        constantUsersPerSec(1).during(1.minute)
      )
  )
    .protocols(Protocol.httpProtocol)
    .assertions(
      // Assertions to validate basic performance
      global.responseTime.max.lt(5000),  // Maximum response
      global.responseTime.mean.lt(2000),  //Average response
      global.successfulRequests.percent.gte(80),  // Successful queries

      // Specific assertions for critical operations
      details("Verify OTP").successfulRequests.percent.gte(80),
      details("Login").responseTime.percentile3.lt(2000)
    )
}