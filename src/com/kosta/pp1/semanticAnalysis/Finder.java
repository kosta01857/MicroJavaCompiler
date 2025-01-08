package com.kosta.pp1.semanticAnalysis;

import com.kosta.pp1.ast.VarDeclRecursive;
import com.kosta.pp1.ast.VarDeclaration;
import com.kosta.pp1.ast.FunctionParameters;
import com.kosta.pp1.ast.IdDeclaration;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.kosta.pp1.ast.VarDecl;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.utils.Utils;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import com.kosta.pp1.ast.LocalVarDeclarations;
import com.kosta.pp1.ast.LocalVarDeclarationsConcrete;
import com.kosta.pp1.ast.LocalVarDeclarationsRecursive;
import com.kosta.pp1.ast.MethodDeclaration;
import com.kosta.pp1.ast.MethodDeclarations;
import com.kosta.pp1.ast.MethodDeclarationsRecursive;
import com.kosta.pp1.ast.MethodDefinition;
import com.kosta.pp1.ast.Statement;
import com.kosta.pp1.ast.Statements;
import com.kosta.pp1.ast.StatementsRecursive;
import com.kosta.pp1.ast.SyntaxNode;
import com.kosta.pp1.ast.Term;
import com.kosta.pp1.ast.TermConcrete;
import com.kosta.pp1.ast.TermRecursive;
import com.kosta.pp1.ast.IdDefinition;
import com.kosta.pp1.ast.IdDefinitionList;
import com.kosta.pp1.ast.IdDefinitionListConcrete;
import com.kosta.pp1.ast.IdDefinitionListRecursive;
import com.kosta.pp1.ast.FunctionParameterDeclRecursive;
import com.kosta.pp1.Register;
import com.kosta.pp1.ast.AddTerm;
import com.kosta.pp1.ast.AddTermConcrete;
import com.kosta.pp1.ast.AddTermRecursive;
import com.kosta.pp1.ast.ClassBody;
import com.kosta.pp1.ast.ClassDeclaration;
import com.kosta.pp1.ast.ClassDeclarationExtend;
import com.kosta.pp1.ast.ClassDeclarationNoExtend;
import com.kosta.pp1.ast.Condition;
import com.kosta.pp1.ast.ConditionConcrete;
import com.kosta.pp1.ast.ConditionFact;
import com.kosta.pp1.ast.ConditionRecursive;
import com.kosta.pp1.ast.ConditionTerm;
import com.kosta.pp1.ast.ConditionTermConcrete;
import com.kosta.pp1.ast.ConditionTermRecursive;
import com.kosta.pp1.ast.Expression;
import com.kosta.pp1.ast.Expressions;
import com.kosta.pp1.ast.ExpressionsConcrete;
import com.kosta.pp1.ast.ExpressionsRecursive;
import com.kosta.pp1.ast.Factor;
import com.kosta.pp1.ast.FunctionParameterDeclConcrete;

public class Finder {
	public static List<Statement> findStatements(Statements statements) {
		List<Statement> list = new ArrayList<>();
		while (statements instanceof StatementsRecursive) {
			StatementsRecursive statementsR = (StatementsRecursive) statements;
			Statement statement = statementsR.getStatement();
			list.add(statement);
			statements = statementsR.getStatements();
		}
		return list;
	}

	static ClassBody findClassBody(ClassDeclaration classDecl){
		if (classDecl instanceof ClassDeclarationNoExtend) {
			return ((ClassDeclarationNoExtend)classDecl).getClassBody();
		} else {
			return ((ClassDeclarationExtend)classDecl).getClassBody();
		}
	}

	public static List<IdDeclaration> findIdDeclarations(VarDeclaration varDeclarations) {
		List<IdDeclaration> list = new ArrayList<>();
		Map<SyntaxNode,Struct> map = Register.getInstance().getTypeMap();
		while (varDeclarations instanceof VarDeclRecursive) {
			VarDeclRecursive varDeclR = (VarDeclRecursive) varDeclarations;
			IdDeclaration idDecl = varDeclR.getIdDeclaration();
			map.put(idDecl,SemanticAnalyzer.getInstance().getCurrentType());
			list.add(idDecl);
			varDeclarations = varDeclR.getVarDeclaration();
		}
		VarDecl decl = (VarDecl) varDeclarations;
		IdDeclaration idDecl = decl.getIdDeclaration();
		map.put(idDecl,SemanticAnalyzer.getInstance().getCurrentType());
		list.add(idDecl);
		return list;
	}

	public static List<VarDeclarationList> findVarDeclarationLists(LocalVarDeclarations declarations) {
		List<VarDeclarationList> list = new ArrayList<>();
		while (declarations instanceof LocalVarDeclarationsRecursive) {
			LocalVarDeclarationsRecursive declsR = (LocalVarDeclarationsRecursive) declarations;
			list.add(declsR.getVarDeclarationList());
			declarations = declsR.getLocalVarDeclarations();
		}
		LocalVarDeclarationsConcrete decls = (LocalVarDeclarationsConcrete) declarations;
		list.add(decls.getVarDeclarationList());
		return list;
	}

