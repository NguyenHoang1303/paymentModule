package com.example.paymentmodule.enums;

public enum Status {
    ACTIVE, DELETE;

    public enum Payment {
        PAID, UNPAID, REFUND, REFUNDED, FAIL
    }

    public enum Transaction {
        SUCCESS, FAIL
    }

    public enum Order {
        CANCEL, PENDING, PROCESSING, DONE, REFUND
    }
}
