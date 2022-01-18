package com.example.paymentmodule.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationException {

    private String defaultMessage;
    private String objectName;
    private String field;
    private Object rejectedValue;


    public static final class ValidationExceptionBuilder {
        private String defaultMessage;
        private String objectName;
        private String field;
        private Object rejectedValue;

        private ValidationExceptionBuilder() {
        }

        public static ValidationExceptionBuilder aValidationException() {
            return new ValidationExceptionBuilder();
        }

        public ValidationExceptionBuilder withDefaultMessage(String defaultMessage) {
            this.defaultMessage = defaultMessage;
            return this;
        }

        public ValidationExceptionBuilder withObjectName(String objectName) {
            this.objectName = objectName;
            return this;
        }

        public ValidationExceptionBuilder withField(String field) {
            this.field = field;
            return this;
        }

        public ValidationExceptionBuilder withRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
            return this;
        }

        public ValidationException build() {
            ValidationException validationException = new ValidationException();
            validationException.setDefaultMessage(defaultMessage);
            validationException.setObjectName(objectName);
            validationException.setField(field);
            validationException.setRejectedValue(rejectedValue);
            return validationException;
        }
    }
}
