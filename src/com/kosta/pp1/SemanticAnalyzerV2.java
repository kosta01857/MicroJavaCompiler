package com.kosta.pp1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kosta.pp1.ast.ArrayDecl;
import com.kosta.pp1.ast.Condition;
import com.kosta.pp1.ast.ConstDeclarationList;
import com.kosta.pp1.ast.Designator;
import com.kosta.pp1.ast.DesignatorStatement;
import com.kosta.pp1.ast.DesignatorStmt;
import com.kosta.pp1.ast.DoWhile;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.FuncPars;
import com.kosta.pp1.ast.FunctionArgumentList;
import com.kosta.pp1.ast.FunctionParameterDeclConcrete;
import com.kosta.pp1.ast.FunctionParameterDeclRecursive;
import com.kosta.pp1.ast.FunctionParameters;
import com.kosta.pp1.ast.GlobalVarDeclaration;
import com.kosta.pp1.ast.IdDecl;
import com.kosta.pp1.ast.IdDeclaration;
import com.kosta.pp1.ast.IdDefinition;
import com.kosta.pp1.ast.IdDefinitionList;
import com.kosta.pp1.ast.IdDefinitionListConcrete;
import com.kosta.pp1.ast.IdDefinitionListRecursive;
import com.kosta.pp1.ast.IfElse;
import com.kosta.pp1.ast.IfOnly;
import com.kosta.pp1.ast.IfStatement;
import com.kosta.pp1.ast.IfStmt;
import com.kosta.pp1.ast.LocalVarDeclarations;
import com.kosta.pp1.ast.LocalVarDeclarationsConcrete;
import com.kosta.pp1.ast.LocalVarDeclarationsRecursive;
import com.kosta.pp1.ast.MethodDefinition;
import com.kosta.pp1.ast.MethodDefinitionNoLocals;
import com.kosta.pp1.ast.MethodSignature;
import com.kosta.pp1.ast.MethodSignatureTyped;
import com.kosta.pp1.ast.MethodSignatureVoid;
import com.kosta.pp1.ast.PostDec;
import com.kosta.pp1.ast.PostInc;
import com.kosta.pp1.ast.ProgName;
import com.kosta.pp1.ast.Program;
import com.kosta.pp1.ast.SetDesignation;
import com.kosta.pp1.ast.Statement;
import com.kosta.pp1.ast.Statements;
import com.kosta.pp1.ast.StatementsRecursive;
import com.kosta.pp1.ast.Type;
import com.kosta.pp1.ast.VarDecl;
import com.kosta.pp1.ast.VarDeclRecursive;
import com.kosta.pp1.ast.VarDeclaration;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.ast.VarDesignation;
import com.kosta.pp1.ast.VisitorAdaptor;
import com.kosta.pp1.ast.WhileCond;
import com.kosta.pp1.ast.WhileDesignator;
import com.kosta.pp1.ast.WhileSimple;
import com.kosta.pp1.ast.WhileStmt;
import com.kosta.pp1.utils.SemanticAnalyzerUtils;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzerV2 extends VisitorAdaptor {
	
	static Map<Obj,List<Struct>> functionTypeMap = new HashMap<>();
	static Struct currentType = Tab.noType;
	static class Utils{
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
		static void varDesignationPass(VarDesignation varDesignation){
			Expression expr = varDesignation.getExpression();
			String name = varDesignation.getDesignator().getName();
			if(!Utils.objExists(name)){
				SemanticAnalyzerUtils.report_error("use of undeclared identifier " + name,varDesignation);
			}
			Obj ident = Tab.find(name);
			Struct type = ident.getType();
			boolean error = SemanticAnalyzerUtils.ExprTypeCheck(expr,type);
			SemanticAnalyzerUtils.reportUse(ident,varDesignation);
		}
		static void findIdDeclarationsRecursive(VarDeclaration varDeclarations,List<IdDeclaration> list){
			if (varDeclarations instanceof VarDecl){
				VarDecl decl = (VarDecl)varDeclarations;
				list.add(decl.getIdDeclaration());
				return;
			}
			VarDeclRecursive varDeclR = (VarDeclRecursive)varDeclarations;
			list.add(varDeclR.getIdDeclaration());
			findIdDeclarationsRecursive(varDeclR.getVarDeclaration(),list);
		}
		static Obj registerIdDeclaration(IdDeclaration decl){
			String name;
			Struct myType = currentType;
			Obj ret;
			if(decl instanceof ArrayDecl){
				ArrayDecl arrDecl = (ArrayDecl)decl;
				name = arrDecl.getArrayDeclaration().getName();
				if(objExistsInScope(name)){
					SemanticAnalyzerUtils.report_error("Variable with name " + name + " was already declared!", decl);
					return null;
				}
				Struct arrStruct = new Struct(Struct.Array);
				arrStruct.setElementType(currentType);
				myType = arrStruct;
				ret = Tab.insert(Obj.Var,name,arrStruct);
			}
			else{
				IdDecl idDecl = (IdDecl)decl;
				name = idDecl.getIdentDecl().getName();
				if(objExistsInScope(name)){
					SemanticAnalyzerUtils.report_error("Variable with name " + name + " was already declared!", decl);
					return null;
				}
				ret = Tab.insert(Obj.Var,name,currentType);
			}
			SemanticAnalyzerUtils.report_info("Var Declaration " + name + " of type " + SemanticAnalyzerUtils.typeString(myType) + " level:"+ret.getLevel(),decl); 
			return ret;
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
		static void findVarDeclarationListsRecursive(LocalVarDeclarations declarations,List<VarDeclarationList> list){
			if(declarations instanceof LocalVarDeclarationsConcrete){
				LocalVarDeclarationsConcrete decls = (LocalVarDeclarationsConcrete)declarations;
				list.add(decls.getVarDeclarationList());
				return;
			}
			LocalVarDeclarationsRecursive declsR = (LocalVarDeclarationsRecursive)declarations;
			list.add(declsR.getVarDeclarationList());
			findVarDeclarationListsRecursive(declsR.getLocalVarDeclarations(),list);
		}
		static void declarationListPass(VarDeclarationList list){
			Type type = list.getType();
			Struct struct = inferType(type);
			currentType = struct;
			List<IdDeclaration> idDecls = Utils.findIdDeclarations(list.getVarDeclaration());
			idDecls.forEach(decl -> Utils.registerIdDeclaration(decl));
		}
		static Struct inferType(Type type){
			String typeName = type.getTypeName();
			Obj typeNode = Tab.find(typeName);
			if(typeNode.getKind() == Obj.Type){
				return typeNode.getType();
			}
			return Tab.noType;
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
		static void findIdDefinitionsRecursive(IdDefinitionList defList,List<IdDefinition> list){
			if(defList instanceof IdDefinitionListConcrete){
				IdDefinitionListConcrete defListC = (IdDefinitionListConcrete)defList; 
				list.add(defListC.getIdDefinition());
				return;
			}
			IdDefinitionListRecursive defListR = (IdDefinitionListRecursive) defList;
			list.add(defListR.getIdDefinition());
			findIdDefinitionsRecursive(defListR.getIdDefinitionList(),list);
		}
		static void registerIdDefinition(IdDefinition def){
			String name;
			Struct myType = currentType;
			name = def.getName();
			if(objExists(name)){
				SemanticAnalyzerUtils.report_error("Variable with name " + name + " was already defined!", def);
				return;
			}
			if(!SemanticAnalyzerUtils.literalTypeCheck(def.getLiteral(),currentType)){
				SemanticAnalyzerUtils.report_error("bad type assignment for constant " + name,def);
				return;
			}
			Obj node = Tab.insert(Obj.Con,name,currentType);
			node.setAdr(SemanticAnalyzerUtils.inferValueFromLiteral(def.getLiteral()));
			SemanticAnalyzerUtils.report_info("Const Definition with name " + name + " of type " + SemanticAnalyzerUtils.typeString(myType) + " value is: " + node.getAdr(),def); 
		}
		static void registerFunctionParameters(FunctionArgumentList funcArgs,Obj funcNode){
			List<Struct> list = new ArrayList<>();
			functionTypeMap.put(funcNode, list);
			if(funcArgs instanceof FuncPars){
				int paramCount = 1;
				FuncPars funcPars = (FuncPars)funcArgs;
				FunctionParameters parameters = funcPars.getFunctionParameters();
				while(parameters instanceof FunctionParameterDeclRecursive){
					paramCount++;
					FunctionParameterDeclRecursive paramR = (FunctionParameterDeclRecursive) parameters;
					currentType = inferType(paramR.getType());
					list.add(currentType);
					Obj o = registerIdDeclaration(paramR.getIdDeclaration());
					parameters = paramR.getFunctionParameters();
				}
				FunctionParameterDeclConcrete paramC = (FunctionParameterDeclConcrete)parameters;
				currentType = inferType(paramC.getType());
				Obj o = registerIdDeclaration(paramC.getIdDeclaration());
				funcNode.setLevel(paramCount);
			}
		}
		static Obj registerMethod(MethodSignature s){
			String name;
			Struct sType;
			FunctionArgumentList argList;
			if(s instanceof MethodSignatureTyped){
				MethodSignatureTyped signature = (MethodSignatureTyped)s;
				name = signature.getMethodName();
				Type type = signature.getType();
				sType = inferType(type);
				argList = signature.getFunctionArgumentList();

			}
			else{
				MethodSignatureVoid signature = (MethodSignatureVoid)s;
				name = signature.getMethodName();
				sType = Tab.noType;
				argList = signature.getFunctionArgumentList();
			}
			Obj funcObj = Tab.insert(Obj.Meth,name,sType);
			Tab.openScope();
			SemanticAnalyzerUtils.report_info("Declaration of method " + funcObj.getName() +" with return type " + SemanticAnalyzerUtils.typeString(sType) +
					" with " + funcObj.getLevel() +" number of args",s);
			registerFunctionParameters(argList,funcObj);
			return funcObj;
		}
		static void registerLocalVariables(LocalVarDeclarations localVarDeclaration){
			List<VarDeclarationList> declLists = new ArrayList<>();
			Utils.findVarDeclarationListsRecursive(localVarDeclaration,declLists);
			SemanticAnalyzerUtils.report_info("Local variables:", null);
			declLists.forEach(Utils::declarationListPass);
		}
		static void postIncPass(PostInc postIncExpr){
			Designator d = postIncExpr.getDesignator();
			String name = d.getName();
			SemanticAnalyzerUtils.report_error("use of variable " + name,postIncExpr);
			Obj node = Tab.find(name);
			if(node == Tab.noObj){
				SemanticAnalyzerUtils.report_error("use of undeclared identifier " + name,postIncExpr);
			}
			if(node.getKind() != Obj.Var){
				SemanticAnalyzerUtils.report_error("cannot use identifier " + name + " in this context",postIncExpr);
			}
			Struct type = node.getType();
			if(type.getKind() != Struct.Int){
				SemanticAnalyzerUtils.report_error("cannot use this operator on the variable " + name,postIncExpr);
			}
		}
		static void setDesignationPass(SetDesignation setDesignation){
			Designator leftD = setDesignation.getDesignator();
			Designator op1 = setDesignation.getDesignator1();
			Designator op2 = setDesignation.getDesignator2();
			if(!objExists(leftD.getName())){
				SemanticAnalyzerUtils.report_error("set " + leftD.getName() + " is undeclared", leftD);
				return;
			}
			if(!objExists(op1.getName())){
				SemanticAnalyzerUtils.report_error("set " + op1.getName() + " is undeclared", leftD);
				return;
			}
			if(!objExists(op2.getName())){
				SemanticAnalyzerUtils.report_error("set " + op2.getName() + " is undeclared", leftD);
				return;
			}
			Obj d1 = Tab.find(leftD.getName());
			Obj d2 = Tab.find(op1.getName());
			Obj d3 = Tab.find(op2.getName());
			SemanticAnalyzerUtils.report_info("use of variable "+d1.getName(), leftD);
			SemanticAnalyzerUtils.report_info("use of variable "+d2.getName(), op1);
			SemanticAnalyzerUtils.report_info("use of variable "+d3.getName(), op2);
			if(!(d1.getType() instanceof SetType)){
				SemanticAnalyzerUtils.report_error("incorrect type for " + d1.getName() + " is undeclared", leftD);
			}
			if(!(d2.getType() instanceof SetType)){
				SemanticAnalyzerUtils.report_error("incorrect type for " + d2.getName() + " is undeclared", leftD);
			}
			if(!(d3.getType() instanceof SetType)){
				SemanticAnalyzerUtils.report_error("incorrect type for " + d3.getName() + " is undeclared", leftD);
			}
		}
		static void designatorStatementPass(DesignatorStatement dStatement){
				if(dStatement instanceof PostDec){
					postDecPass((PostDec)dStatement);
				}
				else if(dStatement instanceof PostInc){
					postIncPass((PostInc)dStatement);
				}
				else if(dStatement instanceof VarDesignation){
					varDesignationPass((VarDesignation)dStatement);
				}
				else if(dStatement instanceof SetDesignation){
					setDesignationPass((SetDesignation)dStatement);
				}
		}
		static void statementPass(Statement statement){
			if(statement instanceof DesignatorStmt){
				DesignatorStmt stmt = (DesignatorStmt)statement;
				DesignatorStatement dStatement = stmt.getDesignatorStatement();
				designatorStatementPass(dStatement);
			}
			else if(statement instanceof WhileStmt){
				whilePass((WhileStmt)statement);
			}
			else if(statement instanceof IfStmt){
				ifPass((IfStmt)statement);
			}
		}
		static void statementsPass(Statements statements){
			List<Statement> list = new ArrayList<>();
			while(statements instanceof StatementsRecursive){
				StatementsRecursive statementsR = (StatementsRecursive)statements;
				Statement statement = statementsR.getStatement();
				list.add(statement);
				statements = statementsR.getStatements();
			}
			list.forEach(Utils::statementPass);
		}
		static void conditionPass(Condition cond){
			//TODO
		}
		static void postDecPass(PostDec postDecExpr){
			Designator d = postDecExpr.getDesignator();
			String name = d.getName();
			SemanticAnalyzerUtils.report_error("use of variable " + name,postDecExpr);
			Obj node = Tab.find(name);
			if(node == Tab.noObj){
				SemanticAnalyzerUtils.report_error("use of undeclared identifier " + name,postDecExpr);
			}
			if(node.getKind() != Obj.Var){
				SemanticAnalyzerUtils.report_error("cannot use identifier " + name + " in this context",postDecExpr);
			}
			Struct type = node.getType();
			if(type.getKind() != Struct.Int){
				SemanticAnalyzerUtils.report_error("cannot use this operator on the variable " + name,postDecExpr);
			}
		}
		static void ifPass(IfStmt stmt){
			IfStatement statement = stmt.getIfStatement();
			if (statement instanceof IfOnly){
				IfOnly ifOnly = (IfOnly)statement;
				Condition cond = ifOnly.getCondition();
				Statement s = ifOnly.getStatement();
				statementPass(s);
				conditionPass(cond);
			}
			else{
				IfElse ifElse = (IfElse) statement;
				Condition cond = ifElse.getCondition();
				conditionPass(cond);
				Statement s = ifElse.getStatement();
				statementPass(s);
				s = ifElse.getStatement1();
				statementPass(s);
			}
		}
		static void whilePass(WhileStmt stmt){
			DoWhile doWhile = stmt.getDoWhile();
			if (doWhile instanceof WhileCond){
				WhileCond whileCond = (WhileCond) doWhile;
				conditionPass(whileCond.getCondition());
				statementPass(whileCond.getStatement());
			}
			else if(doWhile instanceof WhileSimple){
				WhileSimple whileSimple = (WhileSimple) doWhile;
				statementPass(whileSimple.getStatement());
			}
			else if(doWhile instanceof WhileDesignator){
				WhileDesignator whileDesignator = (WhileDesignator)doWhile;
				statementPass(whileDesignator.getStatement());
				conditionPass(whileDesignator.getCondition());
				designatorStatementPass(whileDesignator.getDesignatorStatement());
			}
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
	public void visit(ConstDeclarationList list){
			Type type = list.getType();
			Struct struct = Utils.inferType(type);
			currentType = struct;
			List<IdDefinition> defs = new ArrayList<>();
			Utils.findIdDefinitionsRecursive(list.getIdDefinitionList(),defs);
			defs.forEach(Utils::registerIdDefinition);
	}
	public void visit(MethodDefinitionNoLocals def){
		Utils.registerMethod(def.getMethodSignature());
	}
	public void visit(MethodDefinition methodDefinition){
		MethodSignature signature = methodDefinition.getMethodSignature();
		LocalVarDeclarations varDecls = methodDefinition.getLocalVarDeclarations();
		Statements statements = methodDefinition.getStatements();
		Obj funcObj = Utils.registerMethod(signature);
		Utils.registerLocalVariables(varDecls);
		Tab.chainLocalSymbols(funcObj);
		Utils.statementsPass(statements);
		Tab.closeScope();
		//Tab.dump();
	}


}

