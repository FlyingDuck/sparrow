package club.cookbean.sparrow.function.impl;


import club.cookbean.sparrow.function.PairFunction;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-24. <br><br>
 * Desc:
 */
public class MemoizingPairFunction<A, B, R> implements PairFunction<A, B, R> {
    private final PairFunction<A, B, R> function;
    private boolean handled;
    private R value;

    public static <A, B, R> MemoizingPairFunction<A, B, R> memoize(PairFunction<A, B, R> function) {
        return new MemoizingPairFunction<>(function);
    }

    private MemoizingPairFunction(PairFunction<A, B, R> pairFunction) {
        this.function = pairFunction;
    }


    @Override
    public R apply(A paramA, B paramB) {
        if (handled) {
            return value;
        }
        handled = true;
        value = function.apply(paramA, paramB);
        return value;
    }
}
