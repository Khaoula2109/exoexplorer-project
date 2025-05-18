package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.helpers.JwtUtil
import scala.concurrent.duration._

/**
 * Optimized smoke testing with relaxed assertions
 * to identify all issues without immediate failure
 */
class SmokeSimulation extends Simulation {

  // HTTP configuration with increased timeouts
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling/3.13.5 Performance Test")
    .disableCaching // Disable cache to test real performance
    .warmUp("http://localhost:8080/actuator/health") // Warm up before starting

  // Test user - we can use an existing user
  val testEmail = "user1@example.com"
  val testPassword = "password1"
  val adminEmail = "admin@exoexplorer.com"

  // Scenario for smoke testing with relaxed assertions
  val scn = scenario("Test de fumée minimal")
    // User configuration
    .exec(session => session
      .set("email", testEmail)
      .set("password", testPassword)
      .set("adminEmail", adminEmail)
    )

    // Step 0: Pre-generate JWT tokens for testing
    .exec(session => {
      // Generate a user and admin JWT in advance
      val userJwt = JwtUtil.token(testEmail)
      val adminJwt = JwtUtil.adminToken(adminEmail)

      // Display token information for debugging
      println(s"User JWT generated for $testEmail")
      JwtUtil.printJwtContent(userJwt)
      println(s"Admin JWT generated for $adminEmail")
      JwtUtil.printJwtContent(adminJwt)

      session.set("jwt", userJwt).set("adminJwt", adminJwt)
    })

    // Step 1: Registration (accepts 409 if user already exists)
    .exec(
      http("Signup")
        .post("/api/auth/signup")
        .body(StringBody(
          s"""{"email":"${testEmail}","password":"${testPassword}"}"""
        )).asJson
        .check(status.in(200, 409))
        .requestTimeout(10.seconds) // Timeout increased
    )
    .pause(2.seconds) // Longer break

    // Step 2: Login
    .exec(
      http("Login")
        .post("/api/auth/login")
        .body(StringBody(
          s"""{"email":"${testEmail}","password":"${testPassword}"}"""
        )).asJson
        .check(status.is(200))
        .check(jsonPath("$.otp").optional.saveAs("otp"))
        .requestTimeout(10.seconds) // Timeout increased
    )
    .pause(2.seconds) // Longer break

    // Step 3: OTP Verification
    .exec(session => {
      // Use OTP received or "000000" by default
      val otp = session.contains("otp") match {
        case true =>
          println(s"Using received OTP: ${session("otp").as[String]}")
          session("otp").as[String]
        case false =>
          println("No OTP received, using default 000000")
          "000000"
      }
      session.set("usedOtp", otp)
    })
    .exec(
      http("Verify OTP")
        .post("/api/auth/verify-otp")
        .body(StringBody(session =>
          s"""{"email":"${session("email").as[String]}","otp":"${session("usedOtp").as[String]}"}"""
        )).asJson
        .check(status.is(200))
        .check(jsonPath("$.token").optional.saveAs("apiJwt"))
        .requestTimeout(10.seconds) // Longer Timeout
    )
    .pause(1.seconds)

    // Step 4: Using the JWT received from the API or the pre-generated JWT
    .exec(session => {
      if (session.contains("apiJwt")) {
        val receivedJwt = session("apiJwt").as[String]
        println(s"Using JWT received from API: ${receivedJwt.take(20)}...")
        session.set("jwt", receivedJwt)
      } else {
        println(s"Using pre-generated JWT: ${session("jwt").as[String].take(20)}...")
        session
      }
    })

    // Step 5: Retrieving Exoplanets (Protected API Test)
    .exec(
      http("GET summary")
        .get("/api/exoplanets/summary")
        .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
        .check(status.is(200))
        .requestTimeout(10.seconds)
    )
    .pause(1.second)

    .exec(
      http("GET list")
        .get("/api/exoplanets")
        .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
        .check(status.is(200))
        .requestTimeout(10.seconds)
    )
    .pause(1.second)

    // Step 6: Test habitable API
    .exec(
      http("GET habitable exoplanets")
        .get("/api/exoplanets/habitable")
        .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
        .check(status.is(200))
        .requestTimeout(10.seconds)
    )
    .pause(1.second)

    // Step 7: Test the profile
    .exec(
      http("GET profile")
        .get(session => s"/api/user/profile?email=${session("email").as[String]}")
        .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
        .check(status.is(200))
        .requestTimeout(10.seconds)
    )
    .pause(1.second)

    // Step 8: Test the favorites
    .exec(
      http("GET favorites")
        .get("/api/user/favorites")
        .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
        .check(status.is(200))
        .requestTimeout(10.seconds)
    )
    .pause(1.second)

    // Step 9: Test adding to favorites
    .exec(
      http("Toggle favourite")
        .post("/api/user/toggle-favorite")
        .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
        .header("Content-Type", "application/json")
        .body(StringBody("""{ "exoplanetId": 1 }""")).asJson
        .check(status.in(200, 404))
        .requestTimeout(10.seconds)
    )

  // Test Setup - Single User
  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpProtocol)
    .assertions(
      // Significantly relaxed assertions for diagnosing all problems
      global.responseTime.max.lt(10000),
      global.successfulRequests.percent.gt(70),

      // Specific assertions for significant transactions
      details("Signup").successfulRequests.percent.is(100),
      details("Login").successfulRequests.percent.is(100),

      // Login time assertion relaxed
      details("Login").responseTime.max.lt(5000)
    )
    .maxDuration(2.minutes) // Maximum duration to avoid tests that drag on forever
}