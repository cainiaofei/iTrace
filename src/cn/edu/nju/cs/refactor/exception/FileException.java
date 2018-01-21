package cn.edu.nju.cs.refactor.exception;

public class FileException extends SelfDefineException{
	private static final long serialVersionUID = 1L;
	private String msg;
	
	public FileException() {}
	public FileException(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String getExceptionMsg() {
		return msg;
	}

}
