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
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import org.apache.directory.scim.server.configuration.ServerConfiguration;

/**
 * CDI Lite build compatible extension that adds a default ServerConfiguration implementation.
 *
 */
public class ScimServerBuildCompatibleExtension implements BuildCompatibleExtension {

  private boolean serverConfigFound = false;

  @Registration(types = ServerConfiguration.class)
  public void registration(BeanInfo beanInfo) {
    // detect if any ServerConfiguration beans are found, if a default bean will be created below
    serverConfigFound = true;
  }

  @Synthesis
  public void synthesise(SyntheticComponents syn, Messages messages) {
    // if a ServerConfiguration bean was not found during registration, a default one will be added (along with a warning)
    if (!serverConfigFound) {
      messages.warn("It is recommended to provide a ServerConfiguration bean to configure SCIMple, a default instance will be used.");
      syn.addBean(ServerConfiguration.class)
        .type(ServerConfiguration.class)
        .scope(ApplicationScoped.class)
        .createWith(DefaultServerConfigurationCreator.class);
    }
  }

  public static class DefaultServerConfigurationCreator implements SyntheticBeanCreator<ServerConfiguration> {
    @Override
    public ServerConfiguration create(Instance<Object> lookup, Parameters params) {
      return new ServerConfiguration();
    }
  }
}
