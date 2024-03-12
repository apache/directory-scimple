package org.apache.directory.scim.core.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class ETag {
  private final String value;
  private final boolean weak;
}
