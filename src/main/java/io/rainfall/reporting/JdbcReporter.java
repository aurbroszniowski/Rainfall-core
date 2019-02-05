/*
 * Copyright (c) 2014-2019 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rainfall.reporting;

import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.StatisticsPeek;
import io.rainfall.statistics.StatisticsPeekHolder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * @author Aurelien Broszniowski
 */
public class JdbcReporter<E extends Enum<E>> implements Reporter<E> {

  private String url;
  private String table;
  private String user;
  private String password;
  private int reportId = -1;

  public JdbcReporter() {
    Properties props = new Properties();
    InputStream input = null;

    try {
      String filename = "rainfall.jdbc.properties";
      input = getClass().getClassLoader().getResourceAsStream(filename);
      if (input == null) {
        throw new RuntimeException("Can not find JDBC properties file in classpath. " + filename);
      }
      props.load(input);

      this.url = props.getProperty("url");
      this.table= props.getProperty("table");
      this.user = props.getProperty("user");
      this.password = props.getProperty("password");

      input.close();

      //TODO: get new reportId from existing in table

    } catch (IOException e) {
      throw new RuntimeException("Error when reading JDBC properties. ", e);
    }
  }

  @Override
  public void header(final List<String> description) {
    //TODO insert header in description
  }

  @Override
  public void report(final StatisticsPeekHolder<E> statisticsHolder) {

    StatisticsPeek<E> peek = statisticsHolder.getTotalStatisticsPeeks();
    try {
      Connection connection = DriverManager.getConnection(this.url, this.user, this.password);
//TODO insert      jobid,
// peek.getSumOfPeriodicCounters(),
//      peek.getSumOfPeriodicTps(),
//          peek.getAverageOfPeriodicAverageLatencies()

      Statement stmt = connection.createStatement();
      String sql = "INSERT INTO Registration " +
                   "VALUES (100, 'Zara', 'Ali', 18)";
      stmt.executeUpdate(sql);

      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException("Error when creating JDBC connection. ", e);
    }
  }

  @Override
  public void summarize(final StatisticsHolder<E> statisticsHolder) {

//    StatisticsPeek<E> peek = statisticsHolder.getTotalStatisticsPeeks();

    try {
      Connection connection = DriverManager.getConnection(this.url, this.user, this.password);
//TODO insert      jobid,
//      peek.getSumOfCumulativeCounters(),
//          peek.getSumOfCumulativeTps(),
//          peek.getAverageOfCumulativeAverageLatencies()

      Statement stmt = connection.createStatement();
      String sql = "INSERT INTO Registration " +
                   "VALUES (100, 'Zara', 'Ali', 18)";
      stmt.executeUpdate(sql);

      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException("Error when creating JDBC connection. ", e);
    }
  }

  public int getReportId() {
    return reportId;
  }

  @Override
  public String toString() {
    return "Jdbc reporter (recording to " + this.url + ")";
  }
}
