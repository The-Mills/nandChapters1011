package nandCompiler;

import java.util.List;
import java.util.ArrayList;

public class SymbolTable 
{
	public static final int SYMBOL_STATIC = 0;
	public static final int SYMBOL_FIELD = 1;
	public static final int SYMBOL_ARG = 2;
	public static final int SYMBOL_VAR = 3;
	
	private List<String> staticSymbols = new ArrayList<String>();
	private List<String> fieldSymbols = new ArrayList<String>();
	private List<String> argSymbols = new ArrayList<String>();
	private List<String> varSymbols = new ArrayList<String>();
	
	private List<String> staticTypes = new ArrayList<String>();
	private List<String> fieldTypes = new ArrayList<String>();
	private List<String> argTypes = new ArrayList<String>();
	private List<String> varTypes = new ArrayList<String>();
	
	
	public void addSymbol(String name, String type, int kind)
	{
		switch(kind)
		{
			case SYMBOL_STATIC:
				staticSymbols.add(name);
				staticTypes.add(type);
				break;
			case SYMBOL_FIELD:
				fieldSymbols.add(name);
				fieldTypes.add(type);
				break;
			case SYMBOL_ARG:
				argSymbols.add(name);
				argTypes.add(type);
				break;
			case SYMBOL_VAR:
				varSymbols.add(name);
				varTypes.add(type);
		}
	}
	
	public void printTables()
	{
		System.out.println("STATIC");
		for(int i = 0; i < staticSymbols.size(); i++)
		{
			System.out.println(staticTypes.get(i) + "     " + staticSymbols.get(i));
		}
		System.out.println("FIELD");
		for(int i = 0; i < fieldSymbols.size(); i++)
		{
			System.out.println(fieldTypes.get(i) + "     " + fieldSymbols.get(i));
		}
		System.out.println("ARG");
		for(int i = 0; i < argSymbols.size(); i++)
		{
			System.out.println(argTypes.get(i) + "     " + argSymbols.get(i));
		}
		System.out.println("VAR");
		for(int i = 0; i < varSymbols.size(); i++)
		{
			System.out.println(varTypes.get(i) + "     " + varSymbols.get(i));
		}
	}
	
	public String getType(String name)
	{
		int index = varSymbols.indexOf(name);
		if(index != -1)
			return varTypes.get(index);
		
		index = argSymbols.indexOf(name);
		if(index != -1)
			return argTypes.get(index);
		
		index = fieldSymbols.indexOf(name);
		if(index != -1)
			return fieldTypes.get(index);
		
		index = staticSymbols.indexOf(name);
		if(index != -1)
			return staticTypes.get(index);
		
		return null;
	}
	
	public int getKind(String name)
	{
		if(varSymbols.contains(name))
			return SYMBOL_VAR;
		if(argSymbols.contains(name))
			return SYMBOL_ARG;
		if(fieldSymbols.contains(name))
			return SYMBOL_FIELD;
		if(staticSymbols.contains(name))
			return SYMBOL_STATIC;
		return -1;
	}
	
	public int getIndex(String name)
	{
		int index = varSymbols.indexOf(name);
		if(index != -1)
			return index;
		
		index = argSymbols.indexOf(name);
		if(index != -1)
			return index;
		
		index = fieldSymbols.indexOf(name);
		if(index != -1)
			return index;
		
		index = staticSymbols.indexOf(name);
		if(index != -1)
			return index;
		
		return -1;
	}
	
	public int getVarCount(int type)
	{
		switch(type)
		{
			case SymbolTable.SYMBOL_ARG:
				return argSymbols.size();
			case SymbolTable.SYMBOL_FIELD:
				return fieldSymbols.size();
			case SymbolTable.SYMBOL_STATIC:
				return staticSymbols.size();
			case SymbolTable.SYMBOL_VAR:
				return varSymbols.size();
		}
		return -1;
	}
	
	public void startSubroutine()
	{
		argSymbols.clear();
		varSymbols.clear();
		argTypes.clear();
		varTypes.clear();
	}
	
	public void startClass()
	{
		staticSymbols.clear();
		fieldSymbols.clear();
		staticTypes.clear();
		fieldTypes.clear();
	}
	
	public void reset()
	{
		startSubroutine();
		startClass();
	}
}
