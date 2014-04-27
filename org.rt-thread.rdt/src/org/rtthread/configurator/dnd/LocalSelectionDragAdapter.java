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
package org.rtthread.configurator.dnd;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * TODO 此处填写 class 信息
 * @author Ming.He
 * @date 2011-9-26
 */
public class LocalSelectionDragAdapter extends DragSourceAdapter {

	ISelectionProvider selectionProvider;

	public LocalSelectionDragAdapter(ISelectionProvider provider) {
		selectionProvider = provider;
	}

	// Override
	public void dragSetData(DragSourceEvent event) {
		DragSource dragSource = (DragSource) event.widget;
		Control control = dragSource.getControl();
		if (control != control.getDisplay().getFocusControl()) {
			event.doit = false;
			return;
		}

		IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();

		if (selection.isEmpty()) {
			event.doit = false;
			return;
		}
		LocalSelectionTransfer.getTransfer().setSelection(selection);
		event.doit = true;
	}

	// Override
	public void dragStart(DragSourceEvent event) {
	}

	// Override
	public void dragFinished(DragSourceEvent event) {
		super.dragFinished(event);
	}

}
