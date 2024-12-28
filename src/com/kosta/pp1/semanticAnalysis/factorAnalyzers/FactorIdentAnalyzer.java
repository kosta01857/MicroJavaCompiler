package com.kosta.pp1.semanticAnalysis.factorAnalyzers;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.util.Collection;
import java.util.Iterator;

import com.kosta.pp1.ast.ArrayAccess;
import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.DesignatorTail;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.FactorIdent;
import com.kosta.pp1.ast.MemberAccess;
import com.kosta.pp1.semanticAnalysis.TypeChecker;
import com.kosta.pp1.semanticAnalysis.Utils;
import com.kosta.pp1.utils.myDumpSymbolTableVisitor;

class FactorIdentAnalyzer implements FactorAnalyzer{
	@Override
	public boolean analyze(Factor factor,Struct type,int cnt){
		FactorIdent factorI = (FactorIdent) factor;
		Designator designator = factorI.getDesignator();
        String name = designator.getName();
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
		DesignatorTail tail = designator.getDesignatorTail();
		if(analyzeTail(obj,tail,type)) {
			return true;
		}
        return false;
	}


	public boolean analyzeTail(Obj designator, DesignatorTail tail,Struct leftHandType){
		Struct designatorType = designator.getType();
		Collection<Obj> localSymbols = designatorType.getMembers();
		Utils.report_info("number of members is " + localSymbols.size() ,tail);
		while(tail instanceof MemberAccess|| tail instanceof ArrayAccess){
			if(tail instanceof MemberAccess){
				MemberAccess memberAccess = (MemberAccess)tail;
				String ident = memberAccess.getIdent();
				boolean found = false;
				Obj o = null;
				Iterator<Obj> iter = localSymbols.iterator();
				while(iter.hasNext()){
					o = iter.next();
					Utils.report_info("symbol " + o.getName(),tail);
					if (o.getName().equals(ident)){
						found = true;
						break;
					}
				}
				Obj fieldObj = o;
				if (!found || fieldObj.getKind() != Obj.Fld && fieldObj.getKind() != Obj.Meth){
					Utils.report_error("no such class member exists", tail);
					return true;
				}
				if (!fieldObj.getType().compatibleWith(leftHandType)){
					return true;
				}
				tail = memberAccess.getDesignatorTail();
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
