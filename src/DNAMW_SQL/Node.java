package DNAMW_SQL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class Node implements Serializable {
	private static final long serialVersionUID = -6983532457122569282L;
	ArrayList<Node> children;
	Point topLeftFront;
	Point bottomRightBack;
	static int MaximumEntriesinOctreeNode;
	Vector<Vector<Point>> dataPoints;
	boolean isLeaf;

	public Node(Object x, Object y, Object z, Object x2, Object y2, Object z2) {
		topLeftFront = new Point(x, y, z);
		bottomRightBack = new Point(x2, y2, z2);
		children = new ArrayList<Node>();
		dataPoints = new Vector<Vector<Point>>();
		isLeaf = true;
		
	}

	public String toString() {
		String result = "";
		//System.out.print("MIN"+topLeftFront.x+"-");
		//System.out.print("MAX"+bottomRightBack.x+"-");
		for (int i = 0; i < dataPoints.size(); i++) {
			result = result + i + " "+"-X: "+dataPoints.get(i).get(0).x+"-Y: "+dataPoints.get(i).get(0).y+"-Z: "+dataPoints.get(i).get(0).z;
			
		}
		return result;
	}

	public static Object mid(Object x1, Object x2) {
		String dataType = x1.getClass().getName();
		Object mid;

		switch (dataType) {
		case "java.lang.Integer":
			mid = ((int) x1 + (int) x2) / 2;
			break;
		case "java.lang.Double":
			mid = ((Double) x1 + (Double) x2) / 2;
			break;
		case "java.lang.String":
			mid = printMiddleString((String) x1, (String) x2);
			break;
		case "java.util.Date":
			mid = midDate(x1, x2);
			break;
		default:
			mid = 0;
			break;
		}
		return mid;

	}

	public static Date midDate(Object x1, Object x2) {
		Date mid = null;
		Date date1 = (Date) x1;
		Date date2 = (Date) x2;
		mid = new Date((date1.getTime() + date2.getTime()) / 2);
		return mid;

	}

	public static String printMiddleString(String string1, String string2) {
		String S = "";
		String T = "";
		if (string1.compareTo(string2) > 0) {
			S = string1;
			T = string2;
		} else {
			S = string2;
			T = string1;
		}
		if (S.length() > T.length()) {
			T = T + S.substring(T.length());
		} else {
			if (T.length() > S.length())
				S = S + T.substring(S.length());
		}

		int N = S.length();
		// Stores the base 26 digits after addition
		int[] a1 = new int[N + 1];

		for (int i = 0; i < N; i++) {
			a1[i + 1] = (int) S.charAt(i) - 97 + (int) T.charAt(i) - 97;
		}

		// Iterate from right to left
		// and add carry to next position
		for (int i = N; i >= 1; i--) {
			a1[i - 1] += (int) a1[i] / 26;
			a1[i] %= 26;
		}

		// Reduce the number to find the middle
		// string by dividing each position by 2
		for (int i = 0; i <= N; i++) {

			// If current value is odd,
			// carry 26 to the next index value
			if ((a1[i] & 1) != 0) {

				if (i + 1 <= N) {
					a1[i + 1] += 26;
				}
			}

			a1[i] = (int) a1[i] / 2;
		}
		String results = "";

		for (int i = 1; i <= N; i++) {
			results = results + ((char) (a1[i] + 97));

		}
		return results;
	}

	public void remove(Object x, Object y, Object z) {
		if (this.children.size() == 0) {
			for (int i = 0; i < this.dataPoints.size(); i++) {
				if (DBApp.CompareMinMax(x, this.dataPoints.get(i).get(0).x) == 0
						&& DBApp.CompareMinMax(y, this.dataPoints.get(i).get(0).y) == 0
						&& DBApp.CompareMinMax(z, this.dataPoints.get(i).get(0).z) == 0) {

					this.dataPoints.remove(i);
					// this.dataPaths.remove(i);
					// DBApp.saveOctreeORNode(this, Nodepath);
					i--;
				}
			}
			return;
		} else {
			Object midx = mid(this.topLeftFront.x, this.bottomRightBack.x);
			Object midy = mid(this.topLeftFront.y, this.bottomRightBack.y);
			Object midz = mid(this.topLeftFront.z, this.bottomRightBack.z);

			int pos;

			if (DBApp.CompareMinMax(x, midx) != 1) {
				if (DBApp.CompareMinMax(y, midy) != 1) {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 0;
					else
						pos = 4;
				} else {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 3;
					else
						pos = 7;
				}
			} else {
				if (DBApp.CompareMinMax(y, midy) != 1) {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 1;
					else
						pos = 5;
				} else {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 2;
					else
						pos = 6;
				}
			}
			// String childPath=this.childrenPaths.get(pos);
			(this.children.get(pos)).remove(x, y, z);
		}

	}

	public Vector<String> find(Object x, Object y, Object z) {
		Vector<String> path = new Vector<String>();
		if (this.children.size() == 0) {
			for (int i = 0; i < this.dataPoints.size(); i++) {

				if (DBApp.CompareMinMax(x, this.dataPoints.get(i).get(0).x) == 0
						&& DBApp.CompareMinMax(y, this.dataPoints.get(i).get(0).y) == 0
						&& DBApp.CompareMinMax(z, this.dataPoints.get(i).get(0).z) == 0) {
					for (int j = 0; j < this.dataPoints.get(i).size(); j++) {
						if (!path.contains(this.dataPoints.get(i).get(j).pagePath))
							path.add(this.dataPoints.get(i).get(j).pagePath);

					}

				}
			}

		} else {
			Object midx = mid(this.topLeftFront.x, this.bottomRightBack.x);
			Object midy = mid(this.topLeftFront.y, this.bottomRightBack.y);
			Object midz = mid(this.topLeftFront.z, this.bottomRightBack.z);

			int pos;

			if (DBApp.CompareMinMax(x, midx) != 1) {
				if (DBApp.CompareMinMax(y, midy) != 1) {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 0;
					else
						pos = 4;
				} else {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 3;
					else
						pos = 7;
				}
			} else {
				if (DBApp.CompareMinMax(y, midy) != 1) {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 1;
					else
						pos = 5;
				} else {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 2;
					else
						pos = 6;
				}
			}
			Vector<String> temp = this.children.get(pos).find(x, y, z);
			for (int j = 0; j < temp.size(); j++)
				path.add(temp.get(j));
		}
		return path;

	}

	public Vector<String> findWithOneCoordinate(Object x, boolean isX, boolean isY, boolean isZ, String colName) {
		Vector<String> result = new Vector<String>();
		if (this.children.size() == 0) {
			for (int i = 0; i < this.dataPoints.size(); i++) {
				Object toCompare = null;
				if (isX) {
					toCompare = this.dataPoints.get(i).get(0).x;
				} else {
					if (isY) {
						toCompare = this.dataPoints.get(i).get(0).y;
					} else {
						toCompare = this.dataPoints.get(i).get(0).z;
					}
				}

				if (DBApp.CompareMinMax(x, toCompare) == 0) {
					for (int j = 0; j < this.dataPoints.get(i).size(); j++) {
						if (!result.contains(this.dataPoints.get(i).get(j).pagePath))
							result.add(this.dataPoints.get(i).get(j).pagePath);
					}

				}
			}
		} else {
			Object toCompareFront=null;
			if(isX) {
				toCompareFront=this.topLeftFront.x;
			}else {
				if(isY) {
					toCompareFront=this.topLeftFront.y;
				}else {
					toCompareFront=this.topLeftFront.z;
				}
			}
			Object toCompareBack=null;
			if(isX) {
				toCompareBack=this.bottomRightBack.x;
			}else {
				if(isY) {
					toCompareBack=this.bottomRightBack.y;
				}else {
					toCompareBack=this.bottomRightBack.z;
				}
			}
			Object mid = mid(toCompareFront, toCompareBack);

			ArrayList <Integer> pos =new ArrayList <Integer>();
			if(isX) {
				if (DBApp.CompareMinMax(x, mid) != 1) {
					pos.add(0);
					pos.add(3);
					pos.add(4);
					pos.add(7);
				}else {
					
					pos.add(1);
					pos.add(2);
					pos.add(5);
					pos.add(6);
					
				}
				
			}else {
				if(isY) {
					if (DBApp.CompareMinMax(x, mid) != 1) {
						pos.add(4);
						pos.add(0);
						pos.add(1);
						pos.add(5);
						
					}else {
						pos.add(6);
						pos.add(7);
						pos.add(3);
						pos.add(2);
						
					}
					
				}else {
					if (DBApp.CompareMinMax(x, mid) != 1) {
						pos.add(0);
						pos.add(1);
						pos.add(2);
						pos.add(3);
						
					}else {
						
						pos.add(4);
						pos.add(5);
						pos.add(6);
						pos.add(7);
					}
					
				}
			}
              
			
			Vector<String> temp =new Vector<String>();
			for(int i=0; i<pos.size();i++) {
				temp.addAll(this.children.get(pos.get(i)).findWithOneCoordinate(x,isX,isY,isZ,colName));
			}
			for (int i = 0; i < temp.size(); i++) {
				if (!result.contains(temp.get(i)))
					result.add(temp.get(i));
			}

		}
		// System.out.print(result);

		return result;
	}
	

	public Vector<String> findwithOperator(Object x, boolean isX, boolean isY, boolean isZ, String operator,
			String colName) {

		Vector<String> result = new Vector<String>();
		if (operator == "=") {
           
			result = findWithOneCoordinate(x, isX, isY, isZ, colName);
            
		}
		if (operator == "!=") {
			
				  Vector<String> resultSmaller = findWithSmaller(x, isX, isY, isZ, colName);
				  Vector<String> resultGreater = findWithGreater(x, isX, isY, isZ, colName);
				  //find union
				  result=findUnion(resultSmaller,resultGreater);
		}
		if (operator == ">") {
			
			result = findWithGreater(x, isX, isY, isZ, colName);
			
		}
		if (operator == ">=") {
		
			result = findWithGreater(x, isX, isY, isZ, colName);
			result=findUnion(result,findWithOneCoordinate(x, isX, isY, isZ, colName));
			
		}
		if (operator == "<") {
			
			result = findWithSmaller(x, isX, isY, isZ, colName);
			
		}
		if (operator == "<=") {
		
			result = findWithSmaller(x, isX, isY, isZ, colName);
			result=findUnion(result,findWithOneCoordinate(x, isX, isY, isZ, colName));

		}
		return result;
	}
	private  Vector<String>  findUnion( Vector<String> vector1, Vector<String> vector2) {
		 Vector<String> result=new  Vector<String> ();
		 result.addAll(vector1);
		 
			 for(int i=0 ; i<vector2.size();i++) {
				 if(!result.contains(vector2.get(i)))
					 result.add(vector2.get(i));
			 }
		return result;
	}
//anything greater than x
	public Vector<String> findWithGreater(Object x, boolean isX, boolean isY, boolean isZ, String colName) {
		Vector<String> result = new Vector<String>();
		if (this.children.size() == 0) {

			for (int i = 0; i < this.dataPoints.size(); i++) {
				Object toCompare = null;
				if (isX) {
					toCompare = this.dataPoints.get(i).get(0).x;
				} else {
					if (isY) {
						toCompare = this.dataPoints.get(i).get(0).y;
					} else {
						toCompare = this.dataPoints.get(i).get(0).z;
					}
				}
				if (DBApp.CompareMinMax(toCompare, x) == 1) {
					for (int j = 0; j < this.dataPoints.get(i).size(); j++) {
						if (!result.contains(this.dataPoints.get(i).get(j).pagePath))
							result.add(this.dataPoints.get(i).get(j).pagePath);
					}
				}
			}
		} else {
			Object toCompareFront = null;
			if (isX) {
				toCompareFront = this.topLeftFront.x;
			} else {
				if (isY) {
					toCompareFront = this.topLeftFront.y;
				} else {
					toCompareFront = this.topLeftFront.z;
				}
			}
			Object toCompareBack = null;
			if (isX) {
				toCompareBack = this.bottomRightBack.x;
			} else {
				if (isY) {
					toCompareBack = this.bottomRightBack.y;
				} else {
					toCompareBack = this.bottomRightBack.z;
				}
			}
			Object mid = mid(toCompareFront, toCompareBack);

			ArrayList<Integer> pos = new ArrayList<Integer>();
			if (isX) {
				if (DBApp.CompareMinMax(x, mid) != 1) {
					pos.add(0);
					pos.add(1);
					pos.add(2);
					pos.add(3);
					pos.add(4);
					pos.add(5);
					pos.add(6);
					pos.add(7);
				} else {

					pos.add(1);
					pos.add(2);
					pos.add(5);
					pos.add(6);

				}

			} else {
				if (isY) {
					if (DBApp.CompareMinMax(x, mid) != 1) {
						pos.add(0);
						pos.add(1);
						pos.add(2);
						pos.add(3);
						pos.add(4);
						pos.add(5);
						pos.add(6);
						pos.add(7);
					} else {
						pos.add(6);
						pos.add(7);
						pos.add(3);
						pos.add(2);

					}

				} else {
					if (DBApp.CompareMinMax(x, mid) != 1) {
						pos.add(0);
						pos.add(1);
						pos.add(2);
						pos.add(3);
						pos.add(4);
						pos.add(5);
						pos.add(6);
						pos.add(7);

					} else {

						pos.add(4);
						pos.add(5);
						pos.add(6);
						pos.add(7);
					}

				}
			}

			Vector<String> temp = new Vector<String>();
			for (int i = 0; i < pos.size(); i++) {
				temp.addAll(this.children.get(pos.get(i)).findWithGreater(x, isX, isY, isZ, colName));
			}
			for (int i = 0; i < temp.size(); i++) {
				if (!result.contains(temp.get(i)))
					result.add(temp.get(i));
			}

		}

		return result;
	}

//find anything smaller than me
	public Vector<String> findWithSmaller(Object x, boolean isX, boolean isY, boolean isZ, String colName) {
		Vector<String> result = new Vector<String>();
		if (this.children.size() == 0) {
			
			for (int i = 0; i < this.dataPoints.size(); i++) {
				Object toCompare = null;
				if (isX) {
					toCompare = this.dataPoints.get(i).get(0).x;
				} else {
					if (isY) {
						toCompare = this.dataPoints.get(i).get(0).y;
					} else {
						toCompare = this.dataPoints.get(i).get(0).z;
					}
				}
				
				if (DBApp.CompareMinMax(toCompare, x) == -1) {
				
					for (int j = 0; j < this.dataPoints.get(i).size(); j++)
						if (!result.contains(this.dataPoints.get(i).get(j).pagePath))
							result.add(this.dataPoints.get(i).get(j).pagePath);
				}
			}
		} else {
			Object toCompareFront = null;
			if (isX) {
				toCompareFront = this.topLeftFront.x;
			} else {
				if (isY) {
					toCompareFront = this.topLeftFront.y;
				} else {
					toCompareFront = this.topLeftFront.z;
				}
			}
			Object toCompareBack = null;
			if (isX) {
				toCompareBack = this.bottomRightBack.x;
			} else {
				if (isY) {
					toCompareBack = this.bottomRightBack.y;
				} else {
					toCompareBack = this.bottomRightBack.z;
				}
			}
			Object mid = mid(toCompareFront, toCompareBack);

			ArrayList<Integer> pos = new ArrayList<Integer>();
			if (isX) {
				if (DBApp.CompareMinMax(x, mid) <= 0) {
					
					pos.add(0);
					pos.add(3);
					pos.add(4);
					pos.add(7);
				} else {
				
					pos.add(0);
					pos.add(1);
					pos.add(2);
					pos.add(3);
					pos.add(4);
					pos.add(5);
					pos.add(6);
					pos.add(7);

				}

			} else {
				if (isY) {
					if (DBApp.CompareMinMax(x, mid) <= 0) {
						pos.add(4);
						pos.add(0);
						pos.add(1);
						pos.add(5);

					} else {
						pos.add(0);
						pos.add(1);
						pos.add(2);
						pos.add(3);
						pos.add(4);
						pos.add(5);
						pos.add(6);
						pos.add(7);

					}

				} else {
					if (DBApp.CompareMinMax(x, mid) <= 0) {
						
						pos.add(0);
						pos.add(1);
						pos.add(2);
						pos.add(3);

					} else {
						
						pos.add(0);
						pos.add(1);
						pos.add(2);
						pos.add(3);
						pos.add(4);
						pos.add(5);
						pos.add(6);
						pos.add(7);
					}

				}
			}

			Vector<String> temp = new Vector<String>();
			for (int i = 0; i < pos.size(); i++) {
				
				temp.addAll(this.children.get(pos.get(i)).findWithSmaller(x, isX, isY, isZ, colName));
			}
			for (int i = 0; i < temp.size(); i++) {
				if (!result.contains(temp.get(i)))
					result.add(temp.get(i));
			}

		}

		return result;
	}

	public void updatePath(Object x, Object y, Object z, String newPath, Object PK) {
		if (this.children.size() == 0) {
			for (int i = 0; i < this.dataPoints.size(); i++) {

				if (DBApp.CompareMinMax(x, this.dataPoints.get(i).get(0).x) == 0
						&& DBApp.CompareMinMax(y, this.dataPoints.get(i).get(0).y) == 0
						&& DBApp.CompareMinMax(z, this.dataPoints.get(i).get(0).z) == 0) {
					for (int j = 0; j < this.dataPoints.get(i).size(); j++) {
						if(DBApp.CompareMinMax(this.dataPoints.get(i).get(j).PK,PK)==0) {
							this.dataPoints.get(i).get(j).pagePath = newPath;
							return;
						}
					}

				}
			}

		} else {
			Object midx = mid(this.topLeftFront.x, this.bottomRightBack.x);
			Object midy = mid(this.topLeftFront.y, this.bottomRightBack.y);
			Object midz = mid(this.topLeftFront.z, this.bottomRightBack.z);

			int pos;

			if (DBApp.CompareMinMax(x, midx) != 1) {
				if (DBApp.CompareMinMax(y, midy) != 1) {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 0;
					else
						pos = 4;
				} else {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 3;
					else
						pos = 7;
				}
			} else {
				if (DBApp.CompareMinMax(y, midy) != 1) {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 1;
					else
						pos = 5;
				} else {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 2;
					else
						pos = 6;
				}
			}
			 this.children.get(pos).updatePath(x,  y,  z,  newPath,  PK);
			
		}
		
	}

	public void updateValue(Octree octree,Object newx, Object newy, Object newz, Object x, Object y, Object z, Object key) {
		if (this.children.size() == 0) {
			String path = null;
			for (int i = 0; i < this.dataPoints.size(); i++) {

				if (DBApp.CompareMinMax(x, this.dataPoints.get(i).get(0).x) == 0
						&& DBApp.CompareMinMax(y, this.dataPoints.get(i).get(0).y) == 0
						&& DBApp.CompareMinMax(z, this.dataPoints.get(i).get(0).z) == 0) {
					for (int j = 0; j < this.dataPoints.get(i).size(); j++) {
						if(DBApp.CompareMinMax(this.dataPoints.get(i).get(j).PK,key)==0) {
							if(newx == null)
								newx = x;
							if( newy == null)
								newy = y;
							if(newz ==null)
								newz = z;
							path = this.dataPoints.get(i).get(j).pagePath;
							this.dataPoints.get(i).remove(j);
							 Point newPoint = new Point(newx,newy,newz,path,key);
							 octree.removewithKey(x, y, z,key);
							 octree.insert(newx,newy, newz, path, key);
							 return;
							
							
						}
					}

				}
			}
		
		} else {
			Object midx = mid(this.topLeftFront.x, this.bottomRightBack.x);
			Object midy = mid(this.topLeftFront.y, this.bottomRightBack.y);
			Object midz = mid(this.topLeftFront.z, this.bottomRightBack.z);

			int pos;

			if (DBApp.CompareMinMax(x, midx) != 1) {
				if (DBApp.CompareMinMax(y, midy) != 1) {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 0;
					else
						pos = 4;
				} else {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 3;
					else
						pos = 7;
				}
			} else {
				if (DBApp.CompareMinMax(y, midy) != 1) {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 1;
					else
						pos = 5;
				} else {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 2;
					else
						pos = 6;
				}
			}
			this.children.get(pos).updateValue( octree,newx,  newy,  newz,  x,  y,  z,  key);
			
		}
		
	}


	public void PrintOctree() {
		if(this.children.size()==0) {
			for(int i =0 ;i<this.dataPoints.size();i++) {
				for(int j=0;j<this.dataPoints.get(i).size();j++) {
					System.out.print("Point " +i + " duplicate "+ j + " id "+this.dataPoints.get(i).get(j).PK );
				}
		}
			return;
			}
		else {
			for(int u= 0;u<8;u++) {
				this.children.get(u).PrintOctree();
			}
		}
		
	}

	public void removewithKey(Object x, Object y, Object z, Object key) {
		if (this.children.size() == 0) {
			for(int i= 0;i<this.dataPoints.size();i++)
			for (int j = 0; j < this.dataPoints.get(i).size(); i++) {
				
				if (DBApp.CompareMinMax(x, this.dataPoints.get(i).get(0).x) == 0
						&& DBApp.CompareMinMax(y, this.dataPoints.get(i).get(0).y) == 0
						&& DBApp.CompareMinMax(z, this.dataPoints.get(i).get(0).z) == 0 &&DBApp.CompareMinMax(z, this.dataPoints.get(i).get(j)) == 0 ) {

					this.dataPoints.remove(i);
					// this.dataPaths.remove(i);
					// DBApp.saveOctreeORNode(this, Nodepath);
					i--;
				}
			}
			return;
		} else {
			Object midx = mid(this.topLeftFront.x, this.bottomRightBack.x);
			Object midy = mid(this.topLeftFront.y, this.bottomRightBack.y);
			Object midz = mid(this.topLeftFront.z, this.bottomRightBack.z);

			int pos;

			if (DBApp.CompareMinMax(x, midx) != 1) {
				if (DBApp.CompareMinMax(y, midy) != 1) {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 0;
					else
						pos = 4;
				} else {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 3;
					else
						pos = 7;
				}
			} else {
				if (DBApp.CompareMinMax(y, midy) != 1) {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 1;
					else
						pos = 5;
				} else {
					if (DBApp.CompareMinMax(z, midz) != 1)
						pos = 2;
					else
						pos = 6;
				}
			}
			// String childPath=this.childrenPaths.get(pos);
			(this.children.get(pos)).remove(x, y, z);
		}

		
	}
   
}
