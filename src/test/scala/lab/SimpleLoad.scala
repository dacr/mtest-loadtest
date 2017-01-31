package lab

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import scala.language.postfixOps

class SimpleLoad extends Simulation with DummyDefaults {
  val logger = org.slf4j.LoggerFactory.getLogger(getClass())
  
  val testURL = propOrEnvOrDefault("TEST_URL", "http://backend:8080/mtest-web-project/")
  val vus = propOrEnvOrDefault("TEST_VUS", "20").toInt
  val duration = propOrEnvOrDefault("TEST_DURATION", "5").toInt

  logger.info(s"TEST_URL : $testURL")
  logger.info(s"TEST_VUS : $vus")
  logger.info(s"TEST_DURATION : $duration minutes")

  val customAssertions:List[Assertion] = List(
      propOrEnv("TEST_ASSERT_MAX_RESPTIME").map(x => global.responseTime.mean.lessThan(x.toInt)),
      propOrEnv("TEST_ASSERT_MAX_RESPTIME50").map(x => global.responseTime.percentile1.lessThan(x.toInt)),
      propOrEnv("TEST_ASSERT_MAX_RESPTIME75").map(x => global.responseTime.percentile2.lessThan(x.toInt)),
      propOrEnv("TEST_ASSERT_MAX_RESPTIME95").map(x => global.responseTime.percentile3.lessThan(x.toInt)),
      propOrEnv("TEST_ASSERT_MAX_RESPTIME99").map(x => global.responseTime.percentile4.lessThan(x.toInt)),
      propOrEnv("TEST_ASSERT_OK_PERCENT").map(x => global.successfulRequests.percent.greaterThan(x.toInt)),
      propOrEnv("TEST_ASSERT_MIN_HITRATE").map(x => global.requestsPerSec.greaterThan(x.toInt))
      ).flatten
    
  
  val httpConf = buildDefaultConfig(testURL)

  val scn =
        scenario("Simple load").during(duration minutes) {
          exec(
                http("homepage")
                   .get("/")
                   .headers(defaultHeaders)
                   .check(status.is(200)) )
          .pause(500 milliseconds, 1000 milliseconds)
        }

  setUp(scn.inject(rampUsers(vus) over(20 seconds)))
    .protocols(httpConf)
    .assertions(customAssertions)
}

