/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.wps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.opengis.wps10.WPSCapabilitiesType;
import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IProcess;
import net.refractions.udig.catalog.IProcessInfo;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.CatalogUIPlugin;
import net.refractions.udig.catalog.ui.ISharedImages;
import net.refractions.udig.wps.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.data.wps.WPSFactory;
import org.geotools.data.wps.WebProcessingService;
import org.geotools.feature.NameImpl;
import org.geotools.process.ProcessFactory;

/**
 * Process provided by WPS.
 * </p>
 *
 * @author gdavis, Refractions Research Inc
 * @author Lucas Reed, Refractions Research Inc
 */
public class WPSProcessImpl extends IProcess {

    private WPSServiceImpl service;
    private ProcessFactory processFactory;
    private IProcessInfo info;
    private ImageDescriptor icon;
    private URL identifier;
    private ArrayList<IResolve> members;
    private IResolve parent;
    private Lock iconLock = new ReentrantLock();

    /**
     * Construct <code>WPSProcessImpl</code>.
     * 
     * @param service
     * @param parent the parent Process may be null if parent is the service.
     * @param process
     */
    public WPSProcessImpl( WPSServiceImpl service, IResolve parent, ProcessFactory processFactory ) {
        this.service = service;
        if (parent == null) {
            this.parent = service;
        } else {
            this.parent = parent;
        }
        this.processFactory = processFactory;
        members = new ArrayList<IResolve>();
//        for( Layer child : process.getChildren() ) {
//            if (child != process) {
//                if (child.getName() == null) {
//                    members.add(new WPSFolder(service, this, child));
//                } else {
//                    members.add(new WPSProcessImpl(service, this, child));
//                }
//            }
//        }

        try {
            String name = ((WPSFactory)processFactory).getIdentifier(); 
            if (name == null) {
                WpsPlugin.log("Can't get a unique name for the identifier of WPSProcess: " + processFactory, null);  //$NON-NLS-1$
                throw new RuntimeException("This should be a WPSFolder not an IProcess"); //$NON-NLS-1$
            }

            identifier = new URL(service.getIdentifier().toString() + "#" + name); //$NON-NLS-1$

        } catch (Throwable e) {
            WpsPlugin.log(null, e);
            identifier = service.getIdentifier();
        }
    }

    @Override
    public IResolve parent( IProgressMonitor monitor ) throws IOException {
        return parent;
    }
    /*
     * @see net.refractions.udig.catalog.IGeoResource#getStatus()
     */
    public Status getStatus() {
        return service.getStatus();
    }

    public String getTitle() {
        return null;
    }

    public IProcessInfo getInfo( IProgressMonitor monitor ) throws IOException {
        if (info == null) {
            service.rLock.lock();
            try {
                if (info == null) {
                    info = new WPSProcessInfo(this.processFactory, monitor);
                }
            } finally {
                service.rLock.unlock();
            }
        }
        return info;
    }

    public URL getIdentifier() {
        return identifier;
    }

    @Override
    public List<IResolve> members( IProgressMonitor monitor ) {
        return this.members;
    }

