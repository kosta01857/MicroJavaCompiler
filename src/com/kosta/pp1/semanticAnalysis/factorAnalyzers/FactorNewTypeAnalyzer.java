package com.kosta.pp1.semanticAnalysis.factorAnalyzers;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.FactorNewType;
import com.kosta.pp1.ast.NewType;
import com.kosta.pp1.ast.NewClass;
import com.kosta.pp1.ast.NewArray;
import com.kosta.pp1.ast.Type;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.utils.Utils;
import com.kosta.pp1.semanticAnalysis.TypeChecker;

class FactorNewTypeAnalyzer implements FactorAnalyzer{
	@Override
	public boolean analyze(Factor factor,Struct type,int cnt){
		FactorNewType factN = (FactorNewType)factor;
		// Cannot have more than 1 factor of this type in an Expr
		if (cnt > 1) return true;
		NewType newType = factN.getNewType();
		if (newType instanceof NewArray){
			return analyzeNewArray((NewArray)newType,type);
		}
		else{
			return analyzeNewClass((NewClass)newType,type);
		}
	}


	private boolean analyzeNewArray(NewArray newArr,Struct leftSideType){
		boolean typeCheck;
		Type T = newArr.getType();
		Struct type = Utils.inferType(T);
		Expression expr = newArr.getExpression();
		Obj intObj = Tab.find("int");
		typeCheck = TypeChecker.ExprTypeCheck(expr,intObj.getType());
		typeCheck &= leftSideType.isRefType();
		typeCheck &= type.compatibleWith(leftSideType.getElemType());
		return !typeCheck;
	}

	private boolean analyzeNewClass(NewClass newClass,Struct leftSideType){
		boolean typeCheck;
		Type T = newClass.getType();
		Struct type = Utils.inferType(T);
		typeCheck = type.compatibleWith(leftSideType);
		typeCheck &= type.getKind() == Struct.Class;
		return !typeCheck;
	}

	private Struct getNewArrayType(NewArray newArr){
		return Utils.inferType(newArr.getType());
	}

	private Struct getNewClassType(NewClass newClass){
		return Utils.inferType(newClass.getType());
	}

	public Struct getType(Factor factor){
		FactorNewType factN = (FactorNewType)factor;
		NewType newType = factN.getNewType();
		if (newType instanceof NewArray){
			return getNewArrayType((NewArray)newType);
		}
		else{
			return getNewClassType((NewClass)newType);
		}
	}
}
