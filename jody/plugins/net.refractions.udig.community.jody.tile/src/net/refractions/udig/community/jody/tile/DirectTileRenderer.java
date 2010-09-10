package net.refractions.udig.community.jody.tile;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.internal.render.impl.RendererImpl;
import net.refractions.udig.project.render.IRenderer;
import net.refractions.udig.project.render.RenderException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.event.GTAdapter;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.renderer.lite.GridCoverageRenderer;
import org.geotools.tile.TileMap;
import org.geotools.tile.ZoomLevel;
import org.geotools.tile.cache.TileRange;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class DirectTileRenderer extends RendererImpl implements IRenderer {
    
    private Job load; // no longer used
    private Set<String> drawn=new HashSet(); // names of drawn grid coverages
    
    public void render( final Graphics2D destination, IProgressMonitor monitor ) throws RenderException {
        if( monitor == null ) monitor = new NullProgressMonitor();
        monitor.beginTask("Render TileMap", 100);
        setState( STARTING );        
        IGeoResource handle = getContext().getLayer().findGeoResource(TileMap.class);
        try {
            TileMap tileMap = handle.resolve( TileMap.class, new SubProgressMonitor(monitor,30) );
            Envelope bounds = getRenderBounds();            
            if( bounds == null ) {
                ReferencedEnvelope viewbounds = (ReferencedEnvelope) getContext().getViewportModel().getBounds();
                if( getContext().getCRS().equals( viewbounds.getCRS())){
                    bounds = viewbounds;
                }
                else {
                    // TODO: reproject
                    throw new RenderException("Need to aquire a valid request bounds");
                }
            }
            ZoomLevel zero = (ZoomLevel) tileMap.getInfo().getZoomLevels().iterator().next();
            final TileRange range = tileMap.getTileRange(bounds, (int) getContext().getViewportModel().getScaleDenominator());            
            if (range == TileRange.EMPTY ){
                setState(DONE);                 
                return;                                
            }
            else {
                // load
                if(range.isLoaded() ){
                    drawTiles( destination, range, true ); // instant feedback
                    setState(DONE);                     
                }
                else {
                    drawTiles( destination, range, true ); // instant feedback
                    setState(RENDERING);
                    final IProgressMonitor progress = monitor;
                    range.load( new IProgressMonitor(){
                        public void beginTask( String name, int totalWork ) {
                            progress.beginTask(name, totalWork);
                        }
                        public void done() {
                            drawTiles( destination, range, false );
                            setState(DONE);
                        }
                        public void internalWorked( double work ) {
                            progress.internalWorked( work );
                        }
        
                        public boolean isCanceled() {
                            return progress.isCanceled();
                        }
        
                        public void setCanceled( boolean value ) {
                            progress.setCanceled( value );
                            setState(CANCELLED);                            
                        }
        
                        public void setTaskName( String name ) {
                            progress.setTaskName(name);
                        }
        
                        public void subTask( String name ) {
                            progress.subTask( name );
                        }
        
                        public void worked( int work ) {
                            drawTiles( destination, range, false );
                            setState(RENDERING);
                            progress.worked(work);
                        }
                    });
                }                               
            }
        }
        catch(IOException bad){
            throw new RenderException( bad );
        }
    }
    /*
    public void renderAttempt1( Graphics2D destination, IProgressMonitor monitor ) throws RenderException {
        if( monitor == null ) monitor = new NullProgressMonitor();
        monitor.beginTask("Render TileMap", 100);
        setState( STARTING );        
        IGeoResource handle = getContext().getLayer().findGeoResource(TileMap.class);
        try {
            TileMap tileMap = handle.resolve( TileMap.class, new SubProgressMonitor(monitor,30) );
            Envelope bounds = getRenderBounds();            
            if( bounds == null ) {
                ReferencedEnvelope viewbounds = (ReferencedEnvelope) getContext().getViewportModel().getBounds();
                if( getContext().getCRS().equals( viewbounds.getCRS())){
                    bounds = viewbounds;
                }
                else {
                    // TODO: reproject
                    throw new RenderException("Need to aquire a valid request bounds");
                }
            }
            final TileRange range = tileMap.getTileRange(bounds, (int) getContext().getViewportModel().getScaleDenominator());
            
            if( tiles != null && tiles.equals( range )){
                // we are already loading this one
                System.out.println("Already loading "+tiles);
            }
            else if ( range.isLoaded()){
                tiles = range; // we stayed within the cache!
            }
            else { // we need to load                
                tiles = range;                
                if( load != null ){
                    load.cancel();
                    load = null;
                }
                load = new Job("load"){
                    protected IStatus run( final IProgressMonitor monitor ) {
                        range.load( new IProgressMonitor(){
                            public void beginTask( String name, int totalWork ) {
                                monitor.beginTask(name, totalWork);
                            }
                            public void done() {
                                monitor.done();
                                setState( RENDERING );
                            }

                            public void internalWorked( double work ) {
                                monitor.internalWorked( work );
                            }

                            public boolean isCanceled() {
                                return monitor.isCanceled();
                            }

                            public void setCanceled( boolean value ) {
                                monitor.setCanceled( value );
                            }

                            public void setTaskName( String name ) {
                                monitor.setTaskName(name);
                            }

                            public void subTask( String name ) {
                                monitor.subTask( name );
                            }

                            public void worked( int work ) {
                                setState( RENDERING );                                
                                monitor.worked(work);
                            }                            
                        });
                        return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
                    }                    
                };               
            }
            drawTiles( destination, tiles );
        }
        catch (IOException erp ){
            throw new RenderException( erp );
        }
        finally {
            setState( DONE );
        }
    }
    */    
    private void drawTiles( Graphics2D graphics, TileRange range, boolean all) {
        if( range == TileRange.EMPTY ) return;

        if( all ) drawn.clear(); // don't skip
        
        //double scale = getContext().getViewportModel().getScaleDenominator();
        //if(scale < state.minScale || scale > state.maxScale) return;
        float opacity = 1.0f;
        
        CoordinateReferenceSystem crs = getContext().getViewportModel().getCRS();
        
        Composite oldComposite = graphics.getComposite();        
        AffineTransform graphicsTransform = graphics.getTransform();
        
        for( Iterator i=range.getTiles().iterator(); i.hasNext(); ){
            GridCoverage2D coverage = (GridCoverage2D) i.next();
            Envelope2D bounds = coverage.getEnvelope2D();
            
            //Point p = getContext().worldToPixel( 
            //g.fillOval(p.x, p.y, 10, 10);
            if( coverage.getRenderedImage().getWidth() == 512 ){
                // real image
                String name = coverage.getName().toString();
                if( drawn.contains( name ) ){
                    continue; // skip this image as we have drawn it "last" time                    
                }
                else {
                    drawn.add( name ); // mark this one as drawn for next time
                }
            }
            else {
                // placeholder!
                if( !all ) continue; // skip if this is not the first draw
            }
            GridCoverageRenderer paint = new GridCoverageRenderer(coverage, crs);            
            try {
                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                
                AffineTransform toScreen = getContext().worldToScreenTransform();
                AffineTransform transform  = new AffineTransform(graphicsTransform);                
                transform.concatenate(toScreen);
                
                graphics.setTransform( transform );
                paint.paint( graphics );                               
            }
            catch ( Throwable t ){
                if( coverage != null ){
                    System.out.println("paint tile:"+coverage.getName());
                }
                t.printStackTrace();
                break; // just one for now
            }
            finally {
                graphics.setTransform(graphicsTransform);                
            }
        }       
        graphics.setComposite(oldComposite);
    }
    
    public void render( IProgressMonitor monitor ) throws RenderException {
        Graphics2D g2 = (Graphics2D) context.getImage().getGraphics();
        render( g2, monitor );        
    }
}