package de.borekking.banSystem.util.functional;

@FunctionalInterface
public interface ExceptionConsumer<T, E extends Exception> {

    void accept(T t) throws E;

}
