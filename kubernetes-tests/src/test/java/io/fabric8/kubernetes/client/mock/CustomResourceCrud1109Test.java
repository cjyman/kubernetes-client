/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.kubernetes.client.mock;

import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinitionNames;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.mock.crd.FooBar;
import io.fabric8.kubernetes.client.mock.crd.FooBarList;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@EnableRuleMigrationSupport
class CustomResourceCrud1109Test {
  @Rule
  public KubernetesServer server = new KubernetesServer(true,true);

  private CustomResourceDefinition customResourceDefinition;

  @BeforeEach
  void setUp() {
    customResourceDefinition = server.getClient().apiextensions().v1beta1().customResourceDefinitions()
      .create(CustomResourceDefinitionContext.v1beta1CRDFromCustomResourceType(FooBar.class).build());
  }

  @Test
  @DisplayName("Fix for issue 1109, verifies resources with dashes can be retrieved")
  void test1109() {
    
    final CustomResourceDefinitionNames names = customResourceDefinition.getSpec().getNames();
    final String plural = FooBar.SINGULAR + "s";
    Assertions.assertEquals(plural, names.getPlural());
    Assertions.assertEquals(FooBar.SINGULAR, names.getSingular());
    Assertions.assertEquals("FooBar", names.getKind());
    Assertions.assertEquals(plural + "." + FooBar.GROUP, customResourceDefinition.getMetadata().getName());
    Assertions.assertEquals(FooBar.VERSION, customResourceDefinition.getSpec().getVersion());
    
    // Given
    final MixedOperation<FooBar, FooBarList, Resource<FooBar>> fooBarClient = server.getClient().customResources(FooBar.class, FooBarList.class);
    final FooBar fb1 = new FooBar();
    fb1.getMetadata().setName("example");
    fooBarClient.inNamespace("default").create(fb1);
    final FooBarList list = fooBarClient.inNamespace("default").list();
    Assertions.assertEquals(1, list.getItems().size());
    Assertions.assertEquals("FooBar", list.getItems().iterator().next().getKind());
    // When
    final FooBar fooBar = fooBarClient.inNamespace("default").withName("example").get();
    // Then
    Assertions.assertNotNull(fooBar);
  }
}
