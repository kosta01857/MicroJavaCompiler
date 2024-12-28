package com.kosta.pp1.semanticAnalysis;

import java.util.ArrayList;
import java.util.List;

import com.kosta.pp1.ast.ActPars;
import com.kosta.pp1.ast.ActParsConcrete;
import com.kosta.pp1.ast.AddTerm;
import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.ExprAddTerm;
import com.kosta.pp1.ast.ExprMinusAddTerm;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.MapExpr;
import com.kosta.pp1.ast.Term;
import com.kosta.pp1.semanticAnalysis.factorAnalyzers.FactorAnalyzer;
import com.kosta.pp1.semanticAnalysis.factorAnalyzers.FactorAnalyzerRegistry;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class TypeChecker {
	public static boolean minus = false;
	// returns true if the expr is incorrect
	static public boolean analyzeFactors(List<Factor> factors, Struct type, boolean minus) {
		TypeChecker.minus = minus;
		int cnt = 0;
		for (Factor t : factors) {
			cnt++;
			FactorAnalyzer fAnalyzer = FactorAnalyzerRegistry.getAnalyzerMap().get(t.getClass());
			if(fAnalyzer.analyze(t,type,cnt)){
				return true;
			}
		}
		return false;
	}

	static public boolean addExprTypeCheck(AddTerm addTerm, Struct type, boolean minus) {
		boolean error = false;
		List<Term> terms = Finder.findTerms(addTerm);
		List<Factor> factors = new ArrayList<>();
		for (Term t : terms) {
			factors.addAll(Finder.findFactors(t));
		}
		error = analyzeFactors(factors, type, minus);
		if (error)
			Utils.report_error("expression is of incorrect type", addTerm);
		return error;
	}

	static public boolean mapExprTypeCheck(MapExpr mapExpr) {
		boolean error = false;
		Designator d1 = mapExpr.getDesignator();
		Designator d2 = mapExpr.getDesignator1();
		Obj funcObj = Tab.find(d1.getName());
		Obj arrObj = Tab.find(d2.getName());
		if (funcObj == Tab.noObj || arrObj == Tab.noObj) {
			return true;
		}
		List<Struct> argType = Register.functionTypeMap.get(funcObj);
		Boolean funcCheck = funcObj.getKind() == Obj.Meth;
		funcCheck &= funcObj.getLevel() == 1;
		funcCheck &= funcObj.getType().getKind() == Struct.Int;
		funcCheck &= argType != null ? argType.size() == 1 && argType.get(0).getKind() == Struct.Int : false;
		if (!funcCheck) {
			Utils.report_error("left Designator must be a function of argument int and of return type int", mapExpr);
			error = true;
		}
		Boolean arrCheck = arrObj.getKind() == Obj.Var;
		Struct elemType = arrObj.getType().getElemType();
		arrCheck &= arrObj.getType().getKind() == Struct.Array;
		arrCheck &= elemType != null ? elemType.getKind() == Struct.Int : false;
		if (!arrCheck) {
			Utils.report_error("right Designator must be an array of Integers", mapExpr);
			error = true;
		}
		return error;
	}

	/**
	 * Checks if the Expression types matches the desired type
	 * 
	 * @param expr
	 * @param type
	 * @return Returns true if types match
	 */
	static public boolean ExprTypeCheck(Expression expr, Struct type) {
		if (expr instanceof ExprAddTerm) {
			return !addExprTypeCheck(((ExprAddTerm) expr).getAddTerm(), type, false);
		} else if (expr instanceof ExprMinusAddTerm) {
			return !addExprTypeCheck(((ExprAddTerm) expr).getAddTerm(), type, true);
		} else if (expr instanceof MapExpr) {
			return !mapExprTypeCheck((MapExpr) expr);
		}
		return false;
	}

	/**
	 * Checks if the arguments given to a function or method match the arguments
	 * from its signature
	 * 
	 * @param expr
	 * @param type
	 * @return Returns true if call matches the signature
	 */
	static boolean actParsTypeCheck(ActPars actPars, Obj currentFunction) {
		List<Struct> args = Register.functionTypeMap.get(currentFunction);
		if (actPars instanceof ActParsConcrete) {
			ActParsConcrete actParamsC = (ActParsConcrete) actPars;
			List<Expression> expressions = Finder.findExpressions(actParamsC.getExpressions());
			if (args.size() != expressions.size()) {
				Utils.report_error("Call of function " + currentFunction.getName() + " does not match its signature",
						actPars);
				return false;
			}
			for (int i = 0; i < expressions.size(); i++) {
				Expression exp = expressions.get(i);
				Struct argType = args.get(i);
				if (!TypeChecker.ExprTypeCheck(exp, argType)) {
					Utils.report_error(
							"Call of function " + currentFunction.getName() + " does not match its signature", actPars);
					return false;
				}
			}
		} else if (args.size() > 0) {
			Utils.report_error("Call of function " + currentFunction.getName() + " does not match its signature",
					actPars);
			return false;
		}
		return true;
	}
}
