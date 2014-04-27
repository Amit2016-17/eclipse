package org.rtthread.configurator;

import org.rtthread.configurator.model.Config;
import org.rtthread.configurator.model.Type;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * TreeLabel Provider
 * 
 * @author RDT Team
 * @date 2011-9-3
 */
public class TreeLabelProvider extends StyledCellLabelProvider{

	private static final Image CHECKED = REditorPlugin.getImageDescriptor("icons/checked.gif").createImage();
	private static final Image UNCHECKED = REditorPlugin.getImageDescriptor("icons/unchecked.gif").createImage();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.StyledCellLabelProvider#update(org.eclipse.
	 * jface.viewers.ViewerCell)
	 */
	// Override
	public void update(ViewerCell cell) {
		Config element = (Config) cell.getElement();
		int index = cell.getColumnIndex();
		String columnText = getColumnText(element, index);
		cell.setText(columnText);
		cell.setImage(getColumnImage(element, index));
		cell.setForeground(getForeground(element));
	}

	/**
	 * @param element
	 * @param index
	 * @return
	 */
	private Image getColumnImage(Config element, int index) {
		if (1 == index && (Type.BOOL == element.getType() || Type.SECTION == element.getType())) {
			if (element.getDefaults() instanceof String) return  null;

			String bool = String.valueOf((!"".equals(element.getValue()) && null != element.getValue()) ? element.getValue() :element.getDefaults());
			if ("true".equals(bool))
				return CHECKED;
			if ("false".equals(bool))
				return UNCHECKED;
		}

		return null;
	}

	/**
	 * @param element
	 * @param index
	 * @return
	 */
	private String getColumnText(Config element, int index) {
		switch (index) {
		case 0:
			if (element.getDescripttion() != "")
				return element.getDescripttion();
			return element.getName();
		case 1:
			if (Type.INT == element.getType() || Type.STRING == element.getType() || element.getType() == Type.IPADDR)
				return String.valueOf((!"".equals(element.getValue()) && null != element.getValue()) ? element.getValue() :element.getDefaults());
			if (Type.BOOL == element.getType())
				return "    ";
			return null;
		default:
			throw new RuntimeException("Should not happen");
		}
	}

	private Color getForeground(Object element) {
		if(element instanceof Config){
			Config config = (Config) element;
			if(null != config.getParent()){
				Config parent = config.getParent();
				Object def = (!"".equals(parent.getValue()) || null != parent.getValue()) ? parent.getValue() :parent.getDefaults();
				if(def instanceof Boolean){
					if(!(Boolean) def){
						Display display = Display.getCurrent();
						if(null == display)
							display = Display.getDefault();
						return display.getSystemColor(SWT.COLOR_GRAY);
					}
				}
			}
		}
		return null;
	}

}