    /*
     * @see net.refractions.udig.catalog.IGeoResource#resolve(java.lang.Class,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (adaptee == null) {
            throw new NullPointerException();
        }

        // if (adaptee.isAssignableFrom(IService.class)) {
        // return adaptee.cast( parent);
        // }

        if (adaptee.isAssignableFrom(IProcess.class)) {
            return adaptee.cast(this);
        }

        if (adaptee.isAssignableFrom(IProcessInfo.class)) {
            return adaptee.cast(getInfo(monitor));
        }

        if (adaptee.isAssignableFrom(WebProcessingService.class)) {
            return adaptee.cast(service.getWPS(monitor));
        }

        if (adaptee.isAssignableFrom(org.geotools.process.ProcessFactory.class)) {
            return adaptee.cast(processFactory);
        }
        
        if (adaptee.isAssignableFrom(ImageDescriptor.class)) {
            return adaptee.cast(getIcon(monitor));
        }

        return super.resolve(adaptee, monitor);
    }

    public WPSServiceImpl service( IProgressMonitor monitor ) throws IOException {
        return service;
    }

    /** Must be the same as resolve( ImageDescriptor.class ) */
    public ImageDescriptor getIcon( IProgressMonitor monitor ) {
        iconLock.lock();
        try {
            if (icon == null) {
                icon = fetchIcon(monitor, processFactory, service);
                if (icon == null) {
                    icon = CatalogUIPlugin.getDefault().getImageDescriptor(
                            ISharedImages.GRID_OBJ);
                }
            }
            return icon;
        } finally {
            iconLock.unlock();
        }
    }
    /**
     * This method will fetch the Icon associated with this url (if such is available).
     * 
     * @see WPSFolder
     * @param monitor
     * @return Requested Icon or ISharedImages.GRID_OBJ
     */
    protected static ImageDescriptor fetchIcon( IProgressMonitor monitor, ProcessFactory processFactory, WPSServiceImpl service ) {
        try {
            if (monitor != null)
                monitor.beginTask(Messages.WPSProcessImpl_acquiring_task, 3);
            if (monitor != null)
                monitor.worked(1);

//            if (process.getChildren() != null && process.getChildren().length != 0) {
//                // Do not request "parent" layer graphics - this kills Mapserver
//                return CatalogUIPlugin.getDefault().getImages().getImageDescriptor(
//                        ISharedImages.GRID_OBJ);
//            }

            ImageDescriptor imageDescriptor = requestImage(monitor, processFactory, service);

            Image image = null;
            Image swatch = null;
            try {
                if (monitor != null)
                    monitor.worked(2);
                if (monitor != null)
                    monitor.subTask(Messages.WPSProcessImpl_downloading_icon);
                image = imageDescriptor.createImage();
                Rectangle bound = image.getBounds();
                if (bound.width == 16 && bound.height == 16) {
                    final ImageData data = (ImageData) image.getImageData().clone();
                    return new ImageDescriptor(){
                        public ImageData getImageData() {
                            return (ImageData) data.clone();
                        }
                    };
                }
                if (bound.height < 16 || bound.width < 16) {
                    if (WpsPlugin.getDefault().isDebugging())
                        System.out.println("Icon scaled up to requested size"); //$NON-NLS-1$                                        
                    final ImageData data = image.getImageData().scaledTo(16, 16);
                    return new ImageDescriptor(){
                        public ImageData getImageData() {
                            return (ImageData) data.clone();
                        }
                    };
                }
                swatch = new Image(null, 16, 16);
                GC gc = new GC(swatch);
                int sy = 0; // (bound.height / 2 ) - 8;
                int sx = 0;
                int sw = 0;
                int sh = 0;
                ImageData contents = image.getImageData();
                if (contents == null) {
                    return CatalogUIPlugin.getDefault().getImageDescriptor(
                            ISharedImages.GRID_MISSING);
                }
                if (contents.maskData != null) {
                    // ((width + 7) / 8 + (maskPad - 1)) / maskPad * maskPad
                    int maskPad = contents.maskPad;
                    int scanLine = ((contents.width + 7) / 8 + (maskPad - 1)) / maskPad * maskPad;
                    // skip leading mask ...
                    SKIPY: for( int y = 0; y < contents.height / 2; y++ ) {
                        sy = y;
                        for( int x = 0; x < contents.width / 2; x++ ) {
                            int mask = contents.maskData[y * scanLine + x];
                            if (mask != 0)
                                break SKIPY;
                        }
                    }
                    SKIPX: for( int x = 0; x < contents.width / 2; x++ ) {
                        sx = x;
                        for( int y = sy; y < contents.height / 2; y++ ) {
                            int mask = contents.maskData[y * scanLine + x];
                            if (mask != 0)
                                break SKIPX;
                        }
                    }
                    sh = Math.min(contents.height - sy, 16);
                    sw = Math.min(contents.width - sx, 16);
                    if (WpsPlugin.getDefault().isDebugging())
                        System.out.println("Mask offset to " + sx + "x" + sy); //$NON-NLS-1$ //$NON-NLS-2$                        
                } else if (contents.alphaData != null) {
                    SKIPY: for( int y = 0; y < contents.height / 2; y++ ) {
                        sy = y;
                        for( int x = 0; x < contents.width / 2; x++ ) {
                            int alpha = contents.alphaData[y * contents.width + x];
                            if (alpha != 0)
                                break SKIPY;
                        }
                    }
                    SKIPX: for( int x = 0; x < contents.width / 2; x++ ) {
                        sx = x;
                        for( int y = sy; y < contents.height / 2; y++ ) {
                            int alpha = contents.alphaData[y * contents.width + x];
                            if (alpha != 0)
                                break SKIPX;
                        }
                    }
                    sh = Math.min(contents.height - sy, 16);
                    sw = Math.min(contents.width - sx, 16);
                    if (WpsPlugin.getDefault().isDebugging())
                        System.out.println("Alpha offset to " + sx + "x" + sy); //$NON-NLS-1$ //$NON-NLS-2$                        
                } else {
                    // try ignoring "white"
                    int depth = contents.depth;
                    int scanLine = contents.bytesPerLine;
                    SKIPY: for( int y = 0; y < contents.height / 2; y++ ) {
                        sy = y;
                        for( int x = 0; x < contents.width / 2; x++ ) {
                            int datum = contents.data[y * scanLine + x * depth];
                            if (datum != 0)
                                break SKIPY;
                        }
                    }
                    SKIPX: for( int x = 0; x < contents.width / 2; x++ ) {
                        sx = x;
                        for( int y = sy; y < contents.height / 2; y++ ) {
                            int datum = contents.data[y * scanLine + x * depth];
                            if (datum != 0)
                                break SKIPX;
                        }
                    }
                    sh = Math.min(contents.height - sy, 16);
                    sw = Math.min(contents.width - sx, 16);
                    if (WpsPlugin.getDefault().isDebugging())
                        System.out.println("Alpha offset to " + sx + "x" + sy); //$NON-NLS-1$ //$NON-NLS-2$                        
                }
                // else {
                // sh = Math.min( bound.height, bound.width );
                // sw = Math.min( bound.height, bound.width );
                // }
                if (WpsPlugin.getDefault().isDebugging())
                    System.out.println("Image resized to " + sh + "x" + sw); //$NON-NLS-1$ //$NON-NLS-2$

                gc.drawImage(image, sx, sy, sw, sh, 0, 0, 16, 16);
                final ImageData data = (ImageData) swatch.getImageData().clone();
                return new ImageDescriptor(){
                    public ImageData getImageData() {
                        return (ImageData) data.clone();
                    }
                };
            } finally {
                if (image != null) {
                    image.dispose();
                }
                if (swatch != null) {
                    swatch.dispose();
                }
                if (monitor != null)
                    monitor.worked(3);
            }
        } catch (IOException t) {
            WpsPlugin.trace("Could not get icon", t); //$NON-NLS-1$
            return CatalogUIPlugin.getDefault().getImageDescriptor(
                    ISharedImages.GRID_MISSING);
        }
    }

