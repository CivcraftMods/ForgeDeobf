package com.biggestnerd.forgedeobf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ForgeDeobf {
	
	private HashMap<String, String> methods = new HashMap<String, String>();
	private HashMap<String, String> fields = new HashMap<String, String>();
	private HashMap<String, String> params = new HashMap<String, String>();
	private final File parentFolder;
	
	public static void main(String[] args) {
		File parentFolder;
		if(args.length != 0) {
			parentFolder = new File(args[0]);
		} else {
			parentFolder = new File(System.getProperty("user.dir"));
		}
		if(!parentFolder.isDirectory()) {
			System.err.println("ERROR: You entered an invalid directory, stopping before anything else goes wrong!");
			System.exit(0);
		}
		new ForgeDeobf(parentFolder).start();
	}
	
	public ForgeDeobf(File parentFolder) {
		this.parentFolder = parentFolder;
	}
	
	public void start() {
		loadMethods();
		loadFields();
		loadParams();
		for(File file : getFilesFromDirectory(parentFolder, ".java")) {
			new FileFixThread(file).start();
		}
	}
	
	private ArrayList<File> getFilesFromDirectory(File dir, String fileType) {
		ArrayList<File> files = new ArrayList<File>();
		for(File file : dir.listFiles()) {
			if(file.isDirectory()) {
				files.addAll(getFilesFromDirectory(file, fileType));
			} else if (file.getName().endsWith(fileType)) {
				files.add(file);
			}
		}
		return files;
	}
	
	private void loadMethods() {
		try {
			InputStream in = getClass().getResourceAsStream("/methods.csv");
			if(in == null) {
				URL url = new URL("https://raw.githubusercontent.com/CivcraftMods/ForgeDeobf/master/methods.csv");
				in = url.openStream();
			}
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(isr);
			String line = reader.readLine();
			while((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				methods.put(parts[0], parts[1]);
			}
			reader.close();
			System.out.println("Loaded " + methods.size() + " method obfuscation mappings");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void loadFields() {
		try {
			InputStream in = getClass().getResourceAsStream("/fields.csv");
			if(in == null) {
				URL url = new URL("https://raw.githubusercontent.com/CivcraftMods/ForgeDeobf/master/fields.csv");
				in = url.openStream();
			}
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(isr);
			String line = reader.readLine();
			while((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				fields.put(parts[0], parts[1]);
			}
			reader.close();
			System.out.println("Loaded " + fields.size() + " field obfuscation mappings");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void loadParams() {
		try {
			InputStream in = getClass().getResourceAsStream("/params.csv");
			if(in == null) {
				URL url = new URL("https://raw.githubusercontent.com/CivcraftMods/ForgeDeobf/master/params.csv");
				in = url.openStream();
			}
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(isr);
			String line = reader.readLine();
			while((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				params.put(parts[0], parts[1]);
			}
			reader.close();
			System.out.println("Loaded " + params.size() + " param obfuscation mappings");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	class FileFixThread extends Thread {
		
		private final File file;
		
		public FileFixThread(File file) {
			this.file = file;
		}
		
		public void run() {
			if(file == null || !file.exists()) {
				return;
			}
			ArrayList<String> lines = getFileLines(file);
			ArrayList<String> fixedLines = new ArrayList<String>();
			for(String line : lines) {
				String newLine = line;
				for(String key : methods.keySet()) {
					if(newLine.contains(key)) {
						newLine = newLine.replace(key, methods.get(key));
					}
				}
				for(String key : fields.keySet()) {
					if(newLine.contains(key)) {
						newLine = newLine.replace(key, fields.get(key));
					}
				}
				for(String key : params.keySet()) {
					if(newLine.contains(key)) {
						newLine = newLine.replace(key, params.get(key));
					}
				}
				fixedLines.add(newLine);
			}
			writeFixedLinesToFile(fixedLines, file);
			System.out.println("Successfully deobfuscated " + file.getName());
		}
		
		private void writeFixedLinesToFile(ArrayList<String> fixedLines, File file) {
			try {
				file.createNewFile();
				FileWriter writer = new FileWriter(file);
				for(String line : fixedLines) {
					writer.write(line);
					writer.write("\n");
				}
				writer.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		private ArrayList<String> getFileLines(File file) {
			ArrayList<String> lines = new ArrayList<String>();
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = "";
				while((line = reader.readLine()) != null) {
					lines.add(line);
				}
				reader.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return lines;
		}
	}
}
