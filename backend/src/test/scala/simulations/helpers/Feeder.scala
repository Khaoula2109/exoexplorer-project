package simulations.helpers

import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder

object Feeder {

  /**
   * Feeder for users - loads data from CSV file
   */
  val userFeeder: FeederBuilder = csv("data/users.csv")
    .transform { case (key, value) => value.trim }
    .circular

  /**
   * Exoplanet Feeder - loads data from CSV file
   */
  val exoFeeder: FeederBuilder = csv("data/exoplanets.csv")
    .transform { case (key, value) => value.trim }
    .circular

  /**
   * Backup Code Feeder - loads data from CSV file
   */
  val backupCodesFeeder: FeederBuilder = csv("data/backup_codes.csv")
    .transform { case (key, value) => value.trim }
    .queue

  /**
   * Utility method to obtain a feeder with a distribution probability
   * @param path Path to the CSV file
   * @param randomize If true, use a random strategy instead of circular
   */
  def getFeeder(path: String, randomize: Boolean = false): FeederBuilder = {
    val baseFeeder = csv(path)
      .transform { case (key, value) => value.trim }

    if (randomize) {
      baseFeeder.shuffle
    } else {
      baseFeeder.circular
    }
  }

  /**
   * List of predefined filter parameters for exoplanets
   * Replaces the old filterParamsFeeder which used head/isEmpty
   */
  val filterParams: Array[Map[String, String]] = Array(
    Map("minTemp" -> "0", "maxTemp" -> "300"),
    Map("minDistance" -> "0", "maxDistance" -> "100"),
    Map("name" -> "Kepler"),
    Map("minYear" -> "2000", "maxYear" -> "2020"),
    Map("minTemp" -> "180", "maxTemp" -> "310"),
    Map[String, String]() // Empty map for queries without filters
  )

  /**
   * Method to get a random filter parameter
   * @return Map of filter parameters
   */
  def getRandomFilterParams(): Map[String, String] = {
    filterParams(util.Random.nextInt(filterParams.length))
  }

  /**
   * Method to convert a parameter map to a query string
   * @param params Map of parameters
   * @return Query string (e.g., "?param1=value1&param2=value2" or "" if empty)
   */
  def mapToQueryString(params: Map[String, String]): String = {
    if (params.isEmpty) ""
    else "?" + params.map { case (k, v) => s"$k=$v" }.mkString("&")
  }
}