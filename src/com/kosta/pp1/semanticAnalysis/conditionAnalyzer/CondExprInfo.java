package com.kosta.pp1.semanticAnalysis.conditionAnalyzer;

import com.kosta.pp1.ast.Equal;
import com.kosta.pp1.ast.NotEqual;
import com.kosta.pp1.ast.Relop;

import rs.etf.pp1.symboltable.concepts.Struct;

public class CondExprInfo{
	Struct s1;
	Struct s2;
	Relop op;
	boolean analyze(){
		boolean check;
		check = s1.compatibleWith(s2);
		boolean opCheck = op instanceof Equal || op instanceof NotEqual;
		check &= s1.isRefType() ? opCheck : true; 
		return check;
	}
}
