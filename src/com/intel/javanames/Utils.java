package com.intel.javanames;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class Utils {
	public static BufferedReader openInFile(String s) throws Exception{
		return new BufferedReader(
				new InputStreamReader(
						new FileInputStream(s)
						));
	}
	
	public static BufferedWriter openOutFile(String s) throws Exception{
		return new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(s)
						));
	}

	public static boolean eqOrNull(Object x, Object y){
		if(x == null && y == null){
			return true;
		}else if(x != null && y != null){
			return x.equals(y);
		}else{
			return false;
		}
	}
	
}
