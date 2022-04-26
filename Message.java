import java.io.*;

class Message implements Serializable {
	String task = null;
	Object dataObject = null;

	public String getTask(){
		return task;
	}

	public Object getDataObject(){
		return dataObject;
	}

	public void setTask(String value){
		this.task = value;
	}

	public void setDataObject(Object value){
		this.dataObject = value;
	}
}