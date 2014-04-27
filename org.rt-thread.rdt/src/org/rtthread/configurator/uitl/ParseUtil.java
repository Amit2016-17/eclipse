package org.rtthread.configurator.uitl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.rtthread.configurator.model.Choose;
import org.rtthread.configurator.model.Config;
import org.rtthread.configurator.model.Item;
import org.rtthread.configurator.model.Type;
import org.rtthread.configurator.uitl.CMacro;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 * Parse Header File
 * 
 * @author RDT Team
 * @date 2011-9-9
 */
public class ParseUtil {
	public List<Config> input = new ArrayList<Config>();
	private Map<String, CMacro> map; 
	private BufferedReader reader;

	public static final String ALWAYS = "always";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String SECTION = "section";
	public static final String BOOL = "bool";
	public static final String INTEGER = "integer";
	public static final String STRING = "string";
	public static final String IPADDR = "ipaddr";
	public static final String CHOOSE = "choose";

	/* token definitions */
	public static final int TOKEN_DEFINE = 0x00;
	public static final int TOKEN_IF = 0x01;
	public static final int TOKEN_IFDEF = 0x02;
	public static final int TOKEN_IFNDEF = 0x03;
	public static final int TOKEN_ELSE = 0x04;
	public static final int TOKEN_ENDIF = 0x05;
	public static final int TOKEN_NULL = 0x10;

	public ParseUtil(){
		map = new HashMap();
	}

