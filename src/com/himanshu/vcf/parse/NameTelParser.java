/*
 * Copyright 2013 Himanshu Bhardwaj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.himanshu.vcf.parse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NameTelParser {

	private String vcfDir = "C:\\Mom_Dad_Contacts\\Dad_Phone_Bkp\\contacts\\";

	public List<String> listAllVcfFiles(String directory) {
		// Creating a DirectoryStream which accepts only filenames ending with
		// '.vcf'
		List<String> fileNames = new ArrayList<>();
		Path p = FileSystems.getDefault().getPath(directory);
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(p, "*.vcf")) {
			for (Path path : ds) {
				// Iterate over the paths in the directory and print filenames
				fileNames.add(vcfDir.concat(path.getFileName().toString()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileNames;
	}
	
	public List<String> listAllVcfFiles() {
		return listAllVcfFiles(vcfDir);
	}
	
	public NameTelDTO parseVCard (String vCard) throws IOException {
		NameTelDTO dto = new NameTelDTO();
		try (BufferedReader br = new BufferedReader(new FileReader(vCard))) {
			String sCurrentLine = null;
			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
				if (sCurrentLine.contains("N;")) {
					//Parse Name Here
					int start = sCurrentLine.indexOf("ENCODING=8BIT:");
					String name = sCurrentLine.substring(start+"ENCODING=8BIT:".length(), sCurrentLine.length());
					if (name.startsWith(";")) {
						name = name.substring(1);
					}
					//System.out.println(name);
					dto.setName(name);
				}
				if (sCurrentLine.contains("TEL;")) {
					//Parse Tel Here
					int start = sCurrentLine.indexOf("ENCODING=8BIT:");
					String tel = sCurrentLine.substring(start+"ENCODING=8BIT:".length(), sCurrentLine.length());
					//Only last 10 digits is actual phone number
					if (tel.length() > 10) {
						tel = tel.substring(tel.length()-10, tel.length());
					}
					
					tel = "91".concat(tel);
					//System.out.println(tel);
					try {
						Long.parseLong(tel);
					} catch (NumberFormatException nfe) {
						System.err.println("Error parsing tel for vcard: "+vCard);
					}
					dto.setTel(tel);
				}
			}
		}
		if (dto.getName() == null || dto.getTel() == null) {
			System.err.println("Error parsing vcard: "+vCard);
		}
		return dto;
	}
	
	public static void main(String[] args) {
		NameTelParser parser = new NameTelParser();
		List<String> vcfFiles = parser.listAllVcfFiles();
		List<NameTelDTO> dtoList = new ArrayList<>();
		for (String vcfFile: vcfFiles) {
			try {
				dtoList.add(parser.parseVCard(vcfFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			writesDTOsToFile (dtoList, "C:\\Mom_Dad_Contacts\\Dad_Phone_Bkp\\contacts\\output.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void writesDTOsToFile(List<NameTelDTO> dtoList, String file) throws IOException {
		File f = new File(file);
		if (f.exists()) {
			f.delete();
		}
		if (!f.exists()) {
			f.createNewFile();
		}
		try (FileWriter writer = new FileWriter(file)) {
			for (NameTelDTO dto : dtoList) {
				writer.write(dto.getName()+","+dto.getTel()+"\n");
			}
		}
	}

}
