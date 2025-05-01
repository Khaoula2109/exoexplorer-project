package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios._
import simulations.helpers.Protocol
import scala.concurrent.duration._

/**
 * SoakSimulation - Test d'endurance sur longue durée
 * Applique une charge constante sur une période prolongée pour détecter
 * les fuites de mémoire, problèmes de connexion DB, etc.
 */
class SoakSimulation extends Simulation {

  // Charge de base pour le scénario principal
  val mainUserLoad = 20 // utilisateurs par seconde

  // Charges pour les scénarios secondaires
  val authUserLoad = 15  // nouveaux utilisateurs par seconde
  val exoUserLoad = 25   // utilisateurs consultant les exoplanètes par seconde
  val profileUserLoad = 10 // utilisateurs modifiant leur profil par seconde

  // Durée du test
  val testDuration = 30.minutes

  setUp(
    // Parcours complet d'utilisateurs
    FullUserScenario.builder
      .inject(
        rampUsersPerSec(1).to(mainUserLoad).during(5.minutes), // Montée progressive
        constantUsersPerSec(mainUserLoad).during(testDuration.minus(10.minutes)),
        rampUsersPerSec(mainUserLoad).to(1).during(5.minutes) // Descente progressive
      ),

    // Authentification de nouveaux utilisateurs
    AuthenticationScenario.builder
      .inject(
        nothingFor(2.minutes),
        rampUsersPerSec(1).to(authUserLoad).during(3.minutes),
        constantUsersPerSec(authUserLoad).during(testDuration.minus(8.minutes)),
        rampUsersPerSec(authUserLoad).to(1).during(3.minutes)
      ),

    // Navigation dans les exoplanètes
    ExoplanetScenario.builder
      .inject(
        nothingFor(3.minutes),
        rampUsersPerSec(1).to(exoUserLoad).during(4.minutes),
        constantUsersPerSec(exoUserLoad).during(testDuration.minus(12.minutes)),
        rampUsersPerSec(exoUserLoad).to(1).during(5.minutes)
      ),

    // Gestion de profil
    UserProfileScenario.builder
      .inject(
        nothingFor(5.minutes),
        rampUsersPerSec(1).to(profileUserLoad).during(3.minutes),
        constantUsersPerSec(profileUserLoad).during(testDuration.minus(13.minutes)),
        rampUsersPerSec(profileUserLoad).to(1).during(5.minutes)
      ),

    // Administration occasionnelle
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
      // Assertions pour valider la performance
      global.responseTime.max.lt(10000),  // Le temps de réponse max ne doit pas dépasser 10 secondes
      global.responseTime.mean.lt(3000),  // Le temps moyen ne doit pas dépasser 3 secondes
      global.successfulRequests.percent.gte(80),  // Le pourcentage des requêtes réussies doit être supérieur à 95%

      // Vérifications spécifiques pour le test de longue durée
      global.responseTime.percentile3.lt(5000),
      details("Verify OTP").failedRequests.count.is(0),
      details("GET profile").responseTime.percentile3.lt(2000)
    )
}