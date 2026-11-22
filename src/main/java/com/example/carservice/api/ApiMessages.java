package com.example.carservice.api;

import java.util.Set;

public class ApiMessages {
    public static final int MIN_CAR_YEAR = 1886;
    public static final int MAX_CAR_YEAR = 2100;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;
    public static final int MAX_FULL_NAME_LENGTH = 255;
    public static final int MAX_PHONE_NUMBER_LENGTH = 32;
    public static final int MAX_VIN_LENGTH = 64;
    public static final int MAX_CAR_MAKE_LENGTH = 100;
    public static final int MAX_CAR_MODEL_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 255;
    public static final int MAX_COST_INTEGER_DIGITS = 10;
    public static final int MAX_COST_FRACTION_DIGITS = 2;

    public static final String FULL_NAME_REQUIRED = "Full name is required.";
    public static final String FULL_NAME_TOO_LONG =
            "Full name must not exceed " + MAX_FULL_NAME_LENGTH + " characters.";
    public static final String PHONE_NUMBER_REQUIRED = "Phone number is required.";
    public static final String PHONE_NUMBER_TOO_LONG =
            "Phone number must not exceed " + MAX_PHONE_NUMBER_LENGTH + " characters.";
    public static final String VIN_REQUIRED = "VIN is required.";
    public static final String VIN_TOO_LONG = "VIN must not exceed " + MAX_VIN_LENGTH + " characters.";
    public static final String MAKE_TOO_LONG = "Make must not exceed " + MAX_CAR_MAKE_LENGTH + " characters.";
    public static final String MODEL_TOO_LONG = "Model must not exceed " + MAX_CAR_MODEL_LENGTH + " characters.";
    public static final String YEAR_REQUIRED = "Year is required.";
    public static final String YEAR_OUT_OF_RANGE = "Year must be between " + MIN_CAR_YEAR + " and " + MAX_CAR_YEAR + ".";
    public static final String OWNER_ID_REQUIRED = "Owner ID is required.";
    public static final String SPECIALIZATION_REQUIRED = "Specialization is required.";
    public static final String ORDER_PRIORITY_REQUIRED = "Order priority is required.";
    public static final String MECHANIC_ID_REQUIRED = "Mechanic ID is required.";
    public static final String DESCRIPTION_REQUIRED = "Description is required.";
    public static final String DESCRIPTION_TOO_LONG =
            "Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters.";
    public static final String PARTS_COST_REQUIRED = "Parts cost is required.";
    public static final String LABOR_COST_REQUIRED = "Labor cost is required.";
    public static final String COST_MUST_BE_POSITIVE_OR_ZERO = "Cost must be zero or positive.";
    public static final String COST_DIGITS_INVALID = "Cost must have up to " + MAX_COST_INTEGER_DIGITS
            + " integer digits and " + MAX_COST_FRACTION_DIGITS + " fraction digits.";
    public static final String WORKS_REQUIRED = "At least one work item is required.";
    public static final String SORT_PROPERTY_NOT_ALLOWED = "Sort property '%s' is not allowed.";
    public static final String ORDER_ID = "id";
    public static final String ORDER_STATUS = "status";
    public static final String ORDER_PRIORITY = "priority";
    public static final String ORDER_FINAL_PRICE = "finalPrice";
    public static final String ORDER_COMPLETED_AT = "completedAt";
    public static final String VALIDATION_FIELD_PROPERTY = "field";
    public static final String VALIDATION_MESSAGE_PROPERTY = "message";
    public static final String VALIDATION_ERRORS_PROPERTY = "errors";
    public static final Set<String> ORDER_SORT_PROPERTIES = Set.of(
            ORDER_STATUS,
            ORDER_PRIORITY,
            ORDER_FINAL_PRICE,
            ORDER_COMPLETED_AT,
            ORDER_ID);

    public static final String BAD_REQUEST_TITLE = "Bad Request";
    public static final String NOT_FOUND_TITLE = "Not Found";
    public static final String CONFLICT_TITLE = "Conflict";
    public static final String INTERNAL_ERROR_TITLE = "Internal Server Error";
    public static final String VALIDATION_DETAIL = "One or more request fields are invalid.";
    public static final String MALFORMED_REQUEST_DETAIL = "The request body is malformed or contains an invalid value.";
    public static final String TYPE_MISMATCH_DETAIL = "The request parameter or path variable has an invalid value.";
    public static final String UNKNOWN_ROUTE_DETAIL = "The requested route does not exist.";
    public static final String CONFLICT_DETAIL = "The request conflicts with the current resource state.";
    public static final String DATA_INTEGRITY_DETAIL = "The request conflicts with existing data.";
    public static final String INTERNAL_ERROR_DETAIL = "An unexpected server error occurred.";

    public static final String BAD_REQUEST_TYPE = "https://api.carservice.example/problems/bad-request";
    public static final String VALIDATION_TYPE = "https://api.carservice.example/problems/validation";
    public static final String NOT_FOUND_TYPE = "https://api.carservice.example/problems/not-found";
    public static final String CONFLICT_TYPE = "https://api.carservice.example/problems/conflict";
    public static final String DATA_INTEGRITY_TYPE = "https://api.carservice.example/problems/data-integrity";
    public static final String INTERNAL_ERROR_TYPE = "https://api.carservice.example/problems/internal-error";
    public static final String UNKNOWN_ROUTE_TYPE = "https://api.carservice.example/problems/unknown-route";

    private ApiMessages() {
    }
}
