package com.kosta.pp1.types;

import rs.etf.pp1.symboltable.concepts.Struct;

/**
 * SetType
 */
public class SetType extends Struct {
	static private SetType instance = null;
	static public SetType getInstance(){
		if(instance == null){
			instance = new SetType();
		}
		return instance;
	}
	private SetType(){
		super(10);
	}
	
}
