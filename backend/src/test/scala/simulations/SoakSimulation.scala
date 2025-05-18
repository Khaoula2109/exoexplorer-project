package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios._
import simulations.helpers.Protocol
import scala.concurrent.duration._

/**
 * SoakSimulation - Long-Term Endurance Test
 * Applies a constant load over an extended period to detect
 * memory leaks, DB connection issues, etc.
 */
class SoakSimulation extends Simulation {

  // Base load for the main scenario
  val mainUserLoad = 20

  // Charges for secondary scenarios
  val authUserLoad = 15  // new users per second
  val exoUserLoad = 25   // users viewing exoplanets per second
  val profileUserLoad = 10 // users modifying their profile per second

  // Test duration
  val testDuration = 30.minutes

  setUp(
    // Complete user journey
    FullUserScenario.builder
      .inject(
        rampUsersPerSec(1).to(mainUserLoad).during(5.minutes), // Montée progressive
        constantUsersPerSec(mainUserLoad).during(testDuration.minus(10.minutes)),
        rampUsersPerSec(mainUserLoad).to(1).during(5.minutes) // Descente progressive
      ),

    // Authenticating new users
    AuthenticationScenario.builder
      .inject(
        nothingFor(2.minutes),
        rampUsersPerSec(1).to(authUserLoad).during(3.minutes),
        constantUsersPerSec(authUserLoad).during(testDuration.minus(8.minutes)),
        rampUsersPerSec(authUserLoad).to(1).during(3.minutes)
      ),

    // Navigating exoplanets
    ExoplanetScenario.builder
      .inject(
        nothingFor(3.minutes),
        rampUsersPerSec(1).to(exoUserLoad).during(4.minutes),
        constantUsersPerSec(exoUserLoad).during(testDuration.minus(12.minutes)),
        rampUsersPerSec(exoUserLoad).to(1).during(5.minutes)
      ),

    // Profile management
    UserProfileScenario.builder
      .inject(
        nothingFor(5.minutes),
        rampUsersPerSec(1).to(profileUserLoad).during(3.minutes),
        constantUsersPerSec(profileUserLoad).during(testDuration.minus(13.minutes)),
        rampUsersPerSec(profileUserLoad).to(1).during(5.minutes)
      ),

    // Occasional administration
    DataLoaderScenario.builder
      .inject(
        nothingFor(2.minutes),
        atOnceUsers(2),
        nothingFor(10.minutes),
        atOnceUsers(3),
        nothingFor(10.minutes),
        atOnceUsers(2)
      )
  )
    .protocols(Protocol.httpProtocol)
    .assertions(
      // Assertions to validate performance
      global.responseTime.max.lt(10000),
      global.responseTime.mean.lt(3000),
      global.successfulRequests.percent.gte(80),

      // Specific checks for the long-term test
      global.responseTime.percentile3.lt(5000),
      details("Verify OTP").failedRequests.count.is(0),
      details("GET profile").responseTime.percentile3.lt(2000)
    )
}