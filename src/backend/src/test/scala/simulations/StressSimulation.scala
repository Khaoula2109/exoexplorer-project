package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios._
import simulations.helpers.Protocol
import scala.concurrent.duration._

/**
 * StressSimulation - Test de limite de charge
 * Augmente progressivement la charge jusqu'à trouver le point de rupture
 * de l'application ou jusqu'à atteindre la charge maximale prévue
 */
class StressSimulation extends Simulation {

  // Configuration de la montée en charge
  val rampUpTime = 3.minutes
  val targetUsers = 2000

  val fullUserScenario = FullUserScenario.builder
  val authScenario = AuthenticationScenario.builder
  val exoScenario = ExoplanetScenario.builder
  val profileScenario = UserProfileScenario.builder

  setUp(
    // Montée en charge progressive du scénario principal
    fullUserScenario
      .inject(
        rampUsers(targetUsers / 2).during(rampUpTime)
      ),

    // Montée en charge progressive des authentifications
    authScenario
      .inject(
        nothingFor(30.seconds),
        rampUsers(targetUsers / 4).during(rampUpTime.minus(30.seconds))
      ),

    // Montée en charge progressive des consultations d'exoplanètes
    exoScenario
      .inject(
        nothingFor(1.minute),
        rampUsers(targetUsers / 3).during(rampUpTime.minus(1.minute))
      ),

    // Montée en charge progressive des modifications de profil
    profileScenario
      .inject(
        nothingFor(1.5.minutes),
        rampUsers(targetUsers / 5).during(rampUpTime.minus(1.5.minutes))
      )
  )
    .protocols(Protocol.httpProtocol)
    .assertions(
      // Assertions adaptées au test de stress
      global.responseTime.max.lt(10000),  // Le temps de réponse max ne doit pas dépasser 10 secondes
      global.responseTime.mean.lt(5000),  // Le temps moyen sous 3 secondes
      global.successfulRequests.percent.gte(80),  // Le pourcentage des requêtes réussies doit être supérieur à 90%

      // Vérifications spécifiques pour les opérations critiques
      details("Login").responseTime.percentile3.lt(5000),
      details("GET summary").responseTime.percentile3.lt(4000),
      details("Verify OTP").successfulRequests.percent.gte(80)
    )

    // Configuration pour surveiller l'évolution des temps de réponse
    .throttle(
      reachRps(100).in(1.minute),
      holdFor(30.seconds),
      reachRps(200).in(1.minute),
      holdFor(30.seconds),
      reachRps(300).in(1.minute)
    )
}