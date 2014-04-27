package org.rtthread.configurator.dnd;

import org.rtthread.configurator.REditor;
import org.rtthread.configurator.model.Config;
import org.rtthread.configurator.model.Type;
import org.rtthread.configurator.uitl.ParseUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * 
 * LocalSelectionDropAdapter
 * 
 * @author Ming.He
 * @date 2011-9-26
 */
public class LocalSelectionDropAdapter extends DropTargetAdapter {

	private TreeViewer treeViewer;
	private REditor rEditor;

	public LocalSelectionDropAdapter(REditor rEditor, TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
		this.rEditor = rEditor;
	}

	// Override
	public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}

	// Override
	public void dragOperationChanged(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}

	// Override
	public void drop(final DropTargetEvent event) {
		Display d = event.display;
		d.asyncExec(new Runnable() {

			// Override
			public void run() {
				asyncDrop(event);
			}

		});

	}

	private void asyncDrop(DropTargetEvent event) {
		if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
			TreeItem item = (TreeItem) event.item;
			TreeSelection ts = (TreeSelection) event.data;
			Config config = (Config) ts.getFirstElement();
			if(null == item)
				return;
			Config parent = (Config) item.getData();
			Config oldParent = config.getParent();
			boolean yesOrNo = MessageDialog.openConfirm(event.display.getActiveShell(), "Move", "Move " + config.getName() + " to " + parent.getName());
			if (yesOrNo) {
				if (config.equals(parent) || Type.SECTION != parent.getType())
					return;
				if (null != oldParent) {
					oldParent.removeConfig(config);
				}
				config.setParent(parent);
				parent.addConfig(config);
				treeViewer.remove(config);
				treeViewer.refresh(config);
				treeViewer.refresh(parent);
				TextEditor tEditor = rEditor.getEditor();
				IDocument doc = tEditor.getDocumentProvider().getDocument(tEditor.getEditorInput());
				String editorText = doc.get();
				String oldMsg = config.getOldMessage();
				String newMsg = null;
//				if (null == oldParent) {
//					newMsg = oldMsg.replaceFirst(config.getName(), config.getName() + " " + ParseUtil.START_DEPEND + parent.getName() + ParseUtil.END_DEPEND);
//				} else {
//					newMsg = oldMsg.replaceFirst(oldParent.getName(), parent.getName());
//				}
				int index = editorText.indexOf(oldMsg);
				if (-1 == index) {
					return;
				}
				try {
					doc.replace(index, oldMsg.length(), newMsg);
				} catch (BadLocationException e) {
				}
				config.setOldMessage(newMsg);
			}
		}

	}
}
