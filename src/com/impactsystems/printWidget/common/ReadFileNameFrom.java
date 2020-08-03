package com.impactsystems.printWidget.common;

import java.io.File;

public class ReadFileNameFrom {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File folder = new File("G:/TrackWiseDocumentum/TrackwiseDocument/GirishPatilDoc/Trackwise IWS jars");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
		    System.out.println("File " + listOfFiles[i].getName());
		  } else if (listOfFiles[i].isDirectory()) {
		    System.out.println("Directory " + listOfFiles[i].getName());
		  }
		}
	}

}
