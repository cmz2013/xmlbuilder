package cn.xml;

public class TempletException extends Exception {

	private static final long serialVersionUID = -4279470907399636260L;

	public TempletException (String message) {
		super(message);
	}
	
	public TempletException (Exception e) {
		super(e);
	}
}
