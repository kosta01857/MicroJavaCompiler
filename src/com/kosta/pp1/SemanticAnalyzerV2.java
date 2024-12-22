package com.kosta.pp1;

import java.util.ArrayList;
import java.util.List;

import com.kosta.pp1.ast.ArrayDecl;
import com.kosta.pp1.ast.GlobalVarDeclaration;
import com.kosta.pp1.ast.IdDecl;
import com.kosta.pp1.ast.IdDeclaration;
import com.kosta.pp1.ast.LocalVarDeclarations;
import com.kosta.pp1.ast.LocalVarDeclarationsConcrete;
import com.kosta.pp1.ast.LocalVarDeclarationsRecursive;
import com.kosta.pp1.ast.ProgName;
import com.kosta.pp1.ast.Program;
import com.kosta.pp1.ast.Type;
import com.kosta.pp1.ast.VarDecl;
import com.kosta.pp1.ast.VarDeclRecursive;
import com.kosta.pp1.ast.VarDeclaration;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.ast.VisitorAdaptor;
import com.kosta.pp1.utils.SemanticAnalyzerUtils;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzerV2 extends VisitorAdaptor {
	
	static Struct currentType = Tab.noType;
	static class Utils{
		static public boolean objExists(String name){
			Obj obj = Tab.find(name);
			if(obj != Tab.noObj){
				return true;
			}
			return false;
		}
		static List<IdDeclaration> findIdDeclarations(VarDeclaration varDeclarations){
			List<IdDeclaration> list = new ArrayList<>();
			while(varDeclarations instanceof VarDeclRecursive){
				VarDeclRecursive varDeclR = (VarDeclRecursive)varDeclarations;
				list.add(varDeclR.getIdDeclaration());
				varDeclarations = varDeclR.getVarDeclaration();
			}
			VarDecl decl = (VarDecl)varDeclarations;
			list.add(decl.getIdDeclaration());
			return list;
		}
		static void registerIdDeclaration(IdDeclaration decl){
			String name;
			Struct myType = currentType;
			if(decl instanceof ArrayDecl){
				ArrayDecl arrDecl = (ArrayDecl)decl;
				name = arrDecl.getArrayDeclaration().getName();
				if(objExists(name)){
					SemanticAnalyzerUtils.report_error("Variable with name " + name + " was already declared!", null);
					return;
				}
				Struct arrStruct = new Struct(Struct.Array);
				arrStruct.setElementType(currentType);
				myType = arrStruct;
				Tab.insert(Obj.Var,name,arrStruct);
			}
			else{
				IdDecl idDecl = (IdDecl)decl;
				name = idDecl.getIdentDecl().getName();
				if(objExists(name)){
					SemanticAnalyzerUtils.report_error("Variable with name " + name + " was already declared!", null);
					return;
				}
				Tab.insert(Obj.Var,name,currentType);
			}
			SemanticAnalyzerUtils.report_info("Array Var Declaration " + name + " of type " + SemanticAnalyzerUtils.typeString(myType),decl); 
		}
		static List<VarDeclarationList> findVarDeclarationLists(LocalVarDeclarations declarations){
			List<VarDeclarationList> list = new ArrayList<>();
			while (declarations instanceof LocalVarDeclarationsRecursive) {
				LocalVarDeclarationsRecursive declsR = (LocalVarDeclarationsRecursive)declarations;
				list.add(declsR.getVarDeclarationList());
				declarations = declsR.getLocalVarDeclarations();
			}
			LocalVarDeclarationsConcrete decls = (LocalVarDeclarationsConcrete)declarations;
			list.add(decls.getVarDeclarationList());
			return list;
		}
		static void declarationListPass(VarDeclarationList list){
			Type type = list.getType();
			Struct struct = inferType(type);
			currentType = struct;
			List<IdDeclaration> idDecls = Utils.findIdDeclarations(list.getVarDeclaration());
			idDecls.forEach(Utils::registerIdDeclaration);
		}
		static Struct inferType(Type type){
			String typeName = type.getTypeName();
			Obj typeNode = Tab.find(typeName);
			if(typeNode.getKind() == Obj.Type){
				return typeNode.getType();
			}
			return Tab.noType;
		}
	}

	public void visit(ProgName progName){
		Tab.insert(Obj.Prog, progName.getName(), Tab.noType);
		Tab.openScope();
	}

	public void visit(Program program){
		Obj programNode = Tab.find(program.getProgName().getName());
		Tab.chainLocalSymbols(programNode);
		Tab.closeScope();
	}

	public void visit(GlobalVarDeclaration globalVarDeclaration){
		VarDeclarationList varDeclarationList = globalVarDeclaration.getVarDeclarationList();
		SemanticAnalyzerUtils.report_info("Global variables:", null);
		Utils.declarationListPass(varDeclarationList);
	}
	public void visit(LocalVarDeclarationsRecursive localVarDeclaration){
		List<VarDeclarationList> declLists = Utils.findVarDeclarationLists(localVarDeclaration);
		SemanticAnalyzerUtils.report_info("Local variables:", null);
		declLists.forEach(Utils::declarationListPass);
	}
	public void visit(LocalVarDeclarationsConcrete localVarDeclaration){
		List<VarDeclarationList> declLists = Utils.findVarDeclarationLists(localVarDeclaration);
		SemanticAnalyzerUtils.report_info("Local variables:", null);
		declLists.forEach(Utils::declarationListPass);
	}
}

