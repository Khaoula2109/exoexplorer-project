package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.helpers.Protocol
import simulations.scenarios._
import scala.concurrent.duration._

class ExoExplorerSimulation extends Simulation {

  // Configuration HTTP globale
  val httpProtocol = Protocol.httpProtocol

  // Définition des différents scénarios d'utilisateur
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder
  val userScenario = UserProfileScenario.builder
  val adminScenario = DataLoaderScenario.builder
  val fullJourneyScenario = FullUserScenario.builder

  // Définition de la simulation
  setUp(
    // Scénarios standards des utilisateurs
    authScenario.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(5).during(1.minute)
    ),

    // Après authentification, les utilisateurs naviguent parmi les exoplanetes
    exoScenario.inject(
      nothingFor(10.seconds), // Laisse le temps aux authentifications de se réaliser
      rampUsers(40).during(20.seconds),
      constantUsersPerSec(10).during(1.minute)
    ),

    // Certains utilisateurs gèrent leur profil
    userScenario.inject(
      nothingFor(15.seconds),
      rampUsers(30).during(30.seconds),
      constantUsersPerSec(3).during(1.minute)
    ),

    // Une minorité d'admins effectue des opérations de maintenance
    adminScenario.inject(
      nothingFor(5.seconds),
      atOnceUsers(2),
      nothingFor(30.seconds),
      atOnceUsers(1)
    ),

    // Parcours complet d'un utilisateur
    fullJourneyScenario.inject(
      constantUsersPerSec(2).during(2.minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      // Assertions globales
      global.responseTime.mean.lt(500),
      global.successfulRequests.percent.gt(95),

      // Assertions spécifiques
      details("Verify OTP").successfulRequests.percent.is(100),
      details("GET habitable exoplanets").responseTime.percentile3.lt(800)
    )
}