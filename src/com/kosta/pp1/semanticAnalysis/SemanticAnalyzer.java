package com.kosta.pp1.semanticAnalysis;
import com.kosta.pp1.ast.ClassDeclarationExtend;
import com.kosta.pp1.ast.ClassDeclarationNoExtend;
import com.kosta.pp1.ast.ConstDeclarationList;
import com.kosta.pp1.ast.GlobalVarDeclarationList;
import com.kosta.pp1.ast.MethodDefinition;
import com.kosta.pp1.ast.MethodDefinitionNoLocals;
import com.kosta.pp1.ast.ProgName;
import com.kosta.pp1.ast.Program;
import com.kosta.pp1.ast.VarDeclarationList;
import com.kosta.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import com.kosta.pp1.utils.Utils;
import com.kosta.pp1.types.SetType;
import com.kosta.pp1.Register;

public class SemanticAnalyzer extends VisitorAdaptor {
	private static SemanticAnalyzer instance = null;
	private Struct currentType = Tab.noType;
	private boolean errorDetected = false;

	public Struct getCurrentType(){
		return this.currentType;
	}

	public void setCurrentType(Struct _t){
		this.currentType = _t;
	}

	public void setError(){
		this.errorDetected = true;
	}

	public boolean getError(){
		return this.errorDetected;
	}

	private Analyzer analyzer;
	static public SemanticAnalyzer getInstance(){
		if(instance == null){
			instance = new SemanticAnalyzer();
		}
		return instance;
	}

	private SemanticAnalyzer() {
	}

	public void setAnalyzer(Analyzer _analyzer){
		this.analyzer = _analyzer;
	}
	

	public void visit(ProgName progName) {
		Tab.insert(Obj.Prog, progName.getName(), Tab.noType);
		Tab.openScope();
	}

	public void visit(Program program) {
		analyzer.programPass(program);
	}

	public void visit(GlobalVarDeclarationList globalVarDeclaration) {
		VarDeclarationList varDeclarationList = globalVarDeclaration.getVarDeclarationList();
		analyzer.declarationListPass(varDeclarationList);
	}

	public void visit(ConstDeclarationList list) {
		analyzer.definitionListPass(list);
	}

	public void visit(MethodDefinitionNoLocals def) {
		analyzer.methodDeclarationPass(def);
	}

	public void visit(MethodDefinition methodDefinition) {
		analyzer.methodDeclarationPass(methodDefinition);
	}

	public void visit(ClassDeclarationExtend classDecl){
		analyzer.classDeclarationPass(classDecl);
	}

	public void visit(ClassDeclarationNoExtend classDecl){
		analyzer.classDeclarationPass(classDecl);
	}
	
}
