Rainfall
========

Rainfall is an extensible java framework to implement custom DSL based stress and performance tests in your application.

It has a customisable fluent interface that lets you implement your own DSL when writing tests scenarios, and define your own tests actions and metrics.
Rainfall is open to extensions, two of which are currently in progress,
- Rainfall web is a Yet Another Web Application performance testing library
- Rainfall JCache is a library to test the performance of caches solutions


Quick start
-----------

Performance tests are written in java, we will cover a simple example using Rainfall web:

This tests the performance of calling the Twitter search page.
The scenario is a serie of three consecutive queries that will search for a text string.
It will simulate 5 concurrent users doing nothing for 5 seconds then doing the operations of the scenario.
```
   HttpConfig httpConf = httpConfig()
        .baseURL("http://search.twitter.com");

    Scenario scenario = scenario("Twitter search")
        .exec(http("Recherche Crocro").get("/search.json?q=crocro"))
        .exec(http("Recherche Gatling").get("/search.json?q=gatling"))
        .exec(http("Recherche Java").get("/search.json?q=java"));

    setUp(scenario)
        .executed(atOnce(5, users), nothingFor(5, seconds), atOnce(5, users))
        .config(httpConf)
        .start();
```

Build the project
-----------------
```
  mvn clean install
```

Use it in your project
----------------------
```
  <dependencies>
    <dependency>
      <groupId>org.rainfall</groupId>
      <artifactId>lib</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
```

Writing your own performance framework
--------------------------------------
The basic classes that you will extend are:

org.rainfall.Configuration
Your test will define a configuration (e.g. doing http calls to the twitter search page).

org.rainfall.Execution
Your test will define a scenario, made of a serie of executions (e.g. do nothing, do an operation once)

org.rainfall.Operation
Each execution of the test does some specific Operation (e.g. do the http call with some parameter)


Currently, two performance frameworks are (partially) implemented : rainfall-web and rainfall-jcache
You can refer to those modules to have some idea on how to write your own framework.
