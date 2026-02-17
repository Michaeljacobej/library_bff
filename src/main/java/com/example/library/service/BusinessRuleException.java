package com.example.library.service;

public class BusinessRuleException extends RuntimeException {
  public BusinessRuleException(String message) {
    super(message);
  }
}
