package com.jaypal.oms.inventory.domain.exception;

import com.jaypal.oms.shared.kernel.DomainException;

public class InsufficientStockException extends DomainException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
