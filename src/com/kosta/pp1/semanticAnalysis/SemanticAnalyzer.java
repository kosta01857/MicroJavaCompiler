package com.kosta.pp1.semanticAnalysis;
import java.util.List;

import com.kosta.pp1.ast.ConstDeclarationList;
import com.kosta.pp1.ast.GlobalVarDeclarationList;
import com.kosta.pp1.ast.LocalVarDeclarations;
import com.kosta.pp1.ast.MethodDeclaration;
import com.kosta.pp1.ast.MethodDeclarationsRecursive;
import com.kosta.pp1.ast.MethodDefinition;
import com.kosta.pp1.ast.MethodDefinitionNoLocals;
import com.kosta.pp1.ast.MethodSignature;
import com.kosta.pp1.ast.ProgName;
import com.kosta.pp1.ast.Program;
import com.kosta.pp1.ast.Statements;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.ast.VisitorAdaptor;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor {
	static Struct currentType = Tab.noType;

	public SemanticAnalyzer() {
		Struct bool = new Struct(Struct.Bool);
		Obj boolObj = new Obj(Obj.Type, "bool", bool);

		Tab.currentScope.addToLocals(boolObj);
		Struct set = new SetType();
		Tab.currentScope.addToLocals(new Obj(Obj.Type, "set", set));
	}

	public void visit(ProgName progName) {
		Tab.insert(Obj.Prog, progName.getName(), Tab.noType);
		Tab.openScope();
	}

	public void visit(Program program) {
		Obj programNode = Tab.find(program.getProgName().getName());
		Tab.chainLocalSymbols(programNode);
		Tab.closeScope();
		Boolean found = Finder.findMainFunction();
		if(!found){
			Utils.report_error("main function that matches requirements not found!",null);
		}
	}

	public void visit(GlobalVarDeclarationList globalVarDeclaration) {
		VarDeclarationList varDeclarationList = globalVarDeclaration.getVarDeclarationList();
		Utils.report_info("Global variables:", null);
		Analyzer.declarationListPass(varDeclarationList);
	}

	public void visit(ConstDeclarationList list) {
		Analyzer.definitionListPass(list);
	}
	public void visit(MethodDefinitionNoLocals def) {
		Register.registerMethod(def.getMethodSignature());
	}

	public void visit(MethodDefinition methodDefinition) {
		MethodSignature signature = methodDefinition.getMethodSignature();
		LocalVarDeclarations varDecls = methodDefinition.getLocalVarDeclarations();
		Statements statements = methodDefinition.getStatements();
		Obj funcObj = Register.registerMethod(signature);
		Register.registerLocalVariables(varDecls);
		Tab.chainLocalSymbols(funcObj);
		Analyzer.statementsPass(statements);
		Tab.closeScope();
		// Tab.dump();
	}
}
