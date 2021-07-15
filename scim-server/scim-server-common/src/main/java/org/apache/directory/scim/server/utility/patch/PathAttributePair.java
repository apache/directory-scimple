package org.apache.directory.scim.server.utility.patch;

import lombok.Data;
import lombok.Getter;
import org.apache.directory.scim.spec.schema.Schema;

@Data
class PathAttributePair {
    @Getter
    private final String path;
    @Getter
    private final Schema.Attribute attribute;

    public PathAttributePair(final String path, final Schema.Attribute attribute) {
        this.path = path;
        this.attribute = attribute;
    }
}