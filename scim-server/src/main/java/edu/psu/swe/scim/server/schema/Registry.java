package edu.psu.swe.scim.server.schema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.PostActivate;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import edu.psu.swe.scim.spec.schema.Schema;

@Singleton
@Startup
public class Registry {

  private Map<String, String> schemaMap = new HashMap<>();
  private static List<String> schemaNames = new ArrayList<>();

  @PostConstruct
  @PostActivate
  private void loadSchemaMap() {
    for (String s : schemaNames) {
      String schemaFile = "/schemas/" + s + ".json";
      InputStream is = Schema.class.getClassLoader().getResourceAsStream(schemaFile);
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      StringBuilder sb = new StringBuilder();

      String line;
      try {
        while ((line = reader.readLine()) != null) {
          sb.append(line);
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      schemaMap.put(s.substring(s.lastIndexOf("/") + 1, s.lastIndexOf(".")), sb.toString());
    }
  }
}
