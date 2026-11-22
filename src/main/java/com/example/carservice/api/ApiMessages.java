package com.example.carservice.api;

public class ApiMessages {
    public static final int MIN_CAR_YEAR = 1886;
    public static final int MAX_CAR_YEAR = 2100;

    public static final String FULL_NAME_REQUIRED = "Full name is required.";
    public static final String FULL_NAME_TOO_LONG = "Full name must not exceed 255 characters.";
    public static final String PHONE_NUMBER_REQUIRED = "Phone number is required.";
    public static final String PHONE_NUMBER_TOO_LONG = "Phone number must not exceed 32 characters.";
    public static final String VIN_REQUIRED = "VIN is required.";
    public static final String VIN_TOO_LONG = "VIN must not exceed 64 characters.";
    public static final String MAKE_TOO_LONG = "Make must not exceed 100 characters.";
    public static final String MODEL_TOO_LONG = "Model must not exceed 100 characters.";
    public static final String YEAR_REQUIRED = "Year is required.";
    public static final String YEAR_OUT_OF_RANGE = "Year must be between " + MIN_CAR_YEAR + " and " + MAX_CAR_YEAR + ".";
    public static final String OWNER_ID_REQUIRED = "Owner ID is required.";
    public static final String SPECIALIZATION_REQUIRED = "Specialization is required.";
    public static final String ORDER_PRIORITY_REQUIRED = "Order priority is required.";
    public static final String MECHANIC_ID_REQUIRED = "Mechanic ID is required.";
    public static final String DESCRIPTION_REQUIRED = "Description is required.";
    public static final String DESCRIPTION_TOO_LONG = "Description must not exceed 255 characters.";
    public static final String PARTS_COST_REQUIRED = "Parts cost is required.";
    public static final String LABOR_COST_REQUIRED = "Labor cost is required.";
    public static final String COST_MUST_BE_POSITIVE_OR_ZERO = "Cost must be zero or positive.";
    public static final String COST_DIGITS_INVALID = "Cost must have up to 10 integer digits and 2 fraction digits.";
    public static final String WORKS_REQUIRED = "At least one work item is required.";

    private ApiMessages() {
    }
}
