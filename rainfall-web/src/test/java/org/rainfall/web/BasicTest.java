package org.rainfall.web;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rainfall.Runner;
import org.rainfall.Scenario;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.configuration.ReportingConfig;
import org.rainfall.utils.SystemTest;
import org.rainfall.web.configuration.HttpConfig;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.rainfall.execution.Executions.atOnce;
import static org.rainfall.unit.Units.seconds;
import static org.rainfall.unit.Units.users;
import static org.rainfall.web.WebAssertions.isLessThan;
import static org.rainfall.web.WebAssertions.responseTime;
import static org.rainfall.web.WebExecutions.nothingFor;
import static org.rainfall.web.WebOperations.http;
import static org.rainfall.web.configuration.HttpConfig.httpConfig;

/**
 * @author Aurelien Broszniowski
 */

@Category(SystemTest.class)
public class BasicTest {

  @Test
  public void testBasic() {
    HttpConfig httpConf = httpConfig()
        .baseURL("http://search.twitter.com");
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = ReportingConfig.reportingConfig(ReportingConfig.text());

    Scenario scenario = Scenario.scenario("Twitter search")
        .exec(http("Recherche Crocro").get("/search.json?q=crocro"))
        .exec(http("Recherche Gatling").get("/search.json?q=gatling"))
        .exec(http("Recherche Java").get("/search.json?q=java"));

    Runner.setUp(scenario)
        .executed(atOnce(5, users), nothingFor(5, seconds), atOnce(5, users))
        .config(httpConf, concurrency, reporting)
        .assertion(responseTime(), isLessThan(1, seconds))
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
