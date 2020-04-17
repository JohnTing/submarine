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

package org.apache.submarine.server.submitter.k8s;

import java.io.FileReader;
import java.io.IOException;
// import java.io.InputStream;
import java.util.ArrayList;
// import java.util.Dictionary;
//import java.util.HashMap;
//import java.util.Hashtable;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.JSON;
//import io.kubernetes.client.PodLogs;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.CustomObjectsApi;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.apache.submarine.commons.utils.SubmarineConfVars;
import org.apache.submarine.commons.utils.SubmarineConfiguration;
import org.apache.submarine.commons.utils.exception.SubmarineRuntimeException;
import org.apache.submarine.server.api.exception.InvalidSpecException;
import org.apache.submarine.server.api.job.JobSubmitter;
import org.apache.submarine.server.api.job.Job;
import org.apache.submarine.server.api.job.JobLog;
import org.apache.submarine.server.api.spec.JobLibrarySpec;
import org.apache.submarine.server.api.spec.JobSpec;
// import org.apache.submarine.server.api.spec.JobTaskSpec;
import org.apache.submarine.server.submitter.k8s.util.MLJobConverter;
import org.apache.submarine.server.submitter.k8s.model.MLJob;
import org.apache.submarine.server.submitter.k8s.parser.JobSpecParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JobSubmitter for Kubernetes Cluster.
 */
public class K8sJobSubmitter implements JobSubmitter {
  private final Logger LOG = LoggerFactory.getLogger(K8sJobSubmitter.class);

  private String confPath;

  private static final String TF_JOB_SELECTOR_KEY = "tf-job-name=";
  private static final String PYTORCH_JOB_SELECTOR_KEY = "pytorch-job-name=";

  // K8s API client for CRD
  private CustomObjectsApi api;

  private CoreV1Api COREV1_API;

  public K8sJobSubmitter() {}

  @VisibleForTesting
  public K8sJobSubmitter(String confPath) {
    this.confPath = confPath;
  }

  @Override
  public void initialize(SubmarineConfiguration conf) {
    if (confPath == null || confPath.trim().isEmpty()) {
      confPath = conf.getString(
          SubmarineConfVars.ConfVars.SUBMARINE_K8S_KUBE_CONFIG);
    }
    loadClientConfiguration(confPath);
    if (api == null) {
      api = new CustomObjectsApi();
    }
  }

  private void loadClientConfiguration(String path) {
    try {
      KubeConfig config = KubeConfig.loadKubeConfig(new FileReader(path));
      ApiClient client = ClientBuilder.kubeconfig(config).build();
      COREV1_API = new CoreV1Api(client);
      Configuration.setDefaultApiClient(client);
    } catch (Exception e) {
      LOG.warn("Failed to load the configured K8s kubeconfig file: " +
          e.getMessage(), e);

      LOG.info("Assume running in the k8s cluster, " +
          "try to load in-cluster config");
      try {
        ApiClient client = ClientBuilder.cluster().build();
        Configuration.setDefaultApiClient(client);
      } catch (IOException e1) {
        LOG.error("Initialize K8s submitter failed. " + e.getMessage(), e1);
        throw new SubmarineRuntimeException(500, "Initialize K8s submitter failed.");
      }
    }
  }

  @Override
  public String getSubmitterType() {
    return "k8s";
  }

  @Override
  public Job createJob(JobSpec jobSpec) throws SubmarineRuntimeException {
    Job job;
    try {
      MLJob mlJob = JobSpecParser.parseJob(jobSpec);
      Object object = api.createNamespacedCustomObject(mlJob.getGroup(), mlJob.getVersion(),
          mlJob.getMetadata().getNamespace(), mlJob.getPlural(), mlJob, "true");
      job = parseResponseObject(object, ParseOp.PARSE_OP_RESULT);
    } catch (InvalidSpecException e) {
      throw new SubmarineRuntimeException(200, e.getMessage());
    } catch (ApiException e) {
      throw new SubmarineRuntimeException(e.getCode(), e.getMessage());
    }
    return job;
  }

  @Override
  public Job findJob(JobSpec jobSpec) throws SubmarineRuntimeException {
    Job job;
    try {
      MLJob mlJob = JobSpecParser.parseJob(jobSpec);
      Object object = api.getNamespacedCustomObject(mlJob.getGroup(), mlJob.getVersion(),
          mlJob.getMetadata().getNamespace(), mlJob.getPlural(), mlJob.getMetadata().getName());
      job = parseResponseObject(object, ParseOp.PARSE_OP_RESULT);
    } catch (InvalidSpecException e) {
      throw new SubmarineRuntimeException(200, e.getMessage());
    } catch (ApiException e) {
      throw new SubmarineRuntimeException(e.getCode(), e.getMessage());
    }
    return job;
  }

