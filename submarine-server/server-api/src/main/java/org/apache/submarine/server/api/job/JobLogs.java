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

package org.apache.submarine.server.api.job;

//import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
//import java.util.List;
import java.util.Map;

public class JobLogs {

  private String jobId;
  private String jobType;
  private Map<String, String> LogContent;

  public JobLogs(String jobId, String jobType) {
    jobId = this.jobId;
    jobType = this.jobType;
    LogContent = new HashMap<String, String>();
  }

  public void addLog(String podName, String podLog) {
    LogContent.put(podName, podLog);
  }

  public String getLog(String podName) {
    return LogContent.get(podName);
  }


  public void clearLog() {
    LogContent.clear();
  }

  public Collection<String> getLogNames() {
    return LogContent.values();
  }

  public HashMap<String, String> getLogContent() {
    return new HashMap<String, String>(LogContent);
  }

  public void setLogMap(Map<String, String> logContent) {
    LogContent = new HashMap<String, String>(logContent);
  }
}

