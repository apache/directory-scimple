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

package org.apache.directory.scim.spec.protocol;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Path;

import org.apache.directory.scim.spec.resources.ScimGroup;

//@formatter:off
/**
* From SCIM Protocol Specification, section 3, page 9
* 
* @see <a href="https://tools.ietf.org/html/rfc7644#section-3.2">Scim spec section 3.2</a>
* 
* Resource Endpoint         Operations             Description
 -------- ---------------- ---------------------- --------------------
 Group    /Groups          GET (Section 3.4.1),   Retrieve, add,
                           POST (Section 3.3),    modify Groups.
                           PUT (Section 3.5.1),
                           PATCH (Section 3.5.2),
                           DELETE (Section 3.6)

* @author chrisharm
*
*/
//@formatter:on

@Path("Groups")
@Tag(name="SCIM")
public interface GroupResource extends BaseResourceTypeResource<ScimGroup> {

}
