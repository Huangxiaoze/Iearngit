package mygrapheditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

public class FileManager {
	// �ļ����������
	private static FileInputStream inFileInputStream = null;
	private static FileOutputStream outFileOutputStream = null;
	// ���л������뵼��ͼ��ؼ���һ��API
	private static ObjectInputStream inObjectInputStream = null;
	private static ObjectOutputStream outObjectOutputStream = null;

	public static Vector<Layer> loadFile(String path) {
		try {

			File file = new File(path);
			inFileInputStream = new FileInputStream(file);
			inObjectInputStream = new ObjectInputStream(inFileInputStream);
			Vector<Layer> elements = (Vector<Layer>) inObjectInputStream.readObject(); // ���ļ��л�ȡ����
			inFileInputStream.close();
			inObjectInputStream.close();
			return elements;

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		return null;

	}
	
	public static void saveFile(String path, Vector<Layer> layers) {
		try {
			File file = new File(path);
			outFileOutputStream = new FileOutputStream(file);
			outObjectOutputStream = new ObjectOutputStream(outFileOutputStream); 
			outObjectOutputStream.writeObject(layers);  // ��ͼ�ζ��󵼳����ļ�
			outFileOutputStream.close();
			outObjectOutputStream.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
}
