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
package club.cookbean.sparrow.function.impl;


import club.cookbean.sparrow.function.Function;

public class MemoizingFunction<key, Result> implements Function<key, Result> {
    private final Function<key, Result> function;
    private boolean handled;
    private Result result;

    public static <key, Result> MemoizingFunction<key, Result> memoize(Function<key, Result> function) {
        return new MemoizingFunction<>(function);
    }

    private MemoizingFunction(Function<key, Result> pairFunction) {
        this.function = pairFunction;
    }

    @Override
    public Result apply(key key) {
        if (handled) {
            return result;
        }
        handled = true;
        result = function.apply(key);
        return result;
    }
}
