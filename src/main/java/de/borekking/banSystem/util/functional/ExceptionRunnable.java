package de.borekking.banSystem.util.functional;

@FunctionalInterface
public interface ExceptionRunnable<T extends Exception> {

    void run() throws T;

}
