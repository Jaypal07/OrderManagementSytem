package com.jaypal.oms.order.domain.exception;

import com.jaypal.oms.shared.kernel.DomainException;

public class InvalidOrderStateException extends DomainException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
