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

package edu.psu.swe.scim.spec.protocol.data;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import edu.psu.swe.scim.server.filter.FilterLexer;
import edu.psu.swe.scim.server.filter.FilterParser;
import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;
import edu.psu.swe.scim.spec.protocol.filter.ValuePathExpression;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class PatchOperationPath {

  private ValuePathExpression valuePathExpression;

  public PatchOperationPath() {
    
  }

  public PatchOperationPath(String patchPath) throws FilterParseException {
    parsePatchPath(patchPath);
  }

  protected void parsePatchPath(String patchPath) throws FilterParseException {
    FilterLexer l = new FilterLexer(new ANTLRInputStream(patchPath));
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

      this.valuePathExpression = patchPathListener.getValuePathExpression();
    } catch (IllegalStateException e) {
      throw new FilterParseException(e);
    }
  }

  @Override
  public String toString() {
    return valuePathExpression.toFilter();
  }

}
