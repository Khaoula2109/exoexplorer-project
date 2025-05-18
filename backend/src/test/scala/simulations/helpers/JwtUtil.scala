package simulations.helpers

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.time.Instant

/** Generates a JWT on the test side that is fully compatible
 * with TokenService (secret + algorithm + claims).
 */
object JwtUtil {

  private val secret = "MyVerySecureJWTSecret123456789"
  private val algo   = "HmacSHA256"

  private def base64Url(data: Array[Byte]): String =
    Base64.getUrlEncoder.withoutPadding.encodeToString(data)

  private def hmac(data: String): String = {
    val mac = Mac.getInstance(algo)
    mac.init(new SecretKeySpec(secret.getBytes, algo))
    base64Url(mac.doFinal(data.getBytes))
  }

  /** Returns a JWT valid for 2 hours for the provided email (standard user). */
  def token(email: String): String = {
    val now  = Instant.now.getEpochSecond
    val exp  = now + 7200        // 2h (augmenté pour les tests)
    val head = base64Url("""{"alg":"HS256","typ":"JWT"}""".getBytes)
    val payl = base64Url(
      s"""{"sub":"$email","iat":$now,"exp":$exp,"roles":["USER"]}""".getBytes
    )
    s"$head.$payl.${hmac(s"$head.$payl")}"
  }

  /** Returns a JWT valid for 2 hours for the email provided with ADMIN role. */
  def adminToken(email: String): String = {
    val now  = Instant.now.getEpochSecond
    val exp  = now + 7200        // 2h (augmenté pour les tests)
    val head = base64Url("""{"alg":"HS256","typ":"JWT"}""".getBytes)
    val payl = base64Url(
      s"""{"sub":"$email","iat":$now,"exp":$exp,"roles":["USER","ADMIN"]}""".getBytes
    )
    s"$head.$payl.${hmac(s"$head.$payl")}"
  }

  /** Displays a JWT for debugging. */
  def printJwtContent(token: String): Unit = {
    try {
      val parts = token.split('.')
      if (parts.length >= 2) {
        val payload = new String(Base64.getUrlDecoder.decode(parts(1)))
        println(s"JWT Payload: $payload")
      } else {
        println("Invalid JWT format")
      }
    } catch {
      case e: Exception => println(s"Error decoding JWT: ${e.getMessage}")
    }
  }
}