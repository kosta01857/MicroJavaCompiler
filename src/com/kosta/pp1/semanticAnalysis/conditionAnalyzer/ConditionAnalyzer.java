package com.kosta.pp1.semanticAnalysis.conditionAnalyzer;

import java.util.ArrayList;
import java.util.List;

import com.kosta.pp1.ast.ConditionFact;
import com.kosta.pp1.ast.ConditionFactExpression;
import com.kosta.pp1.ast.ConditionFactExpressions;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Relop;
import com.kosta.pp1.semanticAnalysis.TypeChecker;

public class ConditionAnalyzer{
	static private List<CondExprInfo> getExpressionInfoFromCondFactor(ConditionFact factor){
		List<CondExprInfo> exprs = new ArrayList<>();
		if(factor instanceof ConditionFactExpressions){
			ConditionFactExpressions fact = (ConditionFactExpressions)factor;
			Expression s1 = fact.getExpression();
			Expression s2 = fact.getExpression1();
			Relop op = fact.getRelop();
			CondExprInfo info = new CondExprInfo();
			info.op = op;
			info.s1 = TypeChecker.getExpressionType(s1);
			info.s2 = TypeChecker.getExpressionType(s2);
			exprs.add(info);
			return exprs;
		}
		return exprs;
	}

	static private List<CondExprInfo> getExpressionsInfoFromCondFactors(List<ConditionFact> factors){
		List<CondExprInfo> exprs = new ArrayList<>();
		factors.forEach(factor -> exprs.addAll(getExpressionInfoFromCondFactor(factor)));
		return exprs;
	}

	static public boolean analyzeCondFactors(List<ConditionFact> factors){
		List<CondExprInfo> expressionsLists = getExpressionsInfoFromCondFactors(factors);
		for(CondExprInfo info : expressionsLists) {
			if (! info.analyze()) return false;
		}
		return true;
	}
}
