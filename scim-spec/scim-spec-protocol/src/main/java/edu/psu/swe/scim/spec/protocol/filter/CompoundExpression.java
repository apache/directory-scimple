package edu.psu.swe.scim.spec.protocol.filter;

public class CompoundExpression extends Expression<Expression> {

  @Override
  public String toString(String prefix) {
    StringBuilder sb = new StringBuilder();
    //sb.append(prefix);
    sb.append(getOperator().name());
    sb.append("\n");
    sb.append(prefix);
    sb.append("|\n");
    if(getLeft() != null) {
	    sb.append(prefix);
	    sb.append("+---");
	    sb.append(getLeft().toString(prefix + "|   "));
    }
    if(getRight() != null) {
      sb.append(prefix);
      sb.append("+---");
      sb.append(getRight().toString(prefix + "    "));
    }
    sb.append(prefix);
    sb.append("\n");
    return sb.toString();
  }
  
}
