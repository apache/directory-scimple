package edu.psu.swe.scim.errai.showcase;

import java.util.List;
import java.util.stream.Stream;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.Window;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;

import edu.psu.swe.scim.errai.client.business.common.ListResponse;
import edu.psu.swe.scim.errai.client.business.resourcetype.ResourceType;
import edu.psu.swe.scim.errai.client.business.schema.Attribute;
import edu.psu.swe.scim.errai.client.business.schema.Schema;
import edu.psu.swe.scim.errai.client.business.scim.ScimServiceProvider;

@EntryPoint
public class ScimErraiShowcase {
  
  Div root;
  
  @AfterInitialization
  public void start() {
    Span label = (Span) Window.getDocument().createElement("span");
    label.setInnerHTML("This is a test");
    root = (Div) Window.getDocument().getElementById("scimErraiShowcase");
    root.appendChild(label);
    
    RestClient.setJacksonMarshallingActive(true);

    RestClient.create(ScimServiceProvider.class, new RemoteCallback<ListResponse<Schema>>() {

      @Override
      public void callback(ListResponse<Schema> response) {
        showSchemaList(response.getResources());
      }
      
    }, 200).getSchemas();
    
    RestClient.create(ScimServiceProvider.class, new RemoteCallback<ListResponse<ResourceType>>() {

		@Override
		public void callback(ListResponse<ResourceType> response) {
			showResourceTypeList(response.getResources());
		}
    
    }, 200).getResourceTypes();
  }
  
  void showResourceTypeList(List<ResourceType> resourceTypes) {
	  resourceTypes.forEach(s -> {
		  Div resourceType = (Div) Window.getDocument().createElement("div");
		  resourceType.setInnerHTML(s.toString());
		  root.appendChild(resourceType);
	  });
  }
  
  void showSchemaList(List<Schema> schemas) {
    schemas.forEach(s -> {
      Div schema = (Div) Window.getDocument().createElement("div");
      schema.setInnerHTML(s.getName() + " - " + s.getAttributes().length);
      root.appendChild(schema);
      Stream.of(s.getAttributes()).forEach(a -> {
        root.appendChild(getAttribute(a, ""));
      });
    });
  }
  
  Div getAttribute(Attribute a, String spacer) {
    String adjustedSpacer = spacer + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    Div attribute = (Div) Window.getDocument().createElement("div");
    attribute.appendChild(getSpan(adjustedSpacer));
    attribute.appendChild(getSpan("Name: " + a.getName()));
    attribute.appendChild(getSpan("Type: " + a.getType().name()));
    if(Attribute.Type.COMPLEX.equals(a.getType())) {
      Stream.of(a.getSubAttributes()).forEach(sa -> attribute.appendChild(getAttribute(sa, adjustedSpacer)));
    }
    return attribute;
  }
  
  Span getSpan(String innerHtml) {
    Span spacer = (Span) Window.getDocument().createElement("span");
    spacer.setInnerHTML(innerHtml);
    return spacer;
  }

}
