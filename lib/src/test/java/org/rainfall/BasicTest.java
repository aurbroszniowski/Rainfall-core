package org.rainfall;

import org.junit.Test;
import org.rainfall.gatling.configuration.HttpConfig;

import static org.rainfall.Runner.setUp;
import static org.rainfall.Scenario.scenario;
import static org.rainfall.gatling.GatlingOperations.atOnce;
import static org.rainfall.gatling.GatlingOperations.http;
import static org.rainfall.gatling.GatlingOperations.nothingFor;
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

    Scenario scenario = scenario("Twitter search")
        .exec(http("Recherche JCertif").get("/search.json?q=jcertif"))
        .exec(http("Recherche Gatling").get("/search.json?q=gatling"))
        .exec(http("Recherche Nantes").get("/search.json?q=nantes"))
        .exec(http("Recherche Scala").get("/search.json?q=scala"));

    setUp(scenario)
        .executed(atOnce(5, users), nothingFor(5, seconds), atOnce(2, users))
        .config(httpConf)
        .start();


  }

}
