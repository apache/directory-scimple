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

package org.apache.directory.scim.spring;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.ws.rs.core.Application;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.repository.RepositoryRegistry;
import org.apache.directory.scim.core.repository.SelfIdResolver;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.protocol.UserResource;
import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.apache.directory.scim.server.rest.EtagGenerator;
import org.apache.directory.scim.server.rest.ScimResourceHelper;
import org.apache.directory.scim.server.rest.UserResourceImpl;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Autoconfigures default beans needed for Apache SCIMple.
 */
@Configuration
@AutoConfigureBefore(JerseyAutoConfiguration.class)
public class ScimpleSpringConfiguration {

  @Bean
  @ConditionalOnMissingBean
  ServerConfiguration serverConfiguration() {
    return new ServerConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean
  EtagGenerator etagGenerator() {
    return new EtagGenerator();
  }

  @Bean
  @ConditionalOnMissingBean
  SchemaRegistry schemaRegistry() {
    return new SchemaRegistry();
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(SelfIdResolver.class)
  Instance<SelfIdResolver> selfIdResolverInstance(SelfIdResolver selfIdResolver) {
    return SpringInstance.of(selfIdResolver);
  }

  @Bean
  @ConditionalOnMissingBean
  RepositoryRegistry repositoryRegistry(SchemaRegistry schemaRegistry, List<Repository<? extends ScimResource>> scimResources) {
    return new RepositoryRegistry(schemaRegistry, scimResources);
  }

  @Bean
  @ConditionalOnMissingBean
  Application jaxrsApplication() {
    return new ScimpleJaxRsApplication();
  }

  @Bean
  @ConditionalOnMissingBean
  public ResourceConfig conf(Application app) {
    ResourceConfig config = ResourceConfig.forApplication(app);

    config.register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(UserResourceImpl.class).to(UserResource.class); // Used by SelfResource
      }
    });
    return config;
  }

  /**
   * Basic JAX-RS application that includes the required SCIMple classes.
   */
  static class ScimpleJaxRsApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
      return ScimResourceHelper.scimpleFeatureAndResourceClasses();
    }
  }

  /**
   * An implementation of {@code Instance} to expose Spring beans.
   * @param <T> the required bean type
   */
  static class SpringInstance<T> implements Instance<T> {

    private final List<T> beans;

    public SpringInstance(T[] beans) {
      this(Arrays.asList(beans));
    }

    public SpringInstance(List<T> beans) {
      this.beans = beans;
    }

    @Override
    public Instance<T> select(Annotation... qualifiers) {
      throw new NotImplementedException("This implementation does not support the `select` method.");
    }

    @Override
    public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
      throw new NotImplementedException("This implementation does not support the `select` method.");
    }

    @Override
    public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
      throw new NotImplementedException("This implementation does not support the `select` method.");
    }

    @Override
    public boolean isUnsatisfied() {
      return beans.isEmpty();
    }

    @Override
    public boolean isAmbiguous() {
      return beans.size() > 1;
    }

    @Override
    public void destroy(T instance) {
      beans.remove(instance);
    }

    @Override
    public Handle<T> getHandle() {
      throw new NotImplementedException("This implementation does not support the `getHandle` method.");
    }

    @Override
    public Iterable<? extends Handle<T>> handles() {
      throw new NotImplementedException("This implementation does not support the `handles` method.");
    }

    @Override
    public T get() {
      if (isAmbiguous()) {
        throw new IllegalStateException("Multiple bean instances found, expecting only one.");
      }
      return beans.stream()
        .findFirst()
        .orElse(null);
    }

    @Override
    public Iterator<T> iterator() {
      return beans.iterator();
    }

    public static <T> SpringInstance<T> of(T... beans) {
      return new SpringInstance<>(beans);
    }
  }
}
