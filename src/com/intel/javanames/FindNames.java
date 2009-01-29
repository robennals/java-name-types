package com.intel.javanames;

import java.io.BufferedReader;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GoodPack{
	String pack;
	int count;
	int total;
	float rate;
	float score;
	
	public String toString(){
		return pack + "["+count+"/"+total+"]";
	}
}

class Uses{	
	HashMap<String,Vector<NameUse>> hsh = new HashMap<String,Vector<NameUse>>();
	HashMap<String,Integer> counthsh = new HashMap<String,Integer>();
	HashMap<String,Integer> packtotals = new HashMap<String,Integer>();
	HashMap<String,Integer> packtypetotals = new HashMap<String,Integer>();
	
	
	Vector<GoodPack> getGoodPacks(NameUse nu){
		HashSet<String> all = nu.packs;
		Vector<GoodPack> out = new Vector<GoodPack>();
		for(String pack : all){
			float total = packtotals.get(nu.name+pack);
			float typetotal = packtypetotals.get(nu.name+nu.basetype+nu.typeargs+pack);
			if(total >= 4 && (typetotal / total) >= 0.74){
				GoodPack goodpack = new GoodPack();
				goodpack.pack = pack;
				goodpack.count = (int)typetotal;
				goodpack.total = (int)total;
				goodpack.rate = typetotal / total;
				goodpack.score = typetotal + (typetotal/total);
				out.add(goodpack);
			}
		}
		return out;
	}
	
	void addUse(String pack, String name, String basetype, String typeargs){
		if(typeargs == null){
			typeargs = "";
		}
		
		if(!counthsh.containsKey(name)){
			counthsh.put(name,1);
		}else{
			counthsh.put(name,counthsh.get(name)+1);
		}
		
		String packkey = name+pack;
		if(!packtotals.containsKey(packkey)){
			packtotals.put(packkey,1);
		}else{
			packtotals.put(packkey,packtotals.get(packkey)+1);
		}
	
		packkey = name+basetype+typeargs+pack;
		if(!packtypetotals.containsKey(packkey)){
			packtypetotals.put(packkey,1);
		}else{
			packtypetotals.put(packkey,packtypetotals.get(packkey)+1);
		}
	
		
		if(hsh.containsKey(name)){
			for(NameUse u : hsh.get(name)){
				if(u.name.equals(name) 
						&& u.basetype.equals(basetype)
						&& Utils.eqOrNull(u.typeargs, typeargs)){
					if(!u.packs.contains(pack)){
						u.packs.add(pack);
					}
					u.count++;
					return;
				}
			}			
		}else{
			hsh.put(name, new Vector<NameUse>());
		}
		NameUse nu = new NameUse();
		nu.packs = new HashSet<String>();
		nu.packs.add(pack);
		nu.name = name;
		nu.basetype = basetype;
		nu.typeargs = typeargs;
		nu.count = 1;
		hsh.get(name).add(nu);	
	}
}

class NameUse{
	HashSet<String> packs;		// which package
	String name;		// what variable name
	String basetype;	// what is the type
	String typeargs; 	// what the type args are
	int count; 		// how many times it appears
}

public class FindNames {

	static Pattern typepat = Pattern.compile("(\\w+)(<[\\w\\s<>,]+>)?\\s+(\\w+)");
	static Pattern packpat = Pattern.compile("package\\s+([\\w\\.]+)");	
	static Pattern strpat = Pattern.compile("\"[^\"]+\"");
	
	static int count = 0;
	
