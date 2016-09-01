package edu.psu.swe.scim.spec.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Iso8601DateTimeAdapter extends XmlAdapter<String, Date> {
  
  private static final String ISO_8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS";
  private SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATE_TIME_FORMAT);
  
  @Override
  public String marshal(Date date)
  {
    if (date == null)
    {
      return null;
    }

    return dateFormat.format(date);
  }

  @Override
  public Date unmarshal(String date) throws Exception
  {
    if (date == null || date.isEmpty())
    {
      return null;
    }

    return dateFormat.parse(date);
  }

}
