package org.processmining.confs;

import org.processmining.models.YourFirstInput;
import org.processmining.models.YourSecondInput;

public class YourConfiguration {

	private boolean yourBoolean;
	private int yourInteger;
	private String yourString;
	
	public YourConfiguration(YourFirstInput input1, YourSecondInput input2) {
		setYourBoolean(input1.equals(input2));
		setYourInteger(input1.toString().length() - input2.toString().length());
		setYourString(input1.toString() + input2.toString());
	}

	public void setYourBoolean(boolean yourBoolean) {
		this.yourBoolean = yourBoolean;
	}

	public boolean isYourBoolean() {
		return yourBoolean;
	}

	public void setYourInteger(int yourInteger) {
		this.yourInteger = yourInteger;
	}

	public int getYourInteger() {
		return yourInteger;
	}

	public void setYourString(String yourString) {
		this.yourString = yourString;
	}

	public String getYourString() {
		return yourString;
	}
}
