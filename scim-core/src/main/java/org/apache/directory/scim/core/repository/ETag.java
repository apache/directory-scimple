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

package org.apache.directory.scim.core.repository;

/**
 * Bean representing an ETag, as defined by RFC 7232 section 2.3.
 * <p>
 * From RFC 7644 - SCIM Protocol
 * <p>
 * When supported by a SCIM Server, SCIM ETags MUST be specified
 * as an HTTP header and SHOULD be specified within the 'version'
 * attribute contained in the resource's 'meta' attribute.
 * <p>
 * From RFC 7232 - HTTP Conditional Requests
 * <p>
 * The "ETag" header field in a response provides the current entity-tag
 * for the selected representation, as determined at the conclusion of
 * handling the request.  An entity-tag is an opaque validator for
 * differentiating between multiple representations of the same
 * resource, regardless of whether those multiple representations are
 * due to resource state changes over time, content negotiation
 * resulting in multiple representations being valid at the same time,
 * or both.  An entity-tag consists of an opaque quoted string, possibly
 * prefixed by a weakness indicator ({@code W/}).
 * <p>
 * Examples:<ul>
 *      <li>ETag: "xyzzy"</li>
 *      <li>ETag: W/"xyzzy"</li>
 *      <li>ETag: ""</li>
 * </ul>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7232#section-2.3">Conditional Requests - RFC 7232</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7644#section-3.14">SCIM Protocol -  RFC 7644</a>
 */
public class ETag {
    private String value;
    private boolean weak;
    
    public ETag( String value, boolean weak )
    {
        this.value = value;
        this.weak = weak;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }


    /**
     * @return the weak
     */
    public boolean isWeak()
    {
        return weak;
    }
    
    
    /**
     * Gets the hashcode of this object.
     *
     * @see java.lang.Object#hashCode()
     * @return The instance hash code
     */
    @Override
    public int hashCode()
    {
        int h = 37;
        
        h = h * 17 + ( value != null ? value.hashCode() : 0 );
        h = h * 17 + ( weak ? 1 : 0 );
        
        return h;
    }
    
    
    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( !( obj instanceof ETag ) )
        {
            return false;
        }
        
        ETag that = (ETag)obj;
        
        
        if ( value == null )
        {
            return weak = that.weak && that.value == null;
        }
        else
        {
            return weak == that.weak && value.equals( that.value );
        }
    }
}
