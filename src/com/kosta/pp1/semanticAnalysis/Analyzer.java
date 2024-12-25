package com.kosta.pp1.semanticAnalysis;

import com.kosta.pp1.ast.ActPars;
import com.kosta.pp1.ast.ActParsConcrete;
import com.kosta.pp1.ast.Break;
import com.kosta.pp1.ast.Condition;
import com.kosta.pp1.ast.ConstDeclarationList;
import com.kosta.pp1.ast.Continue;
import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.DesignatorStatement;
import com.kosta.pp1.ast.DesignatorStmt;
import com.kosta.pp1.ast.DoWhile;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.FunctionCall;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.ast.VarDesignation;
import com.kosta.pp1.ast.WhileCond;
import com.kosta.pp1.ast.WhileDesignator;
import com.kosta.pp1.ast.WhileSimple;
import com.kosta.pp1.ast.WhileStmt;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import java.util.List;
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
import com.kosta.pp1.ast.StatementBlock;
import com.kosta.pp1.ast.Statements;
import com.kosta.pp1.ast.Type;

public class Analyzer {
	static boolean inDoWhile = false;
	static Obj currentFunction = null;

	static void varDesignationPass(VarDesignation varDesignation) {
		Expression expr = varDesignation.getExpression();
		String name = varDesignation.getDesignator().getName();
		if (!Utils.objExists(name)) {
			Utils.report_error("use of undeclared identifier " + name, varDesignation);
		}
		Obj ident = Tab.find(name);
		Struct type = ident.getType();
		Boolean typeCheck;
		typeCheck = ident.getKind() == Obj.Var;
		typeCheck |= ident.getKind() == Obj.Fld;
		typeCheck |= ident.getKind() == Obj.Elem;
		if (!typeCheck) {
			Utils.report_error("Designator cannot be of type " + Utils.typeString(type), varDesignation);
			return;
		}
		boolean error = TypeChecker.ExprTypeCheck(expr, type);
		Utils.reportUse(ident, varDesignation);
	}

	static void declarationListPass(VarDeclarationList list) {
		Type type = list.getType();
		Struct struct = Utils.inferType(type);
		if (struct == Tab.noType) {
			Utils.report_error("Type of name " + type.getTypeName() + " doesnt exist", list);
			return;
		}
		SemanticAnalyzer.currentType = struct;
		List<IdDeclaration> idDecls = Finder.findIdDeclarations(list.getVarDeclaration());
		idDecls.forEach(Register::registerIdDeclaration);
	}

	static void definitionListPass(ConstDeclarationList list) {
		Type type = list.getType();
		Struct struct = Utils.inferType(type);
		if (struct == Tab.noType) {
			Utils.report_error("Type of name " + type.getTypeName() + " doesnt exist", list);
			return;
		}
		SemanticAnalyzer.currentType = struct;
		List<IdDefinition> idDecls = Finder.findIdDefinitions(list.getIdDefinitionList());
		idDecls.forEach(Register::registerIdDefinition);
	}

	static void postIncPass(PostInc postIncExpr) {
		Designator d = postIncExpr.getDesignator();
		String name = d.getName();
		Utils.report_info("use of variable " + name, postIncExpr);
		Obj node = Tab.find(name);
		if (node == Tab.noObj) {
			Utils.report_error("use of undeclared identifier " + name, postIncExpr);
		}
		if (node.getKind() != Obj.Var) {
			Utils.report_error("cannot use identifier " + name + " in this context", postIncExpr);
		}
		Struct type = node.getType();
		if (type.getKind() != Struct.Int) {
			Utils.report_error("cannot use this operator on the variable " + name, postIncExpr);
		}
	}

	static void setDesignationPass(SetDesignation setDesignation) {
		Designator leftD = setDesignation.getDesignator();
		Designator op1 = setDesignation.getDesignator1();
		Designator op2 = setDesignation.getDesignator2();
		if (!Utils.objExists(leftD.getName())) {
			Utils.report_error("set " + leftD.getName() + " is undeclared", leftD);
			return;
		}
		if (!Utils.objExists(op1.getName())) {
			Utils.report_error("set " + op1.getName() + " is undeclared", leftD);
			return;
		}
		if (!Utils.objExists(op2.getName())) {
			Utils.report_error("set " + op2.getName() + " is undeclared", leftD);
			return;
		}
		Obj d1 = Tab.find(leftD.getName());
		Obj d2 = Tab.find(op1.getName());
		Obj d3 = Tab.find(op2.getName());
		Utils.report_info("use of variable " + d1.getName(), leftD);
		Utils.report_info("use of variable " + d2.getName(), op1);
		Utils.report_info("use of variable " + d3.getName(), op2);
		if (!(d1.getType() instanceof SetType)) {
			Utils.report_error("incorrect type for " + d1.getName() + " is undeclared", leftD);
		}
		if (!(d2.getType() instanceof SetType)) {
			Utils.report_error("incorrect type for " + d2.getName() + " is undeclared", leftD);
		}
		if (!(d3.getType() instanceof SetType)) {
			Utils.report_error("incorrect type for " + d3.getName() + " is undeclared", leftD);
		}
	}

