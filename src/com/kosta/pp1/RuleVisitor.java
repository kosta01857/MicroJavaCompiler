package com.kosta.pp1;

import org.apache.log4j.Logger;

import com.kosta.pp1.ast.VisitorAdaptor;

import com.kosta.pp1.ast.*;

public class RuleVisitor extends VisitorAdaptor{

	int printCallCount = 0;
	int varDeclCount = 0;
	
	Logger log = Logger.getLogger(getClass());
	
	//@Override
	//public void visit(VarDeclRecursiveID varDecl){
	//	varDeclCount++;
	//}

	//public void visit(VarDeclRecursiveARR varDecl){
	//	varDeclCount++;
	//}
	//
	//

	//public void visit(VarDeclID varDecl){
	//	varDeclCount++;
	//}

	//public void visit(VarDeclARR varDecl){

	//	varDeclCount++;
	//}
	

	@Override
   public void visit(PrintOne print) {
	printCallCount++;
	}
	
	@Override
   public void visit(PrintTwo print) {
	printCallCount++;
	}
	

}
