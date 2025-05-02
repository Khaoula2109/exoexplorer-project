package simulations.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import simulations.helpers.{Feeder, JwtUtil}

object ExoplanetScenario {

  private val queries = Array(
    "?minTemp=0&maxTemp=300",
    "?minDistance=0&maxDistance=100",
    "?name=Kepler",
    "?minYear=2000&maxYear=2020",
    "?minTemp=180&maxTemp=310", // Plage de température habitable
    ""
  )

  val builder = scenario("Browse exoplanets")
    // Étape 1: Ajout d'une étape d'initialisation pour garantir que jwt et email sont disponibles
    .feed(Feeder.userFeeder) // Alimentation en données utilisateur
    .exec(session => {
      // Si jwt n'est pas disponible, générer un jwt à partir de l'email du feeder
      if (!session.contains("jwt")) {
        val email = session("email").as[String]
        session.set("jwt", JwtUtil.token(email))
      } else {
        session
      }
    })
    .repeat(5) {
      pace(200.millis, 600.millis)
        .exec(session => {
          // Sélectionner une requête aléatoire
          val randomQuery = queries(util.Random.nextInt(queries.length))
          session.set("randomQuery", randomQuery)
        })
        .exec(
          http("GET summary")
            .get(session => "/api/exoplanets/summary" + session("randomQuery").as[String])
            .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
            .check(status.is(200))
            .check(jsonPath("$.content[0].id").optional.saveAs("randomExoplanetId"))
        )
        .pause(100.millis)
        .exec(
          http("GET list")
            .get("/api/exoplanets")
            .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
            .check(status.is(200))
        )
        .pause(150.millis)
        // Accéder aux détails d'une exoplanète aléatoire si on a récupéré un id
        .doIf(session => session.contains("randomExoplanetId")) {
          exec(
            http("GET exoplanet details")
              .get(session => s"/api/exoplanets/${session("randomExoplanetId").as[String]}/details")
              .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
              .check(status.is(200))
          )
        }
        .pause(100.millis)
        // Chercher des exoplanètes potentiellement habitables
        .randomSwitch(
          30.0 -> exec(
            http("GET habitable exoplanets")
              .get("/api/exoplanets/habitable")
              .header("Authorization", session => s"Bearer ${session("jwt").as[String]}")
              .check(status.is(200))
          )
        )
    }
}