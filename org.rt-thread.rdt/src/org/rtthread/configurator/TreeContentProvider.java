package org.rtthread.configurator;

import java.util.List;

import org.rtthread.configurator.model.Config;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * TreeContentProvider
 * 
 * @author RDT Team
 * @date 2011-9-3
 */
@SuppressWarnings("unchecked")
public class TreeContentProvider implements ITreeContentProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	// Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	// Override

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (null == oldInput || null == newInput)
			return;
		List<Config> oldIn = (List<Config>) oldInput;
		List<Config> newIn = (List<Config>) newInput;
		for (Config oldCon : oldIn) {
			for (Config newCon : newIn) {
				if (oldCon.getName().equals(newCon.getName())) {
					copyConfig(newCon, oldIn, oldCon);
				}
			}
		}
		viewer.refresh();
	}

	private void copyConfig(Config newCon, List<Config> configArray, Config config) {
		for (Config con : configArray) {
			if (con.getName().equals(config.getName())) {
				copyConfig(newCon, config);
				break;
			}
			if (null != con.getChildren() && !con.getChildren().isEmpty()) {
				copyConfig(newCon, con.getChildren(), config);
			}
		}
	}

	private void copyConfig(Config newConfig, Config oldConfig) {
		oldConfig.setChildren(newConfig.getChildren());
		oldConfig.setChoose(newConfig.getChoose());
		oldConfig.setDefaults(newConfig.getDefaults());
		oldConfig.setDefine(newConfig.getDefine());
		oldConfig.setOldMessage(newConfig.getOldMessage());
		oldConfig.setParent(newConfig.getParent());
		oldConfig.setValue(newConfig.getValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.
	 * Object)
	 */
	// Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>)
			return ((List<?>) inputElement).toArray();
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
	 * Object)
	 */
	// Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Config)
			return ((Config) parentElement).getChildren().toArray();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object
	 * )
	 */
	// Override
	public Object getParent(Object element) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
	 * Object)
	 */
	// Override
	public boolean hasChildren(Object element) {
		if (element instanceof Config)
			return !((Config) element).getChildren().isEmpty();
		return false;
	}
}
