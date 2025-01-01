package com.kosta.pp1.semanticAnalysis.factorAnalyzers;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Struct;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.Literal;
import com.kosta.pp1.ast.NUMBER;
import com.kosta.pp1.ast.BOOL;
import com.kosta.pp1.ast.CHAR;
import com.kosta.pp1.ast.FactorLiteral;
import com.kosta.pp1.semanticAnalysis.Utils;
import com.kosta.pp1.semanticAnalysis.TypeChecker;

class FactorLiteralAnalyzer implements FactorAnalyzer{
	@Override
	public boolean analyze(Factor factor,Struct type,int cnt){
		FactorLiteral factL = (FactorLiteral)factor;
		Literal literal = factL.getLiteral();
		Boolean notInt = literal instanceof BOOL || literal instanceof CHAR;
		if (TypeChecker.minus ? notInt : notInt && cnt > 1) {
			return true;
		}
		if (!Utils.literalTypeCheck(literal, type)) {
			return true;
		}
		return false;
	}

	@Override
	public Struct getType(Factor factor){
		FactorLiteral factL = (FactorLiteral)factor;
		Literal literal = factL.getLiteral();
		if (literal instanceof BOOL) return Tab.find("bool").getType();
		if (literal instanceof CHAR) return Tab.find("char").getType();
		if (literal instanceof NUMBER) return Tab.find("int").getType();
		return null;
	}
}
