package com.kosta.pp1.semanticAnalysis;

import com.kosta.pp1.ast.Condition;
import com.kosta.pp1.ast.ConstDeclarationList;
import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.DesignatorStatement;
import com.kosta.pp1.ast.DesignatorStmt;
import com.kosta.pp1.ast.DoWhile;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.ast.VarDesignation;
import com.kosta.pp1.ast.WhileCond;
import com.kosta.pp1.ast.WhileDesignator;
import com.kosta.pp1.ast.WhileSimple;
import com.kosta.pp1.ast.WhileStmt;
import com.kosta.pp1.utils.SemanticAnalyzerUtils;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import java.util.List;
import java.util.ArrayList;
import com.kosta.pp1.ast.IdDeclaration;
import com.kosta.pp1.ast.IdDefinition;
import com.kosta.pp1.ast.IfElse;
import com.kosta.pp1.ast.IfOnly;
import com.kosta.pp1.ast.IfStatement;
import com.kosta.pp1.ast.IfStmt;
import com.kosta.pp1.ast.PostDec;
import com.kosta.pp1.ast.PostInc;
import com.kosta.pp1.ast.SetDesignation;
import com.kosta.pp1.ast.Statement;
import com.kosta.pp1.ast.Statements;
import com.kosta.pp1.ast.StatementsRecursive;
import com.kosta.pp1.ast.Type;
public class Analyzer{
	static void varDesignationPass(VarDesignation varDesignation){
		Expression expr = varDesignation.getExpression();
		String name = varDesignation.getDesignator().getName();
		if(!Utils.objExists(name)){
			Utils.report_error("use of undeclared identifier " + name,varDesignation);
		}
		Obj ident = Tab.find(name);
		Struct type = ident.getType();
		boolean error = SemanticAnalyzerUtils.ExprTypeCheck(expr,type);
		Utils.reportUse(ident,varDesignation);
	}
	static void declarationListPass(VarDeclarationList list){
		Type type = list.getType();
		Struct struct = Utils.inferType(type);
		SemanticAnalyzer.currentType = struct;
		List<IdDeclaration> idDecls = Finder.findIdDeclarations(list.getVarDeclaration());
		idDecls.forEach(Register::registerIdDeclaration);
	}
	static void definitionListPass(ConstDeclarationList list){
		Type type = list.getType();
		Struct struct = Utils.inferType(type);
		SemanticAnalyzer.currentType = struct;
		List<IdDefinition> idDecls = Finder.findIdDefinitions(list.getIdDefinitionList());
		idDecls.forEach(Register::registerIdDefinition);
	}
	static void postIncPass(PostInc postIncExpr){
		Designator d = postIncExpr.getDesignator();
		String name = d.getName();
		Utils.report_info("use of variable " + name,postIncExpr);
		Obj node = Tab.find(name);
		if(node == Tab.noObj){
			Utils.report_error("use of undeclared identifier " + name,postIncExpr);
		}
		if(node.getKind() != Obj.Var){
			Utils.report_error("cannot use identifier " + name + " in this context",postIncExpr);
		}
		Struct type = node.getType();
		if(type.getKind() != Struct.Int){
			Utils.report_error("cannot use this operator on the variable " + name,postIncExpr);
		}
	}
	static void setDesignationPass(SetDesignation setDesignation){
		Designator leftD = setDesignation.getDesignator();
		Designator op1 = setDesignation.getDesignator1();
		Designator op2 = setDesignation.getDesignator2();
		if(!Utils.objExists(leftD.getName())){
			Utils.report_error("set " + leftD.getName() + " is undeclared", leftD);
			return;
		}
		if(!Utils.objExists(op1.getName())){
			Utils.report_error("set " + op1.getName() + " is undeclared", leftD);
			return;
		}
		if(!Utils.objExists(op2.getName())){
			Utils.report_error("set " + op2.getName() + " is undeclared", leftD);
			return;
		}
		Obj d1 = Tab.find(leftD.getName());
		Obj d2 = Tab.find(op1.getName());
		Obj d3 = Tab.find(op2.getName());
		Utils.report_info("use of variable "+d1.getName(), leftD);
		Utils.report_info("use of variable "+d2.getName(), op1);
		Utils.report_info("use of variable "+d3.getName(), op2);
		if(!(d1.getType() instanceof SetType)){
			Utils.report_error("incorrect type for " + d1.getName() + " is undeclared", leftD);
		}
		if(!(d2.getType() instanceof SetType)){
			Utils.report_error("incorrect type for " + d2.getName() + " is undeclared", leftD);
		}
		if(!(d3.getType() instanceof SetType)){
			Utils.report_error("incorrect type for " + d3.getName() + " is undeclared", leftD);
		}
	}
	static void designatorStatementPass(DesignatorStatement dStatement){
			if(dStatement instanceof PostDec){
				postDecPass((PostDec)dStatement);
			}
			else if(dStatement instanceof PostInc){
				postIncPass((PostInc)dStatement);
			}
			else if(dStatement instanceof VarDesignation){
				varDesignationPass((VarDesignation)dStatement);
			}
			else if(dStatement instanceof SetDesignation){
				setDesignationPass((SetDesignation)dStatement);
			}
	}
	static void statementPass(Statement statement){
		if(statement instanceof DesignatorStmt){
			DesignatorStmt stmt = (DesignatorStmt)statement;
			DesignatorStatement dStatement = stmt.getDesignatorStatement();
			designatorStatementPass(dStatement);
		}
		else if(statement instanceof WhileStmt){
			whilePass((WhileStmt)statement);
		}
		else if(statement instanceof IfStmt){
			ifPass((IfStmt)statement);
		}
	}
	static void statementsPass(Statements statements){
		List<Statement> list = new ArrayList<>();
		while(statements instanceof StatementsRecursive){
			StatementsRecursive statementsR = (StatementsRecursive)statements;
			Statement statement = statementsR.getStatement();
			list.add(statement);
			statements = statementsR.getStatements();
		}
		list.forEach(Analyzer::statementPass);
	}
	static void conditionPass(Condition cond){
		//TODO
	}
	static void postDecPass(PostDec postDecExpr){
		Designator d = postDecExpr.getDesignator();
		String name = d.getName();
		Utils.report_info("use of variable " + name,postDecExpr);
		Obj node = Tab.find(name);
		if(node == Tab.noObj){
			Utils.report_error("use of undeclared identifier " + name,postDecExpr);
		}
		if(node.getKind() != Obj.Var){
			Utils.report_error("cannot use identifier " + name + " in this context",postDecExpr);
		}
		Struct type = node.getType();
		if(type.getKind() != Struct.Int){
			Utils.report_error("cannot use this operator on the variable " + name,postDecExpr);
		}
	}
	static void ifPass(IfStmt stmt){
		IfStatement statement = stmt.getIfStatement();
		if (statement instanceof IfOnly){
			IfOnly ifOnly = (IfOnly)statement;
			Condition cond = ifOnly.getCondition();
			Statement s = ifOnly.getStatement();
			statementPass(s);
			conditionPass(cond);
		}
		else{
			IfElse ifElse = (IfElse) statement;
			Condition cond = ifElse.getCondition();
			conditionPass(cond);
			Statement s = ifElse.getStatement();
			statementPass(s);
			s = ifElse.getStatement1();
			statementPass(s);
		}
	}
	static void whilePass(WhileStmt stmt){
		DoWhile doWhile = stmt.getDoWhile();
		if (doWhile instanceof WhileCond){
			WhileCond whileCond = (WhileCond) doWhile;
			conditionPass(whileCond.getCondition());
			statementPass(whileCond.getStatement());
		}
		else if(doWhile instanceof WhileSimple){
			WhileSimple whileSimple = (WhileSimple) doWhile;
			statementPass(whileSimple.getStatement());
		}
		else if(doWhile instanceof WhileDesignator){
			WhileDesignator whileDesignator = (WhileDesignator)doWhile;
			statementPass(whileDesignator.getStatement());
			conditionPass(whileDesignator.getCondition());
			designatorStatementPass(whileDesignator.getDesignatorStatement());
		}
	}
}
