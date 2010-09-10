package net.refractions.udig.georss;




import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;
import net.refractions.udig.catalog.ui.workflow.ConnectionState;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;



public class GeoRSSWizardPage extends WizardPage implements UDIGConnectionPage {
	
	final static String[] types = {"GeoRSS", "Directory"};
	private String url = "";
	
	//private Map <String, Serializable> params = null;
	public static final String GEORSS_WIZARD = "GEORSS_WIZARD";
	//public static final String GEORSS_RECENT = "GEORSS_RECENT";
	public IDialogSettings settings;

	//public Combo urlCombo;
	
	

	public GeoRSSWizardPage() {
		super("GeoRSSWizardPage");
		settings = Activator.getDefault().getDialogSettings().getSection(GEORSS_WIZARD);
		if (settings == null){
			settings = Activator.getDefault().getDialogSettings().addNewSection(GEORSS_WIZARD);
		}
	}
	
	
	
	public Map <String, Serializable> defaultParams(){
		IStructuredSelection selection = (IStructuredSelection)PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService()
				.getSelection();
		
		Map<String, Serializable> toParams = toParams(selection );
		if(!toParams.isEmpty()){
			return toParams;
		}
		
		GeoRSSConnectionFactory connectionFactory = new GeoRSSConnectionFactory();
		Map<String, Serializable> params = connectionFactory.createConnectionParameters(getState().getWorkflow().getContext());
		if (params != null)
			return params;
		return Collections.emptyMap();
		}
	
		private ConnectionState getState() {
			WorkflowWizard wizard = (WorkflowWizard)getWizard();
			ConnectionState currentState = (ConnectionState)wizard.getWorkflow().getCurrentState();
			return currentState;
	}



		public Map<String, Serializable> toParams(IStructuredSelection selection) {
		  if (selection != null){
			  GeoRSSConnectionFactory connectionFactory = new GeoRSSConnectionFactory();
			  for (Iterator itr = selection.iterator(); itr.hasNext();){
				  Map<String, Serializable> params = connectionFactory
				  .createConnectionParameters(itr.next());
				  if (!params.isEmpty())
					  return params;
			  }
		  }
		return Collections.emptyMap();
	}



		static public Map<String,Serializable> createParams( IResolve handle ){
	       if (handle instanceof GeoRSSGeoResource ){
	            GeoRSSGeoResource layer = (GeoRSSGeoResource) handle;
	            GeoRSSService georsss;
	            try {
	                georsss = (GeoRSSService) layer.parent( null );
	                return georsss.getConnectionParams();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }                    
	        }
	        else if( handle.canResolve( GeoRSSDataStore.class )){
	            
	            return createParams( handle );
	        }
	        return Collections.emptyMap();
	    }



	public void createControl(Composite parent) {
		GridData gridData; 
		Composite composite = new Composite (parent, SWT.NULL);
		
		GridLayout gridLayout = new GridLayout();
		int columns = 1;
		gridLayout.numColumns = columns;
		composite.setLayout(gridLayout);
		
		gridData = new GridData();
		Label urlLabel = new Label (composite, SWT.NONE);
		urlLabel.setText("Enter a URL that points to a GeoRSS document");
		urlLabel.setLayoutData(gridData);
		
		gridData = new GridData (GridData.FILL_HORIZONTAL);
		gridData.widthHint = 400;
		
		
		
		
		//urlCombo.setText(url);
		//urlCombo.addModifyListener(this);
		
		setControl(composite);
		
		
		
	}

	public void widgetDefualtSelected (SelectionEvent e) {
		e.getClass();
		if(getWizard().canFinish()){
			getWizard().performFinish();
		}
	}
	
	public List <IService> getResources(IProgressMonitor monitor) throws Exception {
		URL location = new URL (url);
		GeoRSSServiceExtension creator = new GeoRSSServiceExtension();
		Map <String, Serializable> params = creator.createParams(location);
		IService service = new GeoRSSService(location, params);
		service.getInfo(monitor);
		List<IService> servers = new ArrayList<IService>();
		servers.add(service);
		
		return servers;
		
		
	}



	public Map<String, Serializable> getParams() {
		/*try {
			URL location = new URL(url);
			
			GeoRSSServiceExtension creator = new GeoRSSServiceExtension();
            String errorMessage=creator.reasonForFailure(location);
            if( errorMessage!=null ){
                setErrorMessage(errorMessage);
                return Collections.emptyMap();
            }else
                return creator.createParams(location);
		}
		catch(MalformedURLException e) {
			return null;
		}*/
		Map<String, Serializable> params = defaultParams();
		return params;
	}



	/*public void modifyText(ModifyEvent e) {
		// TODO Auto-generated method stub
		
	}*/



}
