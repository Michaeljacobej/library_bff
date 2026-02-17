package com.example.library.sqladapter;

import java.util.List;
import java.util.Map;

public interface SqlAdapterClient {
  int execute(String sql, Map<String, Object> params);

  List<Map<String, Object>> query(String sql, Map<String, Object> params);
}
