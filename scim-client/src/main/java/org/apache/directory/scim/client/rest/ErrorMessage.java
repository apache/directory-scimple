/*
 * The Pennsylvania State University © 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.scim.client.rest;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ErrorMessage
{

  @XmlElement(name="status")
  @XmlJavaTypeAdapter(XmlStatusAdapter.class)
  private Status status;

  @XmlElementWrapper(name="errorMessageList")
  @XmlElement(name="errorMessage")
  List<String> errorMessages = new ArrayList<>();

  @XmlElementWrapper(name="referenceList", nillable = true)
  @XmlElement(name = "link", nillable = true)
  List<String> externalLinks = null;

  Map<String, Object> headerMap = new HashMap<>();

  public ErrorMessage()
  {}

  public ErrorMessage(Status status)
  {
    this.status = status;
  }

  public void setStatus(Status status)
  {
    this.status = status;
  }

  public Status getStatus()
  {
    return status;
  }

  public Map<String, Object> getHeaderMap() {
    return headerMap;
  }

  public void setHeader(String header, Object value) {
    this.headerMap.put(header, value);
  }

  public Object removeHeader(String header) {
    return this.headerMap.remove(header);
  }


  public void setErrorMessageList(List<String> messageList)
  {
    if (messageList == null)
    {
      errorMessages.clear();
    }
    else
    {
      errorMessages = messageList;
    }
  }

  public void addErrorMessage(String message)
  {
    errorMessages.add(message);
  }

  public List<String> getErrorMessageList()
  {
    return Collections.unmodifiableList(errorMessages);
  }

  public void setExternalLinkList(List<String> linkList)
  {
    externalLinks = linkList;
  }

  public void addExtenalLink(URL link)
  {
    if (externalLinks == null)
    {
      externalLinks  = new ArrayList<>();
    }

    externalLinks.add(link.toString());
  }

  public List<String> getExternalLinkList()
  {
    return externalLinks;
  }

  public Response toResponse()
  {
    return buildResponse(status).build();
  }

  public Response toResponse(String mediaType)
  {
    return buildResponse(status).type(mediaType).build();
  }  

  public Response toResponse(MediaType mediaType)
  {
    return buildResponse(status).type(mediaType).build();
  }  

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Status = ");
    sb.append(status.getStatusCode());
    sb.append("\n");

    if (errorMessages != null)
    {
      sb.append("Error Messages");
      sb.append("\n");
      for (String s : errorMessages)
      {
        sb.append(s);
        sb.append("\n");
      }
    }

    if (externalLinks != null)
    {
      sb.append("External Links");
      for (String s : externalLinks)
      {
        sb.append(s);
        sb.append("\n");
      }
    }
    return sb.toString();
  }


  private ResponseBuilder buildResponse(Status status){
    ResponseBuilder builder = Response.status(status);

    for(Map.Entry<String, Object> entry : headerMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      builder.header(key, value);
    }
    builder.entity(this);

    return builder;
  }
}
