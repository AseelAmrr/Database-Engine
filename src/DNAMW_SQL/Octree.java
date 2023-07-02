package DNAMW_SQL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class Octree implements Serializable {
	private static final long serialVersionUID = -503092555084069661L;
	Node root;
	
	String pathToTree;
	int countNodes;
	String Colx;
	String Coly;
	String Colz;

	public Octree(Object x1, Object y1, Object z1, Object x2, Object y2, Object z2, String strTableName,
			String[] strarrColName) {
		String directoryPath = "resources/" + "data/" + strTableName + "/indices/" + strarrColName[0] + strarrColName[1]
				+ strarrColName[2];
		pathToTree = "resources/" + "data/" + strTableName + "/indices/" + strarrColName[0] + strarrColName[1]
				+ strarrColName[2] + "/Octree.class";

		
		root = new Node(x1, y1, z1, x2, y2, z2);
		countNodes = 1;
		Colx = strarrColName[0];
		Coly = strarrColName[1];
		Colz = strarrColName[2];
		new File(directoryPath).mkdirs();

		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(pathToTree);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void insert(Object x, Object y, Object z, String path,Object PK) {
		Node root = this.root;
	

		if (root.isLeaf) {
			
			if (root.dataPoints.size() < Node.MaximumEntriesinOctreeNode) {
				for (int i = 0; i < root.dataPoints.size() ; i++) {
					// checks if each x y or z is in the octree
					if (DBApp.CompareMinMax(x, root.dataPoints.get(i).get(0).x) == 0
							&& DBApp.CompareMinMax(y, root.dataPoints.get(i).get(0).y) == 0
							&& DBApp.CompareMinMax(z, root.dataPoints.get(i).get(0).z) == 0) {
						
						Point p = new Point(x, y, z,path,PK);
						root.dataPoints.get(i).add(p);
						return;
					}
				}
				
				Point toAdd = new Point(x, y, z,path,PK);
				Vector<Point> temp1 = new Vector<Point>();
				temp1.add(toAdd);

				root.dataPoints.add(temp1);

			} else {

				Object midx = Node.mid(root.topLeftFront.x, root.bottomRightBack.x);
				Object midy = Node.mid(root.topLeftFront.y, root.bottomRightBack.y);
				Object midz = Node.mid(root.topLeftFront.z, root.bottomRightBack.z);
				split(root, midx, midy, midz);
				rearrange(root, x, y, z, path,PK, midx, midy, midz);

			}

		} else {
			Node curRoot = root;
			boolean found = false;
			while (!found && curRoot != null) {

				for (int i = 0; i < curRoot.children.size(); i++) {
				
					Node curNode = curRoot.children.get(i);
					if (DBApp.CompareMinMax(x, curNode.topLeftFront.x) == -1
							|| DBApp.CompareMinMax(x, curNode.bottomRightBack.x) == 1
							|| DBApp.CompareMinMax(y, curNode.topLeftFront.y) == -1
							|| DBApp.CompareMinMax(y, curNode.bottomRightBack.y) == 1
							|| DBApp.CompareMinMax(z, curNode.topLeftFront.z) == -1
							|| DBApp.CompareMinMax(z, curNode.bottomRightBack.z) == 1) {
						// not in boundary

					} else {
						if (curNode.isLeaf) {
							if (curNode.dataPoints.size() < Node.MaximumEntriesinOctreeNode) {
								for (int m = 0; m < root.dataPoints.size(); m++) {
									// checks if each x y or z is in the octree
									if (DBApp.CompareMinMax(x, root.dataPoints.get(m).get(0).x) == 0
											&& DBApp.CompareMinMax(y, root.dataPoints.get(m).get(0).y) == 0
											&& DBApp.CompareMinMax(z, root.dataPoints.get(m).get(0).z) == 0) {
										Point p = new Point(x, y, z,path,PK);
										root.dataPoints.get(i).add(p);
										return;
									}
								}
								
								Point toAdd = new Point(x, y, z,path,PK);
								Vector<Point> temp1 = new Vector<Point>();
								temp1.add(toAdd);

								curNode.dataPoints.add(temp1);

								found = true;

							} else {
								Object midx = Node.mid(curNode.topLeftFront.x, curNode.bottomRightBack.x);
								Object midy = Node.mid(curNode.topLeftFront.y, curNode.bottomRightBack.y);
								Object midz = Node.mid(curNode.topLeftFront.z, curNode.bottomRightBack.z);
								split(curNode, midx, midy, midz);
								rearrange(curNode, x, y, z, path, PK,midx, midy, midz);
								found = true;

							}

						} else {
							curRoot = curNode;
							break;
						}
					}
				}
			}
		}
	}

	public void split(Node root, Object midx, Object midy, Object midz) {
		String nodePath;
		for (int i = 0; i < 8; i++) {
			Node newNode = null;
			nodePath = pathToTree + "/" + countNodes++ + ".class";
			if (i == 0) {

				newNode = new Node(root.topLeftFront.x, root.topLeftFront.y, root.topLeftFront.z, midx, midy, midz);
			}
			if (i == 1) {
				newNode = new Node(midx, root.topLeftFront.y, root.topLeftFront.z, root.bottomRightBack.x, midy, midz);

			}
			if (i == 2) {
				newNode = new Node(midx, midy, root.topLeftFront.z, root.bottomRightBack.x, root.bottomRightBack.y,
						midz);
			}
			if (i == 3) {
				newNode = new Node(root.topLeftFront.x, midy, root.topLeftFront.z, midx, root.bottomRightBack.y, midz);
			}
			if (i == 4) {
				newNode = new Node(root.topLeftFront.x, root.topLeftFront.y, midz, midx, midy, root.bottomRightBack.z);
			}
			if (i == 5) {
				newNode = new Node(midx, root.topLeftFront.y, midz, root.bottomRightBack.x, midy,
						root.bottomRightBack.z);
			}
			if (i == 6) {
				newNode = new Node(midx, midy, midz, root.bottomRightBack.x, root.bottomRightBack.y,
						root.bottomRightBack.z);
			}
			if (i == 7) {
				newNode = new Node(root.topLeftFront.x, midy, midz, midx, root.bottomRightBack.y,
						root.bottomRightBack.z);
			}
			root.children.add(newNode);

		}
		root.isLeaf = false;

	}

//rearrange in case node is split
	public void rearrange(Node root, Object xNew, Object yNew, Object zNew, String path,Object PK, Object midx, Object midy,
			Object midz) {
		
		Point toAdd = new Point(xNew, yNew, zNew,path,PK);
		Vector<Point> temp1 = new Vector<Point>();
		temp1.add(toAdd);

		root.dataPoints.add(temp1);

		for (int i = 0; i < root.dataPoints.size(); i++) {
			
			Vector<Point> curPoint = root.dataPoints.get(i);
			Object x = curPoint.get(0).x;
			Object y = curPoint.get(0).y;
			Object z = curPoint.get(0).z;
			int pos;
			if (DBApp.CompareMinMax(x, midx) <= 0) {
				if (DBApp.CompareMinMax(y, midy) <= 0) {
					if (DBApp.CompareMinMax(z, midz) <= 0)
						pos = 0;
					else
						pos = 4;
				} else {
					if (DBApp.CompareMinMax(z, midz) <= 0)
						pos = 3;
					else
						pos = 7;
				}
			} else {
				if (DBApp.CompareMinMax(y, midy) <= 0) {
					if (DBApp.CompareMinMax(z, midz) <= 0)
						pos = 1;
					else
						pos = 5;
				} else {
					if (DBApp.CompareMinMax(z, midz) <= 0)
						pos = 2;
					else
						pos = 6;
				}
			}
			root.children.get(pos).dataPoints.add(curPoint);

		}
		
		root.dataPoints.removeAllElements();

	}

	public Vector<String> find(Object x, Object y, Object z) {
		Node root = this.root;
		return root.find(x, y, z);

	}

	public Vector<String> findOne(Object x, String colName) {
		boolean isX = colName.equals(Colx);
		boolean isY = colName.equals(Coly);
		boolean isZ = colName.equals(Colz);
		Node root = this.root;
		return root.findWithOneCoordinate(x, isX, isY, isZ, colName);

	}

	public void remove(Object x, Object y, Object z) {
		Node root = this.root;
		root.remove(x, y, z);
		

	}

	public Vector<String> findOperator(Object x, String colName, String operator) {
		boolean isX = colName.equals(Colx);
		boolean isY = colName.equals(Coly);
		boolean isZ = colName.equals(Colz);

		Node root = this.root;
		return root.findwithOperator(x, isX, isY, isZ, operator, colName);

	}

	public Vector<String> findSmaller(Object x, String colName) {
		boolean isX = colName.equals(Colx);
		boolean isY = colName.equals(Coly);
		boolean isZ = colName.equals(Colz);

		Node root = this.root;
		return root.findWithSmaller(x, isX, isY, isZ, colName);

	}

	public void PrintOctreeA() {
		root.PrintOctree();

	}
	public void PrintOctree() {
		Node cur = this.root;
		System.out.println("root" + cur + " ");

		for (int i = 0; i < 8; i++) {
			System.out.print("Child" + i + " ");
			System.out.print(cur.children.get(i) + "------");
		}
		System.out.println();

		cur = this.root.children.get(2);
		for (int i = 0; i < 8; i++) {
			System.out.print("Child" + i + " ");
			System.out.print(cur.children.get(i)  + "------");
		}

	}

	public static void printTree(Node node, String prefix) {
		if (node == null) {
			return;
		}
		System.out.print(prefix + node);
		String childPrefix = prefix + "  ";
		for (int i = 0; i < node.children.size(); i++) {
			printTree(node.children.get(i), childPrefix);
		}
	}
	public void updatePath(Object first, Object second, Object third, String newPath,Object PK) {
		root.updatePath(first,  second,  third,  newPath,  PK);
		
	}
	

	
	public void updateValue(Octree octree,Object x, Object y, Object z, Object oldx, Object oldy, Object oldz, Object key,String path) {
		
		removewithKey(oldx,oldy,oldz,key);
		insert( x,  y,  z,  path,key);
		
		
	}
    private void printOctree(Node node, String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + node); // Print the current node

        ArrayList<Node> children = node.children;
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Node child = children.get(i);
            boolean isLast = (i == size - 1);
            String newPrefix = prefix + (isTail ? "    " : "│   ");
            printOctree(child, newPrefix, isLast); // Recursively print the child nodes
        }
    }

    // Public method to print the Octree
    public void printOctree() {
        if (root != null) {
            printOctree(root, "", true);
        }
    }
    
    
    
    
    public static void main(String[] args) {
    	String pathToTree = "resources/" + "data/" + "students" + "/indices/" + "first_namelast_namegpa" + "/Octree.class";
		Octree test = DBApp.loadOctree(pathToTree);
		

		
			test.printOctree();
			}
    ///////////////////////////////////////////////////////////////

	public void removewithKey(Object x, Object y, Object z, Object key) {
		Node root = this.root;
		root.removewithKey(x, y, z,key);
		
	}
  
        // Recursive method to print the Octree nodes
    

        // ... other Octree methods ...
    



}


	
	


