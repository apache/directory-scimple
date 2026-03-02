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

package org.apache.directory.scim.spec.patch;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.apache.directory.scim.spec.filter.FilterLexer;
import org.apache.directory.scim.spec.filter.FilterParser;
import org.apache.directory.scim.spec.filter.FilterParseException;
import org.apache.directory.scim.spec.filter.ValuePathExpression;

import java.io.Serial;
import java.io.Serializable;

public class PatchOperationPath implements Serializable {

  @Serial
  private static final long serialVersionUID = 449365558879593512L;

  private final ValuePathExpression valuePathExpression;

  public PatchOperationPath(ValuePathExpression valuePathExpression) {
    this. valuePathExpression = valuePathExpression;
  }

  static ValuePathExpression parsePatchPath(String patchPath) throws FilterParseException {
    FilterLexer l = new FilterLexer(CharStreams.fromString(patchPath));
    FilterParser p = new FilterParser(new CommonTokenStream(l));
    p.setBuildParseTree(true);

    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
      }
    });

    try {
      ParseTree tree = p.patchPath();
      PatchPathListener patchPathListener = new PatchPathListener();
      ParseTreeWalker.DEFAULT.walk(patchPathListener, tree);

      return patchPathListener.getValuePathExpression();
    } catch (IllegalStateException e) {
      throw new FilterParseException(e);
    }
  }

  @Override
  public String toString() {
    return valuePathExpression.toFilter();
  }

  public static PatchOperationPath fromString(String patchPath) throws FilterParseException {
    return new PatchOperationPath(parsePatchPath(patchPath));
  }

  public ValuePathExpression getValuePathExpression() {
    return this.valuePathExpression;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof PatchOperationPath)) return false;
    final PatchOperationPath other = (PatchOperationPath) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$valuePathExpression = this.getValuePathExpression();
    final Object other$valuePathExpression = other.getValuePathExpression();
    if (this$valuePathExpression == null ? other$valuePathExpression != null : !this$valuePathExpression.equals(other$valuePathExpression))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof PatchOperationPath;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $valuePathExpression = this.getValuePathExpression();
    result = result * PRIME + ($valuePathExpression == null ? 43 : $valuePathExpression.hashCode());
    return result;
  }
}
