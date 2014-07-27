package org.rainfall;

import org.junit.Test;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.gatling.configuration.HttpConfig;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.rainfall.Runner.setUp;
import static org.rainfall.Scenario.scenario;
import static org.rainfall.gatling.GatlingExecutions.atOnce;
import static org.rainfall.gatling.GatlingExecutions.nothingFor;
import static org.rainfall.gatling.GatlingOperations.http;
import static org.rainfall.gatling.GatlingUnits.seconds;
import static org.rainfall.gatling.GatlingUnits.users;
import static org.rainfall.gatling.configuration.HttpConfig.httpConfig;

/**
 * @author Aurelien Broszniowski
 */

public class BasicTest {

  @Test
  public void testBasic() {
    HttpConfig httpConf = httpConfig()
        .baseURL("http://search.twitter.com");
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);

    Scenario scenario = scenario("Twitter search")
        .exec(http("Recherche Crocro").get("/search.json?q=crocro"))
        .exec(http("Recherche Gatling").get("/search.json?q=gatling"))
        .exec(http("Recherche Scala").get("/search.json?q=scala"));

    setUp(scenario)
        .executed(atOnce(5, users), nothingFor(5, seconds), atOnce(5, users))
        .config(httpConf, concurrency)
        .start();


/*
    HttpConfig httpConf = HttpConfig.httpConfig
        .baseURL("http://search.twitter.com")
        .acceptHeader("* /*")  pas d'espace!!!
        .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.3")
        .acceptEncodingHeader("gzip,deflate,sdch")
        .acceptLanguageHeader("en-US,en;q=0.8")
        .connection("keep-alive")
        .userAgentHeader("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.22 (KHTML, like Gecko) Ubuntu/12.10 Chromium/25.0.1364.172 Chrome/25.0.1364.172 Safari/537.22")

    Scenario scn = Scenario.scenario("Recherches sur Twitter")
        .exec(http("Recherche JCertif").get("/search.json?q=jcertif"))
        .exec(http("Recherche Gatling").get("/search.json?q=gatling"))
        .exec(http("Recherche Nantes").get("/search.json?q=nantes"))
        .exec(http("Recherche Scala").get("/search.json?q=scala"))

    scn.beExecuted(
        GatlingOperations.atOnce(10, Unit.users), GatlingOperations.nothingFor(10, Unit.seconds),
        GatlingOperations.atOnce(10, Unit.users))


        setUp(
        // operations
            scn.beExecuted(
            GatlingOperations.nothingFor(4, Unit.seconds), GatlingOperations.atOnce(10, Unit.users), ramp(10, users), over(5, seconds),
            constantRate(20, usersPerSec), during(15, seconds),
            rampRate(10, usersPerSec), to(20, usersPerSec), during(10.minutes),
            split(1000, users).into(ramp(10, users), over(10, seconds)).separatedBy(10, seconds),
            split(1000, users).into(ramp(10, users)over(10, seconds)).separatedBy(atOnce(30, users))))
        // configuration
        .protocols(httpConf)
        //assertions
        .assertions(global.responseTime.max.lessThan(1000));
*/
  }

}