	public static List<IdDefinition> findIdDefinitions(IdDefinitionList defList) {
		List<IdDefinition> list = new ArrayList<>();
		while (defList instanceof IdDefinitionListRecursive) {
			IdDefinitionListRecursive defListR = (IdDefinitionListRecursive) defList;
			list.add(defListR.getIdDefinition());
			defList = defListR.getIdDefinitionList();
		}
		IdDefinitionListConcrete defListC = (IdDefinitionListConcrete) defList;
		list.add(defListC.getIdDefinition());
		return list;
	}

	public static List<IdDeclaration> findFunctionParameters(FunctionParameters parameters) {
		List<IdDeclaration> list = new ArrayList<>();
		Map<SyntaxNode,Struct> map = Register.getInstance().getTypeMap();
		while (parameters instanceof FunctionParameterDeclRecursive) {
			FunctionParameterDeclRecursive paramR = (FunctionParameterDeclRecursive) parameters;
			IdDeclaration idDecl = paramR.getIdDeclaration();
			map.put(idDecl, Utils.inferType(paramR.getType()));
			list.add(idDecl);
			parameters = paramR.getFunctionParameters();
		}
		FunctionParameterDeclConcrete paramC = (FunctionParameterDeclConcrete) parameters;
		IdDeclaration idDecl = paramC.getIdDeclaration();
		map.put(idDecl, Utils.inferType(paramC.getType()));
		list.add(idDecl);
		return list;
	}

	public static List<Term> findTerms(AddTerm term) {
		List<Term> terms = new ArrayList<>();
		while (term instanceof AddTermRecursive) {
			AddTermRecursive termR = (AddTermRecursive) term;
			terms.add(termR.getTerm());
			term = termR.getAddTerm();
		}
		AddTermConcrete termR = (AddTermConcrete) term;
		terms.add(termR.getTerm());
		return terms;
	}

	public static List<Factor> findFactors(Term term) {
		List<Factor> factors = new ArrayList<>();
		while (term instanceof TermRecursive) {
			TermRecursive termRecursive = (TermRecursive) term;
			Factor factor = termRecursive.getFactor();
			factors.add(factor);
			term = termRecursive.getTerm();
		}
		TermConcrete termConcrete = (TermConcrete) term;
		Factor factor = termConcrete.getFactor();
		factors.add(factor);
		return factors;
	}

	public static List<Expression> findExpressions(Expressions expressions) {
		List<Expression> expr = new ArrayList<>();
		while (expressions instanceof ExpressionsRecursive) {
			ExpressionsRecursive exprR = (ExpressionsRecursive) expressions;
			expr.add(exprR.getExpression());
			expressions = exprR.getExpressions();
		}
		ExpressionsConcrete exprC = (ExpressionsConcrete) expressions;
		expr.add(exprC.getExpression());
		return expr;
	}

	public static boolean findMainFunction() {
		Obj main = Tab.find("main");
		if (main == Tab.noObj) {
			Utils.report_error("No function called main found", null);
			return false;
		}
		if (main.getKind() != Obj.Meth) {
			Utils.report_error("No function called main found", null);
			return false;
		}
		Boolean mainCheck;
		mainCheck = main.getType().getKind() == Struct.None;
		mainCheck &= main.getLevel() == 0;
		if (!mainCheck) {
			Utils.report_info("" + main.getLevel() + " |" + main.getType().getKind(), null);
			Utils.report_error("main found, but signature doesnt match the required one", null);
			return false;
		}
		return true;
	}

	public static List<MethodDeclaration> findMethodDeclarations(MethodDeclarations decls){
		List<MethodDeclaration> list = new ArrayList<>();
		while(decls instanceof MethodDeclarationsRecursive){
			MethodDeclarationsRecursive methDecl = (MethodDeclarationsRecursive)decls;
			list.add(methDecl.getMethodDeclaration());
			decls = methDecl.getMethodDeclarations();
		}
		return list;
	}

	public static List<ConditionTerm> findConditionTerms(Condition condition){
		List<ConditionTerm> terms = new ArrayList<>();
		while(condition instanceof ConditionRecursive){
			ConditionRecursive condR = (ConditionRecursive)condition;
			terms.add(condR.getConditionTerm());
			condition = condR.getCondition();
		}
		ConditionConcrete condC = (ConditionConcrete)condition;
		terms.add(condC.getConditionTerm());
		return terms;
	}

	public static List<ConditionFact> findConditionFactors(ConditionTerm term){
		List<ConditionFact> factors = new ArrayList<>();
		while(term instanceof ConditionTermRecursive){
			ConditionTermRecursive condTermR = (ConditionTermRecursive)term;
			factors.add(condTermR.getConditionFact());
			term = condTermR.getConditionTerm();
		}
		ConditionTermConcrete condTermC = (ConditionTermConcrete)term;
		factors.add(condTermC.getConditionFact());
		return factors;
	}
}
