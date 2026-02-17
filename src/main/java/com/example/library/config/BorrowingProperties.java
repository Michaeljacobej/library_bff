package com.example.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.borrowing")
public class BorrowingProperties {
  private int maxActiveLoansPerMember = 5;
  private int maxLoanDays = 14;

  public int getMaxActiveLoansPerMember() {
    return maxActiveLoansPerMember;
  }

  public void setMaxActiveLoansPerMember(int maxActiveLoansPerMember) {
    this.maxActiveLoansPerMember = maxActiveLoansPerMember;
  }

  public int getMaxLoanDays() {
    return maxLoanDays;
  }

  public void setMaxLoanDays(int maxLoanDays) {
    this.maxLoanDays = maxLoanDays;
  }
}
