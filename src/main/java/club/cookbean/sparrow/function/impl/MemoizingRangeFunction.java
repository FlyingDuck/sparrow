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

import club.cookbean.sparrow.function.RangeFunction;

import java.util.List;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/7 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public class MemoizingRangeFunction<Key, Start, End, Value> implements RangeFunction<Key, Start, End, Value> {

    private final RangeFunction<Key, Start, End, Value> function;
    private boolean handled;
    private List<Value> values;

    public static <Key, Start, End, Value> MemoizingRangeFunction<Key, Start, End, Value> memoize(RangeFunction<Key, Start, End, Value> function) {
        return new MemoizingRangeFunction<>(function);
    }

    private MemoizingRangeFunction(RangeFunction<Key, Start, End, Value> function) {
        this.function = function;
    }

    @Override
    public List<Value> apply(Key key, Start start, End end) {
        if (handled) {
            return values;
        }
        handled = true;
        values = this.function.apply(key, start, end);
        return values;
    }
}
