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

package org.apache.directory.scim.server.spi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.*;
import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ScimServerInitializer implements Extension {

  private final Logger log = LoggerFactory.getLogger(ScimServerInitializer.class);

  private final Set<Bean<?>> eagerBeans = new HashSet<>();

  public <T> void collect(@Observes ProcessBean<T> event) {
    if (event.getAnnotated().isAnnotationPresent(Eager.class)) {
      eagerBeans.add(event.getBean());
    }
  }

  public <T, X> void collect(@Observes ProcessProducerMethod<T, X> event) {
    if (event.getAnnotated().isAnnotationPresent(Eager.class)) {
      eagerBeans.add(event.getBean());
    }
  }

  public <T, X> void collect(@Observes ProcessProducerField<T, X> event) {
    if (event.getAnnotated().isAnnotationPresent(Eager.class)) {
      eagerBeans.add(event.getBean());
    }
  }

  public void defaultBeans(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
    if (beanManager.getBeans(ServerConfiguration.class).isEmpty()) {
      log.warn("It is recommended to provide a ServerConfiguration bean to configure SCIMple, a default instance will be used.");
      afterBeanDiscovery.addBean()
        .scope(ApplicationScoped.class)
        .types(ServerConfiguration.class)
        .beanClass(ServerConfiguration.class)
        .createWith((CreationalContext<ServerConfiguration> cc) -> {
          AnnotatedType<ServerConfiguration> at = beanManager.createAnnotatedType(ServerConfiguration.class);
          BeanAttributes<ServerConfiguration> ba = beanManager.createBeanAttributes(at);
          return beanManager.createBean(ba, ServerConfiguration.class, beanManager.getInjectionTargetFactory(at)).create(cc);
        });
    }
  }

  public void load(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
    eagerBeans.forEach(bean -> {
      // call a real method so the proxied bean gets created
      beanManager.getReference(bean, Object.class, beanManager.createCreationalContext(bean)).toString();
    });
  }
}
