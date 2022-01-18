package com.example.paymentmodule.exception;

import com.example.paymentmodule.response.RESTResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity handlerNotFoundException(NotFoundException ex, WebRequest req) {
        return new ResponseEntity<>(new RESTResponse.SimpleError()
                .setCode(HttpStatus.NOT_FOUND.value())
                .setMessage(ex.getMessage())
                .build(),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity handlerValidationException(BindException ex) {
        List<FieldError> errors = ex.getFieldErrors();
        List<ValidationException> list = new ArrayList<>();
        HashMap<String,String> hashMap = new HashMap<>();
        for (FieldError e : errors) {
            hashMap.put(e.getField(), e.getDefaultMessage());
        }
        return new ResponseEntity<>(new RESTResponse.Error()
                .addErrors(hashMap)
                .build(), HttpStatus.BAD_REQUEST);
    }


}
