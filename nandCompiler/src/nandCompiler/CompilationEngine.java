package nandCompiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import fileIO.FileWriter;
import fileIO.Writer;

public class CompilationEngine 
{
	private JackTokenizer theTokenizer;
	private Writer vmWriter = new FileWriter();
	private String currentClassName;
	private String currentFunctionReturnType;
	private String currentFunctionKind;
	private SymbolTable table = new SymbolTable();
	private int ifStatementCount = 0;
	private int whileStatementCount = 0;
	
	public CompilationEngine(JackTokenizer t)
	{
		theTokenizer = t;
	}
	
	public void setJackFile(File f)
	{
		String path = f.getAbsolutePath();
		path = path.replace(".jack", ".vm");
		vmWriter.open(path);
	}
	
	public void reset()
	{
		vmWriter.reset();
		currentClassName = "";
		currentFunctionReturnType = "";
		currentFunctionKind = "";
		table.reset();
		ifStatementCount = 0;
		whileStatementCount = 0;
	}
	
	public void compileClass()
	{	
		//System.out.println("START compileClass");
		table.startClass();
		
		theTokenizer.advanceToken(); 						// Advance past 'class' token
		currentClassName = theTokenizer.getCurrentToken();	// Get className
		theTokenizer.advanceToken();						// Advance past 'className' token
		theTokenizer.advanceToken(); 						// Advance past '{' token
		
		while(theTokenizer.hasMoreTokens())
		{
			switch(theTokenizer.getCurrentToken())
			{
				case "static": case "field":
					compileClassVarDec();
					break;
				case "constructor": case "function": case "method": 
					compileSubroutine();
					break;
				case "}":
					theTokenizer.advanceToken();
					break;
			}
		}
		
		//System.out.println("END compileClass");
	}
	
	/*
	 * Compiles a SINGLE class variable declaration statement
	 */
	private void compileClassVarDec()
	{
		int tableCode;
		//System.out.println("START compileClassVarDec");
		
		String dependency = theTokenizer.getCurrentToken(); // 'static' or 'field'
		theTokenizer.advanceToken();
		
		if(dependency.equals("static"))
			tableCode = SymbolTable.SYMBOL_STATIC;
		else
			tableCode = SymbolTable.SYMBOL_FIELD;
		
		String type = theTokenizer.getCurrentToken();       // 'int', 'char', 'boolean', or custom object class.
		theTokenizer.advanceToken();
		
		List<String> names = new ArrayList<String>();       // Variable names
		names.add(theTokenizer.getCurrentToken());       
		theTokenizer.advanceToken();
		
		while(!theTokenizer.getCurrentToken().equals(";"))
		{
			theTokenizer.advanceToken(); 					// Advance past comma
			names.add(theTokenizer.getCurrentToken());		// Add next variable name
			theTokenizer.advanceToken();					// Advance to comma or semicolon
		}
		theTokenizer.advanceToken(); 						// Advance past semicolon
		
		for(String n : names)
			table.addSymbol(n, type, tableCode);
		
		//System.out.println("END compileClassVarDec");
		
		// WRITE VM CODE HERE, USING dependency, type, AND names
	}
	
