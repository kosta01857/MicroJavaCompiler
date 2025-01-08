package com.kosta.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import com.kosta.pp1.ast.Program;
import com.kosta.pp1.ast.VisitorAdaptor;
import com.kosta.pp1.semanticAnalysis.Analyzer;
import com.kosta.pp1.semanticAnalysis.SemanticAnalyzer;
import com.kosta.pp1.semanticAnalysis.SemanticAnalyzerFactory;
import com.kosta.pp1.types.SetType;
import com.kosta.pp1.utils.Log4JUtils;
import com.kosta.pp1.utils.myDumpSymbolTableVisitor;

import rs.etf.pp1.mj.runtime.*;

public class Compiler{
	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
		Struct bool = new Struct(Struct.Bool);
		Obj boolObj = new Obj(Obj.Type, "bool", bool);
		Obj addObj, addAllObj;
		Tab.init();
		Tab.currentScope().addToLocals(addObj = new Obj(Obj.Meth, "add", Tab.noType, 0, 2));
		{
			Tab.openScope();
			Tab.currentScope().addToLocals(new Obj(Obj.Var, "a", SetType.getInstance()));
			Tab.currentScope().addToLocals(new Obj(Obj.Var, "b", Tab.intType));
			addObj.setLocals(Tab.currentScope().getLocals());
			Tab.closeScope();
		}
		Tab.currentScope().addToLocals(addAllObj = new Obj(Obj.Meth, "addAll", Tab.noType, 0, 2));
		{
			Tab.openScope();
			Tab.currentScope().addToLocals(new Obj(Obj.Var, "a", SetType.getInstance()));
			Tab.currentScope().addToLocals(new Obj(Obj.Var, "b", new Struct(Struct.Array, Tab.intType)));
			addAllObj.setLocals(Tab.currentScope().getLocals());
			Tab.closeScope();
		}
		Tab.currentScope.addToLocals(boolObj);
		Struct set = SetType.getInstance();
		Tab.currentScope.addToLocals(new Obj(Obj.Type, "set", set));
	}
	
	public static void main(String[] args) throws Exception {
		Logger log = Logger.getLogger(Compiler.class);
		Reader br = null;
		try {
			File sourceCode = new File("test/temp.mj");
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);

	        Symbol s = p.parse();  //pocetak parsiranja
	        Program prog = (Program)(s.value); 
			// ispis sintaksnog stabla
			log.info(prog.toString(""));
			log.info("===================================");

			// ispis prepoznatih programskih konstrukcija
			SemanticAnalyzer v = SemanticAnalyzerFactory.createInstance();
			prog.traverseBottomUp(v); 
			
			Tab.dump(new myDumpSymbolTableVisitor());
	      
			//log.info(" Print count calls = " + v.printCallCount);

			//log.info(" Deklarisanih promenljivih ima = " + v.varDeclCount);
			if(v.getError()){
				log.info("semantic analysis failed!");
			}
			else{
				log.info("semantic analysis passed!");
			}
			

			
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	
}
