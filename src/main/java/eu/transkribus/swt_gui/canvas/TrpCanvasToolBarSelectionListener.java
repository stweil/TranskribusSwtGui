package eu.transkribus.swt_gui.canvas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.canvas.CanvasMode;
import eu.transkribus.swt_canvas.canvas.CanvasToolBar;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.canvas.listener.CanvasToolBarSelectionListener;
import eu.transkribus.swt_gui.dialogs.ImageEnhanceDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class TrpCanvasToolBarSelectionListener extends CanvasToolBarSelectionListener {

	private final static Logger logger = LoggerFactory.getLogger(TrpCanvasToolBarSelectionListener.class);
	
	TrpCanvasToolBar toolbar;
	TrpSWTCanvas canvas;
	
	
	public TrpCanvasToolBarSelectionListener(TrpCanvasToolBar toolbar,
			TrpSWTCanvas canvas) {
		super((CanvasToolBar) toolbar, (SWTCanvas) canvas);
		this.toolbar = toolbar;
		this.canvas = canvas;
	}
	
	@Override protected CanvasMode getModeForSelectionEvent(SelectionEvent e) {
		if (e.getSource().equals(toolbar.addSpecialRegion.ti)) {
			logger.debug("getting mode for special region toolitem...");
			if (e.detail != SWT.ARROW) {
				CanvasMode mode = toolbar.getModeMap().get(toolbar.addSpecialRegion.getSelected());
				return mode!=null ? mode : CanvasMode.SELECTION;
			} else
				return CanvasMode.SELECTION;
		} else
			return super.getModeForSelectionEvent(e);
		
//		CanvasMode mode = toolbar.getModeMap().get(e.getSource());
//		return mode!=null ? mode : CanvasMode.SELECTION;
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		try {
			logger.debug("toolbar item selected: "+e);
			
			super.widgetSelected(e);
			
			if (canvas.getMode() == TrpCanvasAddMode.ADD_OTHERREGION) {
				TrpCanvasAddMode.ADD_OTHERREGION.data = canvas.getMainWidget().getCanvasWidget().getToolBar().getSelectedSpecialRegionType(); 
			}
				
			Object s = e.getSource();
			
			if (s == toolbar.getViewSettingsMenuItem()) {
				canvas.getMainWidget().getUi().openViewSetsDialog();
			}
			else if (s == toolbar.getImageVersionItem().ti && e.detail != SWT.ARROW) {
				TrpMainWidget.getInstance().reloadCurrentImage();
			}
			else if (s == toolbar.getImgEnhanceItem()) {
				// TODO: open enhance dialog
				ImageEnhanceDialog d = new ImageEnhanceDialog(canvas.getShell());
				d.open();
			}
		} catch (Exception ex) {
			canvas.getMainWidget().onError("Error", ex.getMessage(), ex);
		}
	}

}
