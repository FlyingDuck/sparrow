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
package club.cookbean.sparrow.provider;

import club.cookbean.sparrow.service.Service;

import java.util.Collection;

public interface ServiceProvider<T extends Service> {

    /**
     * Looks up the {@link Service} of the given {@code serviceType}.
     * <p>
     * There is no guarantee that services returned here will be started.
     *
     * @param serviceType the {@code class} of the service being looked up
     * @param <U> the {@link Service} type
     * @return a service instance of type {@code T}, or {@code null} if it couldn't be located
     *
     * @throws IllegalArgumentException if {@code serviceType} is marked with the
     *        {@link club.cookbean.sparrow.annotation.PluralService PluralService} annotation
     *
     * @see Service#start(ServiceProvider)
     */
    <U extends T> U getService(Class<U> serviceType);

    /**
     * Looks up all {@link Service} instances that are subtypes of the given {@code serviceType} supplied.
     * <p>
     * This method must be used to retrieves service types marked with the
     * {@link club.cookbean.sparrow.annotation.PluralService PluralService} annotation.
     *
     * @param serviceType the {@code class} of the service being looked up
     * @param <U> the {@link Service} type
     * @return all the service instances assignable to {@code serviceType}
     */
    <U extends T> Collection<U> getServicesOfType(Class<U> serviceType);

}