	/*
	 * Compiles a SINGLE subroutine
	 * 
	 * Function subroutines --> field variables not recognized.
	 */
	private void compileSubroutine() 
	{
		//System.out.println("START compileSubroutine");
		table.startSubroutine();
		
		currentFunctionKind = theTokenizer.getCurrentToken();           // 'constructor' or 'function' or 'method'
		theTokenizer.advanceToken();
		
		currentFunctionReturnType = theTokenizer.getCurrentToken();     // 'void' or 'int' or 'char' or 'boolean' or custom object class
		theTokenizer.advanceToken();
		
		String name = theTokenizer.getCurrentToken();                   // Subroutine name. 
		theTokenizer.advanceToken();
		
		theTokenizer.advanceToken();  							        // Advance past '('
		 
		compileParameterList();
		
		theTokenizer.advanceToken();   							        // Advance past ')'
		theTokenizer.advanceToken();   							        // Advance past '{'
		
		
		while(theTokenizer.getCurrentToken().equals("var"))
				compileVarDec();
		
		/*
		 * START VM SEGMENT - Writes the function statement.
		 */
		int numLocals = table.getVarCount(SymbolTable.SYMBOL_VAR);
		int numFields = table.getVarCount(SymbolTable.SYMBOL_FIELD);
		switch(currentFunctionKind)
		{
			case "function":
				vmWriter.write("function " + currentClassName + "." + name + " " + numLocals + "\n");
				break;
			case "method":
				vmWriter.write("function " + currentClassName + "." + name + " " + numLocals + "\npush argument 0\npop pointer 0\n");
				break;
			case "constructor":
				vmWriter.write("function " + currentClassName + "." + name + " " + numLocals + "\n");
				vmWriter.write("push constant " + numFields + "\n" + "call Memory.alloc 1\npop pointer 0\n");
				break;
		}
		/*
		 * END VM SEGMENT
		 */
		
		compileStatements();
		
		theTokenizer.advanceToken();  							// Advance past '}'
		
		//table.printTables();
		//System.out.println("END compileSubroutine");
	}
	
	/*
	 * Compiles a COMPLETE parameter list. 
	 */
	private void compileParameterList()
	{
		//System.out.println("START compileParameterList");
		
		List<String> types = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		
		if(!theTokenizer.getCurrentToken().equals(")"))
		{
			types.add(theTokenizer.getCurrentToken());		   // Variable types
			theTokenizer.advanceToken();					
			
			names.add(theTokenizer.getCurrentToken());		   // Variable names
			theTokenizer.advanceToken();
			     
			while(!theTokenizer.getCurrentToken().equals(")"))
			{
				theTokenizer.advanceToken();                   // Advance past ','
				
				types.add(theTokenizer.getCurrentToken());
				theTokenizer.advanceToken();
				
				names.add(theTokenizer.getCurrentToken());
				theTokenizer.advanceToken();
			}
		}
		
		for(int i = 0; i < names.size(); i++)
			table.addSymbol(names.get(i), types.get(i), SymbolTable.SYMBOL_ARG);
		
		//System.out.println("END compileParameterList");
	}
	
	/*
	 * Compiles a SINGLE varDec statement.
	 */
	private void compileVarDec()
	{
		//System.out.println("START compileVarDec");
		
		theTokenizer.advanceToken();   					      // Advance past 'var'
		
		String type = theTokenizer.getCurrentToken(); 		  // Variable type
		theTokenizer.advanceToken();
		
		List<String> names = new ArrayList<String>();		  // Variable names
		names.add(theTokenizer.getCurrentToken());
		theTokenizer.advanceToken();
		
		while(!theTokenizer.getCurrentToken().equals(";"))
		{
			theTokenizer.advanceToken();                      // Advance past ','
			
			names.add(theTokenizer.getCurrentToken());
			theTokenizer.advanceToken();
		}
		
		theTokenizer.advanceToken();                          // Advance past ';'
		
		for(String n : names)
			table.addSymbol(n, type, SymbolTable.SYMBOL_VAR);
		
		//System.out.println("END compileVarDec");
	}
	
	/*
	 * Compiles ALL statements
	 */
	private void compileStatements()
	{
		//System.out.println("START compileStatements");
		
		while(!theTokenizer.getCurrentToken().equals("}"))
		{
			switch(theTokenizer.getCurrentToken())
			{
				case "let":
					compileLet();
					break;
				case "if":
					compileIf();
					break;
				case "while":
					compileWhile();
					break;
				case "do":
					compileDo();
					break;
				case "return":
					compileReturn();
					break;
			}
		}
		
		//System.out.println("END compileStatements");
	}
	
