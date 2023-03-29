package dev.redio.annotation;

public final class Expression {
    public static <T> void result(T result) {
        throw new Error("Annotation processing not successful");
    }
}