    @SuppressWarnings("unchecked")
    private static ImageDescriptor requestImage( IProgressMonitor monitor, org.geotools.process.ProcessFactory processFactory,
            WPSServiceImpl service ) throws IOException {
        WebProcessingService wps = service.getWPS(monitor);

        // TODO: is there any possible way to set a legend graphic in a wps capabilities doc?
        return CatalogUIPlugin.getDefault().getImageDescriptor(
                ISharedImages.GRID_OBJ); 
        
//        if (wps.getCapabilities().getRequest().getGetLegendGraphic() == null) {
//            return CatalogUIPlugin.getDefault().getImages().getImageDescriptor(
//                    ISharedImages.GRID_OBJ);
//        }
//
//        ImageDescriptor imageDescriptor = null;
//        try {
//            GetLegendGraphicRequest request = wps.createGetLegendGraphicRequest();
//            request.setLayer(layer.getName());
//            request.setWidth("16"); //$NON-NLS-1$
//            request.setHeight("16"); //$NON-NLS-1$
//
//            List<String> formats = wps.getCapabilities().getRequest()
//                    .getGetLegendGraphic().getFormats();
//            
//            Collections.sort(formats, new Comparator<String>(){
//
//                public int compare( String format1, String format2 ) {
//                    if( format1.trim().equalsIgnoreCase("image/png") ){ //$NON-NLS-1$
//                        return -1;
//                    }
//                    if( format2.trim().equalsIgnoreCase("image/png") ){ //$NON-NLS-1$
//                        return 1;
//                    }
//                    if( format1.trim().equalsIgnoreCase("image/gif") ){ //$NON-NLS-1$
//                        return -1;
//                    }
//                    if( format2.trim().equalsIgnoreCase("image/gif") ){ //$NON-NLS-1$
//                        return 1;
//                    }
//                    return 0;
//                }
//                
//            });
//            
//            for( Iterator<String> iterator = formats.iterator(); iterator.hasNext() && imageDescriptor==null; ) {
//                String format = iterator.next();
//                
//                imageDescriptor = loadImageDescriptor(wps, request, format );
//            }
//
//            if (imageDescriptor == null) {
//                // cannot understand any of the provided formats
//                return CatalogUIPlugin.getDefault().getImages().getImageDescriptor(
//                        ISharedImages.GRID_OBJ);
//            }
//        } catch (UnsupportedOperationException notAvailable) {
//            WpsPlugin.trace("Icon is not available", notAvailable); //$NON-NLS-1$                
//            return CatalogUIPlugin.getDefault().getImages().getImageDescriptor(
//                    ISharedImages.GRID_OBJ);
//        } catch (ServiceException e) {
//            WpsPlugin.trace("Icon is not available", e); //$NON-NLS-1$                
//            return CatalogUIPlugin.getDefault().getImages().getImageDescriptor(
//                    ISharedImages.GRID_OBJ);
//        }
//        
//        return imageDescriptor;
    }
    
//    private static ImageDescriptor loadImageDescriptor( WebProcessingService wps,
//            GetLegendGraphicRequest request, String desiredFormat ) throws IOException,
//            ServiceException {
//        // TODO: is there any way to issue a request to a WPS server for a legend graphic?
//        return null;
//        
////        if (desiredFormat == null) {
////            return null;
////        }
////        try {
////            ImageDescriptor imageDescriptor;
////            request.setFormat(desiredFormat);
////
////            request.setStyle(""); //$NON-NLS-1$
////
////            System.out.println(request.getFinalURL().toExternalForm());
////
////            GetLegendGraphicResponse response = wps.issueRequest(request);
////
////            imageDescriptor = ImageDescriptor.createFromImageData(getImageData(response
////                    .getInputStream()));
////            return imageDescriptor;
////        } catch (SWTException exc) {
////            WpsPlugin.trace("Icon is not available or has unsupported format", exc); //$NON-NLS-1$                
////            return null;
////        }
//    }