	private void compileDo()
	{
		theTokenizer.advanceToken();
		
		String first = theTokenizer.getCurrentToken();
		theTokenizer.advanceToken();
		
		String symbol = theTokenizer.getCurrentToken();
		String extMethodName = "";
		int numberParameters = 0;
		
		if(symbol.equals("."))
		{
			theTokenizer.advanceToken();
			extMethodName = theTokenizer.getCurrentToken();
			theTokenizer.advanceToken();
			theTokenizer.advanceToken();
			
			int index = table.getIndex(first);
			if(index != -1)  // is an object
			{
				int kind = table.getKind(first);
				String kindString = "", type = table.getType(first);
				switch(kind)
				{
					case SymbolTable.SYMBOL_ARG:
						kindString = "argument";
						break;
					case SymbolTable.SYMBOL_VAR:
						kindString = "local";
						break;
					case SymbolTable.SYMBOL_FIELD:
						kindString = "this";
						break;
					case SymbolTable.SYMBOL_STATIC:
						kindString = "static";
						break;
				}
				vmWriter.write("push " + kindString + " " + index + "\n");
				numberParameters = compileExpressionList();
				theTokenizer.advanceToken();
				vmWriter.write("call " + type + "." + extMethodName + " " + (numberParameters + 1) + "\n");
			}
			else  // is a static function
			{
				numberParameters = compileExpressionList();
				theTokenizer.advanceToken();
				vmWriter.write("call " + first + "." + extMethodName + " " + numberParameters + "\n");
			}	
		}
		else // is a (
		{
			theTokenizer.advanceToken();
			vmWriter.write("push pointer 0\n");
			numberParameters = compileExpressionList();
			theTokenizer.advanceToken();
			vmWriter.write("call " + currentClassName + "." + first + " " + (numberParameters + 1) + "\n");
		}
		theTokenizer.advanceToken();
		vmWriter.write("pop temp 0\n");
	}
	
	private void compileLet()
	{
		//System.out.println("START compileLet");
		
		boolean isArray = false;
		theTokenizer.advanceToken();        // Advance past 'let'
		
		String varName = theTokenizer.getCurrentToken();
		theTokenizer.advanceToken();
		
		if(theTokenizer.getCurrentToken().equals("["))
		{
			theTokenizer.advanceToken();   // Advance past '['
			isArray = true;
			compileExpression();
			theTokenizer.advanceToken();   // Advance past ']'
		}
		
		theTokenizer.advanceToken();       // Advance past '='
		compileExpression();
		theTokenizer.advanceToken();       // Advance past ';'
		
		/*
		 * START VM SEGMENT - Writes let statements
		 */
		
		int varIndex = table.getIndex(varName), varKind = table.getKind(varName);
		String varType = table.getType(varName), varKindString = "";
		switch(varKind)
		{
			case SymbolTable.SYMBOL_ARG:
				varKindString = "argument";
				break;
			case SymbolTable.SYMBOL_VAR:
				varKindString = "local";
				break;
			case SymbolTable.SYMBOL_FIELD:
				varKindString = "this";
				break;
			case SymbolTable.SYMBOL_STATIC:
				varKindString = "static";
				break;
		}
		//boolean isPrimitive = varType.equals("int") || varType.equals("boolean") || varType.equals("char");
		//if(isPrimitive)
		//{
		if(isArray)
		{
			vmWriter.write("pop temp 0\npush " + varKindString + " " + varIndex + "\nadd\npop pointer 1\npush temp 0\npop that 0\n");
		}
		else
		{
			vmWriter.write("pop " + varKindString + " " + varIndex + "\n");
		}
		//}
		//else if(!isArray)
		//{
			//vmWriter.write("pop ");
		//}
		
		/*
		 * END VM SEGMENT
		 */
		
		//System.out.println("END compileLet");
	}
	
