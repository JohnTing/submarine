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

package org.apache.submarine.server.rest;

import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.submarine.server.job.JobManager;
import org.apache.submarine.server.api.job.Job;
import org.apache.submarine.server.response.JsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.submarine.server.api.job.JobSubmitter;
import org.apache.submarine.server.api.spec.JobSpec;
import org.apache.submarine.commons.utils.exception.SubmarineRuntimeException;

/**
 * The API for retrieving job logs
 */
@Path(RestConstants.V1 + "/" + RestConstants.LOGS)
@Produces({ MediaType.TEXT_PLAIN + "; " + RestConstants.CHARSET_UTF8 })
public class JobLogsRestApi {
  private final Logger LOG = LoggerFactory.getLogger(JobLogsRestApi.class);
  private final JobManager jobManager = JobManager.getInstance();

  @GET
  @Path("/{id}")
  public Response getLog(@PathParam(RestConstants.JOB_ID) String id) {

    Job job;
    JobSpec spec;
    try {
      job = jobManager.getJob(id);
      spec = job.getSpec();

    } catch (SubmarineRuntimeException e) {
      return parseJobServiceException(e);
    }

    final JobSubmitter submitter = jobManager.getSubmitter(spec.getSubmitterSpec().getType());
    String logString = submitter.getLog(job);
    return new JsonResponse.Builder<String>(Response.Status.OK).result(logString).build();
  }
  
  @Context
  private HttpServletResponse httpServletResponse;
  @GET
  @Path("/stream/{id}")
  public Response getLogServlet(@PathParam(RestConstants.JOB_ID) String id) {
    
    Job job;
    JobSpec spec;
    try {
      job = jobManager.getJob(id);
      spec = job.getSpec();
    } catch (SubmarineRuntimeException e) {
      LOG.debug(e.toString());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
    }
    ServletOutputStream servletOutputStream;
    httpServletResponse.setBufferSize(4);
    try {
      servletOutputStream = httpServletResponse.getOutputStream();
      final JobSubmitter submitter = jobManager.getSubmitter(spec.getSubmitterSpec().getType());
      InputStream inputStream = submitter.getLogStream(job);
      int onebyte = -1;
      while ((onebyte = inputStream.read()) != -1) {
        servletOutputStream.print((char) onebyte);
        servletOutputStream.flush();
        httpServletResponse.flushBuffer();
      }
    } catch (Exception e) {
      LOG.error(e.toString());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
    }
    return Response.status(Response.Status.OK).entity("").build();
  }
  private Response parseJobServiceException(SubmarineRuntimeException e) {
    return new JsonResponse.Builder<String>(e.getCode()).message(e.getMessage()).build();
  }
}
