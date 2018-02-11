/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.samples.metrics.extendhealthcheck;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.servicecomb.foundation.metrics.health.HealthCheckResult;
import org.apache.servicecomb.foundation.metrics.health.HealthChecker;


//this is a demo health checker for mysql
public class MySqlHealthChecker implements HealthChecker {
  @Override
  public String getName() {
    return "mysql";
  }

  @Override
  public HealthCheckResult check() {
    //add your health check code here
    Connection connection = null;
    try {
      connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test_db?useSSL=false", "root", "pwd");
      return new HealthCheckResult(true, "local mysql health check", "");
    } catch (SQLException e) {
      e.printStackTrace();
      return new HealthCheckResult(false, "local mysql health check", e.toString());
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
