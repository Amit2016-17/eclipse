package org.rtthread.configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.rtthread.configurator.model.Config;
import org.rtthread.configurator.uitl.ParseUtil;

/**
 * ParseTest
 * 
 * @author Ming.He
 * @date 2011-9-9
 */
public class ParseTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ParseTest().test();
	}

	void test() {
		String dir = System.getProperty("user.dir");
		System.out.println(dir);
		try {
			String str = "";
			char[] tempchars = new char[30];
			int charread = 0;
			InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(dir + "/config.h")));
			while ((charread = reader.read(tempchars)) != -1) {
				if ((charread == tempchars.length) && (tempchars[tempchars.length - 1] != 'r')) {
					str += String.valueOf(tempchars);
				} else {
					for (int i = 0; i < charread; i++) {
						if (tempchars[i] == 'r') {
							continue;
						} else {
						  str += tempchars[i];
						}
					}
				}
			}
			ParseUtil util = new ParseUtil();
			util.parseConfig(str);
			List<Config> list = util.getConList();
			loopPrintln(list);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loopPrintln(List<Config> arry){
		for(Config config : arry){
			if(!config.getChildren().isEmpty()){
				loopPrintln(config.getChildren());
			} else{
				System.out.println(config.getName() + "======" + config.getDescripttion() + "========" + config.getDefaults());
			}
		}
	}

}
