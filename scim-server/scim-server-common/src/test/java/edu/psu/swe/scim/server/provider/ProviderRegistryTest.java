package edu.psu.swe.scim.server.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.schema.Schema;

public class ProviderRegistryTest {
  
  @Rule
  public MockitoRule mockito = MockitoJUnit.rule();
  
  Registry registry;
  
  @Mock
  Instance<Provider<ScimUser>> providerInstance;
  
  @Mock
  Provider<ScimUser> provider;
  
  ProviderRegistry providerRegistry;
  
  public ProviderRegistryTest() {
    providerRegistry = new ProviderRegistry();
    registry = new Registry();
    providerRegistry.registry = registry;
  }
  
  @Before
  public void initialize() {
    Mockito.when(providerInstance.get()).thenReturn(provider);
//  Mockito.when(provider.getExtensionList()).thenReturn(Collections.singletonList(Enterprise));
  }
  
  @Test
  public void testAddProvider() throws Exception {
    providerRegistry.registerProvider(ScimUser.class, providerInstance);
    
    Schema schema = registry.getSchema(ScimUser.SCHEMA_URI);
    
    assertThat(schema).isNotNull();
    assertThat(schema.getId()).isEqualTo(ScimUser.SCHEMA_URI);
  }

}
