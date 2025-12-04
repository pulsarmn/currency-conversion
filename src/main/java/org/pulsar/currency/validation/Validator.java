package org.pulsar.currency.validation;

public interface Validator<T> {

    ValidationResult validate(T obj);
}
