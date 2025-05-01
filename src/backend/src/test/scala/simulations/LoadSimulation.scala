package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios.{AuthenticationScenario, DataLoaderScenario, ExoplanetScenario, FullUserScenario, UserProfileScenario}
import simulations.helpers.Protocol

import scala.concurrent.duration._

/**
 * LoadSimulation - Test de charge normal
 * Simule une utilisation réaliste de l'application avec une charge croissante
 * pour évaluer la performance sous une charge normale élevée
 */
class LoadSimulation extends Simulation {

  // Définition des scénarios
  val fullUserScenario = FullUserScenario.builder
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder
  val profileScenario = UserProfileScenario.builder
  val adminScenario = DataLoaderScenario.builder

  // Simulation d'un nombre croissant d'utilisateurs
  setUp(
    // Scénario complet - utilisateurs principaux
    fullUserScenario
      .inject(
        rampUsers(500).during(5.minutes) // Nombre d'utilisateurs rampants pendant 5 minutes
      ),

    // Authentification seule - nouveaux utilisateurs
    authScenario
      .inject(
        nothingFor(30.seconds),
        rampUsers(300).during(4.minutes)
      ),

    // Navigation exoplanètes - utilisateurs qui explorent
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

      // Vérifications spécifiques par type d'opération
      details("Toggle favourite").responseTime.percentile3.lt(2000),
      details("GET exoplanet details").responseTime.mean.lt(1000),
      details("GET summary").successfulRequests.percent.is(80)
    )
}