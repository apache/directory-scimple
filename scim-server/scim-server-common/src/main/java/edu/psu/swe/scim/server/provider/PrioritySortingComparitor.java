package edu.psu.swe.scim.server.provider;

import java.util.Comparator;
import java.util.Set;

import edu.psu.swe.scim.spec.resources.TypedAttribute;

public class PrioritySortingComparitor implements Comparator<Object> {

  private Set<Object> priorities;

  public PrioritySortingComparitor(Set<Object> priorities) {
    this.priorities = priorities;
  }

  @Override
  public int compare(Object o1, Object o2) {
    if (o1 == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }

    Comparable c1 = getComparableValue(o1);
    Comparable c2 = getComparableValue(o2);
    
    boolean o1Priority = priorities.contains(c1);
    boolean o2Priority = priorities.contains(c2);

    if (o1Priority == o2Priority) {
      return c1.compareTo(c2);
    } else {
      return o1Priority ? -1 : 1;
    }

  }

  public static Comparable getComparableValue(Object obj) {
    if (obj instanceof TypedAttribute) {
      TypedAttribute typed = (TypedAttribute) obj;
      return typed.getType();
    } else if (obj instanceof Comparable) {
      return (Comparable) obj;
    } else {
      return obj.toString();
    }
  }
}