import java.io.*;

class Message implements Serializable {
	String task = null;
	String json = null;

	public String getTask(){
		return task;
	}

	public String getJson(){
		return json;
	}

	public void setTask(String value){
		this.task = value;
	}

	public void setJson(String value){
		this.json = value;
	}
}