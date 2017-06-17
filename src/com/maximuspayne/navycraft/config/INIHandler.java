package com.maximuspayne.navycraft.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.maximuspayne.navycraft.NavyCraft;

public class INIHandler {

	
	public void load(File MCConfig) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(MCConfig));

			String line;
			while( (line=in.readLine() ) != null ) {
				line = line.trim();

				if( line.startsWith( "#" ) )
					continue;

				String[] split = line.split("=");

				NavyCraft.instance.configFile.ConfigSettings.put(split[0], split[1]);
			}
			in.close();
		}
		catch (IOException e) {
		}		
	}
	
	public void save(File MCConfig) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(MCConfig));

			for(Object configLine : NavyCraft.instance.configFile.ConfigSettings.keySet().toArray()) {
				String configKey = (String) configLine;
				bw.write(configKey + "=" + NavyCraft.instance.configFile.ConfigSettings.get(configKey) + System.getProperty("line.separator"));
			}
			bw.close();
		}
		catch (IOException ex) {
		}		
	}
}
