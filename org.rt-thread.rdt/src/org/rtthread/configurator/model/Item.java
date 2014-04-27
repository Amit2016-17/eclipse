package org.rtthread.configurator.model;

/**
 * Item for choose
 * @author RDT Team
 * @date 2011-10-22 
 */
public class Item {

	private int index;
	private String value;
	private String description = null;

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}
	public String getDescription() {
		if (this.description == null) return this.value;
		return this.description;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
	
}