	private void compileWhile() 
	{
		//System.out.println("START compileWhile");
		
		int currentWhile = whileStatementCount;
		whileStatementCount++;
		
		vmWriter.write("label WHILE_CONDITION_" + currentWhile + "\n");
		
		theTokenizer.advanceToken();      // Advance past 'while'
		theTokenizer.advanceToken();      // Advance past '('
		compileExpression();
		
		vmWriter.write("not\nif-goto WHILE_EXIT_" + currentWhile + "\n");
		
		theTokenizer.advanceToken();      // Advance past ')'
		theTokenizer.advanceToken();      // Advance past '{'
		compileStatements();
		theTokenizer.advanceToken();      // Advance past '}'
		
		vmWriter.write("goto WHILE_CONDITION_" + currentWhile + "\nlabel WHILE_EXIT_" + currentWhile + "\n");
		
		//System.out.println("END compileWhile");
	}
	
	private void compileReturn() 
	{
		//System.out.println("START compileReturn");
		
		theTokenizer.advanceToken();     // Advance past 'return'
		
		if(!theTokenizer.getCurrentToken().equals(";"))
			compileExpression();
		theTokenizer.advanceToken();     // Advance past ';'
		
		/*
		 * START VM SEGMENT - Write return statement
		 */
		
		if(currentFunctionReturnType.equals("void"))
			vmWriter.write("push constant 0\n");
		vmWriter.write("return\n");
		
		/*
		 * END VM SEGMENT
		 */
		
		//System.out.println("END compileReturn");
	}
	
	private void compileIf() 
	{
		//System.out.println("START compileIf");
		int ifNumber = ifStatementCount;
		ifStatementCount++;
		
		theTokenizer.advanceToken();       // Advance past 'if'
		theTokenizer.advanceToken();       // Advance past '('
		compileExpression();
		
		vmWriter.write("not\nif-goto IF_END_" + ifNumber + "\n");
		
		theTokenizer.advanceToken();       // Advance past ')'
		theTokenizer.advanceToken();       // Advance past '{'
		compileStatements();
		theTokenizer.advanceToken();       // Advance past '}'
		

		vmWriter.write("goto ELSE_END_" + ifNumber + "\n");
		vmWriter.write("label IF_END_" + ifNumber + "\n");
		if(theTokenizer.getCurrentToken().equals("else"))
		{
			theTokenizer.advanceToken();   // Advance past 'else'
			theTokenizer.advanceToken();   // Advance past '{'
			compileStatements();
			theTokenizer.advanceToken();   // Advance past '}'
		}
		
		vmWriter.write("label ELSE_END_" + ifNumber + "\n");
		
		//System.out.println("END compileIf");
	}
	
	// Expression can end with: ], ;, )
	private void compileExpression() 
	{
		//System.out.println("START compileExpression");
		compileTerm();
		
		String operation = "";
		String currentToken = theTokenizer.getCurrentToken();
		while(!currentToken.equals(")") && !currentToken.equals("]") && !currentToken.equals(";") && !currentToken.equals(","))
		{
			operation = currentToken;
			//System.out.println(operation);
			theTokenizer.advanceToken();
			compileTerm();
			currentToken = theTokenizer.getCurrentToken();
			
			/*
			 * START VM SEGMENT - Handles all operations 
			 */
			
			switch(operation)
			{
				case "+":
					vmWriter.write("add\n");
					break;
				case "-":
					vmWriter.write("sub\n");
					break;
				case "*":
					vmWriter.write("call Math.multiply 2\n");
					break;
				case "/":
					vmWriter.write("call Math.divide 2\n");
					break;
				case "&":
					vmWriter.write("and\n");
					break;
				case "|":
					vmWriter.write("or\n");
					break;
				case "<":
					vmWriter.write("lt\n");
					break;
				case ">":
					vmWriter.write("gt\n");
					break;
				case "=":
					vmWriter.write("eq\n");
					break;
			}
			
			/*
			 * END VM SEGMENT
			 */
		}
		//System.out.println("END compileExpression");
	}
	
