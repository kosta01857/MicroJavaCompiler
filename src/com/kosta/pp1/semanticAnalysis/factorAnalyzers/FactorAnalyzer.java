package com.kosta.pp1.semanticAnalysis.factorAnalyzers;

import rs.etf.pp1.symboltable.concepts.Struct;
import com.kosta.pp1.ast.Factor;
public interface FactorAnalyzer {
    boolean analyze(Factor factor, Struct type, int cnt);


	/** gets the type of the factor
	 * @param factor factor that we need the type of
	 * @return returns the type of factor or null if identifier does not exist
	 */
	Struct getType(Factor factor);

	default Struct getFactorType(Factor factor){
		FactorAnalyzer analyzer = FactorAnalyzerRegistry.getAnalyzerMap().get(factor.getClass());	
		return analyzer.getType(factor);
	}
}

