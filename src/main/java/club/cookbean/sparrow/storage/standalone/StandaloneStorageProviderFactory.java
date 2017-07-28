package club.cookbean.sparrow.storage.standalone;


import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.factory.ServiceFactory;

public class StandaloneStorageProviderFactory implements ServiceFactory<StandaloneStorage.Provider> {
    @Override
    public StandaloneStorage.Provider create(ServiceCreationConfiguration<StandaloneStorage.Provider> configuration) {
        return new StandaloneStorage.Provider();
    }

    @Override
    public Class<? extends StandaloneStorage.Provider> getServiceType() {
        return StandaloneStorage.Provider.class;
    }
}
