package club.cookbean.sparrow.config.impl;


import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.provider.WriteBehindProvider;

/**
 *
 * @author cdennis
 */
public class WriteBehindProviderConfiguration implements ServiceCreationConfiguration<WriteBehindProvider> {

  private final String threadPoolAlias;

  public WriteBehindProviderConfiguration(String threadPoolAlias) {
    this.threadPoolAlias = threadPoolAlias;
  }

  public String getThreadPoolAlias() {
    return threadPoolAlias;
  }

  @Override
  public Class<WriteBehindProvider> getServiceType() {
    return WriteBehindProvider.class;
  }
}
