package edu.psu.swe.scim.common;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;


public final class ScimUtils {
  private ScimUtils() {
  }
  
  public static String toDateString(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.ISO_8601_DATE_FORMAT);
    return dateFormat.format(date);
  }
  
  public static String toDateTimeString(Date date) {
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(Constants.ISO_8601_DATE_TIME_FORMAT);
    return dateTimeFormat.format(date);
  }
  
  public static String toDateString(LocalDate ld) {
    return ld.format(DateTimeFormatter.ISO_DATE);
  }
  
  public static String toDateTimeString(LocalDateTime ldt) {
    return ldt.format(DateTimeFormatter.ISO_DATE_TIME);
  }
  
  public static List<Field> getFieldsUpTo(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
    List<Field> currentClassFields = Lists.newArrayList(startClass.getDeclaredFields());
    Class<?> parentClass = startClass.getSuperclass();
    if (parentClass != null && (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
      List<Field> parentClassFields = (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
      currentClassFields.addAll(parentClassFields);
    }
    return currentClassFields;
  }
}
