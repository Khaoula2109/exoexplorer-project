package simulations.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.helpers.JwtUtil
import scala.concurrent.duration._

/**
 * Scenario for testing the DataLoaderController's functionality
 * that allows loading test data into the application.
 * This scenario is for administrators only.
 */
object DataLoaderScenario {

  // Configuration des emails administrateurs
  private val adminEmails = Array(
    "admin@exoexplorer.com",
    "system@exoexplorer.com",
    "admin_test@exoexplorer.com"
  )

  val builder = scenario("Data Loader Operations")
    // Generate a valid admin JWT for authentication
    .exec(session => {
      // Use an admin email and generate a token with ADMIN role
      val adminEmail = adminEmails(util.Random.nextInt(adminEmails.length))
      val adminJwt = JwtUtil.adminToken(adminEmail) // Utilisation de adminToken au lieu de token

      // Display token information for debugging
      JwtUtil.printJwtContent(adminJwt)

      session.set("adminEmail", adminEmail).set("adminJwt", adminJwt)
    })
    .exec { session =>
      // Debug to identify the token used
      println(s"\n*** DATA LOADER DEBUG → Admin email='${session("adminEmail").as[String]}' ***")
      println(s"*** JWT='${session("adminJwt").as[String].take(20)}...' ***\n")
      session
    }

    // Testing the exoplanet removal endpoint
    .exec(
      http("Clear All Exoplanets")
        .delete("/api/admin/data-loader/clear-exoplanets")
        .header("Authorization", session => s"Bearer ${session("adminJwt").as[String]}")
        .check(status.is(200))
        .check(bodyString.saveAs("clearResponse"))
        .requestTimeout(15.seconds) // Increased timeout for heavy operations
    )
    .exec { session =>
      println(s"Clear Response: ${session("clearResponse").as[String]}")
      session
    }
    .pause(1.second) // Longer pause to ensure deletion is complete

    // Testing the insertion of test exoplanets
    .randomSwitch(
      70.0 -> exec(
        http("Insert 500 Test Exoplanets")
          .post("/api/admin/data-loader/insert-500-exoplanets")
          .header("Authorization", session => s"Bearer ${session("adminJwt").as[String]}")
          .check(status.is(200))
          .check(bodyString.saveAs("insertResponse"))
          .requestTimeout(30.seconds) // Timeout significantly increased for this heavy operation
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
          .requestTimeout(15.seconds) // Timeout increased
      )
        .exec { session =>
          println(s"Habitable Exoplanets Response: ${session("habitableResponse").as[String]}")
          session
        }
    )
    .pause(2.seconds) // Longer break after major operations

    // Check that the data was inserted correctly
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

    // Checking habitable exoplanets if they have been inserted
    .doIf(session => session.contains("habitableResponse")) {
      exec(
        http("Verify Habitable Exoplanets")
          .get("/api/exoplanets/habitable")
          .header("Authorization", session => s"Bearer ${session("adminJwt").as[String]}")
          .check(status.is(200))
          .requestTimeout(10.seconds)
      )
    }

    // Refresh endpoint tests (in ExoplanetController)
    .randomSwitch(
      40.0 -> exec(
        http("Refresh Exoplanet Data")
          .post("/api/exoplanets/refresh")
          .header("Authorization", session => s"Bearer ${session("adminJwt").as[String]}")
          .check(status.is(200))
          .check(bodyString.saveAs("refreshResponse"))
          .requestTimeout(20.seconds) // Timeout greatly increased
      )
        .exec { session =>
          println(s"Refresh Response: ${session("refreshResponse").as[String]}")
          session
        }
    )
}