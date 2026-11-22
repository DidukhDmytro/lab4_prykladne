package com.example.carservice.api;

public class ApiPaths {
    public static final String ROOT_PATH = "/";
    public static final String API = "/api";
    public static final String CUSTOMERS = API + "/customers";
    public static final String CUSTOMER_BY_ID = CUSTOMERS + "/{id}";
    public static final String CARS = API + "/cars";
    public static final String CAR_BY_VIN = CARS + "/{vin}";
    public static final String MECHANICS = API + "/mechanics";
    public static final String MECHANIC_BY_ID = MECHANICS + "/{id}";
    public static final String ORDERS = API + "/orders";
    public static final String ORDER_BY_ID = ORDERS + "/{id}";
    public static final String ORDER_WORKS = ORDER_BY_ID + "/works";
    public static final String ORDER_WORKS_BATCH = ORDER_WORKS + "/batch";
    public static final String COMPLETE_ORDER_WORK = ORDER_WORKS + "/{index}/complete";
    public static final String ASSIGN_ORDER_MECHANIC = ORDER_BY_ID + "/mechanic";
    public static final String CHANGE_ORDER_PRIORITY = ORDER_BY_ID + "/priority";
    public static final String DIAGNOSE_ORDER = ORDER_BY_ID + "/diagnose";
    public static final String APPROVE_ORDER = ORDER_BY_ID + "/approve";
    public static final String START_ORDER = ORDER_BY_ID + "/start";
    public static final String COMPLETE_ORDER = ORDER_BY_ID + "/complete";
    public static final String CANCEL_ORDER = ORDER_BY_ID + "/cancel";
    public static final String ORDER_PRICE = ORDER_BY_ID + "/price";
    public static final String ORDER_PRESENT = ORDER_BY_ID + "/present";

    private ApiPaths() {
    }
}
