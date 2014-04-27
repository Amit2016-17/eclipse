package org.rtthread.configurator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorPart;
import org.rtthread.configurator.model.Config;
import org.rtthread.configurator.model.Item;
import org.rtthread.configurator.model.Type;
import org.rtthread.configurator.uitl.ParseUtil;

/**
 * ConfigEditingSupport
 * 
 * @author RDT Team
 * @date 2011-9-3
 */
public class ConfigEditingSupport extends EditingSupport {

	private CellEditor editor;
	private CellEditor editor1;
	private ComboBoxCellEditor editor2;
	private CellEditor editor3;
	private REditor rEditor;
	private int column;

	/**
	 * @param rEditor
	 * @param viewer
	 */
	public ConfigEditingSupport(REditor rEditor, ColumnViewer viewer, int column) {
		super(viewer);
		switch (column) {
		case 1:
			editor = new TextCellEditor(((TreeViewer) viewer).getTree());
			editor1 = new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
			editor2 = new ComboBoxCellEditor(((TreeViewer) viewer).getTree(), new String[] {}, SWT.READ_ONLY);
			editor3 = new TextCellEditor(((TreeViewer) viewer).getTree(), SWT.READ_ONLY);
			break;
		}
		this.column = column;
		this.rEditor = rEditor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
	 */
	// Override
	protected CellEditor getCellEditor(Object element) {
		Config config = (Config) element;
		if (Type.BOOL == config.getType()) {
			if ((Boolean) (((!"".equals(config.getValue()) && null != config.getValue()) ? config.getValue() : config.getDefaults()) instanceof java.lang.Boolean))
				return editor1;
			else
				return null;
		}
		if (Type.SECTION == config.getType()) {
			if (config.getDefaults() instanceof String) return editor3;

			return editor1;
		}

		if (config.getChoose() != null) {
			List<Item> itemArray = config.getChoose().getItems();
			String[] items = new String[itemArray.size()];
			for (int i = 0; i < itemArray.size(); i++) {
				items[i] = itemArray.get(i).getValue();
			}
			if (editor2 != null)
				editor2.setItems(items);
			
			return editor2;
		}

		return editor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
	 */
	// Override
	protected boolean canEdit(Object element) {
		Config config = (Config) element;
		Config parent = config.getParent();
		
		if (parent != null) {
			Object def = (!"".equals(parent.getValue()) && null != parent.getValue()) ? parent.getValue() : parent.getDefaults();
			if (def instanceof Boolean) {
				return ((Boolean) def);
			}
			if (ParseUtil.ALWAYS.equals(String.valueOf(def))) {
				return true;
			}
		}

		Object def = (!"".equals(config.getValue()) && null != config.getValue()) ? config.getValue() : config.getDefaults();
		if (def instanceof Boolean) {
			return true;
		}

		if (Type.SECTION == config.getType()) {
			if (ParseUtil.ALWAYS.equals(String.valueOf(def))) {
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.EditingSupport#getDefaults(java.lang.Object)
	 */
	// Override
	protected Object getValue(Object element) {
		Config config = (Config) element;
		switch (this.column) {
		case 1:
			if (Type.BOOL == config.getType()
					|| Type.SECTION == config.getType())
				return (!"".equals(config.getValue()) && null != config.getValue()) ? 
						config.getValue() : config.getDefaults();

			if (config.getChoose() != null) {
				List<Item> items = config.getChoose().getItems();
				for (Item item : items) {
					if (item.getValue() == config.getValue()) return item.getIndex();
				}
				return 0;
			}

			return String.valueOf((!"".equals(config.getValue()) && null != config.getValue()) ? 
					config.getValue() : config.getDefaults());
		default:
			break;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object,
	 * java.lang.Object)
	 */
	// Override
	protected void setValue(Object element, Object value) {
		Config config = (Config) element;
		String oldValue = String.valueOf((!"".equals(config.getValue()) && null != config.getValue()) ? config.getValue() : config.getDefaults());
		switch (this.column) {
		case 1:
			if (null != config.getChoose()) {
				String configValue;
				configValue = config.getChoose().getItems().get((Integer)value).getValue();
				if (config.getType() == Type.INT)
					config.setValue(Integer.parseInt(configValue));
				else
					config.setValue(configValue);
				rEditor.updateConfig(config);
				break;
			} else {
				if (config.getType() == Type.SECTION || config.getType() == Type.BOOL){
					boolean enable;
					enable = (Boolean) value;
					if (enable == true) config.setValue(true);
					else config.setValue(false);
				}
				else {
					if (config.getType() == Type.INT)
						config.setValue(Integer.parseInt((String) value));
					else
						config.setValue(value);
				}
				rEditor.updateConfig(config);
				break;
			}

		default:
			break;
		}
		getViewer().update(config, null);
		updateREditor(oldValue, config);
	}

	private void updateREditor(String oldValue, Config config){
		String newValue = config.getValue().toString();
		String content = "";

		if (Type.STRING == config.getType()) {
			newValue = '"' + newValue + '"';
		}
		if (oldValue.equals(newValue))
			return;
		TextEditor tEditor = rEditor.getEditor();
		IDocument doc = tEditor.getDocumentProvider().getDocument(tEditor.getEditorInput());
		String editorText = doc.get();
		
		if (Type.SECTION == config.getType()) {
			updateChildren(config);
			TreeViewer treeViewer = (TreeViewer) getViewer();
			treeViewer.refresh();
		}

		/* search <RDTConfigurator */
		StringReader sreader = new StringReader(editorText);
		BufferedReader reader = new BufferedReader(sreader);
		String line_str = null;

		try {
			while ((line_str = reader.readLine()) != null) {
				if (line_str.indexOf("<RDTConfigurator") == -1) {
					content += line_str + "\n";
				}
				else break;
			}
			
			/* generate XML */
			List<Config> configArray = rEditor.getConfigArray();
			content += ParseUtil.generateXML(configArray);

			/* search </RDTConfigurator> */
			while ((line_str = reader.readLine()) != null) {
				if (line_str.indexOf("</RDTConfigurator>") == -1) {
					continue;
				}
				else break;
			}

			/* append the rest */
			while ((line_str = reader.readLine()) != null) {
				content += line_str + "\n";
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		reader = null;
		/* get old position */
		ITextSelection selection = (ITextSelection) tEditor.getEditorSite().getSelectionProvider().getSelection();
		int line = selection.getEndLine();
		int offset = selection.getOffset();
		int length = selection.getLength();
		
		doc.set(content);

		ITextSelection tsNew = new TextSelection(offset, length);
		rEditor.getEditorSite().getSelectionProvider().setSelection(tsNew);
	}

	private void updateChildren(Config section) {
		List<Config> children = section.getChildren();
		for (Config config : children) {
			config.setParent(section);
		}
	}
}