  @Override
  public Job patchJob(JobSpec jobSpec) throws SubmarineRuntimeException {
    Job job;
    try {
      MLJob mlJob = JobSpecParser.parseJob(jobSpec);
      Object object = api.patchNamespacedCustomObject(mlJob.getGroup(), mlJob.getVersion(),
          mlJob.getMetadata().getNamespace(), mlJob.getPlural(), mlJob.getMetadata().getName(),
          mlJob);
      job = parseResponseObject(object, ParseOp.PARSE_OP_RESULT);
    } catch (InvalidSpecException e) {
      throw new SubmarineRuntimeException(200, e.getMessage());
    } catch (ApiException e) {
      throw new SubmarineRuntimeException(e.getCode(), e.getMessage());
    }
    return job;
  }

  @Override
  public Job deleteJob(JobSpec jobSpec) throws SubmarineRuntimeException {
    Job job;
    try {
      MLJob mlJob = JobSpecParser.parseJob(jobSpec);
      Object object = api.deleteNamespacedCustomObject(mlJob.getGroup(), mlJob.getVersion(),
          mlJob.getMetadata().getNamespace(), mlJob.getPlural(), mlJob.getMetadata().getName(),
          MLJobConverter.toDeleteOptionsFromMLJob(mlJob), null, null, null);
      job = parseResponseObject(object, ParseOp.PARSE_OP_DELETE);
    } catch (InvalidSpecException e) {
      throw new SubmarineRuntimeException(200, e.getMessage());
    } catch (ApiException e) {
      throw new SubmarineRuntimeException(e.getCode(), e.getMessage());
    }
    return job;
  }

  private Job parseResponseObject(Object object, ParseOp op) throws SubmarineRuntimeException {
    Gson gson = new JSON().getGson();
    String jsonString = gson.toJson(object);
    LOG.info("Upstream response JSON: {}", jsonString);
    try {
      if (op == ParseOp.PARSE_OP_RESULT) {
        MLJob mlJob = gson.fromJson(jsonString, MLJob.class);
        return MLJobConverter.toJobFromMLJob(mlJob);
      } else if (op == ParseOp.PARSE_OP_DELETE) {
        V1Status status = gson.fromJson(jsonString, V1Status.class);
        return MLJobConverter.toJobFromStatus(status);
      }
    } catch (JsonSyntaxException e) {
      LOG.warn("K8s submitter: parse response object failed by " + e.getMessage(), e);
    }
    throw new SubmarineRuntimeException(500, "K8s Submitter parse upstream response failed.");
  }

  @Override
  public List<JobLog> getJobLog(JobSpec jobSpec) {
    List<JobLog> jobLoglist = new ArrayList<JobLog>();
    try {
      final V1PodList podList = COREV1_API.listNamespacedPod(
          jobSpec.getSubmitterSpec().getNamespace(),
          "false", null, null,
          getJobLabelSelector(jobSpec), null, null,
          null, null);
      JobLog jobLog = new JobLog();
      for (V1Pod pod : podList.getItems()) {
        String podName = pod.getMetadata().getName();
        String namespace = pod.getMetadata().getNamespace();
        String podLog = COREV1_API.readNamespacedPodLog(
            podName, namespace, null, Boolean.FALSE,
            Integer.MAX_VALUE, null, Boolean.FALSE, 
            Integer.MAX_VALUE, null, Boolean.FALSE);
  
        jobLog.setPodName(podName);
        jobLog.setPodLog(podLog);
        jobLoglist.add(jobLog);
      }
    } catch (final ApiException e) {
      LOG.error("Error when listing pod for job:" + jobSpec.getName(), e.getMessage());
    }
  
    return jobLoglist;
  }
  
  /*
  public HashMap<String, InputStream> getLogStream(final Job job) {
    if (job == null) {
      return null;
    }
    final CoreV1Api COREV1_API = new CoreV1Api(client);
    HashMap<String, InputStream> LogStreamMap = new HashMap<String, InputStream>();

    try {
      final V1PodList podList = COREV1_API.listNamespacedPod(
          job.getSpec().getSubmitterSpec().getNamespace(),
          "false", null, null,
          getJobLabelSelector(job), null, null,
          null, null);

      for (V1Pod pod : podList.getItems()) {
        String podName = pod.getMetadata().getName();
        PodLogs logs = new PodLogs();
        InputStream inputStream = logs.streamNamespacedPodLog(pod);
        LogStreamMap.put(podName, inputStream);
      }

      return LogStreamMap;

    } catch (final ApiException e) {
      LOG.warn("Error when listing pod for job:" + job.toString(), e.getMessage());
    } catch (final IOException e) {
      LOG.warn("Error when get pod log stream", e.getMessage());
    }
    return null;
  }
  */

  private String getJobLabelSelector(JobSpec jobSpec) {
    if (jobSpec.getLibrarySpec()
        .getName().equalsIgnoreCase(JobLibrarySpec.SupportedMLFramework.TENSORFLOW.getName())) {
      return TF_JOB_SELECTOR_KEY + jobSpec.getName();
    } else {
      return PYTORCH_JOB_SELECTOR_KEY + jobSpec.getName();
    }
  }

  private enum ParseOp {
    PARSE_OP_RESULT,
    PARSE_OP_DELETE
  }
}
