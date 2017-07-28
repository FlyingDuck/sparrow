
package club.cookbean.sparrow.factory;


import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.config.impl.PooledExecutionServiceConfiguration;
import club.cookbean.sparrow.service.ExecutionService;
import club.cookbean.sparrow.service.impl.OnDemandExecutionService;
import club.cookbean.sparrow.service.impl.PooledExecutionService;

/**
 *
 * @author cdennis
 */
public class DefaultExecutionServiceFactory implements ServiceFactory<ExecutionService> {

  @Override
  public ExecutionService create(ServiceCreationConfiguration<ExecutionService> configuration) {
    if (configuration == null) {
      return new OnDemandExecutionService();
    } else if (configuration instanceof PooledExecutionServiceConfiguration) {
      return new PooledExecutionService((PooledExecutionServiceConfiguration) configuration);
    } else {
      throw new IllegalArgumentException("Expected a configuration of type PooledExecutionServiceConfiguration but got " + configuration
          .getClass()
          .getSimpleName());
    }
  }

  @Override
  public Class<ExecutionService> getServiceType() {
    return ExecutionService.class;
  }

}