	private String getLine(){
		String line = null;
		String content = null;
		int index;
		
		try {
			while (true) {
				line = reader.readLine();
				if (line == null) return null;
				
				line = line.trim();
				if (line.startsWith("//")) continue;
				index = line.indexOf("/*");
				if (index == -1) return line; /* no comment */
				
				/* get code before comment */
				if (index != 0) {
					content = line.substring(0, index);
				}
				
				index = line.indexOf("*/");
				if (index != -1) {
					if (index != line.length())
						if (content == null) content = line.substring(index + 2);
						else content += line.substring(index + 2);

					/* only comment in this line, get next line */
					if (content == null) continue;
					
					/* return line without comment */
					return content;
				} else {
					/* read all of comments */
					while (true) {
						line = reader.readLine();
						if (line == null)
							return content;

						index = line.indexOf("*/");
						if (index != -1) {
							if (index != line.length())
								content += line.substring(index);

							/* no content */
							if (content == null) break;
							
							return content;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}

	private int getMacroToken(String line_str){
		String token = null;
	
		token = line_str;
		if (token.startsWith("#")){
			token = token.substring(1);
			token = token.trim();
		}
		if (token.startsWith("define"))
			return TOKEN_DEFINE;
		if (token.startsWith("ifdef"))
			return TOKEN_IFDEF;
		if (token.startsWith("ifndef"))
			return TOKEN_IFNDEF;
		if (token.startsWith("else"))
			return TOKEN_ELSE;
		if (token.startsWith("endif"))
			return TOKEN_ENDIF;
		
		return TOKEN_NULL;
	}

	private void parseDefine(String line_str){
		int index;

		/* remove define */
		if (line_str.startsWith("#")) line_str = line_str.substring(1);
		line_str = line_str.substring(6);
		line_str = line_str.trim();
		
		index = line_str.indexOf('\t');
		if (index != -1) {
			String macro_name = null;
			String macro_value = null;
			
			macro_name = line_str.substring(0, index);
			line_str = line_str.substring(index);
			line_str = line_str.trim();
			macro_value = line_str;

			CMacro macro = new CMacro();
			macro.name = macro_name;
			macro.value = macro_value;
			if (macro_value.startsWith("\""))
				macro.type = "String";
			else
				macro.type = "Integer";

			map.put(macro_name, macro);
		}
		else {
			index = line_str.indexOf(' ');
			if (index != -1){
				String macro_name = null;
				String macro_value = null;
				
				macro_name = line_str.substring(0, index);
				line_str = line_str.substring(index);
				line_str = line_str.trim();
				macro_value = line_str;
				
				CMacro macro = new CMacro();
				macro.name = macro_name;
				macro.value = macro_value;
				if (macro_value.startsWith("\""))
					macro.type = "String";
				else
					macro.type = "Integer";

				map.put(macro_name, macro);
			}
			else
			{ /* is "#define NAME" */
				String macro_name = null;
				macro_name = line_str;
				
				CMacro macro = new CMacro();
				macro.name = macro_name;
				macro.value = "true";
				macro.type = "Bool";

				map.put(macro_name, macro);
			}
		}
		
		return;
	}

	private void parseIfDef(String line_str){
		boolean end = false;

		if (line_str.startsWith("#")) line_str = line_str.substring(1);

		/* #ifdef */
		line_str = line_str.substring(5);
		line_str = line_str.trim();
		
		if (getMacroType(line_str) != null){
			while (!end){
				line_str = getLine();
				switch (getMacroToken(line_str)){
				case TOKEN_DEFINE:
					parseDefine(line_str);
					break;
				case TOKEN_IFDEF:
					parseIfDef(line_str);
					break;
				case TOKEN_IFNDEF:
					parseIfNDef(line_str);
					break;
				case TOKEN_ELSE:
					end = true;
					break;
				case TOKEN_ENDIF:
					return ;
				}				
			}
			
			if (end){
				while (true){
					line_str = getLine();
					switch (getMacroToken(line_str)){
					case TOKEN_DEFINE:
					case TOKEN_IFDEF:
					case TOKEN_IFNDEF:
					case TOKEN_ELSE:
						continue;
					case TOKEN_ENDIF:
						return;
					}
				}
			}
		}
		
		return;
	}

	private void parseIfNDef(String line_str){
		boolean end = false;
		
		if (line_str.startsWith("#")) line_str = line_str.substring(1);

		/* #ifndef */
		line_str = line_str.substring(6);
		line_str = line_str.trim();
		
		if (getMacroType(line_str) != null){
			while (!end){
				line_str = getLine();
				if (line_str == null) return ;
				switch (getMacroToken(line_str)){
				case TOKEN_DEFINE:
					parseDefine(line_str);
					break;
				case TOKEN_IFDEF:
					parseIfDef(line_str);
					break;
				case TOKEN_IFNDEF:
					parseIfNDef(line_str);
					break;
				case TOKEN_ELSE:
					end = true;
					break;
				case TOKEN_ENDIF:
					return ;
				}				
			}
			
			if (end){
				while (true){
					line_str = getLine();
					switch (getMacroToken(line_str)){
					case TOKEN_DEFINE:
					case TOKEN_IFDEF:
					case TOKEN_IFNDEF:
					case TOKEN_ELSE:
						continue;
					case TOKEN_ENDIF:
						return;
					}
				}
			}
		}
		
		return;
	}
	
	private void parseIf(String line_str){
		boolean end = false;

		/* #if */
		if (line_str.startsWith("#")) line_str = line_str.substring(1);
		line_str = line_str.substring(2);
		line_str = line_str.trim();
		
		if (getMacroType(line_str) == "Integer" &&
				Integer.parseInt(getMacroValue(line_str)) > 0){
			while (!end){
				line_str = getLine();
				switch (getMacroToken(line_str)){
				case TOKEN_DEFINE:
					parseDefine(line_str);
					break;
				case TOKEN_IFDEF:
					parseIfDef(line_str);
					break;
				case TOKEN_IFNDEF:
					parseIfNDef(line_str);
					break;
				case TOKEN_ELSE:
					end = true;
					break;
				case TOKEN_ENDIF:
					return ;
				}				
			}
			
			if (end){
				while (true){
					line_str = getLine();
					switch (getMacroToken(line_str)){
					case TOKEN_DEFINE:
					case TOKEN_IFDEF:
					case TOKEN_IFNDEF:
					case TOKEN_ELSE:
						continue;
					case TOKEN_ENDIF:
						return;
					}
				}
			}
		}
		else {
			while (!end){
				line_str = getLine();
				switch (getMacroToken(line_str)){
				case TOKEN_DEFINE:
					parseDefine(line_str);
					break;
				case TOKEN_IFDEF:
					parseIfDef(line_str);
					break;
				case TOKEN_IFNDEF:
					parseIfNDef(line_str);
					break;
				case TOKEN_ELSE:
					end = true;
					break;
				case TOKEN_ENDIF:
					return ;
				}				
			}
			
			if (end){
				while (true){
					line_str = getLine();
					switch (getMacroToken(line_str)){
					case TOKEN_DEFINE:
					case TOKEN_IFDEF:
					case TOKEN_IFNDEF:
					case TOKEN_ELSE:
						continue;
					case TOKEN_ENDIF:
						return;
					}
				}
			}
		}
		
		return;
	}

	private void parseLine(String line_str){
		/* only parse line which starts with '#' */
		if (line_str.startsWith("#"))
		{
			/* remove # */
			line_str = line_str.substring(1);
			line_str = line_str.trim();
			
			if (line_str.startsWith("define")){
				/* #define */
				parseDefine(line_str);
			}
			else if (line_str.startsWith("ifdef")){
				/* #ifdef */
				parseIfDef(line_str);
			}
			else if (line_str.startsWith("ifndef")){
				/* #ifndef */
				parseIfNDef(line_str);
			}
			else if (line_str.startsWith("if")){
				/* #if */
				parseIf(line_str);
			}
		}
	}

	public String getMacroValue(String name) {
		String value = null;
		CMacro macro;
		
		macro = map.get(name);
		if (macro != null)
			value = macro.value;
		
		return value;
	}
	
	public String getMacroType(String name){
		String type = null;
		CMacro macro;
		
		macro = map.get(name);
		if (macro != null)
			type = macro.type;

		return type;
	}

	public void dumpMacros() {
        Set<Map.Entry<String, CMacro>> set = map.entrySet();
        for (Iterator<Map.Entry<String, CMacro>> it = set.iterator(); it.hasNext();) {
            Map.Entry<String, CMacro> entry = (Map.Entry<String, CMacro>) it.next();
            CMacro macro = entry.getValue();
            
            System.out.println(macro.name + "[" + macro.type + "]=" + macro.value);
        }
	}

	public void parseMacro(String content) {
		StringReader sreader = new StringReader(content);
		reader = new BufferedReader(sreader);
		String line_str = null;
		while ((line_str = getLine()) != null) {
			parseLine(line_str);
		}
		
		reader = null;
	}

	public String getLineFromString(String content) {
		int end;
		String line = null;
		
		end = content.indexOf("\n");
		if (end != -1){
			line = content.substring(0,  end);
			content = content.substring(end, content.length());
		}
		else {
			line = content;
			content = null;			
		}
		
		return line;
	}

	public String getComments(String content) {
		String xml = "";
		String line = "";
		
		StringReader sreader = new StringReader(content);
		BufferedReader reader = new BufferedReader(sreader);
		
		while (true) {
			try {
				if (line.length() == 0) 
					line = reader.readLine();
				if (line == null){
					reader.close();
					break;
				}
			} catch (IOException e) {
				break;
			}

			line = line.trim();

			if (line.startsWith("//")) {
				line = line.substring(2, line.length());
				line = line.trim();
				
				if (line.startsWith("#define ")) {
					line = ""; 
				}
				else {
					xml += line + "\r\n";
					line = "";					
				}
			}
			else {
				int begin, end;
				
				begin = line.indexOf("/*");
				if (begin != -1) {
					begin = begin + 2;
					end = line.indexOf("*/");
					if (end != -1) {
						xml += line.substring(begin, end) + "\r\n";
						line = line.substring(end + 2, line.length());
					}
					else {
						xml += line.substring(begin, line.length()) + "\r\n";
						while (true) {
							try {
								line = reader.readLine();
								if (line == null){
									reader.close();
									break;
								}
							} catch (IOException e) {
								break;
							}
							
							end = line.indexOf("*/");
							if (end != -1) {
								xml += line + "\r\n";
							}
							else {
								xml += line.substring(0, end);
								line = line.substring(end + 1, line.length());
								line = "";
								break;
							}
						}
					}
				}
				else line = "";
			}
		}

		reader = null;
		return xml;
	}

	public void handleSection(Node node, NamedNodeMap map){
		NodeList ns = node.getChildNodes();
		Config config = new Config();
		config.setType(Type.SECTION);
		Node n = map.getNamedItem("name");
		String name = n.getNodeValue();
		config.setName(name);
		config.setDefine(name);

		n = map.getNamedItem("description");
		if (n != null){
			config.setDescripttion(n.getNodeValue());
		}

		n = map.getNamedItem("default");
		if (n != null) {
			if (ALWAYS.equals(n.getNodeValue())){
				config.setDefaults(ALWAYS);
				config.setValue(true);
			}
			else { 
				if (TRUE.equals(n.getNodeValue()))
					config.setDefaults(true);
				else
					config.setDefaults(false);
				if (getMacroType(name) != null) {
					if (getMacroValue(name) == "true")
						config.setValue(true);
					else
						config.setValue(false);
				} 
				else {
					config.setValue(false);
				}
			}
		}

		for (int i = 0; i < ns.getLength(); i ++) {
			n = ns.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				NamedNodeMap m = n.getAttributes();
				handleItem(config, n, m);
			}
		}
		
		input.add(config);
	}

	public void handleChoose(Config config, Node node) {
		/* parse choose items */
		NodeList ns = node.getChildNodes();
		if (ns.getLength() > 0) {
			Choose choose = new Choose();
			int item_index = 0;
			int default_index;
			
			if (config.getType() == Type.STRING){
				default_index = Integer.parseInt((String) config.getDefaults());
				/* the default of choose is the index number */
				config.setDefaults(default_index);
			}
			else {
				default_index = (Integer) config.getDefaults(); /* Integer type */
			}
			
			for (int index = 0; index < ns.getLength(); index ++){
				Node firstNode = ns.item(index);
				if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
					if (firstNode.getFirstChild() != null){
						String value = firstNode.getFirstChild().getNodeValue();
						NamedNodeMap m = firstNode.getAttributes();
						Node tempNode = m.getNamedItem("description");
						Item item = new Item();
						item.setValue(value);
						
						if (tempNode != null){
							item.setDescription(tempNode.getNodeValue());
						}
						
						item.setIndex(item_index);
						item_index ++;

						choose.addItem(item);
					}
				}
			}
			
			config.setChoose(choose);
		}
	}

	public void handleItem(Config parent, Node node, NamedNodeMap map){
		Node n = null;
		String name;
		String default_value = null;
		String type = node.getNodeName();
		Config config = new Config();
		n = map.getNamedItem("name");
		name = n.getNodeValue();
		config.setName(name);
		config.setDefine(name);
	
		n = map.getNamedItem("description");
		if (n != null){
			config.setDescripttion(n.getNodeValue());
		}

		n = map.getNamedItem("default");
		if (n != null){
			default_value = n.getNodeValue();
		}

		if (BOOL.equals(type)){ /* bool item */
			config.setType(Type.BOOL);
			default_value.replace("\"", "");
			if (TRUE.equals(default_value))
				config.setDefaults(true);
			else 
				config.setDefaults(false);

			if (getMacroType(name) != null){
				if (getMacroValue(name) == "true")
					config.setValue(true);
				else
					config.setValue(false);
			}
			else
				config.setValue(false);
		}
		else if (INTEGER.equals(type)){ /* integer item */
			config.setType(Type.INT);

			int defaultInt = 0;
			if (default_value != null){
				default_value.replace("\"", "");
				/* get default value */
				defaultInt = Integer.parseInt(default_value);
			}
			config.setDefaults(defaultInt);
			
			if (getMacroType(name) == "Integer")
				config.setValue(Integer.parseInt(getMacroValue(name)));
			else if (default_value != null){
				/* set default */
				config.setValue(defaultInt);
			}
			else config.setValue(0);

			/* parse choose items */
			handleChoose(config, node);
		}
		else if (STRING.equals(type)){
			config.setType(Type.STRING);
			if (default_value != null)
				config.setDefaults(default_value);
			else
				config.setDefaults("");

			if (getMacroType(name) == "String")
				config.setValue(getMacroValue(name));
			else if (default_value != null) {
				config.setValue(default_value);
			}
			else config.setValue("");
			
			/* parse choose items */
			handleChoose(config, node);
		}
		else if (IPADDR.equals(type)){
			config.setType(Type.IPADDR);

			if (default_value != null)
				config.setDefaults(default_value);
			else {
				default_value = "0.0.0.0";
				config.setDefaults("0.0.0.0");
			}
			
			if ((getMacroType(name + "0") != "Integer") ||
				(getMacroType(name + "1") != "Integer") ||
				(getMacroType(name + "2") != "Integer") ||
				(getMacroType(name + "3") != "Integer")) {
				config.setValue(default_value);	
			}
			else {
				String value = getMacroValue(name + "0") + "." +
						getMacroValue(name + "1") + "." +
						getMacroValue(name + "2") + "." +
						getMacroValue(name + "3");

				config.setValue(value);
			}
		}

		if (parent != null){
			parent.addConfig(config);
			config.setParent(parent);
		}
		else {
			input.add(config);
		}
	}

	public void parseXML(String xml) {
		if ((xml == null) || (xml == "")) return;
		
		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			document.getDocumentElement().normalize();
			NodeList node = document.getElementsByTagName("RDTConfigurator");
			if (node == null) return;

			Node n = node.item(0);
			node = n.getChildNodes();

			for (int i = 0; i < node.getLength(); i++) {
				Node firstNode = node.item(i);

				if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
					NamedNodeMap map = firstNode.getAttributes();

					if (SECTION.equals(firstNode.getNodeName())) handleSection(firstNode, map);
					else handleItem(null, firstNode, map);
				}
			}
		} catch (SAXParseException parse_error) {
			String str = String.format("RT-Thread Configurator: \n%d: %s", 
					parse_error.getLineNumber(),
					parse_error.getMessage());
			Shell shell = PlatformUI.getWorkbench().
	                getActiveWorkbenchWindow().getShell();
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setMessage(str);
			messageBox.setText("XML Error");
			messageBox.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void parseConfig(String str) {
		String comments;
		String content = "";
		StringReader sreader = new StringReader(str);
		BufferedReader reader = new BufferedReader(sreader);
		String line_str = null;
		
		/* only parse <RDTConfigurator and </RDTConfigurator>*/
		try {
			while ((line_str = reader.readLine()) != null) {
				if (line_str.indexOf("<RDTConfigurator") != -1) break;
			}
			
			content += line_str + "\n";
			while ((line_str = reader.readLine()) != null) {
				if (line_str.indexOf("</RDTConfigurator>") != -1) break;
				content += line_str + "\n";
			}
			content += line_str + "\n";
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		str = content;
		reader = null;

		/* parser macro */
		parseMacro(str);
		
		/* get comments to parse XML */
		comments = getComments(str);
		/* parse XML */
		parseXML(comments);
	}

	public List<Config> getConList() {
		return input;
	}
	
	static private String generateBool(Config cfg) {
		String content = "";
		boolean enable;
		
		/* generate bool */
		content += String.format("// <bool name=\"%s\" description=\"%s\" default=\"%s\" />\n", 
				cfg.getName(), cfg.getDescripttion(), cfg.getDefaults());
		enable = (Boolean) cfg.getValue();
		if (enable == true) {
			content += String.format("#define %s\n", cfg.getName());
		}
		else {
			content += String.format("// #define %s\n", cfg.getName());
		}

		return content;
	}

	static private String generateInteger(Config cfg) {
		String content = "";
		/* generate integer */
		if (cfg.getChoose() != null) /* generate choose */ {
			content += String.format("// <integer name=\"%s\" description=\"%s\" default=\"%d\">\n", 
					cfg.getName(), cfg.getDescripttion(), cfg.getDefaults());
			List<Item> items = cfg.getChoose().getItems();
			for (Item item : items) {
				if (item.getDescription() != null)
					content += String.format("// <item description=\"%s\">%s</item>\n", 
							item.getDescription(), item.getValue());
				else
					content += String.format("// <item>%s</item>\n", item.getValue());
			}
			content += "// </integer>\n";
		}
		else {
			content += String.format("// <integer name=\"%s\" description=\"%s\" default=\"%d\" />\n", 
					cfg.getName(), cfg.getDescripttion(), cfg.getDefaults());
		}
		content += String.format("#define %s\t%d\n", cfg.getName(), cfg.getValue());

		return content;
	}

	static private String generateString(Config cfg) {
		String content = "";
		/* generate string */
		if (cfg.getChoose() != null) /* generate choose */ {
			content += String.format("// <string name=\"%s\" description=\"%s\" default=\"%d\">\n", 
					cfg.getName(), cfg.getDescripttion(), cfg.getDefaults());
			List<Item> items = cfg.getChoose().getItems();
			for (Item item : items) {
				if (item.getDescription() != null)
					content += String.format("// <item description=\"%s\">%s</item>\n", 
							item.getDescription(), item.getValue());
				else
					content += String.format("// <item>%s</item>\n", item.getValue());
			}
			content += "// </string>\n";
		}
		else {
			content += String.format("// <string name=\"%s\" description=\"%s\" default=\"%s\" />\n", 
					cfg.getName(), cfg.getDescripttion(), cfg.getDefaults());
		}
		content += String.format("#define %s\t%s\n", cfg.getName(), cfg.getValue());

		return content;
	}

	static private String generateIPAddr(Config cfg) {
		String content = "";
		String ipaddr = "";
		
		ipaddr = (String) cfg.getValue();

		content += String.format("// <ipaddr name=\"%s\" description=\"%s\" default=\"%s\" />\n", 
					cfg.getName(), cfg.getDescripttion(), cfg.getDefaults());

		/* 0.0.0.0 */
		content += String.format("#define %s0 %s\n", cfg.getName(),ipaddr.substring(0, ipaddr.indexOf(".")));
		ipaddr = ipaddr.substring(ipaddr.indexOf(".") + 1);
		content += String.format("#define %s1 %s\n", cfg.getName(),ipaddr.substring(0, ipaddr.indexOf(".")));
		ipaddr = ipaddr.substring(ipaddr.indexOf(".") + 1);
		content += String.format("#define %s2 %s\n", cfg.getName(),ipaddr.substring(0, ipaddr.indexOf(".")));
		ipaddr = ipaddr.substring(ipaddr.indexOf(".") + 1);
		content += String.format("#define %s3 %s\n", cfg.getName(),ipaddr);
		
		return content;
	}

	static public String generateXML(List<Config> configArray) {
		String content = "";
		/* generated <RDTConfigurator */
		content += "// <RDTConfigurator URL=\"http://www.rt-thread.com/eclipse\"> \n\n";

		for (Config cfg : configArray) {
			if (cfg.getType() == Type.SECTION){
				String default_str;
				boolean enable = true;
				
				if (cfg.getDefaults() instanceof String) default_str = "always";
				else {
					enable = (Boolean) cfg.getDefaults();
					if (enable == true) default_str = "true";
					else default_str = "false";
					
					enable = (Boolean) cfg.getValue();
				}
				/* generate section */
				content += String.format("// <section name=\"%s\" description=\"%s\" default=\"%s\" >\n", 
						cfg.getName(), cfg.getDescripttion(), default_str);
				if (default_str != "always") {
					if (enable == true) content += String.format("#define %s\n", cfg.getName());
					else content += String.format("// #define %s\n", cfg.getName());
				}

				for (Config child : cfg.getChildren()) {
					if (child.getType() == Type.INT) content += generateInteger(child);
					if (child.getType() == Type.BOOL) content += generateBool(child);
					if (child.getType() == Type.STRING) content += generateString(child);
					if (child.getType() == Type.IPADDR) content += generateIPAddr(child);
				}
				content += "// </section>\n\n";
			}
			else if (cfg.getType() == Type.BOOL) {
				content += generateBool(cfg);
			}
			else if (cfg.getType() == Type.INT) {
				content += generateInteger(cfg);
			}
			else if (cfg.getType() == Type.STRING) {
				content += generateString(cfg);
			}
			else if (cfg.getType() == Type.IPADDR) {
				content += generateIPAddr(cfg);
			}
		}

		/* generated </RDTConfigurator>*/
		content += "// </RDTConfigurator> \n";

		return content;
	}
}
