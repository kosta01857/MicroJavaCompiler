package com.kosta.pp1.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.kosta.pp1.ast.AddTerm;
import com.kosta.pp1.ast.AddTermConcrete;
import com.kosta.pp1.ast.AddTermRecursive;
import com.kosta.pp1.ast.BOOL;
import com.kosta.pp1.ast.CHAR;
import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.ExprAddTerm;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.FactorIdent;
import com.kosta.pp1.ast.FactorLiteral;
import com.kosta.pp1.ast.Literal;
import com.kosta.pp1.ast.NUMBER;
import com.kosta.pp1.ast.SyntaxNode;
import com.kosta.pp1.ast.Term;
import com.kosta.pp1.ast.TermConcrete;
import com.kosta.pp1.ast.TermRecursive;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzerUtils {
	static Logger log = Logger.getLogger(SemanticAnalyzerUtils.class);
	public static boolean literalTypeCheck(Literal literal,Struct type){
		if(literal instanceof CHAR){
			if (type.getKind() != Struct.Char){
				report_error("BAD TYPE", null);
				return false;
			}
		}
		if(literal instanceof NUMBER){
			if (type.getKind() != Struct.Int){
				report_error("BAD TYPE", null);
				return false;
			}
		}
		if(literal instanceof BOOL){
			if (type.getKind() != Struct.Bool){
				report_error("BAD TYPE", null);
				return false;
			}
		}
		return true;
	}
	
	public static int inferValueFromLiteral(Literal literal){
		if(literal instanceof CHAR){
			CHAR c = (CHAR)literal;
			return (int) c.getVal();
			
		}
		if(literal instanceof NUMBER){
			NUMBER num = (NUMBER)literal;
			return num.getVal();
		}
		if(literal instanceof BOOL){
			BOOL b = (BOOL)literal;
			Boolean bool = b.getVal();
			if(bool) return 1;
			else return 0;
		}
		return -1;
	}
	
	static public void report_error(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" at line").append(line);
		log.error(msg.toString());
	}
	
	static public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" at line").append(line);
		log.info(msg.toString());
	}

	static public String typeString(Struct type){
		int typeInt = type.getKind();
		//report_info("type int is "+typeInt, null);
		switch (typeInt) {
			case 1:
				return "Integer";
			case 2:
				return "Character";
			case 5: return "Boolean";
			case 3: {
						StringBuilder sb = new StringBuilder();
						sb.append("Array of ");
						sb.append(typeString(type.getElemType()));
						sb.append("s");
						return sb.toString();

			}
			default:
				return "";
		}
	}

	static public void reportUse(Obj object,SyntaxNode node){
		String name = object.getName();
		switch(object.getKind()){
			case 0: {report_info("use of constant variable "+name + " of type " + typeString(object.getType()),node); break;}
			case 1: {report_info("use of variable "+name + " of type " + typeString(object.getType()),node); break;}
			case 3: {report_info("use of method "+name,node); break;}

		}
	}
	static public boolean ExprTypeCheck(Expression expr,Struct type){
		boolean error = false;
		List<Factor> factors = new ArrayList<>();
		if (expr instanceof ExprAddTerm){
			ExprAddTerm exprAddTerm = (ExprAddTerm)expr;
			AddTerm addTerm = exprAddTerm.getAddTerm();
			while(addTerm instanceof AddTermRecursive){
				AddTermRecursive addTermRecursive = (AddTermRecursive)addTerm;
				Term term = addTermRecursive.getTerm();
				while(term instanceof TermRecursive){
					TermRecursive termRecursive = (TermRecursive)term;
					Factor factor = termRecursive.getFactor();
				if(factor instanceof FactorLiteral || factor instanceof FactorIdent){
						factors.add(factor);
				}
					term = termRecursive.getTerm();
				}
				TermConcrete termConcrete = (TermConcrete) term;
				Factor factor = termConcrete.getFactor();
				if(factor instanceof FactorLiteral || factor instanceof FactorIdent){
					factors.add(factor);
				}
				addTerm = addTermRecursive.getAddTerm();
			}
			AddTermConcrete addTermConcrete = (AddTermConcrete)addTerm;
			Term term = addTermConcrete.getTerm();
			while(term instanceof TermRecursive){
				TermRecursive termRecursive = (TermRecursive)term;
				Factor factor = termRecursive.getFactor();
				if(factor instanceof FactorLiteral || factor instanceof FactorIdent){
					factors.add(factor);
				}
				term = termRecursive.getTerm();
			}
			TermConcrete termConcrete = (TermConcrete) term;
			Factor factor = termConcrete.getFactor();
				if(factor instanceof FactorLiteral || factor instanceof FactorIdent){
					factors.add(factor);
			}
			for(Factor t : factors){
				if(t instanceof FactorIdent){
					FactorIdent factorI = (FactorIdent)t; 
					String name = factorI.getDesignator().getName();
					Obj obj = Tab.find(name);
					reportUse(obj,factorI);
					if(obj == Tab.noObj){
						report_error("use of undeclared identifier "+name, factorI);
						return true;
					}
					if(!obj.getType().assignableTo(type)){
						report_error("cannot assign type of "+typeString(obj.getType()), factorI);
						return true;
					}
				}
				else if(t instanceof FactorLiteral){
					FactorLiteral factL = (FactorLiteral)t;
					Literal literal = factL.getLiteral();
					if(literal instanceof BOOL){
						error = true;
						break;
					}
					if (!literalTypeCheck(literal, type)){
						error = true;
						break;
					}
				}
			}
			if(error) report_error("expression is of incorrect type", null);
		}
		return error;
	}
}

