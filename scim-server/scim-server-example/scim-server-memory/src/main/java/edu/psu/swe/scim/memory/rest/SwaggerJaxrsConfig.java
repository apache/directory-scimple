package edu.psu.swe.scim.memory.rest;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Swagger;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.ServletContext;
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