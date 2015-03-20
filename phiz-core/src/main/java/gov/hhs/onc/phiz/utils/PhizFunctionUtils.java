package gov.hhs.onc.phiz.utils;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.commons.lang3.ClassUtils;

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

    public static <T, U> Stream<U> mapAssignable(Stream<T> inStream, Class<U> clazz) {
        return filterAssignable(inStream, clazz).map(clazz::cast);
    }

    public static <T, U> Stream<T> filterAssignable(Stream<T> inStream, Class<U> clazz) {
        return inStream.filter(((Predicate<T>) Objects::nonNull).and(obj -> ClassUtils.isAssignable(obj.getClass(), clazz)));
    }
}
