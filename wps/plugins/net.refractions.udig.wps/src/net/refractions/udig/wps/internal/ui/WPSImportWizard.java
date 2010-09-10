/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */

package net.refractions.udig.wps.internal.ui;

import net.refractions.udig.catalog.CatalogPlugin;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * WPS server import wizard main class
 *
 * @author Lucas Reed, Refractions Research Inc
 */
public class WPSImportWizard extends Wizard implements IImportWizard {
	private ImportWizardPage0 page0 = null;

	public boolean performFinish() {
		if (false == this.page0.canFlipToNextPage())
		{
			return false;
		}

		CatalogPlugin.getDefault().getLocalCatalog().add(this.page0.service);

		return true;
	}

	@Override
	public boolean canFinish() {
		return true;
	}

	@Override
	public void addPages()
	{
		this.page0 = new ImportWizardPage0("");	//$NON-NLS-1$
		this.addPage(this.page0);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// Intentionally blank
	}
}
