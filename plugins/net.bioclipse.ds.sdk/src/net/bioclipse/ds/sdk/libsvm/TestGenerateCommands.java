package net.bioclipse.ds.sdk.libsvm;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.Server;

public class TestGenerateCommands {

	public static void main(String[] args) throws IOException {

		//		-i /Users/ola/workspaces/bioclipse3/SignProject/bursi_nosalts_molsign.sdf -ap "Ames test categorisation" -c true -pa mutagen 
		//		-o /tmp/signmodel -folds 5 -hstart 0 -hend 3 -pjobs 16 -ptype slurm		

		String pathToSDFile = "/Users/ola/data/chang.sdf";
		String activityProperty= "BIO";
		String outputDir = "/tmp/signmodelfinal";
		boolean classification = false;

		SignModel modelbuilder = new SignModel(pathToSDFile, activityProperty, outputDir, classification);

		modelbuilder.setOptimizationType("array");
		modelbuilder.setStartHeight(0);
		modelbuilder.setEndHeight(3);
		modelbuilder.setGammaStart(6);
		modelbuilder.setGammaEnd(7);
		modelbuilder.setcStart(1);
		modelbuilder.setcEnd(3);
		modelbuilder.setNoParallelJobs(8);
		modelbuilder.setParallelType("shell");
		modelbuilder.setEchoTime(false);
		modelbuilder.setJarpath("/Users/ola/repos/bioclipse.ds/plugins/net.bioclipse.ds.sdk/build");

		List<List<Point>> jobs = modelbuilder.setUpJobs();
		final List<String> commands = modelbuilder.generateShellCommands(jobs);

		boolean onlyLastLine=true;
		Server server = new Server(onlyLastLine);
		List<String> output = server.runCommands(new ArrayList<String>() {
			{
				for (String cmd : commands){
					System.out.print("Added command: " + cmd);
					add(cmd);
				}
			}
		});

		double hRMSE=0;
		double hC=0;
		double hGamma=0;
		
		for(String s : output) {
			System.out.println("Result: "  + s);
			String[] bigparts = s.split(" ");
			String[] nums = bigparts[5].split(":");
			if (hRMSE<Double.parseDouble(nums[0])){
				hRMSE=Double.parseDouble(nums[0]);
				hC=Double.parseDouble(nums[1]);
				hGamma=Double.parseDouble(nums[2]);
			}
		}
		
		System.out.println("Dedeuced optimum values for RMSE=" + hRMSE + ": c=" + hC + ", gamma=" + hGamma);
		System.out.println("Building final model...");
		
		//Build final model
		modelbuilder.setOptimizationType("none");
		modelbuilder.setTrainFinal(true);
		modelbuilder.setCfinal(hC);
		modelbuilder.setGammafinal(hGamma);
	
		modelbuilder.BuildModel();

		System.out.println("DONE!");

	}

}
