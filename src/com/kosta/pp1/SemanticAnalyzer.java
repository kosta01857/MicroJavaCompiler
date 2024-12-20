package com.kosta.pp1;

import org.apache.log4j.Logger;

import com.kosta.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

/**
 * SemanticAnalyzer
 */
public class SemanticAnalyzer extends VisitorAdaptor {

	Logger log = Logger.getLogger(getClass());
	Struct currentType = Tab.noType;
	
	public void report_error(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}
	
	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}



	public void visit(ProgName progName){
		progName.obj = Tab.insert(Obj.Prog, progName.getName(), Tab.noType);
		Tab.openScope();
	}
	public void visit(Program program){
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
	}


	public void visit(Type type){
		Obj typeNode = Tab.find(type.getTypeName());
		if(typeNode == Tab.noObj){
			type.struct = Tab.noType;
		}
		else{
			if(Obj.Type == typeNode.getKind()){
				type.struct = typeNode.getType();
			}
			else{
				report_error("DECLARED TYPE NAME IS NOT A KNOWN TYPE", null);
				type.struct = Tab.noType;
			}
		}
		currentType = type.struct;
	}

	public void visit(VarDeclID varDecl){
		Tab.insert(Obj.Var,varDecl.getIdDeclaration().getName(),currentType);
	}
	public void visit(VarDeclARR varDecl){
		Tab.insert(Obj.Var,varDecl.getIdArrayDeclaration().getName(),currentType);
	}
	public void visit(IdDefinitionListConcrete def){
		String name = def.getIdDefinition().getName();
		Literal literal = def.getIdDefinition().getLiteral();
		if(literal instanceof CHAR){
			if (currentType.getKind() != Struct.Char){
				report_error("BAD TYPE", null);
				return;
			}
		}
		if(literal instanceof NUMBER){
			if (currentType.getKind() != Struct.Int){
				report_error("BAD TYPE", null);
				return;
			}
		}
		if(literal instanceof BOOL){
			if (currentType.getKind() != Struct.Bool){
				report_error("BAD TYPE", null);
				return;
			}
		}
		Tab.insert(Obj.Con, name, currentType);
	}
	public void visit(IdDefinitionListRecursive defRecursive){
		IdDefinition def = defRecursive.getIdDefinition();
		String name = def.getName();
		Literal literal = def.getLiteral();
		if(literal instanceof CHAR){
			if (currentType.getKind() != Struct.Char){
				report_error("BAD TYPE", null);
				return;
			}
		}
		if(literal instanceof NUMBER){
			if (currentType.getKind() != Struct.Int){
				report_error("BAD TYPE", null);
				return;
			}
		}
		if(literal instanceof BOOL){
			if (currentType.getKind() != Struct.Bool){
				report_error("BAD TYPE", null);
				return;
			}
		}
		Tab.insert(Obj.Con, name, currentType);
	}
}

