package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.scenarios._
import simulations.helpers.Protocol
import scala.concurrent.duration._

/**
 * AdvancedMixedSimulation - Simulation réaliste mixant différents profils d'utilisateurs
 * Cette simulation contient des scénarios variés représentant un usage réel
 * de l'application avec des proportions réalistes entre les différents types d'utilisateurs
 */
class AdvancedMixedSimulation extends Simulation {

  // Définition des scénarios
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
    // 1. Nouveaux utilisateurs s'inscrivant et se connectant (20%)
    authScenario
      .inject(
        rampUsersPerSec(1).to(10).during(rampUpTime),
        constantUsersPerSec(10).during(steadyStateTime),
        rampUsersPerSec(10).to(1).during(rampDownTime)
      ),

    // 2. Utilisateurs parcourant le catalogue d'exoplanètes (40%)
    exoScenario
      .inject(
        nothingFor(30.seconds),
        rampUsersPerSec(1).to(20).during(rampUpTime.minus(30.seconds)),
        constantUsersPerSec(20).during(steadyStateTime),
        rampUsersPerSec(20).to(1).during(rampDownTime)
      ),

    // 3. Utilisateurs gérant leur profil et favoris (15%)
    profileScenario
      .inject(
        nothingFor(1.minute),
        rampUsersPerSec(1).to(7.5).during(rampUpTime.minus(1.minute)),
        constantUsersPerSec(7.5).during(steadyStateTime),
        rampUsersPerSec(7.5).to(1).during(rampDownTime)
      ),

    // 4. Utilisateurs effectuant un parcours complet (25%)
    fullUserScenario
      .inject(
        rampUsersPerSec(1).to(12.5).during(rampUpTime),
        constantUsersPerSec(12.5).during(steadyStateTime),
        rampUsersPerSec(12.5).to(1).during(rampDownTime)
      ),

    // 5. Administrateurs effectuant des opérations de maintenance (<1%)
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
      // Assertions générales
      global.responseTime.mean.lt(3000),
      global.responseTime.percentile3.lt(2000),
      global.successfulRequests.percent.gte(80),

      // Assertions spécifiques aux opérations critiques
      details("Verify OTP").responseTime.percentile3.lt(5000),
      details("Login").successfulRequests.percent.gte(80),
      details("GET summary").responseTime.mean.lt(2000),
      details("Toggle favourite").responseTime.mean.lt(1000)
    )
}