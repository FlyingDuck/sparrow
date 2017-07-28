/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.cookbean.sparrow.factory;


import club.cookbean.sparrow.annotation.ServiceDependencies;
import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.config.WriteBehindConfiguration;
import club.cookbean.sparrow.config.impl.WriteBehindProviderConfiguration;
import club.cookbean.sparrow.provider.ServiceProvider;
import club.cookbean.sparrow.provider.WriteBehindProvider;
import club.cookbean.sparrow.service.ExecutionService;
import club.cookbean.sparrow.service.Service;
import club.cookbean.sparrow.writer.CacheWriter;
import club.cookbean.sparrow.writer.WriteBehind;
import club.cookbean.sparrow.writer.impl.StripedWriteBehind;

/**
 * @author Abhilash
 *
 */
public class WriteBehindProviderFactory implements ServiceFactory<WriteBehindProvider> {

  @Override
  public WriteBehindProvider create(ServiceCreationConfiguration<WriteBehindProvider> configuration) {
    if (configuration == null) {
      return new Provider();
    } else if (configuration instanceof WriteBehindProviderConfiguration) {
      return new Provider(((WriteBehindProviderConfiguration) configuration).getThreadPoolAlias());
    } else {
      throw new IllegalArgumentException("WriteBehind configuration must not be provided at CacheManager level");
    }
  }

  @ServiceDependencies(ExecutionService.class)
  public static class Provider implements WriteBehindProvider {

    private final String threadPoolAlias;
    private volatile ExecutionService executionService;

    protected Provider() {
      this(null);
    }

    protected Provider(String threadPoolAlias) {
      this.threadPoolAlias = threadPoolAlias;
    }

    @Override
    public void stop() {
      // no-op

    }

    @Override
    public void start(ServiceProvider<Service> serviceProvider) {
      executionService = serviceProvider.getService(ExecutionService.class);
    }

    @Override
    public CacheWriter createWriteBehindWriter(CacheWriter cacheWriter, WriteBehindConfiguration writeBehindConfiguration) {
      if (cacheWriter == null) {
        throw new NullPointerException("WriteBehind requires a non null CacheLoaderWriter.");
      }
      return new StripedWriteBehind(executionService, threadPoolAlias, writeBehindConfiguration, cacheWriter);
    }

    @Override
    public void releaseWriteBehindWriter(CacheWriter cacheWriter) {
      if(cacheWriter != null) {
        ((WriteBehind)cacheWriter).stop();
      }
    }
  }

  @Override
  public Class<WriteBehindProvider> getServiceType() {
    return WriteBehindProvider.class;
  }

}
