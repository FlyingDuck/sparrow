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
package club.cookbean.sparrow.factory;


import club.cookbean.sparrow.config.ServiceCreationConfiguration;
import club.cookbean.sparrow.factory.ServiceFactory;
import club.cookbean.sparrow.storage.standalone.StandaloneStorage;

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
