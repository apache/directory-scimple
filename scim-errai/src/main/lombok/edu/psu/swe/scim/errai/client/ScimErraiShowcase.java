package edu.psu.swe.scim.errai.client;

import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.Window;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;

@EntryPoint
public class ScimErraiShowcase {
  
  @AfterInitialization
  public void start() {
    Span label = (Span) Window.getDocument().createElement("span");
    label.setInnerHTML("This is a test");
    Div root = (Div) Window.getDocument().getElementById("scimErraiShowcase");
    root.appendChild(label);
  }

}
