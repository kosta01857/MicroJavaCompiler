package com.kosta.pp1.semanticAnalysis;

import java.util.ArrayList;
import java.util.List;

import com.kosta.pp1.ast.ActPars;
import com.kosta.pp1.ast.ActParsConcrete;
import com.kosta.pp1.ast.AddTerm;
import com.kosta.pp1.ast.ConditionFact;
import com.kosta.pp1.ast.ConditionFactExpression;
import com.kosta.pp1.ast.ConditionFactExpressions;
import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.Equal;
import com.kosta.pp1.ast.ExprAddTerm;
import com.kosta.pp1.ast.ExprMinusAddTerm;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.MapExpr;
import com.kosta.pp1.ast.NotEqual;
import com.kosta.pp1.ast.Relop;
import com.kosta.pp1.ast.Term;
import com.kosta.pp1.semanticAnalysis.conditionAnalyzer.ConditionAnalyzer;
import com.kosta.pp1.semanticAnalysis.factorAnalyzers.FactorAnalyzer;
import com.kosta.pp1.semanticAnalysis.factorAnalyzers.FactorAnalyzerRegistry;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class TypeChecker {
	public static boolean minus = false;
	static private List<Struct> getFactorTypes (List<Factor> factors) {
		List<Struct> exprTypes = new ArrayList<>();
		for (Factor t : factors) {
			FactorAnalyzer fAnalyzer = FactorAnalyzerRegistry.getAnalyzerMap().get(t.getClass());
			exprTypes.add(fAnalyzer.getType(t));
		}
		return exprTypes;
	}

	static private boolean compatibleClassTypes(Struct leftSide,Struct rightSide){
		while(rightSide != null){
			if (leftSide.compatibleWith(rightSide)) return true;
			rightSide = rightSide.getElemType();
		}
		return false;
	}

	static private Struct analyzeFactors(List<Struct> factorTypes,boolean minus){
		int count = factorTypes.size();
		StringBuilder builder = new StringBuilder();
		factorTypes.forEach(factor -> builder.append(Utils.typeString(factor) + " "));
		Utils.report_info(builder.toString(),null);
		for(Struct s:factorTypes){
			if(s == null) return null;
			boolean isNotInt = s.getKind() != Struct.Int;
			if(minus && isNotInt){
				return null;
			}
			if(count > 1 && isNotInt){
				return null;
			}
		}
		return factorTypes.get(0);
	}

	static public Struct addExprType(AddTerm addTerm, boolean minus) {
		List<Term> terms = Finder.findTerms(addTerm);
		List<Factor> factors = new ArrayList<>();
		for (Term t : terms) {
			factors.addAll(Finder.findFactors(t));
		}
		Struct exprType = analyzeFactors(TypeChecker.getFactorTypes(factors), minus);
		if (exprType == null)
			Utils.report_error("expression is of incorrect type", addTerm);
		return exprType;
	}

	static public Struct mapExprTypeCheck(MapExpr mapExpr) {
		Designator d1 = mapExpr.getDesignator();
		Designator d2 = mapExpr.getDesignator1();
		Obj funcObj = Tab.find(d1.getName());
		Obj arrObj = Tab.find(d2.getName());
		Struct type = arrObj.getType();
		if (funcObj == Tab.noObj || arrObj == Tab.noObj) {
			return null;
		}
		List<Struct> argType = Register.functionTypeMap.get(funcObj);
		Boolean funcCheck = funcObj.getKind() == Obj.Meth;
		funcCheck &= funcObj.getLevel() == 1;
		funcCheck &= funcObj.getType().getKind() == Struct.Int;
		funcCheck &= argType != null ? argType.size() == 1 && argType.get(0).getKind() == Struct.Int : false;
		if (!funcCheck) {
			Utils.report_error("left Designator must be a function of argument int and of return type int", mapExpr);
			type = null;
		}
		Boolean arrCheck = arrObj.getKind() == Obj.Var;
		Struct elemType = arrObj.getType().getElemType();
		arrCheck &= arrObj.getType().getKind() == Struct.Array;
		arrCheck &= elemType != null ? elemType.getKind() == Struct.Int : false;
		if (!arrCheck) {
			Utils.report_error("right Designator must be an array of Integers", mapExpr);
			type = null;
		}
		return type;
	}

	static public Struct getExpressionType(Expression expr){
		if (expr instanceof ExprAddTerm) {
			return addExprType(((ExprAddTerm) expr).getAddTerm(),false);
		} else if (expr instanceof ExprMinusAddTerm) {
			return addExprType(((ExprAddTerm) expr).getAddTerm(),true);
		} else if (expr instanceof MapExpr) {
			return mapExprTypeCheck((MapExpr) expr);
		}
		return null;
	}

	/**
	 * Checks if the Expression types matches the desired type
	 * 
	 * @param expr
	 * @param type
	 * @return Returns true if types match
	 */
	static public boolean ExprTypeCheck(Expression expr, Struct type) {
		Struct s = getExpressionType(expr);
		if (type.getKind() == Struct.Class && s.getKind() == Struct.Class){
			if(!compatibleClassTypes(type,s)) return false;
		}
		if (!type.compatibleWith(s)){
			return false;
		}
		return true;
	}


	static public boolean conditionTypeCheck(List<ConditionFact> factors){
		boolean err = !ConditionAnalyzer.analyzeCondFactors(factors);
		if(err) Utils.report_error("bad conditional expression",factors.get(0));
		return err;
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
