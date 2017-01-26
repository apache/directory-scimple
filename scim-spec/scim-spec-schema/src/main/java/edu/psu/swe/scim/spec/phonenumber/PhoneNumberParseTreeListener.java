package edu.psu.swe.scim.spec.phonenumber;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

//import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.GlobalNumberContext;
//import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.LocalNumberContext;
import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser.PhoneNumberContext;

public class PhoneNumberParseTreeListener extends PhoneNumberParserBaseListener {

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

//	@Override
//	public void enterGlobalNumber(GlobalNumberContext ctx) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void exitGlobalNumber(GlobalNumberContext ctx) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void enterLocalNumber(LocalNumberContext ctx) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void exitLocalNumber(LocalNumberContext ctx) {
//		// TODO Auto-generated method stub
//		
//	}

}
