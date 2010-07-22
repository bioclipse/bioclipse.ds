package net.bioclipse.ds.report.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class TestEndpoint {

	/**
	 * Used in iReport
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List createBeanCollection () {

		// simulated collection for use in iReport
		List endpoints = new ArrayList ();


		FileInputStream fis;
		try {
			fis = new FileInputStream(new File("/Users/ola/Pictures/chemstructs/bursiPos1.png"));
			BufferedImage chemimage = ImageIO.read(fis);
			fis.close();
			fis = new FileInputStream(new File("/Users/ola/Pictures/chemstructs/bursiPos1_hi.png"));
			BufferedImage chemimageHI = ImageIO.read(fis);
			fis.close();
			fis = new FileInputStream(new File("/Users/ola/Pictures/chemstructs/no.png"));
			BufferedImage imgNO = ImageIO.read(fis);
			fis.close();
			fis = new FileInputStream(new File("/Users/ola/Pictures/chemstructs/yes.png"));
			BufferedImage imgYES= ImageIO.read(fis);
			fis.close();
//
//			DSReportModel model=new DSReportModel();
//			model.setName("WEHOOW-Adrelanile");
//			model.setStructureImage(chemimage);
//			model.setSmiles("C1CCCCC1");
//			model.setFormula("C6H3O2");
//			model.setInchi("s/12ch/df3");
//			endpoints.add(model);
			
			// Java bean populated with hardcoded row data
			Endpoint ep=new Endpoint("Mutagenicity", "POSITIVE", null, imgNO );
			endpoints.add (ep);
			Test test=new Test("AMES exact", "POSITIVE", imgNO);
			ep.addTest(test);
			Result res = new Result("12345", "POSITIVE", chemimage );
			test.addResult(res);
			res = new Result("98765-33", "POSITIVE", chemimage );
			test.addResult(res);
			test=new Test("AMES structural alerts", "POSITIVE", imgNO);
			ep.addTest(test);
			res = new Result("44-222", "POSITIVE", chemimageHI);
			test.addResult(res);

			Endpoint ep2=new Endpoint("Carcinogenicity", "NEGATIVE", null, imgYES);
			endpoints.add (ep2);
			test=new Test("CPDB exact", "NEGATIVE", imgYES);
			ep2.addTest(test);
			res = new Result("00-22", "NEGATIVE", chemimage );
			test.addResult(res);
			test=new Test("CPDB structural alerts", "POSITIVE", imgNO);
			ep2.addTest(test);
			res = new Result("33-44", "POSITIVE", chemimageHI);
			test.addResult(res);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return endpoints;
	}

}
