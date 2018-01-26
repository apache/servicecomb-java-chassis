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

package org.apache.servicecomb.samples.overwatch.finance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.servicecomb.metrics.common.HealthCheckResult;
import org.apache.servicecomb.metrics.common.HealthChecker;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class MySQLHealthChecker implements HealthChecker {

  private final String address;

  private final String user;

  private final String password;

  public MySQLHealthChecker() {
    this.address = DynamicPropertyFactory.getInstance().getStringProperty("mysql.address",
        "jdbc:mysql://localhost:3306/finance_db?useSSL=false").get();
    this.user = DynamicPropertyFactory.getInstance().getStringProperty("mysql.user",
        "root").get();
    this.password = DynamicPropertyFactory.getInstance().getStringProperty("mysql.password",
        "password").get();
  }

  @Override
  public String getName() {
    return "finance_db";
  }

  @Override
  public HealthCheckResult check() {
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(address, user, password);
      return new HealthCheckResult(true, "mysql health check", "");
    } catch (SQLException e) {
      e.printStackTrace();
      return new HealthCheckResult(false, "mysql health check", e.toString());
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
