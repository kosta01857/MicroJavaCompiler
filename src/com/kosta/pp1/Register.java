package com.kosta.pp1;

import rs.etf.pp1.symboltable.concepts.Struct;
import com.kosta.pp1.ast.IdDeclaration;
import com.kosta.pp1.ast.IdDefinition;
import com.kosta.pp1.ast.LocalVarDeclarations;

import rs.etf.pp1.symboltable.concepts.Obj;
import com.kosta.pp1.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kosta.pp1.ast.ArrayDecl;
import com.kosta.pp1.ast.ClassBody;
import com.kosta.pp1.ast.ClassDeclaration;
import com.kosta.pp1.ast.ClassDeclarationExtend;
import com.kosta.pp1.ast.ClassDeclarationNoExtend;
import com.kosta.pp1.ast.FuncPars;
import com.kosta.pp1.ast.IdDecl;
import rs.etf.pp1.symboltable.Tab;
import com.kosta.pp1.ast.MethodSignature;
import com.kosta.pp1.ast.FunctionArgumentList;
import com.kosta.pp1.ast.FunctionParameters;
import com.kosta.pp1.ast.MethodSignatureTyped;
import com.kosta.pp1.ast.MethodSignatureVoid;
import com.kosta.pp1.ast.SyntaxNode;
import com.kosta.pp1.ast.Type;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.types.SetType;
import com.kosta.pp1.semanticAnalysis.Finder;
import com.kosta.pp1.semanticAnalysis.SemanticAnalyzer;

/**
 * holds key -> value mappings , that describe the AST in more detail
 * contains method to register methods,variables and classes
 * Register
 */
public class Register {
	private Map<Obj, List<Struct>> functionTypeMap = new HashMap<>();
	private Map<SyntaxNode, Struct> typeMap = new HashMap<>();
	private boolean inClass = false;
	public void setInClass(boolean b){
		inClass = b;
	}
	static private Register instance = null;
	private Register(){

	}
	static public Register getInstance(){
		if(instance == null){
			instance = new Register();
		}
		return instance;
	}

	/**
	 * Fetches the mapping between a SyntaxNode and its type
	 * Type represents an Struct Node from the symbol table
	 */
	public Map<SyntaxNode, Struct> getTypeMap(){
		return this.typeMap;
	}

	/**
	 * Fetches the mapping between an Object Node from the symbol table representing a method
	 * and the List containing the types of the method arguments
	 * Types are represented as Struct Nodes from the symbol table
	 */
	public Map<Obj, List<Struct>> getFunctionTypeMap(){
		return this.functionTypeMap;
	}

	private List<Struct> inBuiltFuncGetArgTypes(String func) {
		List<Struct> type = new ArrayList<>();
		switch (func) {
			case "chr": {
				type.add(Tab.find("int").getType());
				break;
			}
			case "ord": {
				type.add(Tab.find("char").getType());
				break;
			}
			case "len": {
				type.add(Tab.find("arr").getType());
				break;
			}
			case "add": {
				type.add(SetType.getInstance());
				type.add(Tab.find("int").getType());
				break;
			}
			case "addAll": {
				type.add(SetType.getInstance());
				type.add(new Struct(Struct.Array, Tab.intType));
				break;
			}
		}
		return type;
	}

	/**
	 * initializes the registry, filling the maps with predefined key-value pairs.
	 * specifically, it fills the functionTypeMap with types for in-built methods
	 */
	public void init() {
		String[] inBuiltFuncs = { "chr", "ord", "len", "add", "addAll" };
		for (String func : inBuiltFuncs) {
			Obj o = Tab.find(func);
			functionTypeMap.put(o, inBuiltFuncGetArgTypes(func));
		}
	}

	public Obj registerVariableDeclaration(IdDeclaration decl) {
		String name;
		Obj ret;
		Struct myType = typeMap.get(decl);
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
			ret = Tab.insert(inClass ? Obj.Fld : Obj.Var, name, arrStruct);
		} else {
			IdDecl idDecl = (IdDecl) decl;
			name = idDecl.getIdentDecl().getName();
			if (Utils.objExistsInScope(name)) {
				Utils.report_error("Variable with name " + name + " was already declared!", decl);
				return null;
			}
			ret = Tab.insert(inClass ? Obj.Fld : Obj.Var, name, myType);
		}
		Utils.reportDeclaration(ret, decl);
		return ret;
	}

	public Obj registerMethod(MethodSignature s) {
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

	public Obj registerVariableDefinition(IdDefinition def) {
		String name;
		name = def.getName();
		Struct myType = SemanticAnalyzer.getInstance().getCurrentType();
		if (Utils.objExists(name)) {
			Utils.report_error("Variable with name " + name + " was already defined!", def);
			return null;
		}
		if (!Utils.literalTypeCheck(def.getLiteral(), myType)) {
			Utils.report_error("bad type assignment for constant " + name, def);
			return null;
		}
		Obj node = Tab.insert(Obj.Con, name, myType);
		node.setAdr(Utils.getValueFromLiteral(def.getLiteral()));
		Utils.reportDeclaration(node, def);
		return node;
	}

	public void registerFunctionParameters(FunctionArgumentList funcArgs, Obj funcNode) {
		List<Struct> typeList = new ArrayList<>();
		List<IdDeclaration> idDecls = new ArrayList<>();
		functionTypeMap.put(funcNode, typeList);
		if (funcArgs instanceof FuncPars) {
			FuncPars funcPars = (FuncPars) funcArgs;
			FunctionParameters parameters = funcPars.getFunctionParameters();
			idDecls = Finder.findFunctionParameters(parameters);
			idDecls.forEach(idDecl -> {
				this.registerVariableDeclaration(idDecl);
				typeList.add(typeMap.get(idDecl));
			});
		}
		funcNode.setLevel(idDecls.size());
	}
	public Obj registerClass(ClassDeclaration classDecl) {
		Obj classObj;
		Struct classStruct;
		if (classDecl instanceof ClassDeclarationNoExtend) {
			ClassDeclarationNoExtend classD = (ClassDeclarationNoExtend) classDecl;
			String className = classD.getName();
			if (Utils.objExists(className)) {
				Utils.report_error("identifier " + className + " already exists!", classDecl);
				return null;
			}
			classStruct = new Struct(Struct.Class);
			classObj = Tab.insert(Obj.Type, className, classStruct);
		} else {
			ClassDeclarationExtend classD = (ClassDeclarationExtend) classDecl;
			String className = classD.getName();
			if (Utils.objExists(className)) {
				Utils.report_error("identifier " + className + " already exists!", classDecl);
				return null;
			}
			classStruct = new Struct(Struct.Class);
			Type extendType = classD.getType();
			Struct extendStruct = Utils.inferType(extendType);
			if (extendStruct == Tab.noType) {
				Utils.report_info("parent class of class " + className + " doesnt exist!", classDecl);
				return null;
			}
			classStruct.setElementType(extendStruct);
			classObj = Tab.insert(Obj.Type, className, classStruct);
		}
		return classObj;
	}
}
