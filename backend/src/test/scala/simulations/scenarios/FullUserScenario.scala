package simulations.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.helpers.{Feeder, JwtUtil}
import scala.concurrent.duration._

object FullUserScenario {

  val builder = scenario("Full User Journey: Signup → Login → Verify OTP → Profile → Toggle Favorite → Explore")
    .feed(Feeder.userFeeder)
    .feed(Feeder.exoFeeder)
    .exec { session =>
      println(s"⭐ Feeder exoplanetId loaded: ${session("exoId").asOption[String].getOrElse("NO exoId")}")
      session.set("exoIdParsed", session("exoId").as[String].toInt)
    }

    // 1. Registration
    .exec(http("Signup")
      .post("/api/auth/signup")
      .header("Content-Type", "application/json")
      .body(StringBody(session =>
        s"""{ "email": "${session("email").as[String]}", "password": "${session("password").as[String]}" }"""
      )).asJson
      .check(status.in(200, 409)))
    .pause(1)

    // 2. Connection
    .exec(http("Login")
      .post("/api/auth/login")
      .header("Content-Type", "application/json")
      .body(StringBody(session =>
        s"""{ "email": "${session("email").as[String]}", "password": "${session("password").as[String]}" }"""
      )).asJson
      .check(status.is(200))
      .check(jsonPath("$.otp").optional.saveAs("otp")))
    .exitHereIfFailed

    // 3. OTP Verification
    .exec(http("Verify OTP")
      .post("/api/auth/verify-otp")
      .header("Content-Type", "application/json")
      .body(StringBody(session => {
        val otp = session.contains("otp") match {
          case true => session("otp").as[String]
          case false => "000000"
        }
        s"""{ "email": "${session("email").as[String]}", "otp": "$otp" }"""
      })).asJson
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("jwt")))
    .exitHereIfFailed

    // Crucial step: ensure jwt is available for the next steps
    .exec(session => {
      if (!session.contains("jwt")) {
        // If JWT not obtained from API, generate fallback JWT
        val email = session("email").as[String]
        println(s" Generating fallback JWT for $email since no JWT was received from API")
        session.set("jwt", JwtUtil.token(email))
      } else {
        session
      }
    })

    // 4. Getting the user profile
    .exec(http("Get Profile")
      .get(session => s"/api/user/profile?email=${session("email").as[String]}")
      .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
      .check(status.is(200)))
    .pause(200.millis)

    // 5. Adding to favorites
    .doIf(session => session.contains("exoIdParsed")) {
      exec(http("Toggle Favorite")
        .post("/api/user/toggle-favorite")
        .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
        .header("Content-Type", "application/json")
        .body(StringBody(session =>
          s"""{ "email": "${session("email").as[String]}", "exoplanetId": ${session("exoIdParsed").as[Int]} }"""
        )).asJson
        .check(status.in(200, 404)))
    }
    .pause(200.millis)

    // 6. Update preferences
    .exec(http("Update Preferences")
      .put("/api/user/preferences")
      .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
      .header("Content-Type", "application/json")
      .body(StringBody(session => {
        val darkMode = if (util.Random.nextBoolean()) "true" else "false"
        val language = if (util.Random.nextBoolean()) "fr" else "en"
        s"""{ "email": "${session("email").as[String]}", "darkMode": ${darkMode}, "language": "${language}" }"""
      })).asJson
      .check(status.is(200)))
    .pause(300.millis)

    // 7. Loading favorites
    .exec(http("Get Favorites")
      .get("/api/user/favorites")
      .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
      .check(status.is(200)))
    .pause(200.millis)

    // 8. Exploring exoplanets
    .exec(session => {
      // Generate a random filter parameter
      val randomParams = Feeder.getRandomFilterParams()
      val queryParams = Feeder.mapToQueryString(randomParams)
      session.set("queryParams", queryParams)
    })
    .randomSwitch(
      70.0 -> exec(
        http("Browse Summary")
          .get(session => s"/api/exoplanets/summary${session("queryParams").as[String]}")
          .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
          .check(status.is(200))
          .check(jsonPath("$.content[0].id").optional.saveAs("randomExoId"))
      ),
      30.0 -> exec(
        http("View Habitable Exoplanets")
          .get("/api/exoplanets/habitable")
          .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
          .check(status.is(200))
      )
    )
    .pause(300.millis)

    // 9. View details of an exoplanet (if ID found)
    .doIf(session => session.contains("randomExoId")) {
      exec(
        http("View Exoplanet Details")
          .get(session => s"/api/exoplanets/${session("randomExoId").as[String]}/details")
          .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
          .check(status.is(200))
      )
    }
    .pause(200.millis)

    // 10. Generation of backup codes (for some users)
    .randomSwitch(
      25.0 -> exec(
        http("Generate Backup Codes")
          .post("/api/auth/generate-backup-codes")
          .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
          .header("Content-Type", "application/json")
          .body(StringBody(session =>
            s"""{ "email": "${session("email").as[String]}", "count": 5 }"""
          )).asJson
          .check(status.is(200))
          .check(jsonPath("$[0]").optional.saveAs("backupCode"))
      )
        .exec { session =>
          println(s"Generated backup code: ${session("backupCode").asOption[String].getOrElse("None")}")
          session
        }
    )
}