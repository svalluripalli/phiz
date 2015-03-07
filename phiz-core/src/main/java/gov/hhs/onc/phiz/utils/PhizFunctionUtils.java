package gov.hhs.onc.phiz.utils;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public final class PhizFunctionUtils {
    private PhizFunctionUtils() {
    }

    public static <T> String[] mapToStringArray(T[] inArr) {
        return mapToStringArray(Stream.of(inArr));
    }

    public static <T> String[] mapToStringArray(Stream<T> inStream) {
        return mapToArray(inStream, Object::toString, String[]::new);
    }

    public static <T, U> U[] mapToArray(T[] inArr, Function<T, U> mapper, IntFunction<U[]> outArrGen) {
        return mapToArray(Stream.of(inArr), mapper, outArrGen);
    }

    public static <T, U> U[] mapToArray(Stream<T> inStream, Function<T, U> mapper, IntFunction<U[]> outArrGen) {
        return inStream.map(mapper).toArray(outArrGen);
    }
}
