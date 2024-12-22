package com.kosta.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import java_cup.runtime.Scanner;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.kosta.pp1.ast.Program;
import com.kosta.pp1.ast.VisitorAdaptor;
import com.kosta.pp1.utils.Log4JUtils;

public class MJParserTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(MJParserTest.class);
		
		Reader br = null;
		try {
			File sourceCode = new File("test/temp.mj");
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);

	        Symbol s = p.parse();  //pocetak parsiranja
			Tab.init();
			Struct bool = new Struct(Struct.Bool);
			Tab.currentScope.addToLocals(new Obj(Obj.Type, "bool", bool));

	        Program prog = (Program)(s.value); 
			// ispis sintaksnog stabla
			log.info(prog.toString(""));
			log.info("===================================");

			// ispis prepoznatih programskih konstrukcija
			VisitorAdaptor v = new SemanticAnalyzerV2();
			prog.traverseBottomUp(v); 
	      
			//log.info(" Print count calls = " + v.printCallCount);

			//log.info(" Deklarisanih promenljivih ima = " + v.varDeclCount);
			
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	
}
