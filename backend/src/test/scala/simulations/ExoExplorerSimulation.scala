package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.helpers.Protocol
import simulations.scenarios._
import scala.concurrent.duration._

class ExoExplorerSimulation extends Simulation {

  // Global HTTP configuration
  val httpProtocol = Protocol.httpProtocol

  // Defining different user scenarios
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder
  val userScenario = UserProfileScenario.builder
  val adminScenario = DataLoaderScenario.builder
  val fullJourneyScenario = FullUserScenario.builder

  // Definition of simulation
  setUp(
    // Standard user scenarios
    authScenario.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(5).during(1.minute)
    ),

    // After authentication, users navigate among exoplanets
    exoScenario.inject(
      nothingFor(10.seconds), // Allow time for authentication to complete
      rampUsers(40).during(20.seconds),
      constantUsersPerSec(10).during(1.minute)
    ),

    // Some users manage their profile
    userScenario.inject(
      nothingFor(15.seconds),
      rampUsers(30).during(30.seconds),
      constantUsersPerSec(3).during(1.minute)
    ),

    // A minority of admins perform maintenance operations
    adminScenario.inject(
      nothingFor(5.seconds),
      atOnceUsers(2),
      nothingFor(30.seconds),
      atOnceUsers(1)
    ),

    // Complete user journey
    fullJourneyScenario.inject(
      constantUsersPerSec(2).during(2.minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      // Global assertions
      global.responseTime.mean.lt(500),
      global.successfulRequests.percent.gt(95),

      // Specific assertions
      details("Verify OTP").successfulRequests.percent.is(100),
      details("GET habitable exoplanets").responseTime.percentile3.lt(800)
    )
}