    private static ImageData getImageData( InputStream in ) {
        ImageData result = null;
        if (in != null) {
            try {
                result = new ImageData(in);
            } catch (SWTException e) {
                if (e.code != SWT.ERROR_INVALID_IMAGE)
                    throw e;
                // fall through otherwise
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    // System.err.println(getClass().getName()+".getImageData(): "+
                    // "Exception while closing InputStream : "+e);
                }
            }
        }
        return result;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    @Override
    public <T> boolean canResolve( Class<T> adaptee ) {
        if (adaptee == null) {
            return false;
        }

        if (adaptee.isAssignableFrom(IProcess.class)
                || adaptee.isAssignableFrom(WebProcessingService.class)
                || adaptee.isAssignableFrom(org.geotools.process.ProcessFactory.class)
                || adaptee.isAssignableFrom(ImageDescriptor.class)
                || adaptee.isAssignableFrom(IService.class) || super.canResolve(adaptee)) {
            return true;
        }

        return false;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return service.getMessage();
    }
    
    private class WPSProcessInfo extends IProcessInfo {

        @SuppressWarnings("unchecked")
        WPSProcessInfo(final ProcessFactory pf, final IProgressMonitor monitor ) throws IOException {

            this.processFactory = pf;

            String factoryName = pf.getClass().getSimpleName();
            String localName;
            if (factoryName.endsWith("Factory") && !factoryName.equals("Factory")) {
                localName = factoryName.substring(0, factoryName.length() - 7);
            } else {
                localName = factoryName;
            }

            processName = new NameImpl("gt", localName);

            WebProcessingService wps = service.getWPS(monitor);
            WPSCapabilitiesType caps = wps.getCapabilities();

            if (processFactory.getTitle(processName) != null && processFactory.getTitle(processName).length() != 0) {
                title = processFactory.getTitle(processName).toString();
            }

            // TODO: get input params
            //calculateBounds();

            String parentid = service != null && service.getIdentifier() != null ? getIdentifier().toString() : ""; //$NON-NLS-1$
            name = processFactory.getTitle(processName).toString();

            getKeywords(caps, parentid);

            if (processFactory.getDescription(processName) != null && processFactory.getDescription(processName).length() != 0) {
                description = processFactory.getDescription(processName).toString();
            } else {
                EList abstract1 = caps.getServiceIdentification().getAbstract();
                if (!abstract1.isEmpty() && abstract1.get(0).toString().length() != 0) {
                    description = abstract1.get(0).toString();
                }
                else {
                    description = "";
                }
            }
            EList abstract1 = caps.getServiceIdentification().getAbstract();
            if (!abstract1.isEmpty() && abstract1.get(0).toString().length() != 0) {
                description = abstract1.get(0).toString();
            }
            else {
                description = "";
            }

            icon = CatalogUIPlugin.getDefault().getImageDescriptor(ISharedImages.GRID_OBJ);

            //icon = fetchIcon( monitor, this.processFactory );
        }

