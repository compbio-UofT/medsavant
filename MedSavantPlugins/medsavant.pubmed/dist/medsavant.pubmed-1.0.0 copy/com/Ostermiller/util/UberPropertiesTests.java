/*
 * UberProperties regression test.
 * Copyright (C) 2003 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */
package com.Ostermiller.util;

import java.util.*;
import java.io.*;

class UberPropertiesTests {
	private final static String[] TESTS = new String[]{
		"Aone=\nAtwo= \nAthree=\t",
		"Bone=1\n Btwo = two \nBthree 3\nBfour: 4",
		"Con\\\ne=on\\\ne\nCtw \\\n o=tw \\\n o\nCth\\\n ree=th \\\nree",
		"Done=one\nDone=two\nDone=three",
		"#Comment\nname value\n!Comment\nname value\n# Comment\\nname value\n #name value\n\t \t!name value",
		"#\n# That was a comment\n\nname:value\nname=value\nname value\n name = value \n	name	=	value	\n  name  =  value  ",
		"# empty properties\nname\nname=\nname:\n	name\n  name    ",
		"# property names of length zero\n:value value\n:value\n=value\n :value\n =value\n:value : has colon\n:value ends with equal =\n:value ends with colon :",
		"name::value starts with colon\nname=:value starts with colon\nname :value starts with colon\nname:value ends with colon:\nname=value ends with colon:\nname value ends with colon:\nname:=value starts with equal\nname==value starts with equal\nname =value starts with equal\nname:value ends with equal=\nname=value ends with equal=\nname value ends with equal=\nname:!value starts with exclamation\nname=!value starts with exclamation\nname !value starts with exclamation\nname:#value starts with pound\nname=#value starts with pound\nname #value starts with pound\nname=value ends with colon :\nname=value ends with equal =",
		"@!#$%^name value!@#$%^&*(){}",
		"\n\n\n\n#comment\n\n \n\t \n ",
		"# escapes\n\\ \\=\\:name=value\\ \\=\\:\n\\u3443\\0233name value\\u3432\\0213",
		"name",
		"name ",
		"name =",
		"",
		"#comment",
		"name= ",
		"name= value",
		"name=value ",
		"name\\\nstillname value\nname\\\n  stillname value\nname\\\nstillname\\\nstillname value\nname\\\n\\\n \\\nstillname value\nname\\\n#stillname value\nname\\\n!stillname value",
		"# empty property\nname\\",
		"# empty property\nname\\\n",
		"# empty property\nname\\\n\n#comment",
		"name= \\\nvalue\nname: \\\nvalue\nname:\\\nvalue\nname=\\\nvalue\nname=\\",
	};

	public static void main(String[] args) throws Exception {
		for (int i=0; i<TESTS.length; i++){
			byte[] bytes = TESTS[i].getBytes("ISO-8859-1");
			Properties p = new Properties();
			p.load(new ByteArrayInputStream(bytes));
			UberProperties up = new UberProperties();
			up.load(new ByteArrayInputStream(bytes));
			String results = compare(up, p);
			if (results != null){
				System.err.println(results);
				System.exit(1);
			}
			CircularByteBuffer cbb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
			up.save(cbb.getOutputStream());
			cbb.getOutputStream().close();
			UberProperties up2 = new UberProperties();
			up2.load(cbb.getInputStream());
			results = compare(up, up2);
			if (results != null){
				System.err.println(results);
				System.exit(1);
			}
		}
	}

	private static String compare(UberProperties uberProps, Properties props){
		String[] upNames = uberProps.propertyNames();
		Enumeration pNamesEnum = props.propertyNames();
		ArrayList pNamesList = new ArrayList();
		while (pNamesEnum.hasMoreElements()){
			pNamesList.add(pNamesEnum.nextElement());
		}
		String[] pNames = (String[])pNamesList.toArray(new String[0]);
		if (upNames.length != pNames.length){
			return ("Number of properties do not match: Uber: " + upNames.length +  " Normal:" + pNames.length);
		}
		for (int i=0; i<pNames.length; i++){
			String upValue = uberProps.getProperty(pNames[i]);
			String pValue = props.getProperty(pNames[i]);
			if (upValue == null) {
				return "UberProperties does not contain property: '" + pNames[i] + "'";
			}
			if (!upValue.equals(pValue)){
				return ("Values for '" + pNames[i] + "' do not match:\n  '" + pValue + "'\n  '" + upValue + "'");
			}
		}
		return null;
	}

	private static String compare(UberProperties up1, UberProperties up2){
		String[] up1Names = up1.propertyNames();
		String[] up2Names = up2.propertyNames();
		if (up1Names.length != up2Names.length){
			return ("Number of properties do not match: Uber: " + up1Names.length +  " Normal:" + up2Names.length);
		}
		for (int i=0; i<up1Names.length; i++){
			String up1Value = up1.getProperty(up1Names[i]);
			String up2Value = up2.getProperty(up1Names[i]);
			if (up2Value == null) {
				return "Second does not contain property: '" + up1Names[i] + "'";
			}
			if (!up1Value.equals(up2Value)){
				return ("Values for '" + up1Names[i] + "' do not match:\n  '" + up1Value + "'\n  '" + up2Value + "'");
			}
		}
		return null;
	}
}
