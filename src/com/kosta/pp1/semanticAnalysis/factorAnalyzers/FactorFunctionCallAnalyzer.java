package com.kosta.pp1.semanticAnalysis.factorAnalyzers;

import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.FactorFunctionCall;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

class FactorFunctionCallAnalyzer implements FactorAnalyzer{
	@Override
	public boolean analyze(Factor factor,Struct type,int cnt){
		FactorFunctionCall funcCall = (FactorFunctionCall)factor;
		Designator d = funcCall.getDesignator();
		String name = d.getName();
		Obj obj = Tab.find(name);
		if (obj.getKind() == Obj.Meth){
			return false;
		}
		return true;
	}

	@Override
	public Struct getType(Factor factor){
		FactorFunctionCall funcCall = (FactorFunctionCall)factor;
		Designator d = funcCall.getDesignator();
		String name = d.getName();
		Obj obj = Tab.find(name);
		if (obj.getKind() == Obj.Meth){
			return obj.getType();
		}
		return null;
	}
	
}

