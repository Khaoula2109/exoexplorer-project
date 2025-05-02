package simulations.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.helpers.JwtUtil
import scala.concurrent.duration._

/**
 * Scénario pour tester les fonctionnalités du DataLoaderController
 * qui permettent de charger des données de test dans l'application.
 * Ce scénario est réservé aux administrateurs.
 */
object DataLoaderScenario {

  // Configuration des emails administrateurs
  private val adminEmails = Array(
    "admin@exoexplorer.com",
    "system@exoexplorer.com",
    "admin_test@exoexplorer.com"
  )

  val builder = scenario("Data Loader Operations")
    // Génération d'un JWT admin valide pour l'authentification
    .exec(session => {
      // Utiliser un email admin et générer un token avec rôle ADMIN
      val adminEmail = adminEmails(util.Random.nextInt(adminEmails.length))
      val adminJwt = JwtUtil.adminToken(adminEmail) // Utilisation de adminToken au lieu de token

      // Afficher les informations du token pour débogage
      JwtUtil.printJwtContent(adminJwt)

      session.set("adminEmail", adminEmail).set("adminJwt", adminJwt)
    })
    .exec { session =>
      // Debug pour identifier le token utilisé
      println(s"\n*** DATA LOADER DEBUG → Admin email='${session("adminEmail").as[String]}' ***")
      println(s"*** JWT='${session("adminJwt").as[String].take(20)}...' ***\n")
      session
    }

    // Test du endpoint de suppression des exoplanètes
    .exec(
      http("Clear All Exoplanets")
        .delete("/api/admin/data-loader/clear-exoplanets")
        .header("Authorization", session => s"Bearer ${session("adminJwt").as[String]}")
        .check(status.is(200))
        .check(bodyString.saveAs("clearResponse"))
        .requestTimeout(15.seconds) // Timeout augmenté pour les opérations lourdes
    )
    .exec { session =>
      println(s"Clear Response: ${session("clearResponse").as[String]}")
      session
    }
    .pause(1.second) // Pause plus longue pour s'assurer que la suppression est complète

    // Test de l'insertion des exoplanètes de test
    .randomSwitch(
      70.0 -> exec(
        http("Insert 500 Test Exoplanets")
          .post("/api/admin/data-loader/insert-500-exoplanets")
          .header("Authorization", session => s"Bearer ${session("adminJwt").as[String]}")
          .check(status.is(200))
          .check(bodyString.saveAs("insertResponse"))
          .requestTimeout(30.seconds) // Timeout largement augmenté pour cette opération lourde
      )
        .exec { session =>
          println(s"Insert Response: ${session("insertResponse").as[String]}")
          session
        },

      30.0 -> exec(
        http("Insert Habitable Exoplanets")
          .post("/api/admin/data-loader/insert-habitable-exoplanets")
          .header("Authorization", session => s"Bearer ${session("adminJwt").as[String]}")
          .check(status.is(200))
          .check(bodyString.saveAs("habitableResponse"))
          .requestTimeout(15.seconds) // Timeout augmenté
      )
        .exec { session =>
          println(s"Habitable Exoplanets Response: ${session("habitableResponse").as[String]}")
          session
        }
    )
    .pause(2.seconds) // Pause plus longue après opérations lourdes

    // Vérification que les données ont été insérées correctement
    .exec(
      http("Verify Inserted Data")
        .get("/api/exoplanets")
        .header("Authorization", session => s"Bearer ${session("adminJwt").as[String]}")
        .check(status.is(200))
        .check(jsonPath("$[0].id").optional.saveAs("firstExoId"))
        .requestTimeout(10.seconds)
    )
    .exec { session =>
      println(s"Verification: Found exoplanet with ID ${session("firstExoId").asOption[String].getOrElse("unknown")}")
      session
    }
    .pause(1.second)

    // Vérification des exoplanètes habitables si elles ont été insérées
    .doIf(session => session.contains("habitableResponse")) {
      exec(
        http("Verify Habitable Exoplanets")
          .get("/api/exoplanets/habitable")
          .header("Authorization", session => s"Bearer ${session("adminJwt").as[String]}")
          .check(status.is(200))
          .requestTimeout(10.seconds)
      )
    }

    // Tests du endpoint de rafraîchissement (dans ExoplanetController)
    .randomSwitch(
      40.0 -> exec(
        http("Refresh Exoplanet Data")
          .post("/api/exoplanets/refresh")
          .header("Authorization", session => s"Bearer ${session("adminJwt").as[String]}")
          .check(status.is(200))
          .check(bodyString.saveAs("refreshResponse"))
          .requestTimeout(20.seconds) // Timeout largement augmenté
      )
        .exec { session =>
          println(s"Refresh Response: ${session("refreshResponse").as[String]}")
          session
        }
    )
}