        private void getKeywords( WPSCapabilitiesType caps, String parentid ) {
            List<String> keywordsFromWPS = new ArrayList<String>();
            keywordsFromWPS = WPSUtils.getKeywords(caps, keywordsFromWPS);

            // TODO:  should processes have keywords?  Currently they do not
//            if (process.getKeywords() != null) {
//                keywordsFromWPS.addAll(Arrays.asList(process.getKeywords()));
//            }

            keywordsFromWPS.add("WPS"); //$NON-NLS-1$
            keywordsFromWPS.add(processFactory.getTitle(processName).toString());
            if (!caps.getServiceIdentification().getTitle().isEmpty() &&
                    caps.getServiceIdentification().getTitle().get(0).toString().length() != 0) {
                keywordsFromWPS.add(caps.getServiceIdentification().getTitle().get(0).toString());
            }
            keywordsFromWPS.add(parentid);
            keywords = keywordsFromWPS.toArray(new String[keywordsFromWPS.size()]);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public URI getSchema() {
            
            return WMSSchema.NAMESPACE;

//            try {
//                return new URI("http://www.opengis.net/wps"); //$NON-NLS-1$
//            } catch (URISyntaxException e) {
//                // do nothing
//                return null;
//            }
        }

        @Override
        public String getTitle() {
            return title;
        }
    }
}