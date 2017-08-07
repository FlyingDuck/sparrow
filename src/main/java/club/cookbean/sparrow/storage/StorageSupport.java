/* Copyright 2017 Bennett Dong. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            final StringBuilder sb = new StringBuilder("No Store.Provider found to handleSet configured resource types ");
            sb.append(resourceType);
            sb.append(" from ");
            formatStoreProviders(storeProviders, sb);
            throw new IllegalStateException(sb.toString());
        } else if (chooices.size() > 1) {
            final StringBuilder sb = new StringBuilder("Multiple Store.Providers found to handleSet configured resource types ");
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
