package com.kosta.pp1.semanticAnalysis;

import com.kosta.pp1.Register;

/**
 * SemanticAnalyzerFactory
 */
public class SemanticAnalyzerFactory {
	public static SemanticAnalyzer createInstance(){
		Register reg = Register.getInstance();
		Analyzer analyzer = Analyzer.getInstance();
		analyzer.setRegister(reg);
		SemanticAnalyzer sa = SemanticAnalyzer.getInstance();
		sa.setAnalyzer(analyzer);
		return sa;
	}
	
}
