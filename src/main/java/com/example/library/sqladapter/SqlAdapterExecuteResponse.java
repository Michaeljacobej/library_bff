package com.example.library.sqladapter;

public class SqlAdapterExecuteResponse {
  private int rowsAffected;

  public SqlAdapterExecuteResponse() {
  }

  public SqlAdapterExecuteResponse(int rowsAffected) {
    this.rowsAffected = rowsAffected;
  }

  public int getRowsAffected() {
    return rowsAffected;
  }

  public void setRowsAffected(int rowsAffected) {
    this.rowsAffected = rowsAffected;
  }
}
