/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at

* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.example.memory.rest;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

@WebListener
public class SwaggerJaxrsConfig implements ServletContextListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerJaxrsConfig.class);

  public static BeanConfig beanConfig = new BeanConfig();

  public void contextInitialized(ServletContextEvent event) {
    LOGGER.debug("Initializing swagger...");

    try {
      // BeanConfig beanConfig = new BeanConfig();
      beanConfig.setBasePath(event.getServletContext().getContextPath() + "/v2");
      beanConfig.setResourcePackage("edu.psu.swe.scim");
      beanConfig.setScan(true);
      beanConfig.setTitle("In-Memory SCIM Server");
      beanConfig.setDescription("In Memory SCIM Server Example Implementation");
      beanConfig.setVersion("2.0");

      Json.mapper().registerModule(new JaxbAnnotationModule());
      Yaml.mapper().registerModule(new JaxbAnnotationModule());

    } catch (Exception e) {
      LOGGER.error("Error initializing swagger", e);
    }
  }

  public void contextDestroyed(ServletContextEvent event) {
    // do on application destroy
  }
}
