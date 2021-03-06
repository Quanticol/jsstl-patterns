package eu.quatincol.jsstl.examples;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import eu.quanticol.jsstl.core.formula.Formula;
import eu.quanticol.jsstl.core.formula.Signal;
import eu.quanticol.jsstl.core.formula.SignalStatistics;
import eu.quanticol.jsstl.core.formula.jSSTLScript;
import eu.quanticol.jsstl.core.monitor.SpatialBooleanSignal;
import eu.quanticol.jsstl.core.monitor.SpatialQuantitativeSignal;
import eu.quanticol.jsstl.core.space.GraphModel;
import eu.quanticol.jsstl.core.space.Location;
import eu.quanticol.jsstl.core.io.FolderSignalReader;
import eu.quanticol.jsstl.core.io.TxtSpatialBoolSat;
import eu.quanticol.jsstl.core.io.TxtSpatialQuantSat;
import eu.quanticol.jsstl.core.io.TxtSpatialQuantSignal;
import eu.quanticol.jsstl.core.signal.BooleanSignal;
import eu.quanticol.jsstl.core.signal.QuantitativeSignal;


import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import matlabcontrol.MatlabProxyFactoryOptions.Builder;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;


public class StochParameterAnalysis {

	public static void main(String[] args) throws MatlabConnectionException,
	MatlabInvocationException, IOException {
		
		
		// %%%%%%%%%%  GRAPH  %%%%%%%%% //		
		// Designing the grid
		GraphModel graph = GraphModel.createGrid(32, 32, 1.0);
		// Computing of the distance matrix
		graph.dMcomputation();
		
		// %%%%%%%%% PROPERTY %%%%%%% //		
		// loading the formulas files
		//ScriptLoader loader  = new ScriptLoader();
		jSSTLScript script = new jSSTLPatternScript();
		// Loading the variables. That we have defined in the formulas files.
		String [] var = script.getVariables();
		
		// %%%%%%%%%%%%% Connection with Matlab %%%%%%%%%%%%/////////				
		Builder optionsBuilder = new Builder();
		optionsBuilder.setHidden(true);
		MatlabProxyFactoryOptions options = optionsBuilder.build();

		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		System.out.print("Connecting to MATLAB... ");
		MatlabProxy proxy = factory.getProxy();
		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
		System.out.println("Done!");

		proxy.eval("cd matlab;");		
		
		//// SMC
		int nprop = 1;
		int runs = 100;
		double [] params = new double[1];
		double h = 0;
		for (int k=0; k < params.length; k++){
			params[k]= h;
			h = h + 0.1;
		}
		double[][] outputmatrix = new double[params.length][runs];
		//double[][] outputmatrix = new double[nprop][runs];
		
		// Choosing the formulas that we want to verify the pattern formula. 
		String [] formulas ={"spotformation"};
		
		String text = "";
		for ( double p: params ){
			System.out.println("p: " + p);
			for ( int j=0 ; j<runs ; j++) {
				System.out.println("runs: " + j);
				/// Loading the  trajectory files.
				proxy.eval("clear all;");
				proxy.eval("close all;");
				proxy.setVariable("param", p);
				proxy.eval("traj = TuringStoch(param)");
				//proxy.eval("TuringDataStoch");
				MatlabNumericArray array = processor.getNumericArray("traj");
				double [][][][] traj = array.getRealArray4D();
				System.out.println(traj.length);
				Signal signal = new Signal( graph, var, traj);
//				/// Monitoring
				for ( int i=0 ; i< nprop; i++) {
					String phi = formulas[i];
					//SignalStatistics statistic = new SignalStatistics(graph.getNumberOfLocations());
					//statistic.add(script.quantitativeSat("pattern", new HashMap<>(), graph, signal));
					
//					double[] satB = script.booleanSat(phi, new HashMap<>(), graph, signal);
//					double[] satQ = script.quantitativeSat(phi, new HashMap<>(), graph, signal);
//					outputmatrix[i][j]= satB[0];
//					System.out.println("Satisfaction value: " + Arrays.toString(satB));
//					System.out.println("Robustness value: " + Arrays.toString(satQ));
					Formula prop = script.getFormula(phi); 
					SpatialBooleanSignal boolSign = prop.booleanCheck(null, graph, signal);
					BooleanSignal b = boolSign.spatialBoleanSignal.get(graph.getLocation(0));
					SpatialQuantitativeSignal quantSign = prop.quantitativeCheck(null, graph, signal);
					QuantitativeSignal q = quantSign.spatialQuantitativeSignal.get(graph.getLocation(0));
					//double satQ = qt.getValueAt(0);
					//outputmatrix[i][j]= satQ;
					System.out.println("all bool value: " + b.toString());
					System.out.println("all quant value: " + q.toString());
					
					text += String.format(Locale.US, " %20.10f", outputmatrix[i][j]);
				}
			}
			text += "\n";
		}
//		// Disconnect Matlab
		proxy.disconnect();	
///		PrintWriter printer = new PrintWriter("data/paramBool.txt");
		///PrintWriter printer = new PrintWriter("data/paramQuant.txt");
		///PrintWriter printer = new PrintWriter("data/stochboolsat.txt");
		//PrintWriter printer = new PrintWriter("data/stochQuantsat.txt");
//		printer.print(text);
//		printer.close();
		
//		System.out.println("Min: "+Arrays.toString(statistic.getMin()));
//		System.out.println("Max: "+Arrays.toString(statistic.getMax()));
//		System.out.println("Avg: "+Arrays.toString(statistic.getAverage()));
//		System.out.println("I_C: "+Arrays.toString(statistic.getStandardDeviation()));
		
//		double [] mean = new double[nprop];
//		for ( int i=0 ; i< nprop; i++) {
//			mean[i]=0;
//			for ( int j=0 ; j<runs ; j++) {
//				mean[i]=mean[i] + outputmatrix[i][j];				
//			}
//			mean[i]=mean[i]/runs;
//			System.out.println("Mean: " + mean[i]);
//		}
		
	}
	
}
