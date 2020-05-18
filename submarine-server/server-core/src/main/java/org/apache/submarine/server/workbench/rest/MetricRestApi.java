/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.submarine.server.workbench.rest;

import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import org.apache.ibatis.session.SqlSession;
import org.apache.submarine.server.workbench.annotation.SubmarineApi;
//import org.apache.submarine.server.workbench.database.MyBatisUtil;
import org.apache.submarine.server.workbench.database.entity.Metric;
//import org.apache.submarine.server.workbench.database.entity.SysUser;
//import org.apache.submarine.server.workbench.database.mappers.MetricMapper;
//import org.apache.submarine.server.workbench.database.mappers.SysUserMapper;
import org.apache.submarine.server.workbench.database.service.MetricService;
import org.apache.submarine.server.response.JsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
//import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
//import java.util.HashMap;

@Path("/metric")
@Produces("application/json")
@Singleton
public class MetricRestApi {
  private static final Logger LOG = LoggerFactory.getLogger(LoginRestApi.class);
  private static final Gson gson = new Gson();
  MetricService metricService = new MetricService();

  @Inject
  public MetricRestApi() {
  }

  @GET
  @Path("/")
  @SubmarineApi
  public Response getMetric(@PathParam("key") String key) {
    Metric metric;
    try {
      metric = metricService.selectByPrimaryKey(key);
      
    } catch (Exception e) {

      LOG.error(e.toString());
      e.printStackTrace();
      return null;
    }
    

    return new JsonResponse.Builder<String>(Response.Status.OK).success(true).result(metric.job_name).build();
  }
}