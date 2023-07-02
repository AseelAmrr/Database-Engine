package DNAMW_SQL;

import java.io.Serializable;

public class Point implements Serializable {

	Object x,y,z;
    String pagePath;
	Object PK;
	public Point(Object x,Object y,Object z,String pagePath,Object PK) {
		this.x=x;
		this.y=y;
		this.z=z;
		this.pagePath = pagePath;
		this.PK = PK;
		
	}
	public Point(Object x,Object y,Object z) {
		this.x=x;
		this.y=y;
		this.z=z;
	}

}
