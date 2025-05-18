package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios._
import simulations.helpers.Protocol
import scala.concurrent.duration._

/**
 * SpikeSimulation - Sudden Load Spike Test
 * Simulates sudden, large traffic spikes to test
 * the application's ability to handle extreme load surges
 */
class SpikeSimulation extends Simulation {

  // Configuring scenarios
  val fullUserScenario = FullUserScenario.builder
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder

  setUp(
    // Sudden spike in new users (registrations + logins)
    authScenario
      .inject(
        // Warm-up phase
        rampUsers(100).during(1.minute),
        // First peak
        nothingFor(30.seconds),
        atOnceUsers(800),
        // Back to normal
        nothingFor(1.minute),
        // Second peak even higher
        atOnceUsers(1200)
      ),

    // Peak of navigation in exoplanets
    exoScenario
      .inject(
        nothingFor(2.minutes), // Wait for users to log in
        atOnceUsers(1000),
        nothingFor(1.minute),
        atOnceUsers(1500)
      ),

    // Peak number of complete users (entire journey)
    fullUserScenario
      .inject(
        nothingFor(3.minutes),
        atOnceUsers(500)
      )
  )
    .protocols(Protocol.httpProtocol)
    .assertions(
      // Assertions adapted to load peaks
      global.responseTime.max.lt(15000),  // Higher tolerance during peaks
      global.responseTime.percentile3.lt(8000),
      global.successfulRequests.percent.gte(80),

      // Ensure critical operations remain functional
      details("Login").successfulRequests.percent.gte(80),
      details("Verify OTP").successfulRequests.percent.gte(80)
    )
}