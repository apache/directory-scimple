package edu.psu.swe.scim.spec.protocol.filter;

public abstract class Expression<T> {

  private T left;
  private Operator operator;
  private T right;

  public T getLeft() {
    return left;
  }

  public void setLeft(T left) {
    this.left = left;
  }

  public Operator getOperator() {
    return operator;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }

  public T getRight() {
    return right;
  }

  public void setRight(T right) {
    this.right = right;
  }
  
  public abstract String toString(String prefix);

}

