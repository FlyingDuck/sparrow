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
package club.cookbean.sparrow.util;

/**
 * This is a modified version of the original Apache class.
 * <p>
 * Helpers for {@code java.lang.System}.
 * <p>
 * If a system property cannot be read due to security restrictions, the corresponding field in this class will be set
 * to {@code null} and a message will be written to {@code System.err}.
 * <p>
 * #ThreadSafe#
 */
public class SystemUtils {

    /**
     * SystemUtils instances should NOT be constructed in standard programming. Instead, the class should be used as
     * {@code SystemUtils.FILE_SEPARATOR}.
     * <p>
     * This constructor is public to permit tools that require a JavaBean instance to operate.
     */
    public SystemUtils() {
        super();
    }

    /**
     * The {@code java.specification.version} System Property. Java Runtime Environment specification version.
     * <p>
     * Defaults to {@code null} if the runtime does not have security access to read this property or the property does
     * not exist.
     * <p>
     * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)} or
     * {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value will be out of
     * sync with that System property.
     *
     */
    public static final String JAVA_SPECIFICATION_VERSION = getSystemProperty("java.specification.version");
    private static final JavaVersion JAVA_SPECIFICATION_VERSION_AS_ENUM = JavaVersion.get(JAVA_SPECIFICATION_VERSION);

    // -----------------------------------------------------------------------
    /**
     * Gets a System property, defaulting to {@code null} if the property cannot be read.
     * <p>
     * If a {@code SecurityException} is caught, the return value is {@code null} and a message is written to
     * {@code System.err}.
     *
     * @param property the system property name
     * @return the system property value or {@code null} if a security problem occurs
     */
    private static String getSystemProperty(final String property) {
        try {
            return System.getProperty(property);
        } catch (final SecurityException ex) {
            // we are not allowed to look at this property
            System.err.println("Caught a SecurityException reading the system property '" + property
                    + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }

    /**
     * Is the Java version at least the requested version.
     * <p>
     * Example input:
     * <ul>
     *   <li>{@code 1.2f} to test for Java 1.2</li>
     *   <li>{@code 1.31f} to test for Java 1.3.1</li>
     * </ul>
     *
     * @param requiredVersion the required version, for example 1.31f
     * @return {@code true} if the actual version is equal or greater than the required version
     */
    public static boolean isJavaVersionAtLeast(final JavaVersion requiredVersion) {
        return JAVA_SPECIFICATION_VERSION_AS_ENUM.atLeast(requiredVersion);
    }


}
