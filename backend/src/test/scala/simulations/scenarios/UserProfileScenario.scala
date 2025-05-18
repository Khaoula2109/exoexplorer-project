package simulations.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.helpers.{Feeder, JwtUtil}
import scala.concurrent.duration._

object UserProfileScenario {

  val builder = scenario("Profile + Favorites + Preferences")
    // User data feed and exoplanets
    .feed(Feeder.userFeeder)
    .feed(Feeder.exoFeeder)
    // Initialization step to ensure jwt availability
    .exec(session => {
      // If jwt is not available, generate a jwt from the feeder's email
      if (!session.contains("jwt")) {
        val email = session("email").as[String]
        session.set("jwt", JwtUtil.token(email))
      } else {
        session
      }
    })
    // Ensure that exoId is an integer for queries
    .exec(session => {
      if (session.contains("exoId")) {
        session.set("exoIdParsed", session("exoId").as[String].toInt)
      } else {
        // Valeur par défaut si exoId n'est pas disponible
        session.set("exoIdParsed", 1)
      }
    })
    // Loading user profile
    .exec(
      http("GET profile")
        .get(session => s"/api/user/profile?email=${session("email").as[String]}")
        .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
        .check(status.is(200))
        .check(bodyString.saveAs("profileResponse"))
    )
    .pause(150.millis)
    // Favorites management
    .exec(
      http("Toggle favourite")
        .post("/api/user/toggle-favorite")
        .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
        .header("Content-Type", "application/json")
        .body(StringBody(session =>
          s"""{ "email": "${session("email").as[String]}", "exoplanetId": ${session("exoIdParsed").as[Int]} }"""
        )).asJson
        .check(status.in(200, 404))
    )
    .pause(200.millis)
    // Loading favorites
    .exec(
      http("GET favorites")
        .get("/api/user/favorites")
        .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
        .check(status.is(200))
    )
    .pause(150.millis)
    // Updating user preferences
    .randomSwitch(
      40.0 -> exec(
        http("Update preferences")
          .put("/api/user/preferences")
          .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
          .header("Content-Type", "application/json")
          .body(StringBody(session => {
            val darkMode = if (util.Random.nextBoolean()) "true" else "false"
            val language = if (util.Random.nextBoolean()) "fr" else "en"
            s"""{ "email": "${session("email").as[String]}", "darkMode": ${darkMode}, "language": "${language}" }"""
          })).asJson
          .check(status.is(200))
      )
    )
    .pause(150.millis)
    // User profile update
    .randomSwitch(
      30.0 -> exec(
        http("Update profile")
          .put("/api/user/update-profile")
          .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
          .header("Content-Type", "application/json")
          .body(StringBody(session =>
            s"""{ "email": "${session("email").as[String]}", "firstName": "Test", "lastName": "User" }"""
          )).asJson
          .check(status.is(200))
      )
    )
    .pause(150.millis)
    // Change password (less frequent)
    .randomSwitch(
      10.0 -> exec(
        http("Change password")
          .post("/api/user/change-password")
          .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
          .header("Content-Type", "application/json")
          .body(StringBody(session =>
            s"""{ "email": "${session("email").as[String]}", "currentPassword": "${session("password").as[String]}", "newPassword": "${session("password").as[String]}123" }"""
          )).asJson
          .check(status.is(200))
      )
    )
}