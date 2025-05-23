package simulations.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.helpers.{Feeder, JwtUtil}
import scala.concurrent.duration._

object AuthenticationScenario {

  val builder = scenario("Signup → Login → Verify OTP → Backup Codes")
    .feed(Feeder.userFeeder)

    .exec { session =>
      println(s"\n*** FEEDER DEBUG → email='${session("email").asOption[String]}' " +
        s"pwd='${session("password").asOption[String]}' ***")
      session
    }

    .exec(
      http("Signup")
        .post("/api/auth/signup")
        .header("Content-Type", "application/json")
        .body(StringBody(session =>
          s"""{ "email": "${session("email").as[String]}", "password": "${session("password").as[String]}" }"""
        )).asJson
        .check(status.in(200, 409)) // We accept 409 if the user already exists
        .check(bodyString.saveAs("signupResponseBody"))
    )
    .exec { session =>
      println("\n=== Signup Response ===")
      println(session("signupResponseBody").as[String])
      session
    }

    .exec(
      http("Login")
        .post("/api/auth/login")
        .header("Content-Type", "application/json")
        .body(StringBody(session =>
          s"""{ "email": "${session("email").as[String]}", "password": "${session("password").as[String]}" }"""
        )).asJson
        .check(status.is(200))
        .check(jsonPath("$.otp").optional.saveAs("otp"))
        .check(bodyString.saveAs("loginResponseBody"))
    )
    .exitHereIfFailed // If the connection fails, stop the scenario
    .exec { session =>
      println("\n=== Login Response ===")
      println(session("loginResponseBody").as[String])
      session
    }

    .exec(
      http("Verify OTP")
        .post("/api/auth/verify-otp")
        .header("Content-Type", "application/json")
        .body(StringBody(session => {
          val otp = session.contains("otp") match {
            case true => session("otp").as[String]
            case false => "000000" // default value if otp absent
          }
          s"""{ "email": "${session("email").as[String]}", "otp": "$otp" }"""
        })).asJson
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("jwt")) // Retrieving the token
        .check(bodyString.saveAs("verifyOtpResponseBody"))
    )
    .exitHereIfFailed // If OTP verification fails, stop the scenario
    .exec { session =>
      println("\n=== Verify OTP Response ===")
      println(session("verifyOtpResponseBody").as[String])
      println(s"--- JWT token: ${session("jwt").asOption[String].getOrElse("No token")} ---")
      session
    }

    // Ensure the JWT is available even if the API fails
    .exec(session => {
      if (!session.contains("jwt")) {
        // Generate a fallback JWT if the API did not provide one
        val email = session("email").as[String]
        println(s"⚠️ Generating fallback JWT for $email since no JWT was received from API")
        session.set("jwt", JwtUtil.token(email))
      } else {
        session
      }
    })

    // Added backup code generation
    .randomSwitch(
      40.0 -> exec(
        http("Generate Backup Codes")
          .post("/api/auth/generate-backup-codes")
          .header("Content-Type", "application/json")
          .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
          .body(StringBody(session =>
            s"""{ "email": "${session("email").as[String]}", "count": 5 }"""
          )).asJson
          .check(status.is(200))
          .check(bodyString.saveAs("backupCodesResponse"))
          .check(jsonPath("$[0]").optional.saveAs("backupCode"))
      )
        .exec { session =>
          println("\n=== Backup Codes Generated ===")
          println(session("backupCodesResponse").as[String])
          session
        }
        // Test the use of a backup code if one has been generated
        .doIf(session => session.contains("backupCode")) {
          exec(
            http("Verify Backup Code")
              .post("/api/auth/verify-backup-code")
              .header("Content-Type", "application/json")
              .body(StringBody(session =>
                s"""{ "email": "${session("email").as[String]}", "backupCode": "${session("backupCode").as[String]}" }"""
              )).asJson
              .check(status.is(200))
              .check(jsonPath("$.token").optional.saveAs("newJwt"))
          )
            .exec { session =>
              if (session.contains("newJwt")) {
                println(s"--- New JWT from backup code: ${session("newJwt").as[String].take(20)}... ---")
                session.set("jwt", session("newJwt").as[String])
              } else {
                println("--- No new JWT received from backup code verification ---")
                session
              }
            }
        }
    )
}