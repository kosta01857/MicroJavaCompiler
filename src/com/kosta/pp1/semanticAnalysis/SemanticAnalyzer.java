package com.kosta.pp1.semanticAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.kosta.pp1.ast.ArrayDecl;
import com.kosta.pp1.ast.BOOL;
import com.kosta.pp1.ast.CHAR;
import com.kosta.pp1.ast.Condition;
import com.kosta.pp1.ast.ConstDeclarationList;
import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.DesignatorStatement;
import com.kosta.pp1.ast.DesignatorStmt;
import com.kosta.pp1.ast.DoWhile;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.FuncPars;
import com.kosta.pp1.ast.FunctionArgumentList;
import com.kosta.pp1.ast.FunctionParameterDeclConcrete;
import com.kosta.pp1.ast.FunctionParameterDeclRecursive;
import com.kosta.pp1.ast.FunctionParameters;
import com.kosta.pp1.ast.GlobalVarDeclarationList;
import com.kosta.pp1.ast.IdDecl;
import com.kosta.pp1.ast.IdDeclaration;
import com.kosta.pp1.ast.IdDefinition;
import com.kosta.pp1.ast.IdDefinitionList;
import com.kosta.pp1.ast.IdDefinitionListConcrete;
import com.kosta.pp1.ast.IdDefinitionListRecursive;
import com.kosta.pp1.ast.IfElse;
import com.kosta.pp1.ast.IfOnly;
import com.kosta.pp1.ast.IfStatement;
import com.kosta.pp1.ast.IfStmt;
import com.kosta.pp1.ast.Literal;
import com.kosta.pp1.ast.LocalVarDeclarations;
import com.kosta.pp1.ast.LocalVarDeclarationsConcrete;
import com.kosta.pp1.ast.LocalVarDeclarationsRecursive;
import com.kosta.pp1.ast.MethodDefinition;
import com.kosta.pp1.ast.MethodDefinitionNoLocals;
import com.kosta.pp1.ast.MethodSignature;
import com.kosta.pp1.ast.MethodSignatureTyped;
import com.kosta.pp1.ast.MethodSignatureVoid;
import com.kosta.pp1.ast.NUMBER;
import com.kosta.pp1.ast.PostDec;
import com.kosta.pp1.ast.PostInc;
import com.kosta.pp1.ast.ProgName;
import com.kosta.pp1.ast.Program;
import com.kosta.pp1.ast.SetDesignation;
import com.kosta.pp1.ast.Statement;
import com.kosta.pp1.ast.Statements;
import com.kosta.pp1.ast.StatementsRecursive;
import com.kosta.pp1.ast.SyntaxNode;
import com.kosta.pp1.ast.Type;
import com.kosta.pp1.ast.VarDecl;
import com.kosta.pp1.ast.VarDeclRecursive;
import com.kosta.pp1.ast.VarDeclaration;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.ast.VarDesignation;
import com.kosta.pp1.ast.VisitorAdaptor;
import com.kosta.pp1.ast.WhileCond;
import com.kosta.pp1.ast.WhileDesignator;
import com.kosta.pp1.ast.WhileSimple;
import com.kosta.pp1.ast.WhileStmt;
import com.kosta.pp1.utils.SemanticAnalyzerUtils;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor {
	static Struct currentType = Tab.noType;

	public SemanticAnalyzer(){
		Struct bool = new Struct(Struct.Bool);
		Obj boolObj = new Obj(Obj.Type, "bool", bool);
		
		Tab.currentScope.addToLocals(boolObj);
		Struct set = new SetType();
		Tab.currentScope.addToLocals(new Obj(Obj.Type, "set", set));
	}

	public void visit(ProgName progName){
		Tab.insert(Obj.Prog, progName.getName(), Tab.noType);
		Tab.openScope();
	}
	public void visit(Program program){
		Obj programNode = Tab.find(program.getProgName().getName());
		Tab.chainLocalSymbols(programNode);
		Tab.closeScope();
	}
	public void visit(GlobalVarDeclarationList globalVarDeclaration){
		VarDeclarationList varDeclarationList = globalVarDeclaration.getVarDeclarationList();
		Utils.report_info("Global variables:", null);
		Analyzer.declarationListPass(varDeclarationList);
	}
	public void visit(ConstDeclarationList list){
		Analyzer.definitionListPass(list);
	}
	public void visit(MethodDefinitionNoLocals def){
		Register.registerMethod(def.getMethodSignature());
	}
	public void visit(MethodDefinition methodDefinition){
		MethodSignature signature = methodDefinition.getMethodSignature();
		LocalVarDeclarations varDecls = methodDefinition.getLocalVarDeclarations();
		Statements statements = methodDefinition.getStatements();
		Obj funcObj = Register.registerMethod(signature);
		Register.registerLocalVariables(varDecls);
		Tab.chainLocalSymbols(funcObj);
		Analyzer.statementsPass(statements);
		Tab.closeScope();
		//Tab.dump();
	}
}

