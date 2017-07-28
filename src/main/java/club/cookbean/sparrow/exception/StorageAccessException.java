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
package club.cookbean.sparrow.exception;

/**
 * Generic exception used when an internal operation fails on a {@link com.xiaomi.miui.global.redis.Cache}.
 */
public class StorageAccessException extends Exception {

    private static final long serialVersionUID = 5249505200891654779L;

    /**
     * Creates a new exception wrapping the {@link Throwable cause} passed in.
     *
     * @param cause the cause of this exception
     */
    public StorageAccessException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception wrapping the {@link Throwable cause} passed in and with the provided message.
     *
     * @param message information about the exception
     * @param cause the cause of this exception
     */
    public StorageAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception with the provided message.
     *
     * @param message information about the exception
     */
    public StorageAccessException(String message) {
        super(message);
    }

}
