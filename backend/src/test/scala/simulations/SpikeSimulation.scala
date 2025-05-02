package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios._
import simulations.helpers.Protocol
import scala.concurrent.duration._

/**
 * SpikeSimulation - Test de pics de charge soudains
 * Simule des pics de trafic importants et soudains pour tester
 * la capacité de l'application à gérer des montées en charge extrêmes
 */
class SpikeSimulation extends Simulation {

  // Configuration des scénarios
  val fullUserScenario = FullUserScenario.builder
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder

  setUp(
    // Pic soudain de nouveaux utilisateurs (inscriptions + connexions)
    authScenario
      .inject(
        // Phase d'échauffement
        rampUsers(100).during(1.minute),
        // Premier pic
        nothingFor(30.seconds),
        atOnceUsers(800),
        // Retour à la normale
        nothingFor(1.minute),
        // Deuxième pic encore plus élevé
        atOnceUsers(1200)
      ),

    // Pic de navigation dans les exoplanètes
    exoScenario
      .inject(
        nothingFor(2.minutes), // Attendre que les utilisateurs se connectent
        atOnceUsers(1000),
        nothingFor(1.minute),
        atOnceUsers(1500)
      ),

    // Pic d'utilisateurs complets (parcours entier)
    fullUserScenario
      .inject(
        nothingFor(3.minutes),
        atOnceUsers(500)
      )
  )
    .protocols(Protocol.httpProtocol)
    .assertions(
      // Assertions adaptées aux pics de charge
      global.responseTime.max.lt(15000),  // Tolérance plus élevée pendant les pics
      global.responseTime.percentile3.lt(8000), // 75% des requêtes sous 8 secondes
      global.successfulRequests.percent.gte(80),  // Accepter 20% d'échecs pendant les pics

      // S'assurer que les opérations critiques restent fonctionnelles
      details("Login").successfulRequests.percent.gte(80),
      details("Verify OTP").successfulRequests.percent.gte(80)
    )
}