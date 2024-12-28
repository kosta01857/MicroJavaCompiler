package com.kosta.pp1.semanticAnalysis.factorAnalyzers;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;


import com.kosta.pp1.ast.ArrayAccess;
import com.kosta.pp1.ast.DesignatorTail;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.FactorIdent;
import com.kosta.pp1.ast.MethodCall;
import com.kosta.pp1.semanticAnalysis.TypeChecker;
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
		Struct objType = obj.getType();
		if(objType.getKind() == Struct.Array) objType = objType.getElemType();
        if (!objType.assignableTo(type)) {
            Utils.report_error("cannot assign type of " + Utils.typeString(objType), factorI);
            return true;
        }
		DesignatorTail tail = factorI.getDesignator().getDesignatorTail();
		if(analyzeTail(tail)) {
			return true;
		}
        return false;
	}


	public boolean analyzeTail(DesignatorTail tail){
		while(tail instanceof MethodCall || tail instanceof ArrayAccess){
			if(tail instanceof MethodCall){
				MethodCall methodCall = (MethodCall)tail;
				String ident = methodCall.getIdent();
				Obj fieldObj = Tab.find(ident);
				if (fieldObj.getKind() != Obj.Fld){
					Utils.report_error("no such field exists", tail);
					return true;
				}
				tail = methodCall.getDesignatorTail();
			}
			else {
				ArrayAccess arrayAccess = (ArrayAccess)tail;
				Expression expr = arrayAccess.getExpression();
				if(!TypeChecker.ExprTypeCheck(expr,Tab.find("int").getType())){
					Utils.report_error("expression inside [] must be of type int",tail);
					return true;
				}
				tail = arrayAccess.getDesignatorTail();
			}
		}
		return false;
	}
}