	private void compileTerm()
	{
		//System.out.println("START compileTerm");
		String firstToken = theTokenizer.getCurrentToken();
		int firstType = theTokenizer.getTokenType();
		theTokenizer.advanceToken();
		String lookAhead = theTokenizer.getCurrentToken();
		
		switch(firstType)
		{
			case Compiler.TOKEN_INTEGER:
				/*
				 * START VM SEGMENT - Code for constant integer term
				 */
				
				vmWriter.write("push constant " + firstToken + "\n");
				
				/*
				 * END VM SEGMENT
				 */
				//System.out.println("END compileTerm - INTEGER");
				return;
				
			case Compiler.TOKEN_STRING:
				
				/*
				 * START VM SEGMENT - Code for Strings
				 */
				
				vmWriter.write("push constant " + firstToken.length() + "\ncall String.new 1\n");
				int currentChar;
				for(int i = 0; i < firstToken.length(); i++)
				{
					currentChar = (int)(firstToken.charAt(i));
					vmWriter.write("push constant " + currentChar + "\ncall String.appendChar 2\n");
					
				}
				
				/*
				 * END VM SEGMENT
				 */
				
				//System.out.println("END compileTerm - STRING");
				return;
		}
		
		switch(firstToken)
		{
			case "true":
				/*
				 * START VM SEGMENT - Code for representing true
				 */
				
				vmWriter.write("push constant 0\nnot\n");
				
				/*
				 * END VM SEGMENT
				 */
				
				//System.out.println("END compileTerm - TRUE");
				return;
				
				
			case "false": case "null":
				
				/*
				 * START VM SEGMENT - Code for representing false and null
				 */
				
				vmWriter.write("push constant 0\n");
				
				/*
				 * END VM SEGMENT
				 */
				
				//System.out.println("END compileTerm - NULL/FALSE");
				return;
				
				
			case "this":
				
				/*
				 * START VM SEGMENT - Code for representing this 
				 */
				
				vmWriter.write("push pointer 0\n");
				
				/*
				 * END VM SEGMENT
				 */
				
				//System.out.println("END compileTerm - THIS");
				return;
				
				
			case "(":
				compileExpression();
				theTokenizer.advanceToken();  // Advance past ')'
				//System.out.println("END compileTerm - (");
				return;
				
				
			case "-":
				compileTerm();
				
				/*
				 * START VM SEGMENT - Code for arithmetic negation
				 */
				
				vmWriter.write("neg\n");
				
				/*
				 * END VM SEGMENT
				 */
				
				//System.out.println("END compileTerm - NEGATIVE SIGN");
				return;
				
				
			case "~":
				compileTerm();
				
				/*
				 * START VM SEGMENT - Code for bitwise negation 
				 */
				
				vmWriter.write("not\n");
				
				/*
				 * END VM SEGMENT
				 */
				
				//System.out.println("END compileTerm - BIT NEGATION");
				return;
				
		}
		
		int numberParameters = 0;
		String extMethodName = "";
		switch(lookAhead)
		{
			case "[":
				theTokenizer.advanceToken();  // Advance past '['
				compileExpression();
				theTokenizer.advanceToken();  // Advance past ']'
				
				/*
				 * VM SEGMENT START - Array term
				 */
				int indexArray = table.getIndex(firstToken);
				int kindArray = table.getKind(firstToken);
				String kindStringArray = "";
				switch(kindArray)
				{
					case SymbolTable.SYMBOL_ARG:
						kindStringArray = "argument";
						break;
					case SymbolTable.SYMBOL_VAR:
						kindStringArray = "local";
						break;
					case SymbolTable.SYMBOL_FIELD:
						kindStringArray = "this";
						break;
					case SymbolTable.SYMBOL_STATIC:
						kindStringArray = "static";
						break;
				}
				vmWriter.write("push " + kindStringArray + " " + indexArray + "\nadd\npop pointer 1\npush that 0\n");
				
				/*
				 * VM SEGMENT END
				 */
				
				//System.out.println("END compileTerm - ARRAY [");
				return;
				
			
			case ".":
				theTokenizer.advanceToken();
				extMethodName = theTokenizer.getCurrentToken();
				theTokenizer.advanceToken();
				theTokenizer.advanceToken();
				
				int index = table.getIndex(firstToken);
				if(index != -1)
				{
					int kind = table.getKind(firstToken);
					String kindString = "", type = table.getType(firstToken);
					switch(kind)
					{
						case SymbolTable.SYMBOL_ARG:
							kindString = "argument";
							break;
						case SymbolTable.SYMBOL_FIELD:
							kindString = "this";
							break;
						case SymbolTable.SYMBOL_STATIC:
							kindString = "static";
							break;
						case SymbolTable.SYMBOL_VAR:
							kindString = "local";
							break;
					}
					vmWriter.write("push " + kindString + " " + index + "\n");
					numberParameters = compileExpressionList();
					theTokenizer.advanceToken();
					vmWriter.write("call " + type + "." + extMethodName + " " + numberParameters + "\n");
					return;
				}
				else
				{
					numberParameters = compileExpressionList();
					theTokenizer.advanceToken();
					vmWriter.write("call " + firstToken + "." + extMethodName + " " + numberParameters + "\n");
					return;
				}
			
			case "(":
				theTokenizer.advanceToken();
				vmWriter.write("push pointer 0\n");
				numberParameters = compileExpressionList();
				theTokenizer.advanceToken();
				vmWriter.write("call " + currentClassName + "." + firstToken + " " + (numberParameters + 1) + "\n");
				return;
				
			default:
				
				/*
				 * START VM SEGMENT - varName term code 
				 */
				
				int varIndex = table.getIndex(firstToken), varKind = table.getKind(firstToken);
				String varType = table.getType(firstToken), varKindString = "";
				
				switch(varKind)
				{
					case SymbolTable.SYMBOL_ARG:
						varKindString = "argument";
						break;
					case SymbolTable.SYMBOL_VAR:
						varKindString = "local";
						break;
					case SymbolTable.SYMBOL_FIELD:
						varKindString = "this";
						break;
					case SymbolTable.SYMBOL_STATIC:
						varKindString = "static";
						break;
				}
				boolean isPrimitive = varType.equals("int") || varType.equals("boolean") || varType.equals("char");
				if(isPrimitive)
				{
					vmWriter.write("push " + varKindString + " " + varIndex + "\n");
				}
				
				/*
				 * END VM SEGMENT
				 */
				
				//System.out.println("END compileTerm - VARNAME");
				return;
		}
	}
	
