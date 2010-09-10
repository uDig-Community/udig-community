package net.refractions.udig.community.jody.tile;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.media.jai.util.Range;

import org.geotools.tile.TileMap;
import org.geotools.tile.TileServer;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.render.Renderer;
import net.refractions.udig.project.render.IRenderContext;
import net.refractions.udig.project.render.IRenderMetrics;
import net.refractions.udig.project.render.IRenderMetricsFactory;
import net.refractions.udig.project.render.IRenderer;

/**
 * Will be chosen for TileService where the TileRange is not loaded.
 * 
 * @author Jody Garnett
 * @since 1.1.0
 */
public class DirectTileFactory implements IRenderMetricsFactory {

    /**
     * Use this renderer for TileServer.class.
     */
    public boolean canRender( IRenderContext context ) throws IOException {
        return context.getLayer().findGeoResource( TileMap.class ) != null;
    }

    public IRenderMetrics createMetrics( final IRenderContext context ) {
        return new IRenderMetrics(){
            /**
             * This seems confused?
             */
            public boolean canAddLayer( ILayer layer ) {
                return context.getLayer() == layer;
            }
            /** Style not used by renderer */
            public boolean canStyle( String styleID, Object value ) {
                return false;
            }

            public Renderer createRenderer() {
                return new DirectTileRenderer();
            }

            public IRenderContext getRenderContext() {
                return context;
            }

            public IRenderMetricsFactory getRenderMetricsFactory() {
                return DirectTileFactory.this;
            }

            /**
             * This is not an optimized renderer - we need to
             * actually inspect the TileRange before choosing
             * if we are appropriate.
             */
            public boolean isOptimized() {
                return false;
            }
            public Set<Range> getValidScaleRanges() {
                return Collections.emptySet();
            }
        };
    }
    
    public Class< ? extends IRenderer> getRendererType() {
        return DirectTileRenderer.class;
    }

}
