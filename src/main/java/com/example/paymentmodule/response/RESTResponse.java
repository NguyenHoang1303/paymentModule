package com.example.paymentmodule.response;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * Tạo dữ liệu trả về cho client qua api, đáp ứng một số yêu cầu từ mobile.
 * Không chứa http status code vì phải là tham số trong spring ResponseEntity.
 * */
public class RESTResponse {
    public static String STATUS = "status";
    public static String MESSAGE = "message";
    public static String DATA = "data";
    public static String ERRORS = "errors";
    public static String PAGINATION = "pagination";

    private HashMap<String, Object> response;

    // MUST be private.
    private RESTResponse() {
        this.response = new HashMap<>();
    }

    public HashMap<String, Object> getResponse() {
        return response;
    }

    public void setResponse(HashMap<String, Object> response) {
        this.response = response;
    }

    public void addResponse(String key, Object value) {
        this.response.put(key, value);
    }

    public static class Error {

        private HashMap<String, String> errors;
        private int status;
        private String message;

        public Error() {
            this.errors = new HashMap<>();
            this.status = 0;
            this.message = "";
        }

        public Error setStatus(int status) {
            this.status = status;
            return this;
        }

        public Error setMessage(String message) {
            this.message = message;
            return this;
        }

        public Error addError(String key, String value) {
            this.errors.put(key, value);
            return this;
        }

        public Error addErrors(HashMap<String, String> errors) {
            this.errors.putAll(errors);
            return this;
        }

        public HashMap<String, Object> build() {
            RESTResponse restResponse = new RESTResponse();
            restResponse.addResponse(DATA, this.status);
            restResponse.addResponse(MESSAGE, this.message);
            String errorKey = DATA;
            if (this.errors.size() > 1) {
                errorKey = ERRORS;
            }
            restResponse.addResponse(errorKey, this.errors);
            return restResponse.getResponse();
        }
    }

    public static class SimpleError {

        private int code;
        private String message;

        public SimpleError() {
            this.code = HttpStatus.BAD_REQUEST.value();
            this.message = HttpStatus.BAD_REQUEST.name();
        }

        public SimpleError setCode(int code) {
            this.code = code;
            return this;
        }

        public SimpleError setMessage(String message) {
            this.message = message;
            return this;
        }

        public HashMap<String, Object> build() {
            RESTResponse restResponse = new RESTResponse();
            restResponse.addResponse(STATUS, this.code);
            restResponse.addResponse(MESSAGE, this.message);
            restResponse.addResponse(DATA, new ArrayList<>());
            return restResponse.getResponse();
        }
    }


    public static class Success {

        private int status;
        private String message;
        private List<Object> data;
        private RESTPagination pagination;

        public Success() {
            this.status = HttpStatus.OK.value();
            this.message = HttpStatus.OK.name();
            this.data = new ArrayList<>();
        }

        public Success setStatus(int status) {
            this.status = status;
            return this;
        }

        public Success setMessage(String message) {
            this.message = message;
            return this;
        }

        public Success setPagination(RESTPagination pagination) {
            this.pagination = pagination;
            return this;
        }

        public Success addData(Object obj) {
            this.data.add(obj);
            return this;
        }

        public Success addData(List listObj) {
            this.data.addAll(listObj);
            return this;
        }

        public HashMap<String, Object> build() {
            RESTResponse restResponse = new RESTResponse();
            restResponse.addResponse(STATUS, this.status);
            restResponse.addResponse(MESSAGE, this.message);
            if (this.data.size() == 1) {
                restResponse.addResponse(DATA, this.data.get(0));

            } else {
                restResponse.addResponse(DATA, this.data);
            }
            if (this.pagination != null) {
                restResponse.addResponse(PAGINATION, this.pagination);
            }
            return restResponse.getResponse();
        }

        public HashMap<String, Object> buildData() {
            RESTResponse restResponse = new RESTResponse();
            restResponse.addResponse(DATA, this.status);
            restResponse.addResponse(MESSAGE, this.message);
            restResponse.addResponse(DATA, this.data);
            if (this.pagination != null) {
                restResponse.addResponse(PAGINATION, this.pagination);
            }
            return restResponse.getResponse();
        }

    }
}
