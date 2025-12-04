package org.pulsar.currency.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private final List<Error> errors = new ArrayList<>();

    public void add(Error error) {
        if (error != null) {
            errors.add(error);
        }
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<Error> getErrors() {
        return errors;
    }
}