	static void designatorStatementPass(DesignatorStatement dStatement) {
		if (dStatement instanceof PostDec) {
			postDecPass((PostDec) dStatement);
		} else if (dStatement instanceof PostInc) {
			postIncPass((PostInc) dStatement);
		} else if (dStatement instanceof VarDesignation) {
			varDesignationPass((VarDesignation) dStatement);
		} else if (dStatement instanceof SetDesignation) {
			setDesignationPass((SetDesignation) dStatement);
		} else if (dStatement instanceof FunctionCall){
			functionCallPass((FunctionCall)dStatement);
		}
	}

	static void functionCallPass(FunctionCall call){
		Designator d = call.getDesignator();
		String name = d.getName();
		if(!Utils.objExists(name)){
			Utils.report_error("call of undeclared function "+name,call);
			return;
		}
		currentFunction = Tab.find(name);
		boolean funcCheck;
		if (currentFunction.getKind() != Obj.Meth){
			Utils.report_error("Designator " + name  + " is not a function!",call);
			return;
		}
		Utils.reportUse(currentFunction,call);
		actParsPass(call.getActPars());
	}

	static void actParsPass(ActPars actPars){
		List<Struct> args = Register.functionTypeMap.get(currentFunction);
		if(actPars instanceof ActParsConcrete){
			ActParsConcrete actParamsC = (ActParsConcrete) actPars;
			List<Expression> expressions = Finder.findExpressions(actParamsC.getExpressions());
			if(args.size() != expressions.size()){
				Utils.report_error("Call of function " + currentFunction.getName() + " does not match its signature",actPars);
				return;
			}
			for(int i = 0;i < expressions.size();i++){
				Expression exp = expressions.get(i);
				Struct argType = args.get(i);
				if(!TypeChecker.ExprTypeCheck(exp,argType)){
					Utils.report_error("Call of function " + currentFunction.getName() + " does not match its signature",actPars);
					return;
				}
			}
		}
		else if (args.size() > 0){
			Utils.report_error("Call of function " + currentFunction.getName() + " does not match its signature",actPars);
		}
	}

	static void statementPass(Statement statement) {
		if (statement instanceof StatementBlock) {
			StatementBlock stmtBlk = (StatementBlock) statement;
			statementsPass(stmtBlk.getStatements());
		}
		if (statement instanceof DesignatorStmt) {
			DesignatorStmt stmt = (DesignatorStmt) statement;
			DesignatorStatement dStatement = stmt.getDesignatorStatement();
			designatorStatementPass(dStatement);
		} else if (statement instanceof WhileStmt) {
			whilePass((WhileStmt) statement);
		} else if (statement instanceof IfStmt) {
			ifPass((IfStmt) statement);
		} else if (statement instanceof Break) {
			if (!inDoWhile) {
				Utils.report_error("cannot use break outside do while loop", statement);
			}
		} else if (statement instanceof Continue) {
			if (!inDoWhile) {
				Utils.report_error("cannot use continue outside do while loop", statement);
			}
		}
	}

	static void statementsPass(Statements statements) {
		List<Statement> list = Finder.findStatements(statements);
		list.forEach(Analyzer::statementPass);
	}

	static void conditionPass(Condition cond) {

	}

	static void postDecPass(PostDec postDecExpr) {
		Designator d = postDecExpr.getDesignator();
		String name = d.getName();
		Utils.report_info("use of variable " + name, postDecExpr);
		Obj node = Tab.find(name);
		if (node == Tab.noObj) {
			Utils.report_error("use of undeclared identifier " + name, postDecExpr);
		}
		Boolean badObj;
		badObj = !(node.getKind() == Obj.Var);
		badObj &= !(node.getKind() == Obj.Elem);
		badObj &= !(node.getKind() == Obj.Fld);
		if (badObj) {
			Utils.report_error("cannot use identifier " + name + " in this context", postDecExpr);
		}
		Struct type = node.getType();
		if (type.getKind() != Struct.Int) {
			Utils.report_error("cannot use this operator on the variable " + name, postDecExpr);
		}
	}

	static void ifPass(IfStmt stmt) {
		IfStatement statement = stmt.getIfStatement();
		if (statement instanceof IfOnly) {
			IfOnly ifOnly = (IfOnly) statement;
			Condition cond = ifOnly.getCondition();
			Statement s = ifOnly.getStatement();
			statementPass(s);
			conditionPass(cond);
		} else {
			IfElse ifElse = (IfElse) statement;
			Condition cond = ifElse.getCondition();
			conditionPass(cond);
			Statement s = ifElse.getStatement();
			statementPass(s);
			s = ifElse.getStatement1();
			statementPass(s);
		}
	}

	static void whilePass(WhileStmt stmt) {
		DoWhile doWhile = stmt.getDoWhile();
		inDoWhile = true;
		if (doWhile instanceof WhileCond) {
			WhileCond whileCond = (WhileCond) doWhile;
			conditionPass(whileCond.getCondition());
			statementsPass(whileCond.getStatements());
		} else if (doWhile instanceof WhileSimple) {
			WhileSimple whileSimple = (WhileSimple) doWhile;
			statementsPass(whileSimple.getStatements());
		} else if (doWhile instanceof WhileDesignator) {
			WhileDesignator whileDesignator = (WhileDesignator) doWhile;
			statementsPass(whileDesignator.getStatements());
			conditionPass(whileDesignator.getCondition());
			designatorStatementPass(whileDesignator.getDesignatorStatement());
		}
		inDoWhile = false;
	}
}
