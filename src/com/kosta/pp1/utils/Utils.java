package com.kosta.pp1.utils;

import org.apache.log4j.Logger;

import com.kosta.pp1.ast.BOOL;
import com.kosta.pp1.ast.CHAR;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.Literal;
import com.kosta.pp1.ast.NUMBER;
import com.kosta.pp1.ast.SyntaxNode;
import com.kosta.pp1.ast.Type;
import com.kosta.pp1.semanticAnalysis.factorAnalyzers.FactorAnalyzer;
import com.kosta.pp1.semanticAnalysis.factorAnalyzers.FactorAnalyzerRegistry;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import com.kosta.pp1.semanticAnalysis.SemanticAnalyzer;
import com.kosta.pp1.Register;
import com.kosta.pp1.types.SetType;
import com.kosta.pp1.semanticAnalysis.TypeChecker;

public class Utils{
	
	public static enum ObjectName{
		Const("Const"),
		Var("Var"),
		Type("Type"),
		Method("Method"),
		Field("Field"),
		Element("Element"),
		Prog("Program");
		public String name;
		ObjectName(String s){
			this.name = s;
		}
	}


	static Logger log = Logger.getLogger(SemanticAnalyzer.class);
	static public boolean objExistsInScope(String name){
		Obj obj = Tab.currentScope().findSymbol(name);
		if(obj != null){
			return true;
		}
		return false;
	}
	static public boolean objExists(String name){
		Obj obj = Tab.find(name);
		if(obj != Tab.noObj){
			return true;
		}
		return false;
	}
	/** Converts Type object to Struct object
	* @param type
	* @return Struct object associated with Type object, if Type is of unknown type, it will return noType
	 */
	static public Struct inferType(SyntaxNode sNode){
		Struct S = Register.getInstance().getTypeMap().get(sNode);

		if(S != null) return S;
		if(sNode instanceof Type){
			String typeName = ((Type)sNode).getTypeName();
			Obj typeNode = Tab.find(typeName);
			if(typeNode.getKind() == Obj.Type){
				Register.getInstance().getTypeMap().put(sNode,typeNode.getType());
				return typeNode.getType();
			}
			return Tab.noType;
		}
		else if (sNode instanceof Expression){
			S = TypeChecker.getExpressionType((Expression)sNode);
			Register.getInstance().getTypeMap().put(sNode,S);
			return S;
		}
		else if (sNode instanceof Factor){
			FactorAnalyzer fAnalyzer = FactorAnalyzerRegistry.getAnalyzerMap().get(sNode.getClass());
			S = fAnalyzer.getType((Factor)sNode);
			Register.getInstance().getTypeMap().put(sNode,S);
			return S;
		}
		return Tab.noType;
	}
	/**
	 * Checks if a literal is of specified type
	 * @param literal Literal whose type we are checking
	 * @param type Type to compare the Literal's type
	 * @return Returns true if the types match, false otherwise
	 * 
	 */
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
	/** Returns int value of Literal. For bool -> 1 for true, 0 for false. For char it will return ASCI code
	* @param literal Literal whose value we want
	* @return 	 
	* */
	public static int getValueFromLiteral(Literal literal){
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
			msg.append (" at line ").append(line);
		log.error(msg.toString());
		SemanticAnalyzer.getInstance().setError();
	}
	
	static public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" at line ").append(line);
		log.info(msg.toString());
	}
	/** Converts Type to String
	* @param type 
	* @return String name of the type
	*/
	static public String typeString(Struct type){
		if (type instanceof SetType){
			return "Set";
		}
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
			case 4:
				return "Class";
			default: return "None";
		}
	}
	static public void reportUse(Obj object,SyntaxNode node){
		String name = object.getName();
		boolean global = object.getLevel() > 0 ? false:true;
		switch(object.getKind()){
			case 0: {report_info("use of constant variable "+name + " of type " + typeString(object.getType()) + " level: " + object.getLevel(),node); break;}
			case 1: {report_info("use of "+ (global ? "global":"local") + " variable "+name + " of type " + typeString(object.getType()) + " level: " + object.getLevel(),node); break;}
			case 3: {report_info("function call of name "+name + " of return type " + typeString(object.getType()) + " number of args: " + object.getLevel(),node); break;}
		}
	}
	static public void reportDeclaration(Obj object,SyntaxNode node){
		String name = object.getName();
		switch(object.getKind()){
			case 0: {report_info("Declaration of constant variable "+name + " of type " + typeString(object.getType()) + " level: " + object.getLevel(),node); break;}
			case 1: {report_info("Declaration of variable "+name + " of type " + typeString(object.getType()) + " level: " + object.getLevel(),node); break;}
			case 3: {report_info("Declaration of method "+name + " of return type " + typeString(object.getType()) + " number of args: " + object.getLevel(),node); break;}
		}
	}

}
