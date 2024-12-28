package com.kosta.pp1.semanticAnalysis.factorAnalyzers;

import java.util.HashMap;
import java.util.Map;

import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.FactorIdent;
import com.kosta.pp1.ast.FactorLiteral;
import com.kosta.pp1.ast.FactorNewType;
import com.kosta.pp1.ast.FactorFunctionCall;

public class FactorAnalyzerRegistry{
	static private final Map<Class<? extends Factor>,FactorAnalyzer> analyzers = new HashMap<>();
	static
	{
		analyzers.put(FactorIdent.class,new FactorIdentAnalyzer());
		analyzers.put(FactorLiteral.class,new FactorLiteralAnalyzer());
		analyzers.put(FactorNewType.class,new FactorNewTypeAnalyzer());
		analyzers.put(FactorFunctionCall.class,new FactorFunctionCallAnalyzer());
	}
	public static Map<Class<? extends Factor>,FactorAnalyzer> getAnalyzerMap(){
		return analyzers;
	}
}

