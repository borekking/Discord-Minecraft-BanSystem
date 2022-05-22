package de.borekking.banSystem.util.functional;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLExceptionConsumer<T> extends ExceptionConsumer<T, SQLException> {}