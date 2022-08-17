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

package org.apache.directory.scim.example.spring;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.ws.rs.core.Application;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.directory.scim.server.provider.Provider;
import org.apache.directory.scim.server.provider.ProviderRegistry;
import org.apache.directory.scim.server.provider.SelfIdResolver;
import org.apache.directory.scim.server.rest.RequestContext;
import org.apache.directory.scim.server.rest.ScimResourceHelper;
import org.apache.directory.scim.server.rest.UserResourceImpl;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.server.utility.AttributeUtil;
import org.apache.directory.scim.server.utility.EtagGenerator;
import org.apache.directory.scim.spec.extension.ScimExtensionRegistry;
import org.apache.directory.scim.spec.protocol.UserResource;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * This class would be moved to a Spring-Boot module, and used to autoconfigure an application.
 */
@Configuration
public class ScimpleSpringConfiguration {

// TODO: ServerConfiguration should be configured based on application.properties?
// TODO: commented out for now, @ConditionalOnMissingBean would only work if this were an AutoConfig from another module
//  @Bean
//  @ConditionalOnMissingBean
//  ServerConfiguration serverConfiguration() {
//    return new ServerConfiguration();
//  }

  @Bean
  @ConditionalOnMissingBean
  Registry registry() {
    return new Registry();
  }

  @Bean
  @ConditionalOnMissingBean
  Instance<SelfIdResolver> selfIdResolverInstance(SelfIdResolver selfIdResolver) {
    return SpringInstance.of(selfIdResolver);
  }

  @Bean
  @ConditionalOnMissingBean
  ProviderRegistry providerRegistry(Registry registry, ScimExtensionRegistry scimExtensionRegistry, List<Provider<? extends ScimResource>> scimResources) {
    ProviderRegistry providerRegistry = new ProviderRegistry(registry, scimExtensionRegistry, new SpringInstance<>(scimResources));
    providerRegistry.configure();
    return providerRegistry;
  }

  @Bean
  @ConditionalOnMissingBean
  ScimExtensionRegistry scimExtensionRegistry() {
    return ScimExtensionRegistry.getInstance();
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
        bind(RequestContext.class).to(RequestContext.class); // needs to load from HK2 to include @Context
        bind(UserResourceImpl.class).to(UserResource.class); // Used by SelfResource

        // basic beans, this could also be defined as @Beans above too
        bind(EtagGenerator.class).to(EtagGenerator.class);
        bind(AttributeUtil.class).to(AttributeUtil.class);
//        bind(ServerConfiguration.class).to(ServerConfiguration.class);
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
      return new HashSet<>(ScimResourceHelper.getScimClassesToLoad());
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
