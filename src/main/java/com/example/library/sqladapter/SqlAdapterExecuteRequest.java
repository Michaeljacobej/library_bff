package com.example.library.sqladapter;

import java.util.Map;

public class SqlAdapterExecuteRequest {
  private String sql;
  private Map<String, Object> params;

  public SqlAdapterExecuteRequest() {
  }

  public SqlAdapterExecuteRequest(String sql, Map<String, Object> params) {
    this.sql = sql;
    this.params = params;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }
}
