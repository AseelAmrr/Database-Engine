package DNAMW_SQL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

public class Page implements Serializable{

	private static final long serialVersionUID = 2322476242345289032L;

	Vector<Hashtable<String,Object>> rows;
	public static int max;
	Object minValue;
	Object maxValue;
	public Page() {
	        rows = new Vector<>(max);
	    }
	
	public boolean isEmpty() {
		return this.rows.size() == 0;
	}

	public boolean isfull() {
		return this.rows.size() == max;
	}
	 
	
	

}
