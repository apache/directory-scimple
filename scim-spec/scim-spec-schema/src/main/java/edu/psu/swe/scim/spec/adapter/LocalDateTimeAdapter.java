package edu.psu.swe.scim.spec.adapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
  
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	@Override
	public LocalDateTime unmarshal(String v) throws Exception {
		if (v == null) {
			return null;
		}
		return LocalDateTime.parse(v, FORMATTER);
	}

	@Override
	public String marshal(LocalDateTime v) throws Exception {
		if (v == null) {
		  return null;
		}
		return FORMATTER.format(v);
	}
}
