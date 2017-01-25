package edu.psu.swe.scim.spec.phonenumber;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.PhoneNumberContext;

public class PhoneNumberParseTreeListener implements PhoneNumberListener {

	@Override
	public void visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterPhoneNumber(PhoneNumberContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitPhoneNumber(PhoneNumberContext ctx) {
		// TODO Auto-generated method stub

	}

}
