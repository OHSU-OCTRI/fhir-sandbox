package org.octri.fhir_sandbox.exception;

import org.springframework.http.HttpStatus;

/**
 * An exception to display to users.
 */
public class DisplayedException extends RuntimeException {

    private HttpStatus httpStatus;

    public DisplayedException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}