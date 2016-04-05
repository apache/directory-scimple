package edu.psu.swe.scim.spec.protocol.filter;

public class SimpleExpression extends Expression<String> {

  @Override
  public String toString(String prefix) {
    StringBuilder sb = new StringBuilder();
    //sb.append(prefix);
    sb.append(getOperator().name());
    sb.append("\n");
    sb.append(prefix);
    sb.append("|\n");
    sb.append(prefix);
    sb.append("+---");
    sb.append(getLeft());
    sb.append("\n");
    sb.append(prefix);
    sb.append("+---");
    sb.append(getRight());
    sb.append("\n");
    sb.append(prefix);
    sb.append("\n");
    return sb.toString();
  }
  
}

