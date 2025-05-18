package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios.{AuthenticationScenario, DataLoaderScenario, ExoplanetScenario, FullUserScenario, UserProfileScenario}
import simulations.helpers.Protocol

import scala.concurrent.duration._

/**
 * LoadSimulation - Normal Load Test
 * Simulates realistic application usage with increasing load
 * to evaluate performance under normal high load
 */
class LoadSimulation extends Simulation {

  // Definition of scenarios
  val fullUserScenario = FullUserScenario.builder
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder
  val profileScenario = UserProfileScenario.builder
  val adminScenario = DataLoaderScenario.builder

  // Simulation of an increasing number of users
  setUp(
    // Complete scenario - main users
    fullUserScenario
      .inject(
        rampUsers(500).during(5.minutes) // Number of crawling users for 5 minutes
      ),

    // Authentication only - new users
    authScenario
      .inject(
        nothingFor(30.seconds),
        rampUsers(300).during(4.minutes)
      ),

    // Exoplanet Navigation - Users Exploring
    exoScenario
      .inject(
        nothingFor(1.minute),
        rampUsers(400).during(4.minutes)
      ),

    // Gestion profil - utilisateurs gérant leurs préférences
    profileScenario
      .inject(
        nothingFor(1.5.minutes),
        rampUsers(200).during(3.minutes)
      ),

    // Quelques admins pour les opérations de maintenance
    adminScenario
      .inject(
        nothingFor(1.minute),
        atOnceUsers(1),
        nothingFor(1.minute),
        atOnceUsers(2),
        nothingFor(2.minutes),
        atOnceUsers(2)
      )
  )
    .protocols(Protocol.httpProtocol)
    .assertions(
      // Assertion pour valider la performance
      global.responseTime.max.lt(5000),
      global.responseTime.mean.lt(2000),
      global.successfulRequests.percent.gte(80),

      // Specific checks by type of operation
      details("Toggle favourite").responseTime.percentile3.lt(2000),
      details("GET exoplanet details").responseTime.mean.lt(1000),
      details("GET summary").successfulRequests.percent.is(80)
    )
}