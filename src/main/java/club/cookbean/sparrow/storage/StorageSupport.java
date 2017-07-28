package club.cookbean.sparrow.storage;


import club.cookbean.sparrow.provider.ServiceProvider;
import club.cookbean.sparrow.redis.RedisResource;
import club.cookbean.sparrow.service.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class StorageSupport {

    private StorageSupport() {}

    public static Storage.Provider selectSorageProvider(ServiceProvider<Service> serviceProvider, RedisResource.ResourceType resourceType) {
        final Collection<Storage.Provider> storeProviders = serviceProvider.getServicesOfType(Storage.Provider.class);

        List<Storage.Provider> chooices = new ArrayList<>(storeProviders.size());
        for (final Storage.Provider provider : storeProviders) {
            if (provider.choose(resourceType)) {
                chooices.add(provider);
            }
        }

        // check
        if (chooices.isEmpty()) {
            final StringBuilder sb = new StringBuilder("No Store.Provider found to handleWriteSingle configured resource types ");
            sb.append(resourceType);
            sb.append(" from ");
            formatStoreProviders(storeProviders, sb);
            throw new IllegalStateException(sb.toString());
        } else if (chooices.size() > 1) {
            final StringBuilder sb = new StringBuilder("Multiple Store.Providers found to handleWriteSingle configured resource types ");
            sb.append(resourceType);
            sb.append(": ");
            formatStoreProviders(chooices, sb);
            throw new IllegalStateException(sb.toString());
        }

        return chooices.get(0);
    }

    private static StringBuilder formatStoreProviders(final Collection<Storage.Provider> storeProviders, final StringBuilder sb) {
        sb.append('{');
        boolean prependSeparator = false;
        for (final Storage.Provider provider : storeProviders) {
            if (prependSeparator) {
                sb.append(", ");
            } else {
                prependSeparator = true;
            }
            sb.append(provider.getClass().getName());
        }
        sb.append('}');
        return sb;
    }
}
