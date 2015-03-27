Rainfall
========

Rainfall is an extensible java framework to implement custom DSL based stress and performance tests in your application.

It has a customisable fluent interface that lets you implement your own DSL when writing tests scenarios, and define your own tests actions and metrics.
Rainfall is open to extensions, three of which are currently in progress,
- Rainfall web is a Yet Another Web Application performance testing library
- Rainfall JCache is a library to test the performance of JSR107 caches solutions
- Rainfall Ehcache is a library to test the performance of Ehcache 2 and 3

![Built on DEV@cloud](https://www.cloudbees.com/sites/default/files/styles/large/public/Button-Built-on-CB-1.png?itok=3Tnkun-C)

[![Build Status](https://rainfall.ci.cloudbees.com/buildStatus/icon?job=Rainfall core)](https://rainfall.ci.cloudbees.com/job/Rainfall%20core/)

Components
----------
[Rainfall-core](https://github.com/aurbroszniowski/Rainfall-core) is the core library containing the key elements of the framework.
 When writing your framework implementation, you must include this library as a dependency.

[Rainfall-web](https://github.com/aurbroszniowski/Rainfall-web) is the Web Application performance testing implementation.

[Rainfall-jcache](https://github.com/aurbroszniowski/Rainfall-jcache) is the JSR107 caches performance testing implementation.

[Rainfall-ehcache](https://github.com/aurbroszniowski/Rainfall-ehcache) is the Ehcache 2.x/3.x performance testing implementation.


Quick start
-----------

This module is Rainfall-core.
It contains the base classes that you will extend in order to write your own performance framework.

*io.rainfall.Configuration*
Your test will define a configuration (e.g. doing http calls to the twitter search page).

*io.rainfall.Execution*
Your test will define a scenario, made of a serie of executions (e.g. do nothing, do an operation once)

*io.rainfall.Operation*
Each execution of the test does some specific Operation (e.g. do the http call with some parameter)


A certain number of those classes are already implemented and available for your tests. See details [on the wiki](https://github.com/aurbroszniowski/Rainfall-core/wiki)


Performance test
----------------
**Beware, Rainfall-core is only the core library, in order to write tests, you need to use an existing implementation 
(e.g. Rainfall-jcache) or write an implementation yourself**

Performance tests are written in java, we will cover a simple example using Rainfall web:

This tests the performance of calling the Twitter search page.
The scenario is a serie of three consecutive queries that will search for a text string.
It will simulate 5 concurrent users doing nothing for 5 seconds then doing the operations of the scenario.
```java
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
Rainfall is compiled with Java 6, 7 or 8
```maven
  mvn clean install
```

Use it in your project
----------------------
```maven
  <dependencies>
    <dependency>
      <groupId>io.rainfall</groupId>
      <artifactId>rainfall-core</artifactId>
      <version>1.0.3-SNAPSHOT</version>
    </dependency>
  </dependencies>
```

Writing your own performance framework
--------------------------------------
You need to create a new project, similarly to one of the existing implementations (Rainfall-jcache, Rainfall-ehcache, Rainfall-web).


Thanks to the following companies for their support to FOSS:
------------------------------------------------------------

[ej-technologies for JProfiler](http://www.ej-technologies.com/products/jprofiler/overview.html)

[Sonatype for Nexus](http://www.sonatype.org/)

[Cloudbees for cloud-based continuous delivery](https://www.cloudbees.com/)

and of course [Github](https://github.com/) for hosting this project.

