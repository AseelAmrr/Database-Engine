package DNAMW_SQL;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Date;
import java.util.Vector;


//import org.junit.jupiter.api.Assertions;

public class DBApp {
	int MaximumRowsCountinTablePage;
	int MaximumEntriesinOctreeNode;

	public void init() {

		try (FileReader reader = new FileReader("resources/DBApp.config")) {
			Properties prop = new Properties();
			prop.load(reader);
			MaximumRowsCountinTablePage = Integer.parseInt((prop.getProperty("MaximumRowsCountinTablePage")));
			MaximumEntriesinOctreeNode = Integer.parseInt((prop.getProperty("MaximumEntriesinOctreeNode")));
			Page.max = MaximumRowsCountinTablePage;
			Node.MaximumEntriesinOctreeNode = MaximumEntriesinOctreeNode;
		} catch (FileNotFoundException ex) {
			// FileNotFoundException catch is optional and can be collapsed
		} catch (IOException ex) {

		}

	}

	// gets table names in the database (metadatafile)
	public static ArrayList<String> tableNamesinDB() {
		ArrayList<String> results = new ArrayList<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("resources/metadata.csv"));
			String line = br.readLine();

			while (line != null) {
				String[] content = line.split(",");
				if (content.length == 8) {

					if (!results.contains(content[0])) {
						results.add(content[0]);
					}
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;

	}

	// check if data type is correct and max and min are valid
	public boolean DataType(Hashtable<String, String> htblColNameType, Hashtable<String, String> htblColNameMin,
			Hashtable<String, String> htblColNameMax) throws DBAppException {
		Set<String> keys = htblColNameType.keySet();
		for (String key : keys) {
			if (!((htblColNameType.get(key)).equals("java.lang.Integer")
					|| (htblColNameType.get(key)).equals("java.lang.String")
					|| (htblColNameType.get(key)).equals("java.lang.Double")
					|| htblColNameType.get(key).equals("java.util.Date"))) {
				return false;
			} else {
				if ((htblColNameMin.get(key) == null) || (htblColNameMax.get(key) == null)
						|| htblColNameMin.contains("") || htblColNameMax.contains("")) {
					return false;
				}
			}
		}
		return true;

	}

	public void createTable(String strTableName, String strClusteringKeyColumn,
			Hashtable<String, String> htblColNameType, Hashtable<String, String> htblColNameMin,
			Hashtable<String, String> htblColNameMax) throws DBAppException {
		Vector<String> createdColoumns = new Vector<>();
		int noofcolumns = htblColNameType.size();

		if (!(tableNamesinDB().contains(strTableName))) {
			if (htblColNameType.containsKey(strClusteringKeyColumn)) {
				if (DataType(htblColNameType, htblColNameMin, htblColNameMax)) {
					if (checkDataTypes(htblColNameType, htblColNameMin, htblColNameMax)) {

						try {
							FileWriter outputFile = new FileWriter("resources/metadata.csv", true);
							// CSVWriter writer = new CSVWriter(outputFile);

							Enumeration<String> e = htblColNameType.keys();
							while (e.hasMoreElements()) {
								String key = e.nextElement();

								outputFile.append("\n");
								outputFile.append(strTableName);
								outputFile.append(", ");
								outputFile.append(key);
								outputFile.append(", ");
								outputFile.append(htblColNameType.get(key));
								outputFile.append(", ");
								if (strClusteringKeyColumn == key)
									outputFile.append("True");
								else
									outputFile.append("False");
								outputFile.append(", ");
								outputFile.append("null");// index name
								outputFile.append(", ");
								outputFile.append("null");// index type
								outputFile.append(", ");
								outputFile.append(htblColNameMin.get(key));
								outputFile.append(", ");
								outputFile.append(htblColNameMax.get(key));
								createdColoumns.add(key);

							}
							outputFile.flush();
							outputFile.close();
							Table T = new Table(strTableName, strClusteringKeyColumn, createdColoumns);
							T.Columns = createdColoumns;

							String Path = "resources/" + "data/" + strTableName;
							String tablePath = "resources/" + "data/" + strTableName + "/tableInfo.class";
							new File(Path).mkdirs();
							FileOutputStream fileOut = new FileOutputStream(tablePath);
							ObjectOutputStream out = new ObjectOutputStream(fileOut);
							out.writeObject(T);
							out.close();
							fileOut.close();
							String resourcesDirectory = "resources/" + "data/" + strTableName + "/indices";
							new File(resourcesDirectory).mkdir();
							saveTable(T, tablePath);
						} catch (IOException io) {

						}
					} else {

						throw new DBAppException("Error Not Compatible types");
					}

				} else {
					throw new DBAppException("Data Type is invalid or Min and Max are missing");
				}
			} else {
				throw new DBAppException("Primary Key is missing");
			}
		} else {
			throw new DBAppException("The table already exists");
		}
	}

	public boolean checkIndexExists(String strTableName, String[] strarrColName) {

		String tablepath = "resources/" + "data/" + strTableName + "/tableInfo.class";
		String directoryPath = "resources/" + "data/" + strTableName;
		Table T = loadTable(tablepath);
		for (int k = 0; k < strarrColName.length; k++) {

			for (int i = 0; i < T.indices.size(); i++) {

				for (int j = 0; j < T.indices.get(i).size(); j++) {

					if (strarrColName[k].equals(T.indices.get(i).get(j))) {

						return true;
					}

				}

			}

		}
		return false;
	}

	public void createIndex(String strTableName, String[] strarrColName) throws DBAppException {
		if (!(tableNamesinDB().contains(strTableName)))
			throw new DBAppException("This table does not exist.");
		String tablepath = "resources/" + "data/" + strTableName + "/tableInfo.class";
		String directoryPath = "resources/" + "data/" + strTableName;
		Table T = loadTable(tablepath);

		for (int i = 0; i < strarrColName.length; i++) {
			if (!T.Columns.contains(strarrColName[i]))
				throw new DBAppException("You added a coloumn that is not in the table");
		}

		if (checkIndexExists(strTableName, strarrColName)) {

			throw new DBAppException("Index already exists on this column.");
		}
		if (strarrColName.length < 3 || strarrColName.length > 3) {

			throw new DBAppException("you must enter 3 coloumns");
		}

		Object boundaryMaxX = getMax(strTableName, strarrColName[0]);
		Object boundaryMinX = getMin(strTableName, strarrColName[0]);
		Object boundaryMaxY = getMax(strTableName, strarrColName[1]);
		Object boundaryMinY = getMin(strTableName, strarrColName[1]);
		Object boundaryMaxZ = getMax(strTableName, strarrColName[2]);
		Object boundaryMinZ = getMin(strTableName, strarrColName[2]);
		Octree O = new Octree(boundaryMinX, boundaryMinY, boundaryMinZ, boundaryMaxX, boundaryMaxY, boundaryMaxZ,
				strTableName, strarrColName);

		writeToMeta(strTableName, strarrColName);
		Vector<String> temp1 = new Vector<>();
		String tempString = "";
		for (int i = 0; i < 3; i++) {
			temp1.add(strarrColName[i]);
			tempString += strarrColName[i];

		}
		T.indices.add(temp1);
		T.indicesName.add(tempString);
		if (T.pageArr.size() != 0) {
			for (int i = 0; i < T.pageArr.size(); i++) {
				Page p = loadPage(T.pageArr.get(i));

				for (int j = 0; j < T.numberOfRows.get(i); j++) {
					Hashtable<String, Object> currentRow = p.rows.get(j);
					Object first = currentRow.get(strarrColName[0]);
					Object second = currentRow.get(strarrColName[1]);
					Object third = currentRow.get(strarrColName[2]);
					Object PK = currentRow.get(T.strClusteringKeyColumn);
					O.insert(first, second, third, T.pageArr.get(i), PK);
					saveOctreeORNode(O, O.pathToTree);
				}
				p = null;
			}

		}
		saveTable(T, tablepath);
	}

	public void writeToMeta(String strTableName, String[] strarrColName) {
		String indexName = strarrColName[0] + strarrColName[1] + strarrColName[2] + "Index";
		File file = new File("resources/metadata.csv");
		File newFile = new File("resources/metadata1.csv");
		FileWriter fw;
		try {
			fw = new FileWriter(newFile, true);

			BufferedReader br = new BufferedReader(new FileReader("resources/metadata.csv"));
			String line = br.readLine();

			while (line != null) {

				String[] content = line.split(",");
				if (content.length == 8) {
					String TableName = content[0];

					String columnName = content[1].substring(1);
					String ColumnType = content[2].substring(1);
					String ClusteringKey = content[3].substring(1);
					String indexName1 = content[4].substring(1);
					String indexType = content[5].substring(1);
					String min = content[6].substring(1);
					String max = content[7].substring(1);

					if (strTableName.equals(TableName) && (strarrColName[0].equals(columnName)
							|| strarrColName[1].equals(columnName) || strarrColName[2].equals(columnName))) {
						fw.append("\n");
						fw.append(TableName);
						fw.append(", ");
						fw.append(columnName);
						fw.append(", ");
						fw.append(ColumnType);
						fw.append(", ");
						fw.append(ClusteringKey);
						fw.append(", ");
						fw.append(indexName);// index name
						fw.append(", ");
						fw.append("Octree");// index type
						fw.append(", ");
						fw.append(min);
						fw.append(", ");
						fw.append(max);
					}

					else {
						fw.append("\n");
						fw.append(line);

					}
				}
				line = br.readLine();
			}
			fw.flush();
			fw.close();
			br.close();

			file.delete();
			File dump = new File("resources/metadata.csv");
			newFile.renameTo(dump);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private Object getMax(String strTableName, String string) {
		Object max = null;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("resources/metadata.csv"));
			String line = br.readLine();

			while (line != null) {
				String[] content = line.split(",");
				if (content.length == 8) {
					if (strTableName.equals(content[0]) && (content[1].substring(1)).equals(string)) {

						if (content[2].equals(" java.lang.Double"))
							max = Double.parseDouble(content[7].substring(1));
						else if (content[2].equals(" java.lang.Integer"))
							max = Integer.parseInt(content[7].substring(1));

						else if (content[2].equals(" java.lang.Date")) {
							try {
								max = new SimpleDateFormat("yyyy-MM-dd").parse(content[7].substring(1));
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else
							max = content[7].substring(1);
					}
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return max;
	}

	private Object getMin(String strTableName, String string) {
		Object min = null;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("resources/metadata.csv"));
			String line = br.readLine();

			while (line != null) {
				String[] content = line.split(",");
				if (content.length == 8) {
					if (strTableName.equals(content[0]) && (content[1].substring(1)).equals(string)) {
						if (content[2].equals(" java.lang.Double"))
							min = Double.parseDouble(content[6].substring(1));
						else if (content[2].equals(" java.lang.Integer"))
							min = Integer.parseInt(content[6].substring(1));

						else if (content[2].equals(" java.lang.Date")) {
							try {
								min = new SimpleDateFormat("yyyy-MM-dd").parse(content[6].substring(1));
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else
							min = content[6].substring(1);
					}

				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return min;
	}

	public static boolean checkDataTypes(Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameMin, Hashtable<String, String> htblColNameMax) throws DBAppException {
		Enumeration<String> e = htblColNameType.keys();

		boolean flag = true;
		while (e.hasMoreElements()) {
			String key = e.nextElement();

			if ((htblColNameType.get(key)).equals("java.lang.Integer")) {
				try {
					int x = Integer.parseInt(htblColNameMin.get(key));
					int y = Integer.parseInt(htblColNameMax.get(key));

				} catch (NumberFormatException e1) {
					throw new DBAppException("Error Not Compatible types");
				}
			}
			if ((htblColNameType.get(key)).equals("java.util.Date")) {
				String Date1 = htblColNameMin.get(key);
				try {
					Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(Date1);
					String Date2 = htblColNameMax.get(key);
					Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(Date2);

				} catch (ParseException e1) {

					throw new DBAppException("Error Not Compatible types");
				}

			}

			if ((htblColNameType.get(key)).equals("java.lang.Double")) {
				try {
					String minimum = htblColNameMin.get(key);
					double d1 = Double.parseDouble(minimum);
					String maximum = htblColNameMax.get(key);
					double d2 = Double.parseDouble(maximum);

				} catch (NumberFormatException e1) {
					throw new DBAppException("Error Not Compatible types");
				}

			}
			if ((htblColNameType.get(key)).equals("java.lang.String")) {
				try {
					int x = Integer.parseInt(htblColNameMin.get(key));
					flag = false;
				} catch (Exception io) {

					flag = true;
				}
				if (flag == false)
					return false;
				try {
					int y = Integer.parseInt(htblColNameMax.get(key));
					flag = false;
				} catch (Exception io) {

					flag = true;
				}

				if (flag == false)
					return false;
				try {
					String Date1 = htblColNameMin.get(key);
					Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(Date1);
					flag = false;
				} catch (Exception m) {

					flag = true;

				}
				if (flag == false)
					return false;
				try {
					String Date2 = htblColNameMax.get(key);
					Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(Date2);
					flag = false;
				} catch (Exception m) {

					flag = true;
				}
				if (flag == false)
					return false;
				try {
					String minimum = htblColNameMin.get(key);
					double d1 = Double.parseDouble(minimum);
					flag = false;
				} catch (Exception o) {

					flag = true;
				}
				if (flag == false)
					return false;
				try {
					String maximum = htblColNameMax.get(key);
					double d2 = Double.parseDouble(maximum);
					flag = false;
				} catch (Exception o) {

					flag = true;
				}
				if (flag == false)
					return false;

			}

		}

		return true;
	}

	public static String getPK(String strTableName) {
		String results;
		BufferedReader br;
		try {

			br = new BufferedReader(new FileReader("resources/metadata.csv"));
			String line = br.readLine();

			while (line != null) {

				String[] content = line.split(",");
				if (content.length == 8) {
					if (content[0].equals(strTableName) && content[3].equals(" True")) {
						results = content[1].substring(1);
						br.close();
						return results;

					}
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	// checks if table contents matches the assigned data type
	// check max and min
	public static boolean tableContents(String strTableName, Hashtable<String, Object> htblColNameValue) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("resources/metadata.csv"));
			String line = br.readLine();

			while (line != null) {
				String[] content = line.split(",");
				if (content.length == 8) {
					if (content[0].equals(strTableName)) {
						String colName = content[1].substring(1);
						if (htblColNameValue.containsKey(colName)) {
							String enteredType = htblColNameValue.get(colName).getClass().getName();
							String datatype = content[2].substring(1);
							if (enteredType.equals(datatype)) {

								if (datatype.equals("java.lang.Integer")) {
									int min = Integer.parseInt(content[6].substring(1));
									int max = Integer.parseInt(content[7].substring(1));
									int data = (int) htblColNameValue.get(colName);

									if (data > max) {

										br.close();
										return false;
									} else {
										if (data < min) {

											br.close();
											return false;

										}
									}

								}
								if (datatype.equals("java.lang.String")) {

									String min = content[6].substring(1);
									String max = content[7].substring(1);
									String data = (String) htblColNameValue.get(colName);
									min = min.toLowerCase();
									max = max.toLowerCase();
									data = data.toLowerCase();
									if (data.compareTo(min) < 0) {

										br.close();
										return false;
									} else {
										if (data.compareTo(max) > 0) {

											br.close();
											return false;
										}
									}
								}
								if (datatype.equals("java.lang.Double")) {
									double min = Double.parseDouble(content[6].substring(1));
									double max = Double.parseDouble(content[7].substring(1));
									double data = (Double) htblColNameValue.get(colName);
									if (data > max) {

										br.close();
										return false;
									} else {
										if (data < min) {

											br.close();
											return false;

										}
									}

								}
								if (datatype.equals("java.util.Date")) {
									try {

										Date min = new SimpleDateFormat("yyyy-MM-dd").parse(content[6].substring(1));
										Date max = new SimpleDateFormat("yyyy-MM-dd").parse(content[7].substring(1));
										Date data = (Date) htblColNameValue.get(colName);

										if (data.compareTo(min) < 0) {

											br.close();
											return false;
										} else {
											if (data.compareTo(max) > 0) {

												br.close();
												return false;
											}
										}

									} catch (ParseException e1) {

										// TODO Auto-generated catch block
										e1.printStackTrace();
									}

								}

							} else {
								br.close();
								return false;
							}

						}
					}
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	// if one > two return 1 else -1 if less than and 0 if equal

	public static int CompareMinMax(Object one, Object two) {

		if ((one.getClass().getName()).equals("java.lang.Integer")) {
			// System.out.println(two+"hereee");
			int data = (int) one;
			int data2 = (int) two;

			if (data > data2) {

				return 1;
			} else {
				if (data < data2) {

					return -1;

				} else
					return 0;
			}

		} else {
			if ((one.getClass().getName()).equals("java.lang.String")) {

				String data = (String) one;
				String data2 = (String) two;
				data = data.toLowerCase();
				data2 = data2.toLowerCase();

				if (data.compareTo(data2) < 0) {

					return -1;
				} else {
					if (data.compareTo(data2) > 0) {

						return 1;
					} else
						return 0;
				}
			}

			else {
				if ((one.getClass().getName()).equals("java.lang.Double")) {

					double data = (Double) one;
					double data2 = (Double) two;

					if (data > data2) {

						return 1;
					} else {
						if (data < data2) {

							return -1;

						} else
							return 0;
					}

				} else {

					Date data = (Date) one;
					Date data2 = (Date) two;

					if (data.compareTo(data2) < 0) {

						return -1;
					} else {
						if (data.compareTo(data2) > 0) {

							return 1;
						} else
							return 0;
					}

				}
			}
		}

	}

	public Table loadTable(String path) {
		Table t = null;
		FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			t = (Table) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return t;
	}

	private void createPagePath(Page page, String path, int id) {
		String pageName = path + "/page" + id + ".class";
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(pageName);

			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(page);
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

	public Page loadPage(String path) {
		Page p = null;
		FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			p = (Page) in.readObject();
			in.close();
			fileIn.close();
			return p;
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;

	}

	public static Octree loadOctree(String path) {
		Octree t = null;
		FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			t = (Octree) in.readObject();
			in.close();
			fileIn.close();
			return t;
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;

	}

	public static Node loadNode(String path) {
		Node t = null;
		FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			t = (Node) in.readObject();
			in.close();
			fileIn.close();
			return t;
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;

	}

	public static void saveTable(Object o, String path) {
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(o);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void savePage(Object o, String path) {
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(o);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveOctreeORNode(Object o, String path) {
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(o);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void insertIntoTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {

		if (tableNamesinDB().contains(strTableName)) {
			String PK = getPK(strTableName);

			if (htblColNameValue.containsKey(PK)) {
				String tablepath = "resources/" + "data/" + strTableName + "/tableInfo.class";
				String directoryPath = "resources/" + "data/" + strTableName;
				Table t = loadTable(tablepath);
				if (PrimaryKeyisTaken(PK, htblColNameValue, t)) {
					throw new DBAppException("Primary key is not unique");
				}
				Enumeration<String> e = htblColNameValue.keys();
				while (e.hasMoreElements()) {
					String key = e.nextElement();
					if (!t.Columns.contains(key))
						throw new DBAppException("You added a coloumn that is not in the table");
				}
				if (!tableContents(strTableName, htblColNameValue)) {
					throw new DBAppException("Coloumn content is incorrect (invalid data type or out of range");
				}
				for (int j = 0; j < t.Columns.size(); j++) { // check that all coloumns has inserted values if not
																// (insert null)
					if (htblColNameValue.get(t.Columns.get(j)) == null)
						throw new DBAppException("You didn't provide a value for coloumn " + t.Columns.get(j));
				}
				int numOfPages = t.pageArr.size();

				if (numOfPages == 0) {

					createPageAndInsert(t, directoryPath, htblColNameValue, strTableName, PK);
					saveTable(t, tablepath);
					return;

				} else {

					int index = 0;
					while (index < t.pageArr.size()) {

						if (CompareMinMax(htblColNameValue.get(PK), t.pageMax.get(index)) == 1) {

							if (t.numberOfRows.get(index) < MaximumRowsCountinTablePage) {

								if (index == t.pageArr.size() - 1) {
									insertIntoPage(t.pageArr.get(index), directoryPath, htblColNameValue, t, index,
											strTableName, PK);
									insertintoOctree(t, strTableName, htblColNameValue, t.pageArr.get(index), PK);
									saveTable(t, tablepath);
									return;
								} else if (CompareMinMax(htblColNameValue.get(PK), t.pageMin.get(index + 1)) == -1) {
									insertIntoPage(t.pageArr.get(index), directoryPath, htblColNameValue, t, index,
											strTableName, PK);
									insertintoOctree(t, strTableName, htblColNameValue, t.pageArr.get(index), PK);
									saveTable(t, tablepath);
									return;
								}

							} else {
								if (index == t.pageArr.size() - 1) {
									createPageAndInsert(t, directoryPath, htblColNameValue, strTableName, PK);// insert
																												// in
																												// octree
																												// in
																												// it
									saveTable(t, tablepath);
									return;
								}
							}
						} else {
							insertIntoPage(t.pageArr.get(index), directoryPath, htblColNameValue, t, index,
									strTableName, PK);
							insertintoOctree(t, strTableName, htblColNameValue, t.pageArr.get(index), PK);
							saveTable(t, tablepath);
							return;
						}

						index++;
					}

				}
				t = null;

			} else
				throw new DBAppException("Primary key was not initialized");

		} else
			throw new DBAppException("Table does not exist");

	}

	private int binary_search(Vector<Hashtable<String, Object>> rows, int first, int last, Hashtable<String, Object> x,
			String PK) {
		int mid;
		while (first < last) {
			mid = (first + last) / 2;
			if (rows.get(mid).get(PK) == x.get(PK))
				return mid;
			else if (CompareMinMax(rows.get(mid).get(PK), x.get(PK)) == 1)
				last = mid - 1;
			else
				first = mid + 1;
		}
		if (first == rows.size())
			return rows.size();
		else if (CompareMinMax(rows.get(first).get(PK), x.get(PK)) == 1)
			return first;
		else
			return first + 1;

	}

	private void createPageAndInsert(Table t, String directoryPath, Hashtable<String, Object> htblColNameValue,
			String strTableName, String PKname) {
		Page p = new Page();
		t.pageID++;
		createPagePath(p, directoryPath, t.pageID);
		String pagePath = directoryPath + "/page" + t.pageID + ".class";
		t.pageArr.add(pagePath);
		t.numberOfRows.add(0);
		t.pageMax.add(0);
		t.pageMin.add(0);
		insertIntoPage(pagePath, directoryPath, htblColNameValue, t, t.pageArr.size() - 1, strTableName, PKname);
		insertintoOctree(t, strTableName, htblColNameValue, pagePath, PKname);

	}

	private void insertIntoPage(String pagePath, String directoryPath, Hashtable<String, Object> htblColNameValue,
			Table t, int index, String strTableName, String PKname) {

		Page PageAccessed = loadPage(pagePath);

		if (PageAccessed.rows.size() == MaximumRowsCountinTablePage) { // page full?
			Hashtable<String, Object> temp = PageAccessed.rows.remove(PageAccessed.rows.size() - 1);

			int o = t.numberOfRows.get(index) - 1;
			t.numberOfRows.remove(index);
			t.numberOfRows.add(index, o);
			insertLastintoNextPage(temp, t, directoryPath, index + 1, strTableName, PKname);
		}
		int b = binary_search(PageAccessed.rows, 0, PageAccessed.rows.size() - 1, htblColNameValue,
				t.strClusteringKeyColumn);

		PageAccessed.rows.add(b, htblColNameValue);

		int put = t.numberOfRows.get(index) + 1;

		t.numberOfRows.remove(index);
		t.numberOfRows.add(index, put);

		Hashtable<String, Object> h2 = PageAccessed.rows.get(PageAccessed.rows.size() - 1);
		t.pageMax.remove(index);
		t.pageMax.add(index, h2.get(t.strClusteringKeyColumn));

		Hashtable<String, Object> h3 = PageAccessed.rows.get(0);
		t.pageMin.remove(index);
		t.pageMin.add(index, h3.get(t.strClusteringKeyColumn));

		savePage(PageAccessed, t.pageArr.get(index));
		PageAccessed = null;
		System.gc();
	}

	private void insertintoOctree(Table t, String strTableName, Hashtable<String, Object> htblColNameValue, String path,
			String PKname) {
		for (int n = 0; n < t.indices.size(); n++) {
			Octree octree = loadOctree(
					"resources/" + "data/" + strTableName + "/indices/" + t.indicesName.get(n) + "/Octree.class");
			Object first = htblColNameValue.get(t.indices.get(n).get(0));
			Object second = htblColNameValue.get(t.indices.get(n).get(1));
			Object third = htblColNameValue.get(t.indices.get(n).get(2));
			Object PK = htblColNameValue.get(PKname);
			octree.insert(first, second, third, path, PK);
			saveOctreeORNode(octree,
					"resources/" + "data/" + strTableName + "/indices/" + t.indicesName.get(n) + "/Octree.class");

		}

	}

	private boolean PrimaryKeyisTaken(String PK, Hashtable<String, Object> htblColNameValue, Table t) {

		Page PageSuspected = null;
		Object valueOfPrimaryKey = htblColNameValue.get(PK);

		for (int i = 0; i < t.pageArr.size(); i++) {
			if (CompareMinMax(valueOfPrimaryKey, t.pageMin.get(i)) != -1
					&& CompareMinMax(valueOfPrimaryKey, t.pageMax.get(i)) != 1) {
				PageSuspected = loadPage(t.pageArr.get(i));
				break;

			}
		}
		if (PageSuspected == null) {
			PageSuspected = null;
			System.gc();
			return false;
		} else if (binarySearch2(PageSuspected.rows, 0, PageSuspected.rows.size() - 1, valueOfPrimaryKey, PK) != -1) {
			PageSuspected = null;
			System.gc();
			return true;
		}
		PageSuspected = null;
		System.gc();
		return false;

	}

	void insertLastintoNextPage(Hashtable<String, Object> temp, Table t, String directoryPath, int index,
			String strTableName, String PKname) {

		if (t.pageArr.size() == index) {

			createPageAndInsert(t, directoryPath, temp, strTableName, PKname);
			for (int n = 0; n < t.indices.size(); n++) {
				Octree octree = loadOctree(
						"resources/" + "data/" + strTableName + "/indices/" + t.indicesName.get(n) + "/Octree.class");
				Object first = temp.get(t.indices.get(n).get(0));
				Object second = temp.get(t.indices.get(n).get(1));
				Object third = temp.get(t.indices.get(n).get(2));
				Object PK = temp.get(t.strClusteringKeyColumn);
				octree.updatePath(first, second, third, t.pageArr.get(index), PK);

			}
		} else {
			Page P = loadPage(t.pageArr.get(index));
			if (t.numberOfRows.get(index) == MaximumRowsCountinTablePage) {
				Hashtable<String, Object> temp2 = P.rows.remove(P.rows.size() - 1);

				P.rows.add(0, temp);
				Hashtable<String, Object> h2 = P.rows.get(P.rows.size() - 1);
				t.pageMax.remove(index);
				t.pageMax.add(index, h2.get(t.strClusteringKeyColumn));
				Hashtable<String, Object> h3 = P.rows.get(0);
				t.pageMin.remove(index);
				t.pageMin.add(index, h3.get(t.strClusteringKeyColumn));
				savePage(P, t.pageArr.get(index)); // zyada
				int newI = index + 1;
				for (int n = 0; n < t.indices.size(); n++) {
					Octree octree = loadOctree("resources/" + "data/" + strTableName + "/indices/"
							+ t.indicesName.get(n) + "/Octree.class");
					Object first = temp.get(t.indices.get(n).get(0));
					Object second = temp.get(t.indices.get(n).get(1));
					Object third = temp.get(t.indices.get(n).get(2));
					Object PK = temp.get(t.strClusteringKeyColumn);
					octree.updatePath(first, second, third, t.pageArr.get(index), PK);

				}
				insertLastintoNextPage(temp2, t, directoryPath, newI, strTableName, PKname);

			} else {

				P.rows.add(0, temp);
				int put = t.numberOfRows.get(index) + 1;
				t.numberOfRows.remove(index);
				t.numberOfRows.add(index, put);
				Hashtable<String, Object> h2 = P.rows.get(P.rows.size() - 1);
				t.pageMax.remove(index);
				t.pageMax.add(index, h2.get(t.strClusteringKeyColumn));
				Hashtable<String, Object> h3 = P.rows.get(0);
				t.pageMin.remove(index);
				t.pageMin.add(index, h3.get(t.strClusteringKeyColumn));
				insertintoOctree(t, strTableName, temp, t.pageArr.get(index), PKname);
				for (int n = 0; n < t.indices.size(); n++) {
					Octree octree = loadOctree("resources/" + "data/" + strTableName + "/indices/"
							+ t.indicesName.get(n) + "/Octree.class");
					Object first = temp.get(t.indices.get(n).get(0));
					Object second = temp.get(t.indices.get(n).get(1));
					Object third = temp.get(t.indices.get(n).get(2));
					Object PK = temp.get(t.strClusteringKeyColumn);
					octree.updatePath(first, second, third, t.pageArr.get(index), PK);

				}
				savePage(P, t.pageArr.get(index));

			}
			P = null;
			System.gc();
		}

	}

	public static String getPKtype(String strTableName) {

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("resources/metadata.csv"));
			String line = br.readLine();

			while (line != null) {
				String[] content = line.split(",");
				if (content.length == 8) {
					if (strTableName.equals(content[0]) && content[3].equals(" True")) {
						br.close();
						return content[2].substring(1);
					}
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	public void updateTable(String strTableName, String strClusteringKeyValue,
			Hashtable<String, Object> htblColNameValue) throws DBAppException {

		if (tableNamesinDB().contains(strTableName)) {
			String tablepath = "resources/" + "data/" + strTableName + "/tableInfo.class";
			String directoryPath = "resources/" + "data/" + strTableName;
			Table t = loadTable(tablepath);
			Enumeration<String> e1 = htblColNameValue.keys();
			while (e1.hasMoreElements()) {
				String key = e1.nextElement();
				if (!t.Columns.contains(key))
					throw new DBAppException("You added a coloumn that is not in the table");
			}

			if (tableContents(strTableName, htblColNameValue)) {
				String PK = getPKtype(strTableName);
				String PKname = getPK(strTableName);
				if (!htblColNameValue.containsKey(PKname)) {
					Object key = null;
					if (PK.equals("java.lang.Integer")) {
						key = Integer.parseInt(strClusteringKeyValue);

					} else {
						if (PK.equals("java.lang.String")) {
							key = strClusteringKeyValue;

						} else {
							if (PK.equals("java.lang.Double")) {
								key = Double.parseDouble(strClusteringKeyValue);

							} else {
								try {
									key = new SimpleDateFormat("YYYY-MM-DD").parse(strClusteringKeyValue);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}
					}
					// index
					if (containCol(t.indices, t.strClusteringKeyColumn) != -1) {
						int OctreeIndex = containCol(t.indices, t.strClusteringKeyColumn);
						Octree octree = loadOctree(
								"resources/" + "data/" + strTableName + "/indices/" + OctreeIndex + "/Octree.class");
						String path = octree.findOne(key, PKname).get(0);// as it is a vectr but will return only one
																			// path(clustering key)
						Page PageAccessed = loadPage(path);
						int b = binarySearch2(PageAccessed.rows, 0, (PageAccessed.rows.size()) - 1, key, PKname);
						Hashtable<String, Object> hash = PageAccessed.rows.get(b); // tuple to be changed
						Enumeration<String> e = htblColNameValue.keys(); // what to be updated colomn name
						Hashtable<String, Object> originalValues = new Hashtable<String, Object>();
						while (e.hasMoreElements()) {
							String Tempkey = e.nextElement();
							originalValues.put(Tempkey, PageAccessed.rows.get(b).get(Tempkey));
							hash.replace(Tempkey, htblColNameValue.get(Tempkey));
						}
						/// here
						while (e.hasMoreElements()) { // if values to be changed have index on it
							String key1 = e.nextElement();
							if (containCol(t.indices, key1) != -1) {
								updateValueOfOctree(t, strTableName, htblColNameValue, key, originalValues,path);
								break;
							}
						}
						savePage(PageAccessed, path);
						PageAccessed = null;
						saveTable(t, tablepath);
						t = null;
						return;
					}

					int L = 0;
					int R = t.pageArr.size() - 1;
					while (L <= R) {

						int m = L + (R - L) / 2;

						if (t.numberOfRows.get(m) > 1) {

							if (CompareMinMax(key, t.pageMin.get(m)) != -1
									&& CompareMinMax(key, t.pageMax.get(m)) != 1) {

								String pathNeeded = t.pageArr.get(m);
								Page PageAccessed = loadPage(pathNeeded);
								int b = binarySearch2(PageAccessed.rows, 0, (PageAccessed.rows.size()) - 1, key,
										PKname);

								if (b != -1) {
									Hashtable<String, Object> hash = PageAccessed.rows.get(b); // tuple to be changed
									Enumeration<String> e = htblColNameValue.keys(); // what to be updated colomn name
									Hashtable<String, Object> originalValues = new Hashtable<String, Object>();
									while (e.hasMoreElements()) {
										String Tempkey = e.nextElement();
										originalValues.put(Tempkey, PageAccessed.rows.get(b).get(Tempkey));
										hash.replace(Tempkey, htblColNameValue.get(Tempkey));

									}
									Enumeration<String> e3 = htblColNameValue.keys();
									// here
									while (e3.hasMoreElements()) { // if values to be changed have index on it
										String key1 = e3.nextElement();
										if (containCol(t.indices, key1) != -1) {
											updateValueOfOctree(t, strTableName, htblColNameValue, key, originalValues,pathNeeded);
											break;
										}
									}
									savePage(PageAccessed, t.pageArr.get(m));
									saveTable(t, tablepath);

								} else {
									// throw new DBAppException("tuple does not exist");
								}
								PageAccessed = null;
								t = null;
								System.gc();
								return;

							}
						} else {
							String pathNeeded = t.pageArr.get(m);
							Page PageAccessed = loadPage(pathNeeded);

							if (CompareMinMax((PageAccessed.rows.get(0).get(PKname)), key) == 0) {
								Hashtable<String, Object> hash = PageAccessed.rows.get(0); // tuple to be changed
								Enumeration<String> e = htblColNameValue.keys(); // what to be updated colomn name
								Hashtable<String, Object> originalValues = new Hashtable<String, Object>();
								while (e.hasMoreElements()) {
									String Tempkey = e.nextElement();
									originalValues.put(Tempkey, PageAccessed.rows.get(0).get(Tempkey));
									hash.replace(Tempkey, htblColNameValue.get(Tempkey));

								}
								Enumeration<String> e4 = htblColNameValue.keys();
								// here
								while (e4.hasMoreElements()) {
									String key1 = e.nextElement();
									if (containCol(t.indices, key1) != -1) {
										updateValueOfOctree(t, strTableName, htblColNameValue, key, originalValues,pathNeeded);
										break;
									}
								}
								savePage(PageAccessed, t.pageArr.get(m));
								saveTable(t, tablepath);
								PageAccessed = null;
								t = null;
								System.gc();
								return;
							}
						}
						if (CompareMinMax(key, t.pageMax.get(m)) == 1) {
							L = m + 1;
						} else {
							R = m - 1;
						}

					}
				} else {
					throw new DBAppException("Can't update primary key");
				}

			} else {
				throw new DBAppException("Incorrect data type or value out of range");
			}
			t = null;

		} else {
			throw new DBAppException("Table does not exist");
		}

	}

	private void updateValueOfOctree(Table t, String strTableName, Hashtable<String, Object> htblColNameValue,
			Object key, Hashtable<String, Object> originalValues ,String path) {
		Enumeration<String> e = htblColNameValue.keys(); // what to be updated colomn name
		ArrayList<String> tempIndices = new ArrayList<String>();
		while (e.hasMoreElements()) { // if values to be changed have index on it
			String key1 = e.nextElement();
			if (containCol(t.indices, key1) != -1)
				tempIndices.add(key1);
		}
		Object x = null;
		Object y = null;
		Object z = null;
		Object oldx = null;
		Object oldy = null;
		Object oldz = null;

		for (int i = 0; i < t.indices.size(); i++) {
			for (int n = 0; n < tempIndices.size(); n++) {
				if (t.indices.get(i).get(0).equals(tempIndices.get(n))) {
					x = htblColNameValue.get(t.indices.get(i).get(0));
					oldx = originalValues.get(t.indices.get(i).get(0));
				} else if (t.indices.get(i).get(1).equals(tempIndices.get(n))) {
					y = htblColNameValue.get(t.indices.get(i).get(1));
					oldy = originalValues.get(t.indices.get(i).get(1));
				} else if (t.indices.get(i).get(2).equals(tempIndices.get(n))) {
					z = htblColNameValue.get(t.indices.get(i).get(2));
					oldz = originalValues.get(t.indices.get(i).get(2));

				}

			}
			if (x != null || y != null || z != null) {
				Octree octree = loadOctree(
						"resources/" + "data/" + strTableName + "/indices/" + t.indicesName.get(i) + "/Octree.class");
				octree.updateValue(octree,x, y, z, oldx, oldy, oldz, key,path);
				saveOctreeORNode(octree,
						"resources/" + "data/" + strTableName + "/indices/" + t.indicesName.get(i) + "/Octree.class");
				octree = null;
			}

		}
	}

	private int containCol(Vector<Vector<String>> indices, String strClusteringKeyColumn) { // returns index of vector
																							// of indices
		for (int i = 0; i < indices.size(); i++)
			for (int j = 0; j < 3; j++) {
				System.out.println(indices.get(i).get(j)+ "-----" +strClusteringKeyColumn );
				if (indices.get(i).get(j).equals(strClusteringKeyColumn))
					return i;
			}
		return -1;
	}

	public int binarySearch2(Vector<Hashtable<String, Object>> rows, int first, int last, Object key, String PKname) {
		int mid = (first + last) / 2;
		while (first <= last) {
			if (CompareMinMax(rows.get(mid).get(PKname), key) == -1) {
				first = mid + 1;
			} else if (CompareMinMax(rows.get(mid).get(PKname), key) == 0) {
				return mid;

			} else {
				last = mid - 1;
			}
			mid = (first + last) / 2;
		}
		if (first > last) {
			return -1;

		}
		return -1;
	}

	public void deleteFromTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {
		if (tableNamesinDB().contains(strTableName)) {
			if (tableContents(strTableName, htblColNameValue)) {
				String tablepath = "resources/" + "data/" + strTableName + "/tableInfo.class";
				String directoryPath = "resources/" + "data/" + strTableName;
				Table t = loadTable(tablepath);
				Enumeration<String> e1 = htblColNameValue.keys();
				while (e1.hasMoreElements()) {
					String key = e1.nextElement();
					if (!t.Columns.contains(key))
						throw new DBAppException("You added a coloumn that is not in the table");
				}
				Vector<Vector<String>> indexToUse = new Vector<Vector<String>>();
				boolean useIndex = false;
				Hashtable<String, Object> indicesTodelete = new Hashtable<>();
				for (int k = 0; k < t.indices.size(); k++) {
					int count = 0;
					for (int m = 0; m < 3; m++) {
						if (htblColNameValue.containsKey(t.indices.get(k).get(m))) {
							count++;
						}
					}
					if (count > 0) {
						useIndex = true;
						indexToUse.add(t.indices.get(k));
						for (int m = 0; m < 3; m++) {
							String key = t.indices.get(k).get(m);
							Object value = htblColNameValue.get(key);
							if (value != null) {
								if (m == 0) {
									indicesTodelete.put(key + "X", value);
								}
								if (m == 1) {
									indicesTodelete.put(key + "Y", value);
								}
								if (m == 2) {
									indicesTodelete.put(key + "Z", value);
								}
							}

						}
					}
				}

				if (useIndex) {
					Vector<String> pagesToLookIn = new Vector<String>();
					for (int s = 0; s < indexToUse.size(); s++) {

						String pathToTree = "resources/" + "data/" + strTableName + "/indices/"
								+ indexToUse.get(s).get(0) + indexToUse.get(s).get(1) + indexToUse.get(s).get(2)
								+ "/Octree.class";
						Octree octree = (Octree) loadOctree(pathToTree);
						Object x = null;
						Object y = null;
						Object z = null;
						for (int m = 0; m < 3; m++) {
							String key = indexToUse.get(s).get(m);
							if (m == 0) {
								x = indicesTodelete.get(key + "X");
							}
							if (m == 1) {

								y = indicesTodelete.get(key + "Y");
							}
							if (m == 2) {

								z = indicesTodelete.get(key + "Z");
							}
						}

						// find intersection
						pagesToLookIn = pagesToLookIn(pagesToLookIn, x, y, z, octree, indexToUse, s);
						octree = null;

					}

					for (int i = 0; i < pagesToLookIn.size(); i++) {
						String pagePath = pagesToLookIn.get(i);
						Page p = loadPage(pagePath);
						int indexIntable = t.pageArr.indexOf(pagesToLookIn.get(i));
						for (int j = 0; j < p.rows.size(); j++) // vector rows inside page
						{
							Enumeration<String> e = p.rows.get(j).keys(); // hashtable tuple
							boolean found = true;
							while (e.hasMoreElements()) {
								String key = e.nextElement();
								Enumeration<String> conditions = htblColNameValue.keys();
								while (conditions.hasMoreElements()) {

									String key1 = conditions.nextElement();
									if (((String.valueOf(p.rows.get(j).get(key1))).equals("null"))
											|| (key.equals(key1) && (CompareMinMax(htblColNameValue.get(key1),
													p.rows.get(j).get(key)) != 0))) {
										found = false;

									}

								}

							}

							if (found) {
								removeFromOctrees(t, p.rows.get(j));
								p.rows.remove(j); // removed tuple
								j--;
								int newV = t.numberOfRows.get(indexIntable) - 1;
								t.numberOfRows.remove(indexIntable);
								t.numberOfRows.insertElementAt(newV, indexIntable);
								savePage(p, pagePath);

							}

							if (p.isEmpty()) {
								File f = new File(pagePath);
								f.delete();
								t.pageArr.remove(indexIntable);
								t.numberOfRows.remove(indexIntable);
								t.pageMin.remove(indexIntable);
								t.pageMax.remove(indexIntable);
								saveTable(t, tablepath);
								System.gc();
								break;

							} else {
								String PK = getPK(strTableName);
								Object min = p.rows.get(0).get(PK);
								Object max = p.rows.get(p.rows.size() - 1).get(PK);
								t.pageMin.remove(indexIntable);
								t.pageMax.remove(indexIntable);
								t.pageMin.insertElementAt(min, indexIntable);
								t.pageMax.insertElementAt(max, indexIntable);
								saveTable(t, tablepath);
							}
							found = true;

						}
						p = null;

					}

				} else {

					for (int i = 0; i < t.pageArr.size(); i++) { // pages

						String pagePath = t.pageArr.get(i);
						Page p = loadPage(pagePath);
						if (htblColNameValue.containsKey(t.strClusteringKeyColumn)) {
							int b = binarySearch2(p.rows, 0, (p.rows.size()) - 1,
									htblColNameValue.get(t.strClusteringKeyColumn), t.strClusteringKeyColumn);

							if (b != -1) {
								for (int k = 0; k < t.Columns.size(); k++) {
									String colName = t.Columns.get(k);
									if (htblColNameValue.containsKey(colName))
										if (((String.valueOf(p.rows.get(b).get(colName))).equals("null"))
												|| CompareMinMax(p.rows.get(b).get(colName),
														htblColNameValue.get(colName)) != 0)
											return;
								}
								removeFromOctrees(t, p.rows.get(b));
								p.rows.remove(b); // removed tuple
								int newV = t.numberOfRows.get(i) - 1;
								t.numberOfRows.remove(i);
								t.numberOfRows.insertElementAt(newV, i);
								savePage(p, pagePath);
								if (t.numberOfRows.get(i) == 0) {
									File f = new File(pagePath);
									f.delete();
									t.pageArr.remove(i);
									t.numberOfRows.remove(i);
									t.pageMin.remove(i);
									t.pageMax.remove(i);
									i--;
									saveTable(t, tablepath);
									System.gc();

								} else {
									String PK = getPK(strTableName);

									Object min = p.rows.get(0).get(PK);
									Object max = p.rows.get(p.rows.size() - 1).get(PK);
									t.pageMin.remove(i);
									t.pageMax.remove(i);
									t.pageMin.insertElementAt(min, i);
									t.pageMax.insertElementAt(max, i);
									saveTable(t, tablepath);
								}
								return;
							}
						} else {
							for (int j = 0; j < p.rows.size(); j++) // vector rows inside page
							{
								Enumeration<String> e = p.rows.get(j).keys(); // hashtable tuple
								boolean found = true;
								while (e.hasMoreElements()) {
									String key = e.nextElement();
									Enumeration<String> conditions = htblColNameValue.keys();
									while (conditions.hasMoreElements()) {
										String key1 = conditions.nextElement();
										if (((String.valueOf(p.rows.get(j).get(key1))).equals("null"))
												|| (key.equals(key1) && (CompareMinMax(htblColNameValue.get(key1),
														p.rows.get(j).get(key)) != 0))) {
											found = false;

										}

									}

								}
								if (found) {
									removeFromOctrees(t, p.rows.get(j));
									p.rows.remove(j); // removed tuple
									j--;

									int newV = t.numberOfRows.get(i) - 1;
									t.numberOfRows.remove(i);
									t.numberOfRows.insertElementAt(newV, i);
									savePage(p, pagePath);

								}

								if (p.isEmpty()) {
									File f = new File(pagePath);
									f.delete();
									t.pageArr.remove(i);
									t.numberOfRows.remove(i);
									t.pageMin.remove(i);
									t.pageMax.remove(i);
									i--;
									saveTable(t, tablepath);
									System.gc();
									break;

								} else {
									String PK = getPK(strTableName);

									Object min = p.rows.get(0).get(PK);
									Object max = p.rows.get(p.rows.size() - 1).get(PK);
									t.pageMin.remove(i);
									t.pageMax.remove(i);
									t.pageMin.insertElementAt(min, i);
									t.pageMax.insertElementAt(max, i);
									saveTable(t, tablepath);
								}
								found = true;

							}
						}

					}
				}
				t = null;

			} else {
				throw new DBAppException("Colomn content is incorrect");
			}

		} else {
			throw new DBAppException("Table does not exist");
		}

	}

	private Vector<String> pagesToLookIn(Vector<String> pagesToLookIn, Object x, Object y, Object z, Octree octree,
			Vector<Vector<String>> indexToUse, int s) {

		if (x != null && y != null && z != null) {
			if (s != 0)
				pagesToLookIn.retainAll(octree.find(x, y, z));
			else
				pagesToLookIn = octree.find(x, y, z);

		}
		if (x == null && y == null && z != null) {
			if (s != 0)
				pagesToLookIn.retainAll(octree.findOne(z, indexToUse.get(s).get(2)));
			else
				pagesToLookIn = octree.findOne(z, indexToUse.get(s).get(2));
		}
		if (x == null && y != null && z == null) {
			if (s != 0)
				pagesToLookIn.retainAll(octree.findOne(y, indexToUse.get(s).get(1)));
			else
				pagesToLookIn = octree.findOne(y, indexToUse.get(s).get(1));

		}
		if (x != null && y == null && z == null) {
			if (s != 0)
				pagesToLookIn.retainAll(octree.findOne(x, indexToUse.get(s).get(0)));
			else
				pagesToLookIn = octree.findOne(x, indexToUse.get(s).get(0));

		}
		if (x != null && y != null && z == null) {
			if (s != 0) {
				pagesToLookIn.retainAll(octree.findOne(x, indexToUse.get(s).get(0)));
				pagesToLookIn.retainAll(octree.findOne(y, indexToUse.get(s).get(1)));
			} else {
				pagesToLookIn = octree.findOne(x, indexToUse.get(s).get(0));
				pagesToLookIn.retainAll(octree.findOne(y, indexToUse.get(s).get(1)));
			}
		}
		if (x != null && y == null && z != null) {
			if (s != 0) {
				pagesToLookIn.retainAll(octree.findOne(x, indexToUse.get(s).get(0)));
				pagesToLookIn.retainAll(octree.findOne(z, indexToUse.get(s).get(2)));
			} else {
				pagesToLookIn = octree.findOne(x, indexToUse.get(s).get(0));
				pagesToLookIn.retainAll(octree.findOne(z, indexToUse.get(s).get(2)));
			}

		}
		if (x == null && y != null && z != null) {
			if (s != 0) {
				pagesToLookIn.retainAll(octree.findOne(y, indexToUse.get(s).get(1)));
				pagesToLookIn.retainAll(octree.findOne(z, indexToUse.get(s).get(2)));
			} else {
				pagesToLookIn = octree.findOne(y, indexToUse.get(s).get(1));
				pagesToLookIn.retainAll(octree.findOne(z, indexToUse.get(s).get(2)));
			}

		}

		return pagesToLookIn;
	}

	private void removeFromOctrees(Table t, Hashtable<String, Object> tuple) {
		for (int i = 0; i < t.indices.size(); i++) {
			Object x = null;
			Object y = null;
			Object z = null;
			for (int j = 0; j < 3; j++) {
				if (j == 0) {
					String key = t.indices.get(i).get(j);
					x = tuple.get(key);
				}
				if (j == 1) {
					String key = t.indices.get(i).get(j);
					y = tuple.get(key);
				}
				if (j == 2) {
					String key = t.indices.get(i).get(j);
					z = tuple.get(key);
				}

			}
			String pathToTree = "resources/" + "data/" + t.tableName + "/indices/" + t.indicesName.get(i)
					+ "/Octree.class";
			Octree octree = (Octree) loadOctree(pathToTree);
			octree.remove(x, y, z);
			saveOctreeORNode(octree, octree.pathToTree);
			octree = null;

		}

	}

	public void printPages(String strTableName) {
		String tablepath = "resources/" + "data/" + strTableName + "/tableInfo.class";
		String directoryPath = "resources/" + "data/" + strTableName;
		Table t = loadTable(tablepath);

		for (int i = 0; i < t.pageArr.size(); i++) {
			// System.out.println("here");
			Page p = loadPage(t.pageArr.get(i));
			// System.out.println(p.rows.size());
			System.out.println("Page" + (i + 1));

			for (int j = 0; j < p.rows.size(); j++) {
				Enumeration<String> e = p.rows.get(j).keys();
				System.out.println("Row" + j);
				while (e.hasMoreElements()) {
					String key = e.nextElement();
					System.out.print(p.rows.get(j).get(key) + " , ");

				}
				System.out.println();

			}
		}

	}

	public boolean CorrectSQLterm(SQLTerm x) throws DBAppException {
		boolean flag = true;
		if (!(x._strOperator == ">" || x._strOperator == ">=" || x._strOperator == "<" || x._strOperator == "<="
				|| x._strOperator == "!=" || x._strOperator == "="))
			throw new DBAppException("Operator is invalid");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("resources/metadata.csv"));
			String line = br.readLine();

			while (line != null) {
				String[] content = line.split(",");
				if (content.length == 8) {
					String colName = content[1].substring(1);

					if (content[0].equals(x._strTableName) && colName.equals(x._strColumnName)) {
						String enteredDatatype = x._objValue.getClass().getName();
						String storedDatatype = content[2].substring(1);

						if (!enteredDatatype.equals(storedDatatype))
							return false;
					}

				}

				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;

	}

	public ArrayList<Hashtable<String, Object>> resultsOfOneTerm(Table t, SQLTerm term) {
		ArrayList<Hashtable<String, Object>> results = new ArrayList<Hashtable<String, Object>>();
		String tableName = term._strTableName;
		String colName = term._strColumnName;

		String operator = term._strOperator;
		for (int i = 0; i < t.pageArr.size(); i++) {
			String pagePath = t.pageArr.get(i);
			Page p = loadPage(pagePath);
			for (int j = 0; j < p.rows.size(); j++) {
				Hashtable<String, Object> tuple = p.rows.get(j);
				if (operator.equals("=") && CompareMinMax(tuple.get(colName), term._objValue) == 0)
					results.add(tuple);
				if (operator.equals(">") && CompareMinMax(tuple.get(colName), term._objValue) == 1)
					results.add(tuple);
				if (operator.equals("<") && CompareMinMax(tuple.get(colName), term._objValue) == -1)
					results.add(tuple);
				if (operator.equals(">=") && CompareMinMax(tuple.get(colName), term._objValue) != -1)
					results.add(tuple);
				if (operator.equals("<=") && CompareMinMax(tuple.get(colName), term._objValue) != 1)
					results.add(tuple);
				if (operator.equals("!=") && CompareMinMax(tuple.get(colName), term._objValue) != 0)
					results.add(tuple);

			}
			p = null;
		}
		t = null;
		return results;

	}

	public ArrayList<Hashtable<String, Object>> TermsAnded(ArrayList<Hashtable<String, Object>> results, SQLTerm term) {

		String colName = term._strColumnName;
		String operator = term._strOperator;
		Object val = term._objValue;
		for (int j = 0; j < results.size(); j++) {

			Hashtable<String, Object> tuple = results.get(j);
			if (operator.equals("=")) {
				if (!(CompareMinMax(tuple.get(colName), val) == 0)) {
					results.remove(j);
					j--;
				}
			}
			if (operator.equals(">")) {
				if (!(CompareMinMax(tuple.get(colName), val) == 1)) {
					results.remove(j);
					j--;
				}
			}
			if (operator.equals("<")) {
				if (!(CompareMinMax(tuple.get(colName), val) == -1)) {
					results.remove(j);
					j--;
				}
			}
			if (operator.equals(">=")) {
				if (!(CompareMinMax(tuple.get(colName), val) != -1)) {
					results.remove(j);
					j--;
				}
			}
			if (operator.equals("<=")) {
				if (!(CompareMinMax(tuple.get(colName), val) != 1)) {
					results.remove(j);
					j--;
				}
			}
			if (operator.equals("!=")) {
				if (!(CompareMinMax(tuple.get(colName), val) != 0)) {

					results.remove(j);
					j--;
				}
			}

		}

		return results;

	}

	public ArrayList<Hashtable<String, Object>> TermsOR(ArrayList<Hashtable<String, Object>> results,
			ArrayList<Hashtable<String, Object>> results2) {
		for (int i = 0; i < results2.size(); i++) {
			if (results.contains(results2.get(i))) {
				results2.remove(i);
				i--;
			}

		}
		results.addAll(results2);
		return results;
	}

	public ArrayList<Hashtable<String, Object>> TermsXOR(ArrayList<Hashtable<String, Object>> results,
			ArrayList<Hashtable<String, Object>> results2, SQLTerm term) {

		ArrayList<Hashtable<String, Object>> results2temp = new ArrayList<Hashtable<String, Object>>();
		results2temp.addAll(results2);
		ArrayList<Hashtable<String, Object>> union = TermsOR(results, results2temp);

		ArrayList<Hashtable<String, Object>> intersect = new ArrayList<Hashtable<String, Object>>();

		for (int i = 0; i < results.size(); i++) {

			if (results2.contains(results.get(i))) {

				intersect.add(results.get(i));
			}
		}

		union.removeAll(intersect);

		return union;

	}

//not equal is not done yet
	public Iterator selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException {
		// if the 3 columns an octree was created on appear in sql term and they are
		// Anded together, then use octree.
		ArrayList<Hashtable<String, Object>> results = new ArrayList<Hashtable<String, Object>>();
		if (arrSQLTerms.length == 0)
			throw new DBAppException("no SQl query was enetered");
		if (arrSQLTerms.length > 1 && strarrOperators.length != (arrSQLTerms.length - 1))
			throw new DBAppException("an Operator is missing");
		ArrayList<String> tablesinDB = tableNamesinDB();
		String queryTableName = arrSQLTerms[0]._strTableName;
		for (int i = 0; i < arrSQLTerms.length; i++) {
			String tableName = arrSQLTerms[i]._strTableName;
			if (i > 0 && !tableName.equals(queryTableName))
				throw new DBAppException("Joins are not supported");
			if (!tablesinDB.contains(tableName))
				throw new DBAppException("table not in Database");
			if (!CorrectSQLterm(arrSQLTerms[i]))
				throw new DBAppException("Corresponding data type of value is incorrect");
		}
		for (int i = 0; i < strarrOperators.length; i++) {
			if (!(strarrOperators[i].equals("AND") || strarrOperators[i].equals("OR")
					|| strarrOperators[i].equals("XOR"))) {
				throw new DBAppException("Operator not suported");
			}

		}
		String tablepath = "resources/" + "data/" + arrSQLTerms[0]._strTableName + "/tableInfo.class";
		String directoryPath = "resources/" + "data/" + arrSQLTerms[0]._strTableName;
		Table t = loadTable(tablepath);
		Vector<Vector<String>> indexToUse = SelectIndex(t, arrSQLTerms, strarrOperators);
		Hashtable<String, Object> indicesToSelect = new Hashtable<>();
		Hashtable<String, String> operatorToSelect = new Hashtable<>();
		if (indexToUse.size() > 0) {

			for (int m = 0; m < arrSQLTerms.length; m++) {

				String key = arrSQLTerms[m]._strColumnName;
				indicesToSelect.put(key, arrSQLTerms[m]._objValue);
				operatorToSelect.put(key, arrSQLTerms[m]._strOperator);

			}

			Vector<String> pagesToLookIn = new Vector<String>();
			for (int k = 0; k < indexToUse.size(); k++) {
				String pathToTree = "resources/" + "data/" + arrSQLTerms[0]._strTableName + "/indices/"
						+ indexToUse.get(k).get(0) + indexToUse.get(k).get(1) + indexToUse.get(k).get(2)
						+ "/Octree.class";
				Octree octree = (Octree) loadOctree(pathToTree);
				Object x = null;
				Object y = null;
				Object z = null;

				String key1 = indexToUse.get(k).get(0);
				String operator1 = "";
				String key2 = indexToUse.get(k).get(1);
				String operator2 = "";
				String key3 = indexToUse.get(k).get(2);
				String operator3 = "";

				x = indicesToSelect.get(key1);
				operator1 = operatorToSelect.get(key1);
				y = indicesToSelect.get(key2);
				operator2 = operatorToSelect.get(key2);
				z = indicesToSelect.get(key3);
				operator3 = operatorToSelect.get(key3);
				if (operator1.equals("=") && operator2.equals("=") && operator3.equals("=")) {
					if (k == 0)
						pagesToLookIn = octree.find(x, y, z);
					else
						pagesToLookIn.retainAll(octree.find(x, y, z));
				} else {

					if (k == 0) {
						pagesToLookIn = octree.findOperator(x, indexToUse.get(k).get(0), operator1);

					} else {
						pagesToLookIn.retainAll(octree.findOperator(x, indexToUse.get(k).get(0), operator1));
					}
					pagesToLookIn.retainAll(octree.findOperator(y, indexToUse.get(k).get(1), operator2));
					pagesToLookIn.retainAll(octree.findOperator(z, indexToUse.get(k).get(2), operator3));

				}

				octree = null;
			}
			results = retriveFromPages(indicesToSelect, operatorToSelect, pagesToLookIn);

		} else {
			// looking for one coloumn
			if (arrSQLTerms.length == 1) {
				results = resultsOfOneTerm(t, arrSQLTerms[0]);
				return results.iterator();
			} else {
				results = resultsOfOneTerm(t, arrSQLTerms[0]);
				for (int i = 0; i < strarrOperators.length; i++) {
					if (strarrOperators[i].equals("AND")) {
						SQLTerm term = arrSQLTerms[i + 1];
						results = TermsAnded(results, term);
					}
					if (strarrOperators[i].equals("OR")) {
						ArrayList<Hashtable<String, Object>> results2 = resultsOfOneTerm(t, arrSQLTerms[i + 1]);
						results = TermsOR(results, results2);
					}
					if (strarrOperators[i].equals("XOR")) {
						SQLTerm term = arrSQLTerms[i + 1];
						ArrayList<Hashtable<String, Object>> results2 = resultsOfOneTerm(t, arrSQLTerms[i + 1]);
						results = TermsXOR(results, results2, term);
					}
				}
			}
		}
		t = null;
		return results.iterator();
	}

	private Vector<Vector<String>> SelectIndex(Table t, SQLTerm[] arrSQLTerms, String[] strarrOperators) {
		Vector<Vector<String>> indexToUse = new Vector<Vector<String>>();
		for (int i = 0; i < strarrOperators.length; i++) {
			if (strarrOperators[i].equals("XOR") || strarrOperators[i].equals("OR"))
				return indexToUse;
		}
		if (arrSQLTerms.length > 2) { // possibility of using octree
			Vector<String> colNamesinSQL = new Vector<String>();

			Vector<Integer> countOfEachOctreeCOl = new Vector<Integer>();
			for (int i = 0; i < t.indices.size(); i++) {
				countOfEachOctreeCOl.add(0);
			}

			for (int i = 0; i < arrSQLTerms.length; i++) {
				String colName = arrSQLTerms[i]._strColumnName;

				for (int j = 0; j < t.indices.size(); j++) {
					if (t.indices.get(j).contains(colName)) {

						if (!colNamesinSQL.contains(colName)) {
							int oldCount = countOfEachOctreeCOl.get(j);
							countOfEachOctreeCOl.remove(j);
							countOfEachOctreeCOl.add(j, ++oldCount);
						}

					}
				}
				if (!colNamesinSQL.contains(colName))
					colNamesinSQL.add(colName);
			}
			for (int i = 0; i < countOfEachOctreeCOl.size(); i++) {
				if (countOfEachOctreeCOl.get(i) == 3)
					indexToUse.add(t.indices.get(i));
			}

		}

		return indexToUse;

	}

//Assuming they are anded together
	private ArrayList<Hashtable<String, Object>> retriveFromPages(Hashtable<String, Object> indicesToSelect,
			Hashtable<String, String> operatorToSelect, Vector<String> pagesToLookIn) {
		ArrayList<Hashtable<String, Object>> results = new ArrayList<Hashtable<String, Object>>();

		for (int i = 0; i < pagesToLookIn.size(); i++) {
			Page p = (Page) loadPage(pagesToLookIn.get(i));
			for (int d = 0; d < p.rows.size(); d++) {
				boolean matches = true;
				Hashtable<String, Object> row = p.rows.get(d);
				Enumeration<String> e = indicesToSelect.keys(); // tuple
				while (e.hasMoreElements()) {
					String key = e.nextElement();
					Object XX = indicesToSelect.get(key);
					String operator = operatorToSelect.get(key);
					if ((operator.equals("=") && CompareMinMax(row.get(key), XX) != 0)
							|| (operator.equals(">") && CompareMinMax(row.get(key), XX) < 1)
							|| (operator.equals("<") && CompareMinMax(row.get(key), XX) > -1)
							|| (operator.equals(">=") && CompareMinMax(row.get(key), XX) == -1)
							|| (operator.equals("<=") && CompareMinMax(row.get(key), XX) == 1)
							|| (operator.equals("!=") && CompareMinMax(row.get(key), XX) == 0)) {
						matches = false;
					}

				}
				if (matches)
					results.add(row);
			}
			p = null;
		}
		return results;
	}
/////////PROJECT EVALUATION
	private static void  insertCoursesRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader coursesTable = new BufferedReader(new FileReader("resources/courses_table.csv"));
        String record;
        Hashtable<String, Object> row = new Hashtable<>();
        int c = limit;
        if (limit == -1) {
            c = 1;
        }
        while ((record = coursesTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");


            int year = Integer.parseInt(fields[0].trim().substring(0, 4));
            int month = Integer.parseInt(fields[0].trim().substring(5, 7));
            int day = Integer.parseInt(fields[0].trim().substring(8));

            Date dateAdded = new Date(year - 1900, month - 1, day);

            row.put("date_added", dateAdded);

            row.put("course_id", fields[1]);
            row.put("course_name", fields[2]);
            row.put("hours", Integer.parseInt(fields[3]));

            dbApp.insertIntoTable("courses", row);
            row.clear();

            if (limit != -1) {
                c--;
            }
        }

        coursesTable.close();
    }

 private static void  insertStudentRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader studentsTable = new BufferedReader(new FileReader("resources/students_table.csv"));
        String record;
        int c = limit;
        if (limit == -1) {
            c = 1;
        }

        Hashtable<String, Object> row = new Hashtable<>();
        while ((record = studentsTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");

            row.put("id", fields[0]);
            row.put("first_name", fields[1]);
            row.put("last_name", fields[2]);

            int year = Integer.parseInt(fields[3].trim().substring(0, 4));
            int month = Integer.parseInt(fields[3].trim().substring(5, 7));
            int day = Integer.parseInt(fields[3].trim().substring(8));

            Date dob = new Date(year - 1900, month - 1, day);
            row.put("dob", dob);

            double gpa = Double.parseDouble(fields[4].trim());

            row.put("gpa", gpa);

            dbApp.insertIntoTable("students", row);
            row.clear();
            if (limit != -1) {
                c--;
            }
        }
        studentsTable.close();
    }
 private static void insertTranscriptsRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader transcriptsTable = new BufferedReader(new FileReader("resources/transcripts_table.csv"));
        String record;
        Hashtable<String, Object> row = new Hashtable<>();
        int c = limit;
        if (limit == -1) {
            c = 1;
        }
        while ((record = transcriptsTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");

            row.put("gpa", Double.parseDouble(fields[0].trim()));
            row.put("student_id", fields[1].trim());
            row.put("course_name", fields[2].trim());

            String date = fields[3].trim();
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8));

            Date dateUsed = new Date(year - 1900, month - 1, day);
            row.put("date_passed", dateUsed);

            dbApp.insertIntoTable("transcripts", row);
            row.clear();

            if (limit != -1) {
                c--;
            }
        }

        transcriptsTable.close();
    }
 private static void insertPCsRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader pcsTable = new BufferedReader(new FileReader("resources/pcs_table.csv"));
        String record;
        Hashtable<String, Object> row = new Hashtable<>();
        int c = limit;
        if (limit == -1) {
            c = 1;
        }
        while ((record = pcsTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");

            row.put("pc_id", Integer.parseInt(fields[0].trim()));
            row.put("student_id", fields[1].trim());

            dbApp.insertIntoTable("pcs", row);
            row.clear();

            if (limit != -1) {
                c--;
            }
        }

        pcsTable.close();
    }
 private static void createTranscriptsTable(DBApp dbApp) throws Exception {
        // Double CK
        String tableName = "transcripts";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("gpa", "java.lang.Double");
        htblColNameType.put("student_id", "java.lang.String");
        htblColNameType.put("course_name", "java.lang.String");
        htblColNameType.put("date_passed", "java.util.Date");

        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("gpa", "0.7");
        minValues.put("student_id", "43-0000");
        minValues.put("course_name", "AAAAAA");
        minValues.put("date_passed", "1990-01-01");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("gpa", "5.0");
        maxValues.put("student_id", "99-9999");
        maxValues.put("course_name", "zzzzzz");
        maxValues.put("date_passed", "2020-12-31");

        dbApp.createTable(tableName, "gpa", htblColNameType, minValues, maxValues);
    }

    private static void createStudentTable(DBApp dbApp) throws Exception {
        // String CK
        String tableName = "students";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("id", "java.lang.String");
        htblColNameType.put("first_name", "java.lang.String");
        htblColNameType.put("last_name", "java.lang.String");
        htblColNameType.put("dob", "java.util.Date");
        htblColNameType.put("gpa", "java.lang.Double");

        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("id", "43-0000");
        minValues.put("first_name", "AAAAAA");
        minValues.put("last_name", "AAAAAA");
        minValues.put("dob", "1990-01-01");
        minValues.put("gpa", "0.7");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("id", "99-9999");
        maxValues.put("first_name", "zzzzzz");
        maxValues.put("last_name", "zzzzzz");
        maxValues.put("dob", "2000-12-31");
        maxValues.put("gpa", "5.0");

        dbApp.createTable(tableName, "id", htblColNameType, minValues, maxValues);
    }
    private static void createPCsTable(DBApp dbApp) throws Exception {
        // Integer CK
        String tableName = "pcs";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("pc_id", "java.lang.Integer");
        htblColNameType.put("student_id", "java.lang.String");


        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("pc_id", "0");
        minValues.put("student_id", "43-0000");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("pc_id", "20000");
        maxValues.put("student_id", "99-9999");

        dbApp.createTable(tableName, "pc_id", htblColNameType, minValues, maxValues);
    }
    private static void createCoursesTable(DBApp dbApp) throws Exception {
        // Date CK
        String tableName = "courses";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("date_added", "java.util.Date");
        htblColNameType.put("course_id", "java.lang.String");
        htblColNameType.put("course_name", "java.lang.String");
        htblColNameType.put("hours", "java.lang.Integer");


        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("date_added", "1901-01-01");
        minValues.put("course_id", "0000");
        minValues.put("course_name", "AAAAAA");
        minValues.put("hours", "1");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("date_added", "2020-12-31");
        maxValues.put("course_id", "9999");
        maxValues.put("course_name", "zzzzzz");
        maxValues.put("hours", "24");

        dbApp.createTable(tableName, "date_added", htblColNameType, minValues, maxValues);

    }
	
	public static void main(String[] args) throws Exception {
		DBApp db= new DBApp();
		db.init();

//
//        SQLTerm[] arrSQLTerms;
//        arrSQLTerms = new SQLTerm[1];
//        arrSQLTerms[0] = new SQLTerm("students","gpa", "!=",new Double(3.0) );
//        arrSQLTerms[0]._strTableName = "students";
//        arrSQLTerms[0]._strColumnName= "gpa";
//        arrSQLTerms[0]._strOperator = "!=";
//        arrSQLTerms[0]._objValue =row.get("first_name");

//        arrSQLTerms[1] = new SQLTerm();
//        arrSQLTerms[1]._strTableName = "students";
//        arrSQLTerms[1]._strColumnName= "gpa";
//        arrSQLTerms[1]._strOperator = "<=";
//        arrSQLTerms[1]._objValue = row.get("gpa");

//        String[]strarrOperators = new String[0];
//       Iterator x= db.selectFromTable(arrSQLTerms, strarrOperators);
       
        //strarrOperators[0] = "OR";
 //     String table = "students";
//
//        row.put("first_name", "fooooo");
//        row.put("last_name", "baaaar");
//
//        Date dob = new Date(1992 - 1900, 9 - 1, 8);
//        row.put("dob", dob);
//        row.put("gpa", 1.1);
//
//        dbApp.updateTable(table, clusteringKey, row);
     // createCoursesTable(db);
//      createPCsTable(db);
//      createTranscriptsTable(db);
//      createStudentTable(db);
//      insertPCsRecords(db,200);
//      insertTranscriptsRecords(db,200);
//      insertStudentRecords(db,200);
		
//		 String table = "students";
	        Hashtable<String, Object> row = new Hashtable();
       // row.put("id", "44-2222");
////	        
	       row.put("first_name", "Nadaaaaaaaaaa");
	        row.put("last_name", "Ahmeddddddd");
	      String table = "students";
	        Date dob = new Date(1995 - 1900, 4 - 1, 1);
	        row.put("dob", dob);
         row.put("gpa", 1.0);
//	        db.insertIntoTable(table, row);
         
//	      String table = "students";
//	      String [] colNames=new String [3];
//	      colNames[0]="first_name";
//	      colNames[1]="last_name";
//	      colNames[2]="gpa";
	//      db.createIndex(table, colNames);

//	        row.put("first_name", "Nora");
//	        row.put("last_name", "Sadek");

	       // Date dob = new Date(1992 - 1900, 9 - 1, 8);
	       // row.put("dob", dob);
	       // row.put("id", "43-7216");

	       // db.deleteFromTable(table, row);
		   //db.printPages("students");
	        db.updateTable(table,"44-2222" , row);
      
//      insertCoursesRecords(db,200);

	}
		
	

}
