package simulations.helpers

import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder

object Feeder {

  /**
   * Feeder pour les utilisateurs - charge les données depuis le fichier CSV
   */
  val userFeeder: FeederBuilder = csv("data/users.csv")
    .transform { case (key, value) => value.trim }
    .circular

  /**
   * Feeder pour les exoplanètes - charge les données depuis le fichier CSV
   */
  val exoFeeder: FeederBuilder = csv("data/exoplanets.csv")
    .transform { case (key, value) => value.trim }
    .circular

  /**
   * Feeder pour les backup codes - charge les données depuis le fichier CSV
   */
  val backupCodesFeeder: FeederBuilder = csv("data/backup_codes.csv")
    .transform { case (key, value) => value.trim }
    .queue // Utiliser queue au lieu de random

  /**
   * Méthode utilitaire pour obtenir un feeder avec une probabilité de distribution
   * @param path Chemin vers le fichier CSV
   * @param randomize Si true, utilise une stratégie aléatoire au lieu de circular
   */
  def getFeeder(path: String, randomize: Boolean = false): FeederBuilder = {
    val baseFeeder = csv(path)
      .transform { case (key, value) => value.trim }

    if (randomize) {
      baseFeeder.shuffle // Utiliser shuffle au lieu de random
    } else {
      baseFeeder.circular
    }
  }

  /**
   * Liste de paramètres de filtrage prédéfinis pour les exoplanètes
   * Remplace l'ancien filterParamsFeeder qui utilisait head/isEmpty
   */
  val filterParams: Array[Map[String, String]] = Array(
    Map("minTemp" -> "0", "maxTemp" -> "300"),
    Map("minDistance" -> "0", "maxDistance" -> "100"),
    Map("name" -> "Kepler"),
    Map("minYear" -> "2000", "maxYear" -> "2020"),
    Map("minTemp" -> "180", "maxTemp" -> "310"),
    Map[String, String]() // Map vide pour les requêtes sans filtres
  )

  /**
   * Méthode pour obtenir un paramètre de filtrage aléatoire
   * @return Map de paramètres de filtrage
   */
  def getRandomFilterParams(): Map[String, String] = {
    filterParams(util.Random.nextInt(filterParams.length))
  }

  /**
   * Méthode pour convertir une Map de paramètres en chaîne de requête
   * @param params Map des paramètres
   * @return Chaîne de requête (ex: "?param1=value1&param2=value2" ou "" si vide)
   */
  def mapToQueryString(params: Map[String, String]): String = {
    if (params.isEmpty) ""
    else "?" + params.map { case (k, v) => s"$k=$v" }.mkString("&")
  }
}