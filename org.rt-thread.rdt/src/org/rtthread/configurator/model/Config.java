/*******************************************************************************
 * Copyright (c) 2005-2011, Chinese Eclipse Community(CEC) All rights reserved. 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.ceclipse.org
 *
 * Contributors:
 *   Ming.He <heming@ceclipse.com> - initial API and implementation 
 *******************************************************************************/
package org.rtthread.configurator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Configure Item
 * @author RDT Team
 * @date 2011-9-3
 */
public class Config{

	private String name;
	
	private int type;
	
	private List<Config> children = new ArrayList<Config>();
	
	private Object defaults;
	
	private Object value;
	
	private String descripttion;
	
	private Config parent;
	
	private String oldMessage;
	
	private Choose choose;
	
	private String define;
	
	/**
	 * @return the oldMessage
	 */
	public String getOldMessage() {
		return oldMessage;
	}

	/**
	 * @param oldMessage the oldMessage to set
	 */
	public void setOldMessage(String oldMessage) {
		this.oldMessage = oldMessage;
	}

	public void addConfig(Config config){
		config.setParent(this);
		children.add(config);
	}
	
	public void removeConfig(Config config){
		children.remove(config);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the children
	 */
	public List<Config> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(List<Config> children) {
		this.children = children;
	}

	/**
	 * @return the description
	 */
	public String getDescripttion() {
		return descripttion;
	}

	/**
	 * @param descripttion the description to set
	 */
	public void setDescripttion(String descripttion) {
		this.descripttion = descripttion;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Config parent) {
		this.parent = parent;
	}

	/**
	 * @return the parent
	 */
	public Config getParent() {
		return parent;
	}

	public void setDefaults(Object defaults) {
		this.defaults = defaults;
	}

	public Object getDefaults() {
		return defaults;
	}

	public void setChoose(Choose choose) {
		this.choose = choose;
	}

	public Choose getChoose() {
		return choose;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setDefine(String define) {
		this.define = define;
	}

	public String getDefine() {
		return define;
	}
}

