package edu.psu.swe.scim.server.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.PostActivate;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class Registry {
  
  private Map<String, String> schemaMap = new HashMap<>();
  private static List<String> schemaNames = new ArrayList<>();

  static {
    schemaNames.add("enterprise-user-schema");
  }
  
  @PostConstruct
  @PostActivate
  private void loadSchemaMap() {

  }
}
