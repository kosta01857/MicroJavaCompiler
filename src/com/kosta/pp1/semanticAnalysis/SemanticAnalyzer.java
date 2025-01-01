package com.kosta.pp1.semanticAnalysis;

import com.kosta.pp1.ast.ClassBody;
import com.kosta.pp1.ast.ClassDeclarationExtend;
import com.kosta.pp1.ast.ClassDeclarationNoExtend;
import com.kosta.pp1.ast.ConditionConcrete;
import com.kosta.pp1.ast.ConditionRecursive;
import com.kosta.pp1.ast.ConstDeclarationList;
import com.kosta.pp1.ast.GlobalVarDeclarationList;
import com.kosta.pp1.ast.LocalVarDeclarations;
import com.kosta.pp1.ast.MethodDefinition;
import com.kosta.pp1.ast.MethodDefinitionNoLocals;
import com.kosta.pp1.ast.MethodSignature;
import com.kosta.pp1.ast.ProgName;
import com.kosta.pp1.ast.Program;
import com.kosta.pp1.ast.Statements;
import com.kosta.pp1.ast.Type;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.ast.VisitorAdaptor;
import com.kosta.pp1.utils.myDumpSymbolTableVisitor;

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
		Boolean found = Finder.findMainFunction();
		if(!found){
			Utils.report_error("main function that matches requirements not found!",null);
		}
		Tab.closeScope();
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
		Analyzer.methodDeclarationPass(def);
	}

	public void visit(MethodDefinition methodDefinition) {
		Analyzer.methodDeclarationPass(methodDefinition);
	}

	public void visit(ClassDeclarationExtend classDecl){
		Utils.report_info("Class declaration", null);
		Obj classObj = Register.registerClass(classDecl);
		Tab.chainLocalSymbols(classObj);
		Tab.closeScope();
	}

	public void visit(ClassDeclarationNoExtend classDecl){
		Utils.report_info("Class declaration", null);
		Obj classObj = Register.registerClass(classDecl);
		Tab.chainLocalSymbols(classObj);
		Tab.closeScope();
	}
	
}
