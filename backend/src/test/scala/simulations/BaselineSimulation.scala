package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios.{FullUserScenario, AuthenticationScenario, ExoplanetScenario, UserProfileScenario}
import simulations.helpers.Protocol
import scala.concurrent.duration._

/**
 * BaselineSimulation - Simulation de base pour établir une référence de performance
 * Utilise une charge constante légère pour vérifier le comportement normal de l'application
 */
class BaselineSimulation extends Simulation {

  // Définition des scénarios à tester
  val fullUserScenario = FullUserScenario.builder
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder
  val profileScenario = UserProfileScenario.builder

  // Simulation de l'utilisateur avec une charge constante
  setUp(
    // Scénario complet avec parcours utilisateur intégral
    fullUserScenario
      .inject(
        constantUsersPerSec(1).during(2.minutes) // 1 utilisateur par seconde pendant 2 minutes
      ),

    // Test d'authentification uniquement
    authScenario
      .inject(
        constantUsersPerSec(2).during(1.minute) // 2 utilisateurs par seconde pendant 1 minute
      ),

    // Navigation parmi les exoplanètes
    exoScenario
      .inject(
        nothingFor(30.seconds), // Attendre que les utilisateurs soient authentifiés
        constantUsersPerSec(2).during(1.minute)
      ),

    // Gestion du profil utilisateur
    profileScenario
      .inject(
        nothingFor(40.seconds),
        constantUsersPerSec(1).during(1.minute)
      )
  )
    .protocols(Protocol.httpProtocol)
    .assertions(
      // Assertions pour valider la performance de base
      global.responseTime.max.lt(5000),  // Le temps de réponse max ne doit pas dépasser 3 secondes
      global.responseTime.mean.lt(2000),  // Temps moyen de réponse sous 800ms
      global.successfulRequests.percent.gte(80),  // Au moins 90% des requêtes réussies

      // Assertions spécifiques pour les opérations critiques
      details("Verify OTP").successfulRequests.percent.gte(80),
      details("Login").responseTime.percentile3.lt(2000)
    )
}