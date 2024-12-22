package com.kosta.pp1.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.kosta.pp1.ast.AddTerm;
import com.kosta.pp1.ast.AddTermConcrete;
import com.kosta.pp1.ast.AddTermRecursive;
import com.kosta.pp1.ast.BOOL;
import com.kosta.pp1.ast.CHAR;
import com.kosta.pp1.ast.ExprAddTerm;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Factor;
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
	public static boolean typeCheck(Literal literal,Struct type){
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
	
	static public void report_error(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}
	
	static public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
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

	static public void evalExpr(Expression expr,Struct type){
		List<Literal> terms = new ArrayList<>();
		if (expr instanceof ExprAddTerm){
			ExprAddTerm exprAddTerm = (ExprAddTerm)expr;
			AddTerm addTerm = exprAddTerm.getAddTerm();
			while(addTerm instanceof AddTermRecursive){
				AddTermRecursive addTermRecursive = (AddTermRecursive)addTerm;
				Term term = addTermRecursive.getTerm();
				while(term instanceof TermRecursive){
					TermRecursive termRecursive = (TermRecursive)term;
					Factor factor = termRecursive.getFactor();
					if(factor instanceof FactorLiteral){
						FactorLiteral factorLiteral = (FactorLiteral)factor;
						terms.add(factorLiteral.getLiteral());
					}
					term = termRecursive.getTerm();
				}
				TermConcrete termConcrete = (TermConcrete) term;
				Factor factor = termConcrete.getFactor();
				if(factor instanceof FactorLiteral){
					FactorLiteral factorLiteral = (FactorLiteral)factor;
					terms.add(factorLiteral.getLiteral());
				}
				addTerm = addTermRecursive.getAddTerm();
			}
			AddTermConcrete addTermConcrete = (AddTermConcrete)addTerm;
			Term term = addTermConcrete.getTerm();
			while(term instanceof TermRecursive){
				TermRecursive termRecursive = (TermRecursive)term;
				Factor factor = termRecursive.getFactor();
				if(factor instanceof FactorLiteral){
					FactorLiteral factorLiteral = (FactorLiteral)factor;
					terms.add(factorLiteral.getLiteral());
				}
				term = termRecursive.getTerm();
			}
			TermConcrete termConcrete = (TermConcrete) term;
			Factor factor = termConcrete.getFactor();
			if(factor instanceof FactorLiteral){
				FactorLiteral factorLiteral = (FactorLiteral)factor;
				terms.add(factorLiteral.getLiteral());
			}
			boolean error = false;
			for(Literal t : terms){
				if (!typeCheck(t, type)){
					error = true;
				}
			}
			if(error) report_error("expression is of incorrect type", null);
		}
	}
}

