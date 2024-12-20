package com.kosta.pp1;

import org.apache.log4j.Logger;

import com.kosta.pp1.ast.VisitorAdaptor;

import com.kosta.pp1.ast.*;

public class RuleVisitor extends VisitorAdaptor{

	int printCallCount = 0;
	int varDeclCount = 0;
	
	Logger log = Logger.getLogger(getClass());
	
	@Override
	public void visit(VarDeclarationDerived1 varDecl){
		varDeclCount++;
	}

	public void visit(VarDeclarationDerived2 varDecl){
		varDeclCount++;
	}
	
	

	public void visit(VarDeclID varDecl){
		varDeclCount++;
	}

	public void visit(VarDeclARR varDecl){

		varDeclCount++;
	}
	

	@Override
   public void visit(PrintStatementDerived1 print) {
	printCallCount++;
	}
	
	@Override
   public void visit(PrintStatementDerived2 print) {
	printCallCount++;
	}
	

}
