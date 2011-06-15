/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.ui.editingtools.parallel;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.tools.edit.Activator;
import net.refractions.udig.tools.edit.EditToolHandler;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import es.axios.udig.ui.editingtools.internal.behaviour.DefaultEditPointProvider;
import es.axios.udig.ui.editingtools.internal.behaviour.IEditPointProvider;

/**
 * Base class for actions triggered by an edit tool constraint, providing template and helper
 * methods to ease the task of adding behaviours or taking comlpete control of the currently active
 * edit tool.
 * <p>
 * Actionset actions configured to for the toolbarPath
 * <code>"net.refractions.udig.tool.edit.behaviour"</code> will be grouped together and should
 * extend this base class minimally by implementing {@link #activate(EditToolHandler)}.
 * </p>
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public abstract class AbstractEditModifierActionDelegate
        implements
            IWorkbenchWindowActionDelegate,
            IActionDelegate2 {

    private EditToolContributorsActivator editToolContributorsActivator;

    /**
     * The edit tool handler active when this mode is activated. {@link #activate()} implementors
     * are free to modify its state. The original state will be restored on {@link #deactivate()}
     */
    private EditToolHandler               currentHandler;

    /**
     * @see IWorkbenchWindowActionDelegate#dispose()
     */
    public final void dispose() {
    }

    /**
     * @see IActionDelegate2#init(IAction)
     */
    public final void init( IAction action ) {
    }

    /**
     * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
     */
    public final void init( IWorkbenchWindow window ) {
    }

    /**
     * Shouldn't be called by the RCP, but {@link #runWithEvent(IAction, Event)} instead
     * 
     * @see IActionDelegate#run(IAction)
     */
    public final void run( IAction action ) {
        throw new UnsupportedOperationException("shouldn't be called. runWithEvent "
                + "should be called instead");
    }

    /**
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public final void selectionChanged( final IAction action, final ISelection selection ) {
    }

    public final void runWithEvent( final IAction action, final Event event ) {
        final IMap activeMap = ApplicationGIS.getActiveMap();
        if (activeMap == null) {
            return;
        }

        final Widget widget = event.widget;
        final ToolItem toolItem = (ToolItem) widget;
        final boolean enabled = toolItem.getSelection();

        final IBlackboard mapBlackBoard = activeMap.getBlackboard();

        currentHandler = getEditToolHandler(mapBlackBoard);

        if (enabled) {
            editToolContributorsActivator = new EditToolContributorsActivator();
            editToolContributorsActivator.activate(currentHandler);
            activate();
            currentHandler.getActivators().add(editToolContributorsActivator);
        } else {
            deactivate();
        }

    }

    /**
     * Obtains the currently being used edit tool handler (i.e. the {@link EditToolHandler} held in
     * the provided blackboard)
     * 
     * @param blackboard the {@link IBlackboard map blackboard} from where to obtain the current
     *        {@link EditToolHandler}
     * @return the {@link EditToolHandler} stored in the <code>blackboard</code>
     * @throws IllegalStateException if <code>blackboard</code> does not contains the
     *         {@link EditToolHandler}
     */
    private EditToolHandler getEditToolHandler( IBlackboard blackboard ) {
        final String key = EditToolHandler.class.getName();
        final EditToolHandler currentHandler = (EditToolHandler) blackboard.get(key);
        if (currentHandler == null) {
            throw new IllegalStateException("the provided blackboard does not "
                    + "holds the EditToolHandler");
        }
        return currentHandler;
    }

    protected EditToolHandler getHandler() {
        return currentHandler;
    }

    /**
     * When called, it is granted that <code>currentHandler</code> is not null, and that
     * {@link #eventBehaviours} contains the current {@link EditToolHandler}'s event behaviours,
     * and modifying that list produces an immediate effect.
     * <p>
     * Implementations are allowed to modify the contents of the protected lists of behaviours
     * directly or to reconfigure <code>currentHandler</code> as they want.
     * </p>
     */
    protected abstract void activate();

    /**
     * Called when the modifier needs to be disabled because the user selected another modifier or
     * explicitly disabled the running one.
     * <p>
     * Restores the current {@link EditToolHandler}'s lists of behaviours to its original state.
     * </p>
     */
    private final void deactivate() {
        editToolContributorsActivator.deactivate(currentHandler);
    }

    /**
     * Utility method for subclasses to temporally set a new {@link IEditPointProvider}.
     * <p>
     * The provider will last until another one is set or until the mode is deactivated.
     * </p>
     * 
     * @param provider
     */
    protected void setEditPointProvider( IEditPointProvider provider ) {
        editToolContributorsActivator.setEditPointProvider(currentHandler, provider);
    }

    /**
     * Adds and activates an Activator to the EditToolHandler's activators list
     * 
     * @param handler
     * @param activator
     */
    protected void addActivator( Activator activator ) {
        currentHandler.getActivators().add(activator);
        activator.activate(currentHandler);
    }

    /**
     * Restores and re-activates the orgiginal activators
     */
    // protected void restoreOriginalActivators() {
    // editToolContributorsActivator.restoreOriginalActivators(currentHandler);
    // }
    /**
     * Removes and deactivates the currently running activators. The firs time called, it means the
     * current edit tool original ones, but nothing stops a subclass from calling this method more
     * than once.
     */
    protected void removeRunningActivators() {
        editToolContributorsActivator.removeRunningActivators(currentHandler);
    }

    private static class EditToolContributorsActivator implements Activator {

        /**
         * Backup of the original edit point provider so it can be restored at deactivation time
         */
        private IEditPointProvider originalEditPointProvider;

        /**
         * Internal handler used to backup the original activators and behaviours comming from the
         * handler in use so they can be restored at mode deactivation time and free subclasses to
         * extend or override the list of activators and handlers while they're active.
         */
        private EditToolHandler    backup;

        /**
         * Called when the mode using this instance has to be activated.
         */
        public void activate( final EditToolHandler handler ) {
            final String key = IEditPointProvider.BLACKBOARD_KEY;
            final IBlackboard mapBlackBoard = getMapBlackboard(handler);
            originalEditPointProvider = (IEditPointProvider) mapBlackBoard.get(key);
            if (null == originalEditPointProvider) {
                originalEditPointProvider = new DefaultEditPointProvider();
                setEditPointProvider(handler, originalEditPointProvider);
            }
            backup = new EditToolHandler((Cursor) null, (Cursor) null);
            backup.getActivators().addAll(handler.getActivators());
            backup.getAcceptBehaviours().addAll(handler.getAcceptBehaviours());
            backup.getBehaviours().addAll(handler.getBehaviours());
            backup.getCancelBehaviours().addAll(handler.getCancelBehaviours());
        }

        /**
         * Called when the current edit tool is deactivated (as a consecuence of this being an
         * {@link Activator}, or when the mode this instance is working on is deactivated (because
         * another one was selected).
         */
        public void deactivate( EditToolHandler handler ) {
            removeRunningActivators(handler);
            restoreOriginalActivators(handler);

            handler.getActivators().clear();
            handler.getAcceptBehaviours().clear();
            handler.getBehaviours().clear();
            handler.getCancelBehaviours().clear();

            handler.getActivators().addAll(backup.getActivators());
            handler.getAcceptBehaviours().addAll(backup.getAcceptBehaviours());
            handler.getBehaviours().addAll(backup.getBehaviours());
            handler.getCancelBehaviours().addAll(backup.getCancelBehaviours());

            setEditPointProvider(handler, originalEditPointProvider);
            originalEditPointProvider = null;
        }

        public void setEditPointProvider( EditToolHandler handler, final IEditPointProvider provider ) {
            final IBlackboard mapBlackboard = getMapBlackboard(handler);
            mapBlackboard.put(IEditPointProvider.BLACKBOARD_KEY, provider);
        }

        private void restoreOriginalActivators( EditToolHandler currentHandler ) {
            for( Activator runnable : backup.getActivators() ) {
                try {
                    currentHandler.getActivators().add(runnable);
                    runnable.activate(currentHandler);
                } catch (Throwable error) {
                    runnable.handleActivateError(currentHandler, error);
                }
            }
        }

        public void removeRunningActivators( EditToolHandler handler ) {
            for( Activator runnable : handler.getActivators() ) {
                if (runnable != this) {
                    try {
                        runnable.deactivate(handler);
                    } catch (Throwable error) {
                        runnable.handleDeactivateError(handler, error);
                    }
                }
            }
            handler.getActivators().clear();
        }

        private IBlackboard getMapBlackboard( final EditToolHandler handler ) {
            final IToolContext context = handler.getContext();
            final IMap map = context.getMap();
            final IBlackboard mapBlackboard = map.getBlackboard();
            return mapBlackboard;
        }

        public void handleActivateError( EditToolHandler handler, Throwable error ) {
            // TODO: handle
            error.printStackTrace();
        }

        public void handleDeactivateError( EditToolHandler handler, Throwable error ) {
            // TODO: handle
            error.printStackTrace();
        }
    }
}
