package net.bioclipse.ds.sdk.libsvm;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import core.Server;

public class TestGenerateCommands {

	public static void main(String[] args) {

		//		-i /Users/ola/workspaces/bioclipse3/SignProject/bursi_nosalts_molsign.sdf -ap "Ames test categorisation" -c true -pa mutagen 
		//		-o /tmp/signmodel -folds 5 -hstart 0 -hend 3 -pjobs 16 -ptype slurm		

		String pathToSDFile = "~/data/chang.sdf";
		String activityProperty= "BIO";
		String outputDir = "tmp/signmodelfinal";
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

		List<List<Point>> jobs = modelbuilder.setUpJobs();
		final List<String> commands = modelbuilder.generateShellCommands(jobs);

		Server server = new Server();
		List<String> output = server.runCommands(new ArrayList<String>() {
			{
				for (String cmd : commands){
					System.out.print("Added command: " + cmd);
					add(cmd);
				}
			}
		});

		for(String s : output) {
			System.out.println("Result: "  + s);
		}

	}

}
