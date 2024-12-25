package com.kosta.pp1.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.kosta.pp1.semanticAnalysis.SetType;
import com.kosta.pp1.ast.AddTerm;
import com.kosta.pp1.ast.AddTermConcrete;
import com.kosta.pp1.ast.AddTermRecursive;
import com.kosta.pp1.ast.BOOL;
import com.kosta.pp1.ast.CHAR;
import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.ExprAddTerm;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.FactorIdent;
import com.kosta.pp1.ast.FactorLiteral;
import com.kosta.pp1.ast.Literal;
import com.kosta.pp1.ast.NUMBER;
import com.kosta.pp1.ast.SyntaxNode;
import com.kosta.pp1.ast.Term;
import com.kosta.pp1.ast.TermConcrete;
import com.kosta.pp1.ast.TermRecursive;
import com.kosta.pp1.semanticAnalysis.Utils;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzerUtils {
	static public boolean analyzeFactors(List<Factor> factors,Struct type){
		boolean error = false;
		int cnt = 0;
		for(Factor t : factors){
			cnt++;
			if(t instanceof FactorIdent){
				FactorIdent factorI = (FactorIdent)t; 
				String name = factorI.getDesignator().getName();
				Obj obj = Tab.find(name);
				Utils.reportUse(obj,factorI);
				if(obj.getType().isRefType() && cnt > 1){
					Utils.report_error("cannot use type of "+Utils.typeString(obj.getType()), factorI);
					return true;
				}
				if(obj == Tab.noObj){
					Utils.report_error("use of undeclared identifier "+name, factorI);
					return true;
				}
				if(!obj.getType().assignableTo(type)){
					Utils.report_error("cannot assign type of "+Utils.typeString(obj.getType()), factorI);
					return true;
				}
			}
			else if(t instanceof FactorLiteral){
				FactorLiteral factL = (FactorLiteral)t;
				Literal literal = factL.getLiteral();
				if(literal instanceof BOOL || literal instanceof CHAR){
					error = true;
					break;
				}
				if (!Utils.literalTypeCheck(literal, type)){
					error = true;
					break;
				}
			}
		}
		return error;
	}
	static public void packFactors(Term term,List<Factor> factors){
		while(term instanceof TermRecursive){
			TermRecursive termRecursive = (TermRecursive)term;
			Factor factor = termRecursive.getFactor();
			if(factor instanceof FactorLiteral || factor instanceof FactorIdent){
					factors.add(factor);
			}
			term = termRecursive.getTerm();
		}
		TermConcrete termConcrete = (TermConcrete) term;
		Factor factor = termConcrete.getFactor();
		if(factor instanceof FactorLiteral || factor instanceof FactorIdent){
			factors.add(factor);
		}
	}
	static public boolean AddExprTypeCheck(AddTerm addTerm,List<Factor> factors,Struct type){
		boolean error = false;
		while(addTerm instanceof AddTermRecursive){
			AddTermRecursive addTermRecursive = (AddTermRecursive)addTerm;
			Term term = addTermRecursive.getTerm();
			packFactors(term,factors);
			addTerm = addTermRecursive.getAddTerm();
		}
		AddTermConcrete addTermConcrete = (AddTermConcrete)addTerm;
		Term term = addTermConcrete.getTerm();
		packFactors(term,factors);
		error = analyzeFactors(factors,type);
		if(error) Utils.report_error("expression is of incorrect type", null);
		return error;
	}
	static public boolean ExprTypeCheck(Expression expr,Struct type){
		boolean error = false;
		List<Factor> factors = new ArrayList<>();
		if (expr instanceof ExprAddTerm){
			ExprAddTerm exprAddTerm = (ExprAddTerm)expr;
			AddTerm addTerm = exprAddTerm.getAddTerm();
			error = AddExprTypeCheck(addTerm,factors,type);
		}
		return error;
	}
}

