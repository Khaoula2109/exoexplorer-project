package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios._
import simulations.helpers.Protocol
import scala.concurrent.duration._

/**
 * AdvancedMixedSimulation - Realistic simulation mixing different user profiles
 * This simulation contains various scenarios representing real-life usage
 * of the application, with realistic proportions between different user types
 */
class AdvancedMixedSimulation extends Simulation {

  // Definition of scenarios
  val fullUserScenario = FullUserScenario.builder
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder
  val profileScenario = UserProfileScenario.builder
  val adminScenario = DataLoaderScenario.builder

  // Configuration du test
  val testDuration = 15.minutes
  val rampUpTime = 3.minutes
  val rampDownTime = 2.minutes
  val steadyStateTime = testDuration.minus(rampUpTime).minus(rampDownTime)

  setUp(
    // 1. New users registering and logging in (20%)
    authScenario
      .inject(
        rampUsersPerSec(1).to(10).during(rampUpTime),
        constantUsersPerSec(10).during(steadyStateTime),
        rampUsersPerSec(10).to(1).during(rampDownTime)
      ),

    // 2. Users browsing the exoplanet catalog (40%)
    exoScenario
      .inject(
        nothingFor(30.seconds),
        rampUsersPerSec(1).to(20).during(rampUpTime.minus(30.seconds)),
        constantUsersPerSec(20).during(steadyStateTime),
        rampUsersPerSec(20).to(1).during(rampDownTime)
      ),

    // 3. Users managing their profile and favorites (15%)
    profileScenario
      .inject(
        nothingFor(1.minute),
        rampUsersPerSec(1).to(7.5).during(rampUpTime.minus(1.minute)),
        constantUsersPerSec(7.5).during(steadyStateTime),
        rampUsersPerSec(7.5).to(1).during(rampDownTime)
      ),

    // 4. Users completing a complete journey (25%)
    fullUserScenario
      .inject(
        rampUsersPerSec(1).to(12.5).during(rampUpTime),
        constantUsersPerSec(12.5).during(steadyStateTime),
        rampUsersPerSec(12.5).to(1).during(rampDownTime)
      ),

    // 5.  Administrators performing maintenance operations (<1%)
    adminScenario
      .inject(
        nothingFor(2.minutes),
        atOnceUsers(1),
        nothingFor(3.minutes),
        atOnceUsers(2),
        nothingFor(3.minutes),
        atOnceUsers(2),
        nothingFor(3.minutes),
        atOnceUsers(1)
      )
  )
    .protocols(Protocol.httpProtocol)
    .assertions(
      // General assertions
      global.responseTime.mean.lt(3000),
      global.responseTime.percentile3.lt(2000),
      global.successfulRequests.percent.gte(80),

      // Assertions specific to critical operations
      details("Verify OTP").responseTime.percentile3.lt(5000),
      details("Login").successfulRequests.percent.gte(80),
      details("GET summary").responseTime.mean.lt(2000),
      details("Toggle favourite").responseTime.mean.lt(1000)
    )
}