package fr.adam.scanner.spoon;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.visitor.CtScanner;

public class IfScanner extends CtScanner {

	public CtIf ifElement = null;
	String toSearch = "";
	
	public IfScanner(String toSearch) {
		super();
		this.toSearch = compareStrings(toSearch);
	}

	@Override
	public void visitCtIf(CtIf ifElement) {
		CtExpression exp =  ifElement.getCondition();
		String formatedCondition = compareStrings(exp.toString());
		System.out.println(ifElement.getCondition());
		if(formatedCondition.equals(toSearch)){
			this.ifElement = ifElement;
		}
		super.visitCtIf(ifElement);
		
	}
	
	public String compareStrings(String l){
		String lf = l.replace("(", "").replace(")", "").replace(" ", "");
		return lf;
	}

	/*@Override
	public void scan(CtElement element) {
		// TODO Auto-generated method stub
		super.scan(element);
		if(element instanceof CtIf){
			System.out.println(element);
		}
	}*/

}
