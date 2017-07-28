package club.cookbean.sparrow.function.impl;


import club.cookbean.sparrow.function.SingleFunction;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-24. <br><br>
 * Desc:
 */
public class MemoizingSingleFunction<A, R> implements SingleFunction<A, R> {
    private final SingleFunction<A, R> function;
    private boolean handled;
    private R value;

    public static <A, R> MemoizingSingleFunction<A, R> memoize(SingleFunction<A, R> function) {
        return new MemoizingSingleFunction<>(function);
    }

    private MemoizingSingleFunction(SingleFunction<A, R> pairFunction) {
        this.function = pairFunction;
    }

    @Override
    public R apply(A paramA) {
        if (handled) {
            return value;
        }
        handled = true;
        value = function.apply(paramA);
        return value;
    }
}
