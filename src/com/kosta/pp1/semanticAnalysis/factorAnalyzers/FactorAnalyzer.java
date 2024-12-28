package com.kosta.pp1.semanticAnalysis.factorAnalyzers;

import rs.etf.pp1.symboltable.concepts.Struct;
import com.kosta.pp1.ast.Factor;
interface FactorAnalyzer {
    boolean analyze(Factor factor, Struct type, int cnt);
}

