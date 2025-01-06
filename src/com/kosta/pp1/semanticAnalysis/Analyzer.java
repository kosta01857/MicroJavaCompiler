package com.kosta.pp1.semanticAnalysis;

import com.kosta.pp1.ast.Break;
import com.kosta.pp1.ast.ClassBody;
import com.kosta.pp1.ast.ClassMethodDeclarations;
import com.kosta.pp1.ast.ClassMethodDecls;
import com.kosta.pp1.ast.Condition;
import com.kosta.pp1.ast.ConditionFact;
import com.kosta.pp1.ast.ConditionTerm;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kosta.pp1.ast.IdDeclaration;
import com.kosta.pp1.ast.IdDefinition;
import com.kosta.pp1.ast.IfElse;
import com.kosta.pp1.ast.IfOnly;
import com.kosta.pp1.ast.IfStatement;
import com.kosta.pp1.ast.IfStmt;
import com.kosta.pp1.ast.LocalVarDeclarations;
import com.kosta.pp1.ast.MethodDeclaration;
import com.kosta.pp1.ast.MethodDeclarations;
import com.kosta.pp1.ast.MethodDefinition;
import com.kosta.pp1.ast.MethodDefinitionNoLocals;
import com.kosta.pp1.ast.MethodSignature;
import com.kosta.pp1.ast.PostDec;
import com.kosta.pp1.ast.PostInc;
import com.kosta.pp1.ast.PrintOne;
import com.kosta.pp1.ast.PrintStatement;
import com.kosta.pp1.ast.PrintStmt;
import com.kosta.pp1.ast.PrintTwo;
import com.kosta.pp1.ast.ReadStatement;
import com.kosta.pp1.ast.ReturnStatement;
import com.kosta.pp1.ast.ReturnStmt;
import com.kosta.pp1.ast.ReturnTyped;
import com.kosta.pp1.ast.ReturnVoid;
import com.kosta.pp1.ast.SetDesignation;
import com.kosta.pp1.ast.Statement;
import com.kosta.pp1.ast.StatementBlock;
import com.kosta.pp1.ast.Statements;
import com.kosta.pp1.ast.Type;

