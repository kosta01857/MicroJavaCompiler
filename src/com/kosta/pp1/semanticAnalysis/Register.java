package com.kosta.pp1.semanticAnalysis;

import rs.etf.pp1.symboltable.concepts.Struct;
import com.kosta.pp1.ast.IdDeclaration;
import com.kosta.pp1.ast.IdDefinition;
import com.kosta.pp1.ast.LocalVarDeclarations;

import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kosta.pp1.ast.ArrayDecl;
import com.kosta.pp1.ast.FuncPars;
import com.kosta.pp1.ast.IdDecl;
import rs.etf.pp1.symboltable.Tab;
import com.kosta.pp1.ast.MethodSignature;
import com.kosta.pp1.ast.FunctionArgumentList;
import com.kosta.pp1.ast.FunctionParameterDeclRecursive;
import com.kosta.pp1.ast.FunctionParameterDeclConcrete;
import com.kosta.pp1.ast.FunctionParameters;
import com.kosta.pp1.ast.MethodSignatureTyped;
import com.kosta.pp1.ast.MethodSignatureVoid;
import com.kosta.pp1.ast.Type;
import com.kosta.pp1.ast.VarDeclarationList;

public class Register {
	static Map<Obj, List<Struct>> functionTypeMap = new HashMap<>();

	static Obj registerIdDeclaration(IdDeclaration decl) {
		String name;
		Obj ret;
		Struct myType = decl.struct;
		if (decl instanceof ArrayDecl) {
			ArrayDecl arrDecl = (ArrayDecl) decl;
			name = arrDecl.getArrayDeclaration().getName();
			if (Utils.objExistsInScope(name)) {
				Utils.report_error("Variable with name " + name + " was already declared!", decl);
				return null;
			}
			Struct arrStruct = new Struct(Struct.Array);
			arrStruct.setElementType(myType);
			myType = arrStruct;
			ret = Tab.insert(Obj.Var, name, arrStruct);
		} else {
			IdDecl idDecl = (IdDecl) decl;
			name = idDecl.getIdentDecl().getName();
			if (Utils.objExistsInScope(name)) {
				Utils.report_error("Variable with name " + name + " was already declared!", decl);
				return null;
			}
			ret = Tab.insert(Obj.Var, name, myType);
		}
		Utils.reportDeclaration(ret, decl);
		return ret;
	}

	static Obj registerMethod(MethodSignature s) {
		String name;
		Struct sType;
		FunctionArgumentList argList;
		if (s instanceof MethodSignatureTyped) {
			MethodSignatureTyped signature = (MethodSignatureTyped) s;
			name = signature.getMethodName();
			Type type = signature.getType();
			sType = Utils.inferType(type);
			argList = signature.getFunctionArgumentList();

		} else {
			MethodSignatureVoid signature = (MethodSignatureVoid) s;
			name = signature.getMethodName();
			sType = Tab.noType;
			argList = signature.getFunctionArgumentList();
		}
		Obj funcObj = Tab.insert(Obj.Meth, name, sType);
		Tab.openScope();
		registerFunctionParameters(argList, funcObj);
		Utils.reportDeclaration(funcObj, s);
		return funcObj;
	}

	static void registerIdDefinition(IdDefinition def) {
		String name;
		name = def.getName();
		Struct myType = SemanticAnalyzer.currentType;
		if (Utils.objExists(name)) {
			Utils.report_error("Variable with name " + name + " was already defined!", def);
			return;
		}
		if (!Utils.literalTypeCheck(def.getLiteral(), myType)) {
			Utils.report_error("bad type assignment for constant " + name, def);
			return;
		}
		Obj node = Tab.insert(Obj.Con, name, myType);
		node.setAdr(Utils.getValueFromLiteral(def.getLiteral()));
		Utils.reportDeclaration(node, def);
	}

	static void registerFunctionParameters(FunctionArgumentList funcArgs, Obj funcNode) {
		List<Struct> typeList = new ArrayList<>();
		functionTypeMap.put(funcNode, typeList);
		if (funcArgs instanceof FuncPars) {
			int paramCount = 1;
			FuncPars funcPars = (FuncPars) funcArgs;
			FunctionParameters parameters = funcPars.getFunctionParameters();
			List<IdDeclaration> idDecls = Finder.findFunctionParameters(parameters);
			idDecls.forEach(idDecl -> {
				Register.registerIdDeclaration(idDecl);
				typeList.add(idDecl.struct);
			});
			funcNode.setLevel(idDecls.size());
		}
	}

	static void registerLocalVariables(LocalVarDeclarations localVarDeclaration) {
		List<VarDeclarationList> declLists = Finder.findVarDeclarationLists(localVarDeclaration);
		Utils.report_info("Local variables:", null);
		declLists.forEach(Analyzer::declarationListPass);
	}
}
