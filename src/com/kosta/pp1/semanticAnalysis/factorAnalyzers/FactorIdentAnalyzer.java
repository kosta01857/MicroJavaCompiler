package com.kosta.pp1.semanticAnalysis.factorAnalyzers;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.FactorIdent;
import com.kosta.pp1.semanticAnalysis.Utils;

class FactorIdentAnalyzer implements FactorAnalyzer{
	@Override
	public boolean analyze(Factor factor,Struct type,int cnt){
		FactorIdent factorI = (FactorIdent) factor;
        String name = factorI.getDesignator().getName();
        Obj obj = Tab.find(name);
        Utils.reportUse(obj, factorI);
        if ((obj.getType().isRefType() || obj.getType().getKind() != Struct.Int) && cnt > 1) {
            Utils.report_error("cannot use type of " + Utils.typeString(obj.getType()), factorI);
            return true;
        }
        if (obj == Tab.noObj) {
            Utils.report_error("use of undeclared identifier " + name, factorI);
            return true;
        }
        if (!obj.getType().assignableTo(type)) {
            Utils.report_error("cannot assign type of " + Utils.typeString(obj.getType()), factorI);
            return true;
        }
        return false;
	}
}
