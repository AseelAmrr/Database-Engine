package DNAMW_SQL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;


public class Table implements Serializable{
	private static final long serialVersionUID = -8721121551932982663L;
	 String tableName;
	  String strClusteringKeyColumn;
	  Vector<String> Columns;
	
	  int pageID;
	  Vector<String> pageArr;
	  Vector <Object>  pageMin;
	  Vector <Object> pageMax;
	  Vector<Integer> numberOfRows;
	  Vector<Vector<String>> indices ;
	  Vector<String> indicesName;
	 
	   
	  Table(String tableName,String strClusteringKeyColumn, Vector<String> Columns){
		  this.tableName=tableName;
		  this.strClusteringKeyColumn=strClusteringKeyColumn;
		  this.Columns=Columns;
		  pageArr=new Vector <>();
		  pageMin=new Vector <>();
		  pageMax=new Vector <>();
		  numberOfRows=new Vector <>();
		  indices = new Vector<Vector<String>>();
	      indicesName = new Vector<>();
	
		  pageID=0;
		
	  }  
	
	

}