public class Analyzer {
	static boolean inDoWhile = false;
	static boolean returnFound = false;
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
			Utils.report_error("Designator cannot be of type " + Utils.ObjectName.values()[ident.getKind()].name,
					varDesignation);
			return;
		}
		boolean error = TypeChecker.ExprTypeCheck(expr, type);
		Utils.reportUse(ident, varDesignation);
	}

	static int declarationListPass(VarDeclarationList list) {
		Type type = list.getType();
		Struct struct = Utils.inferType(type);
		if (struct == Tab.noType) {
			Utils.report_error("Type of name " + type.getTypeName() + " doesnt exist", list);
			return 0;
		}
		SemanticAnalyzer.currentType = struct;
		List<IdDeclaration> idDecls = Finder.findIdDeclarations(list.getVarDeclaration());
		idDecls.forEach(Register::registerIdDeclaration);
		return idDecls.size();
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
		} else if (dStatement instanceof FunctionCall) {
			functionCallPass((FunctionCall) dStatement);
		}
	}

	static void functionCallPass(FunctionCall call) {
		Designator d = call.getDesignator();
		String name = d.getName();
		if (!Utils.objExists(name)) {
			Utils.report_error("call of undeclared function " + name, call);
			return;
		}
		Obj currentFunction = Tab.find(name);
		if (currentFunction.getKind() != Obj.Meth) {
			Utils.report_error("Designator " + name + " is not a function!", call);
			return;
		}
		Utils.reportUse(currentFunction, call);
		TypeChecker.actParsTypeCheck(call.getActPars(), currentFunction);
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
		} else if (statement instanceof ReturnStmt) {
			returnStatementPass((ReturnStmt) statement);
		} else if (statement instanceof PrintStmt){
			printStatementPass((PrintStmt)statement);
		} else if (statement instanceof ReadStatement){
			readStatementPass((ReadStatement)statement);
		}
	}

	static void printStatementPass(PrintStmt stmt){
		PrintStatement printStmt = stmt.getPrintStatement();
		Expression expr;
		if (printStmt instanceof PrintOne){
			PrintOne printOne = (PrintOne)printStmt;
			expr = printOne.getExpression();
		}
		else{
			PrintTwo printTwo = (PrintTwo)printStmt;
			expr = printTwo.getExpression();
		}
		boolean typeCheck;
		typeCheck = TypeChecker.ExprTypeCheck(expr,Tab.find("int").getType());
		typeCheck |= TypeChecker.ExprTypeCheck(expr,Tab.find("char").getType());
		typeCheck |= TypeChecker.ExprTypeCheck(expr,Tab.find("bool").getType());
		typeCheck |= TypeChecker.ExprTypeCheck(expr,SetType.setType);
		if(!typeCheck){
			Utils.report_error("incorrect print call",stmt);
		}
	}
	
	static void readStatementPass(ReadStatement stmt){
		Designator designator = stmt.getDesignator();
		boolean typeCheck;
		String name = designator.getName();
		if(!Utils.objExists(name)){
			Utils.report_error("use of undeclared identifer " + name,stmt);
		}
		Obj dObj = Tab.find(designator.getName());
		Struct dType = dObj.getType();
		typeCheck = dType.getKind() == Struct.Int;
		typeCheck |= dType.getKind() == Struct.Bool;
		typeCheck |= dType.getKind() == Struct.Char;
		boolean objTypeCheck;
		objTypeCheck = dObj.getKind() == Obj.Fld;
		objTypeCheck |= dObj.getKind() == Obj.Var;
		objTypeCheck |= dObj.getKind() == Obj.Elem;
		if(!typeCheck || !objTypeCheck){
			Utils.report_error("incorrect read call",stmt);
		}
	}

	static void returnStatementPass(ReturnStmt stmt) {
		ReturnStatement retStmt = stmt.getReturnStatement();
		if (retStmt instanceof ReturnVoid) {
			if (currentFunction.getType().getKind() != Struct.None) {
				Utils.report_error(
						"this function must return the value of type " + Utils.typeString(currentFunction.getType()),
						stmt);
			}
			return;
		}
		returnFound = true;
		ReturnTyped retTyped = (ReturnTyped) retStmt;
		Expression expr = retTyped.getExpression();
		if (!TypeChecker.ExprTypeCheck(expr, currentFunction.getType())) {
			Utils.report_error(
					"declared return type of function does not match the type of value you are trying to return!",
					stmt);
		}
	}

	static void statementsPass(Statements statements) {
		List<Statement> list = Finder.findStatements(statements);
		list.forEach(Analyzer::statementPass);
	}

	static void conditionPass(Condition cond) {
		List<ConditionTerm> terms = Finder.findConditionTerms(cond);
		List<ConditionFact> condFactors = new ArrayList<>();
		terms.forEach(term -> condFactors.addAll(Finder.findConditionFactors(term)));
		TypeChecker.conditionTypeCheck(condFactors);
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

	static void methodDeclarationPass(MethodDeclaration decl){
		if (decl instanceof MethodDefinitionNoLocals){
			MethodDefinitionNoLocals methodDef = (MethodDefinitionNoLocals)decl;
			MethodSignature signature = methodDef.getMethodSignature();
			Statements statements = methodDef.getStatements();
			Obj funcObj = Register.registerMethod(signature);
			Tab.chainLocalSymbols(funcObj);
			Analyzer.currentFunction = funcObj;
			Analyzer.returnFound = false;
			Analyzer.statementsPass(statements);
			if(funcObj.getType().getKind() != Struct.None && !Analyzer.returnFound){
				Utils.report_error("function "+funcObj.getName() + " must have a return statement",methodDef);
			}
			Tab.closeScope();
			}
		else{
			MethodDefinition methodDefinition = (MethodDefinition)decl;
			MethodSignature signature = methodDefinition.getMethodSignature();
			LocalVarDeclarations varDecls = methodDefinition.getLocalVarDeclarations();
			Statements statements = methodDefinition.getStatements();
			Obj funcObj = Register.registerMethod(signature);
			Register.registerLocalVariables(varDecls);
			Tab.chainLocalSymbols(funcObj);
			Analyzer.currentFunction = funcObj;
			Analyzer.returnFound = false;
			Analyzer.statementsPass(statements);
			if(funcObj.getType().getKind() != Struct.None && !Analyzer.returnFound){
				Utils.report_error("function "+funcObj.getName() + " must have a return statement",methodDefinition);
			}
			Tab.closeScope();
			// Tab.dump();
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
	
	static int classBodyPass(ClassBody body){
		Register.inClass = true;
		ClassMethodDeclarations classMethodDecls = body.getClassMethodDeclarations();
		if (classMethodDecls instanceof ClassMethodDecls){
			ClassMethodDecls decls = (ClassMethodDecls)classMethodDecls;
			MethodDeclarations methodDecls = decls.getMethodDeclarations();
			List<MethodDeclaration> methodDeclList = Finder.findMethodDeclarations(methodDecls);
			methodDeclList.forEach(Analyzer::methodDeclarationPass);
		}
		LocalVarDeclarations localVarDecls = body.getLocalVarDeclarations();
		List<VarDeclarationList> varDeclLists = Finder.findVarDeclarationLists(localVarDecls);
		int cnt = 0;
		for(VarDeclarationList list : varDeclLists){
			cnt += declarationListPass(list);
		}
		Register.inClass = false;
		return cnt;
	}
}
