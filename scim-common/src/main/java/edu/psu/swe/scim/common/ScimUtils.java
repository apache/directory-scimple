package edu.psu.swe.scim.common;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class ScimUtils {
  private ScimUtils() {
  }
  
  private static SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.ISO_8601_DATE_FORMAT);
  private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat(Constants.ISO_8601_DATE_TIME_FORMAT);
  
  public static String toDateString(Date date) {
    return dateFormat.format(date);
  }
  
  public static String toDateTimeString(Date date) {
    return dateTimeFormat.format(date);
  }
  
  public static String toDateString(LocalDate ld) {
    return ld.format(DateTimeFormatter.ISO_DATE);
  }
  
  public static String toDateTimeString(LocalDateTime ldt) {
    return ldt.format(DateTimeFormatter.ISO_DATE_TIME);
  }
  
}
