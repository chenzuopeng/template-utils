package org.czp;

/**
 * 模板解析异常
*
* @author dylan.chen 2008-10-20
* 
*/
public class TemplateEngineException extends Exception{

	  private static final long serialVersionUID = 6386600581767682191L;
	
	  public TemplateEngineException() {
	  }

	  public TemplateEngineException(String message) {
	    super(message);
	  }

	  public TemplateEngineException(String message, Throwable cause) {
	    super(message, cause);
	  }

	  public TemplateEngineException(Throwable cause) {
	    super(cause);
	  }

	  public TemplateEngineException(Object object) {
	    this(object.toString());
	  }
}

