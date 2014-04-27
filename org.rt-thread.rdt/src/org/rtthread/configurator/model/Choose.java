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
 * TODO 此处填写 class 信息
 * @author Ming.He
 * @date 2011-10-22 
 */
public class Choose {

	private String name;
	
	private List<Item> items = new ArrayList<Item>();
	
	private String message;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDef() {
		return def;
	}

	public void setDef(String def) {
		this.def = def;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public List<Item> getItems() {
		return items;
	}

	private String def;
	
	public void addItem(Item item){
		items.add(item);
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
}
