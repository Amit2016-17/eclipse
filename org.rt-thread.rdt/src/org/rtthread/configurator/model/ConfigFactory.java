package org.rtthread.configurator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Config Factory
 * @author RDT Team
 * @date 2011-9-3
 */
public class ConfigFactory {

	public static List<Config> getConig(){
		List<Config> list = new ArrayList<Config>();
		Config config = new Config();
		config.setDefaults(Boolean.valueOf("true"));
		config.setType(Type.BOOL);
		config.setDescripttion("aaaaaaa");
		config.setName("bool1");
		
		list.add(config);
		
		config = new Config();
		config.setDefaults("string1");
		config.setName("string");
		config.setType(Type.STRING);
		
		
		list.add(config);
		
		config = new Config();
		config.setDefaults(1);
		config.setName("int");
		config.setType(Type.INT);
		list.add(config);
		
		config = new Config();
		config.setDefaults(Boolean.valueOf("true"));
		config.setName("section");
		config.setType(Type.SECTION);
		

		
		Config config1 = new Config();
		config1.setDefaults(Boolean.valueOf("true"));
		config1.setName("bool1");
		config1.setType(Type.BOOL);
		config.addConfig(config1);
		
		config1 = new Config();
		config1.setDefaults(Boolean.valueOf("true"));
		config1.setName("bool2");
		config1.setType(Type.BOOL);
		config.addConfig(config1);
		
		config1 = new Config();
		config1.setDefaults("always");
		config1.setName("bool3");
		config1.setType(Type.BOOL);
		config.addConfig(config1);
		
		
		config1 = new Config();
		config1.setDefaults(Boolean.valueOf("true"));
		config1.setName("bool4");
		config1.setType(Type.BOOL);
		config.addConfig(config1);
		
		config1 = new Config();
		config1.setDefaults(Boolean.valueOf("true"));
		config1.setName("bool5");
		config1.setType(Type.BOOL);
		config.addConfig(config1);
		
		list.add(config);
		
		
		
		return list;
	}
}
