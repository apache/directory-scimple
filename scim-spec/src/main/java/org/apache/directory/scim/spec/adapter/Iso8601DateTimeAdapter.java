/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.spec.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

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
