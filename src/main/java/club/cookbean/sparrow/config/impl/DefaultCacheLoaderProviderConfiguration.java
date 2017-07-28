
package club.cookbean.sparrow.config.impl;


import club.cookbean.sparrow.clz.ClassInstanceProviderConfiguration;
import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.loader.CacheLoader;
import club.cookbean.sparrow.provider.CacheLoaderProvider;

public class DefaultCacheLoaderProviderConfiguration extends ClassInstanceProviderConfiguration<String, CacheLoader>
        implements ServiceCreationConfiguration<CacheLoaderProvider> {

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<CacheLoaderProvider> getServiceType() {
    return CacheLoaderProvider.class;
  }

  /**
   * Adds a default {@link CacheLoaderWriter} class and associated constuctor arguments to be used with a cache matching
   * the provided alias.
   *
   * @param alias the cache alias
   * @param clazz the cache loader writer class
   * @param arguments the constructor arguments
   *
   * @return this configuration instance
   */
  public DefaultCacheLoaderProviderConfiguration addLoaderFor(String alias, Class<? extends CacheLoader> clazz, Object... arguments) {
    getDefaults().put(alias, new DefaultCacheLoaderConfiguration(clazz, arguments));
    return this;
  }
}
