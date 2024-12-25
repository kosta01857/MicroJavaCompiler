package com.kosta.pp1.semanticAnalysis;

import com.kosta.pp1.ast.VarDeclRecursive;
import com.kosta.pp1.ast.VarDeclaration;
import com.kosta.pp1.ast.FunctionParameters;
import com.kosta.pp1.ast.IdDeclaration;
import java.util.List;
import java.util.ArrayList;
import com.kosta.pp1.ast.VarDecl;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.ast.LocalVarDeclarations;
import com.kosta.pp1.ast.LocalVarDeclarationsConcrete;
import com.kosta.pp1.ast.LocalVarDeclarationsRecursive;
import com.kosta.pp1.ast.IdDefinition;
import com.kosta.pp1.ast.IdDefinitionList;
import com.kosta.pp1.ast.IdDefinitionListConcrete;
import com.kosta.pp1.ast.IdDefinitionListRecursive;
import com.kosta.pp1.ast.FunctionParameterDeclRecursive;
import com.kosta.pp1.ast.FunctionParameterDeclConcrete;
public class Finder{
	static List<IdDeclaration> findIdDeclarations(VarDeclaration varDeclarations){
		List<IdDeclaration> list = new ArrayList<>();
		while(varDeclarations instanceof VarDeclRecursive){
			VarDeclRecursive varDeclR = (VarDeclRecursive)varDeclarations;
			IdDeclaration idDecl = varDeclR.getIdDeclaration();
			idDecl.struct = SemanticAnalyzer.currentType;
			list.add(idDecl);
			varDeclarations = varDeclR.getVarDeclaration();
		}
		VarDecl decl = (VarDecl)varDeclarations;
		IdDeclaration idDecl = decl.getIdDeclaration();
		idDecl.struct = SemanticAnalyzer.currentType;
		list.add(idDecl);
		return list;
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
	static List<IdDefinition> findIdDefinitions(IdDefinitionList defList){
		List<IdDefinition> list = new ArrayList<>();
		while(defList instanceof IdDefinitionListRecursive){
			IdDefinitionListRecursive defListR = (IdDefinitionListRecursive) defList;
			list.add(defListR.getIdDefinition());
			defList = defListR.getIdDefinitionList();
		}
		IdDefinitionListConcrete defListC = (IdDefinitionListConcrete)defList; 
		list.add(defListC.getIdDefinition());
		return list;
	}
	static List<IdDeclaration> findFunctionParameters(FunctionParameters parameters){
		List<IdDeclaration> list = new ArrayList<>();
		while(parameters instanceof FunctionParameterDeclRecursive){
			FunctionParameterDeclRecursive paramR = (FunctionParameterDeclRecursive) parameters;
			IdDeclaration idDecl = paramR.getIdDeclaration();
			idDecl.struct = Utils.inferType(paramR.getType());
			list.add(idDecl);
			parameters = paramR.getFunctionParameters();
		}
		FunctionParameterDeclConcrete paramC = (FunctionParameterDeclConcrete)parameters;
		IdDeclaration idDecl = paramC.getIdDeclaration();
		idDecl.struct = Utils.inferType(paramC.getType());
		list.add(idDecl);
		return list;
	}
}