	public static boolean isKeyword(String s){
		if(s.equals("import")) return true;
		if(s.equals("public")) return true;
		if(s.equals("private")) return true;
		if(s.equals("transient")) return true;
		if(s.equals("volatile")) return true;
		if(s.equals("throws")) return true;
		if(s.equals("package")) return true;
		if(s.equals("final")) return true;
		if(s.equals("protected")) return true;
		if(s.equals("new")) return true;
		if(s.equals("extends")) return true;
		if(s.equals("throw")) return true;
		if(s.equals("new")) return true;
		if(s.equals("else")) return true;
		if(s.equals("if")) return true;
		if(s.equals("return")) return true;
		if(s.equals("instanceof")) return true;
		if(s.equals("with")) return true;
		if(s.equals("class")) return true;
		if(s.equals("interface")) return true;
		if(s.equals("interfaceof")) return true;		
		if(s.equals("implements")) return true;
		if(s.equals("static")) return true;
		if(s.equals("native")) return true;
		if(s.equals("synchronized")) return true;
		return false;
	}
	
	public static void analyseFileContents(String filename,Uses uses) throws Exception {		
		BufferedReader reader = Utils.openInFile(filename);
		String line;
		String pack = null;
		boolean incomment = false;
		while((line = reader.readLine()) != null){
			if(line.contains("/*")){
				incomment = true;
			}
			if(line.contains("*/")){
				incomment = false;
				continue;
			}
			if(incomment){
				continue;
			}
			if(line.contains("\"")) continue;
			if(line.contains("//")) continue;		// TODO: tidy up comment parsing
			
			Matcher pm = packpat.matcher(line);
			if(pm.find()){
				pack = pm.group(1);
			}
			
			Matcher m = typepat.matcher(line);
			while(m.find()){
				String basetype = m.group(1);
				String typeargs = m.group(2);
				String name = m.group(3);
				if(!isKeyword(basetype) && !isKeyword(name)){
					uses.addUse(pack, name, basetype, typeargs);
				}
			}
		}
		reader.close();
	}
	
	static int depth = 0;
	
	static void printFileName(String s){
		for(int i = 0; i < depth; i++){
			System.out.print(" ");
		}
		System.out.println(s);
	}
	
	public static void analyseFile(String filename,Uses uses) throws Exception{		
//		if(count > 100){
//			return;
//		}
		printFileName(filename);
		File file = new File(filename);
		if(file.isDirectory()){
			depth++;
			File[] children = file.listFiles();
			for(File child : children){
				analyseFile(child.getAbsolutePath(),uses);
			}
			depth--;
		}else if(filename.contains(".java")){
			count++;
			analyseFileContents(filename,uses);
		}
	}
	
	public static void printUses(final Uses uses){
		Vector<String> names = new Vector<String>(uses.counthsh.keySet());
		Collections.sort(names,new Comparator<String>(){
				public int compare(String x, String y){
					return uses.counthsh.get(x) - uses.counthsh.get(y);
				}
			}
		);
		for(String name : names){
			System.out.println("-- "+name+" : "+uses.counthsh.get(name)+" --");
			Vector<NameUse> nus = uses.hsh.get(name);
			Collections.sort(nus,new Comparator<NameUse>(){
				public int compare(NameUse o1, NameUse o2) {
					return o2.count - o1.count;
				}				
			});
			for(NameUse nu : nus){
				String args;
				if(nu.typeargs == null){
					args = "";
				}else{
					args = nu.typeargs;
				}
//				Vector<String> sortedpacks = new Vector<String>(nu.packs);
//				Collections.sort(sortedpacks);			
				Vector<GoodPack> goodpacks = uses.getGoodPacks(nu);
				Collections.sort(goodpacks,new Comparator<GoodPack>(){
					public int compare(GoodPack x, GoodPack y){
						return (int)(1000*(y.score - x.score));
					}
				});
				
				int goodtotal = 0;
				for(GoodPack gp : goodpacks){
					goodtotal+=gp.count;
				}
				
				System.out.println("    "+nu.basetype+args+":"+nu.count+"("+goodtotal+")"+" \t"+goodpacks);
			}
		}	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			System.err.println("Reading files:");
			Uses uses = new Uses();
			analyseFile(args[0],uses);
			System.out.println("-- results --");
			printUses(uses);
		}catch(Exception e){
			System.out.println(e);
		}
	}

}
