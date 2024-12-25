package com.kosta.pp1.semanticAnalysis;

import java.util.ArrayList;
import java.util.List;

import com.kosta.pp1.ast.AddTerm;
import com.kosta.pp1.ast.BOOL;
import com.kosta.pp1.ast.CHAR;
import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.ExprAddTerm;
import com.kosta.pp1.ast.ExprMinusAddTerm;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.FactorIdent;
import com.kosta.pp1.ast.FactorLiteral;
import com.kosta.pp1.ast.Literal;
import com.kosta.pp1.ast.MapExpr;
import com.kosta.pp1.ast.Term;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class TypeChecker{
	static public boolean analyzeAddFactors(List<Factor> factors, Struct type, boolean minus) {
		boolean error = false;
		int cnt = 0;
		for (Factor t : factors) {
			cnt++;
			if (t instanceof FactorIdent) {
				FactorIdent factorI = (FactorIdent) t;
				String name = factorI.getDesignator().getName();
				Obj obj = Tab.find(name);
				Utils.reportUse(obj, factorI);
				if (obj.getType().isRefType() && cnt > 1) {
					Utils.report_error("cannot use type of " + Utils.typeString(obj.getType()), factorI);
					return true;
				}
				if (obj == Tab.noObj) {
					Utils.report_error("use of undeclared identifier " + name, factorI);
					return true;
				}
				if (!obj.getType().assignableTo(type)) {
					Utils.report_error("cannot assign type of " + Utils.typeString(obj.getType()), factorI);
					return true;
				}
			} else if (t instanceof FactorLiteral) {
				FactorLiteral factL = (FactorLiteral) t;
				Literal literal = factL.getLiteral();
				Boolean notInt = literal instanceof BOOL || literal instanceof CHAR;
				if (minus ? notInt : notInt && cnt > 1) {
					error = true;
					break;
				}
				if (!Utils.literalTypeCheck(literal, type)) {
					error = true;
					break;
				}
			}
		}
		return error;
	}
	static public boolean AddExprTypeCheck(AddTerm addTerm, Struct type, boolean minus) {
		boolean error = false;
		List<Term> terms = Finder.findTerms(addTerm);
		List<Factor> factors = new ArrayList<>();
		for (Term t : terms) {
			factors.addAll(Finder.findFactors(t));
		}
		error = analyzeAddFactors(factors, type, minus);
		if (error)
			Utils.report_error("expression is of incorrect type", addTerm);
		return error;
	}
	static public boolean MapExprTypeCheck(MapExpr mapExpr){
		boolean error = false;
		Designator d1 = mapExpr.getDesignator();
		Designator d2 = mapExpr.getDesignator1();
		Obj funcObj = Tab.find(d1.getName());
		Obj arrObj = Tab.find(d2.getName());
		if (funcObj == Tab.noObj || arrObj == Tab.noObj){
			return true;
		}
		List<Struct> argType = Register.functionTypeMap.get(funcObj);
		Boolean funcCheck = funcObj.getKind() == Obj.Meth;
		funcCheck &= funcObj.getLevel() == 1;
		funcCheck &= funcObj.getType().getKind() == Struct.Int;
		funcCheck &= argType != null ? argType.size() == 1 && argType.get(0).getKind() == Struct.Int : false;
		if(!funcCheck){
			Utils.report_error("left Designator must be a function of argument int and of return type int",mapExpr);
			error = true;
		}
		Boolean arrCheck = arrObj.getKind() == Obj.Var;
		Struct elemType = arrObj.getType().getElemType();
		arrCheck &= arrObj.getType().getKind() == Struct.Array;
		arrCheck &= elemType != null ? elemType.getKind() == Struct.Int : false;
		if(!arrCheck){
			Utils.report_error("right Designator must be an array of Integers",mapExpr);
			error = true;
		} 	
		return error;
	}
	static public boolean ExprTypeCheck(Expression expr, Struct type) {
		boolean error = false;
		if (expr instanceof ExprAddTerm) {
			ExprAddTerm exprAddTerm = (ExprAddTerm) expr;
			AddTerm addTerm = exprAddTerm.getAddTerm();
			error = AddExprTypeCheck(addTerm, type, false);
		} else if (expr instanceof ExprMinusAddTerm) {
			ExprMinusAddTerm exprAddTerm = (ExprMinusAddTerm) expr;
			AddTerm addTerm = exprAddTerm.getAddTerm();
			error = AddExprTypeCheck(addTerm, type, true);
		}
		else if (expr instanceof MapExpr){
			MapExpr mapExpr = (MapExpr)expr;
			error = MapExprTypeCheck(mapExpr);

		}
		return !error;
	}
}