	/*
	 * Expression List is used exclusively for subroutine call parameters.
	 */
	private int compileExpressionList() 
	{
		int expressionCount = 0;
		//System.out.println("START compileExpressionList");
		//System.out.println(theTokenizer.getCurrentToken());
		if(!theTokenizer.getCurrentToken().equals(")"))
		{
			compileExpression();
			//System.out.println(theTokenizer.getCurrentToken());
			expressionCount++;
			while(!theTokenizer.getCurrentToken().equals(")"))
			{
				theTokenizer.advanceToken();   // Advance past ','
				compileExpression();
				expressionCount++;
			}
		}
		
		//System.out.println(expressionCount);
		return expressionCount;
		//System.out.println("END compileExpressionList");
	}
	
	
	/*
	 * Compiles ALL varDec statements. Not being used.
	 */
	private void compileVarDec2() 
	{
		List<String> types = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		String currentType;
		while(theTokenizer.getCurrentToken().equals("var"))
		{
			theTokenizer.advanceToken();  					// Advance past 'var'
			
			currentType = theTokenizer.getCurrentToken();
			types.add(currentType);
			theTokenizer.advanceToken();
			
			names.add(theTokenizer.getCurrentToken());
			theTokenizer.advanceToken();
			
			while(!theTokenizer.getCurrentToken().equals(";"))
			{
				theTokenizer.advanceToken();  			  // Advance past ','
				
				types.add(currentType);
				
				names.add(theTokenizer.getCurrentToken());
				theTokenizer.advanceToken();
			}			
			theTokenizer.advanceToken();                 // Advance past ';'
		}	
	}
}