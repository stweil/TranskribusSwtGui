package eu.transkribus.swt_gui.htr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.MultiCheckSelectionCombo;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.ToolBox;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.StructCustomTagSpec;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrDocPagesOrCollectionSelector;
import eu.transkribus.util.TextRecognitionConfig;

public class HtrTextRecognitionDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(HtrTextRecognitionDialog.class);
	
	private HtrTextRecognitionConfigDialog trcd = null;
	
	private Button thisPageBtn, severalPagesBtn;
	private CurrentTranscriptOrDocPagesOrCollectionSelector dps;
	private boolean docsSelected = false;
	private List<DocumentSelectionDescriptor> selectedDocDescriptors;
	
	private Button doLinePolygonSimplificationBtn, keepOriginalLinePolygonsBtn, doStoreConfMatsBtn;
	private MultiCheckSelectionCombo multiCombo;
	
	private Storage store = Storage.getInstance();
	
	private TextRecognitionConfig config;
	private String pages;
	
	ToolBar structureBar;
	ToolItem structureItem;
	ToolBox structureBox;
	
	List<String> selectionArray = new ArrayList<>();
	
	public HtrTextRecognitionDialog(Shell parent) {
		super(parent);
	}
    
	public void setVisible() {
		if(super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(3, false));
		
		//FIXME the document selection is not initialized before the selection dialog is opened once
		//with this selector jobs can be started for complete collections
		dps = new CurrentTranscriptOrDocPagesOrCollectionSelector(cont, SWT.NONE, true,true);		
		dps.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 3, 1));
		
//		thisPageBtn = new Button(cont, SWT.RADIO);
//		thisPageBtn.setText("On this page");
//		thisPageBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
//		thisPageBtn.setSelection(true);
//		
//		severalPagesBtn = new Button(cont, SWT.RADIO);
//		severalPagesBtn.setText("Pages:");
//		severalPagesBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		
//		dps = new DocPagesSelector(cont, SWT.NONE, false, store.getDoc().getPages());
//		dps.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		dps.setEnabled(false);
//		
//		severalPagesBtn.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				dps.setEnabled(severalPagesBtn.getSelection());
//			}
//		});
		
		doLinePolygonSimplificationBtn = new Button(cont, SWT.CHECK);
		doLinePolygonSimplificationBtn.setText("Do polygon simplification");
		doLinePolygonSimplificationBtn.setToolTipText("Perform a line polygon simplification after the recognition process to reduce the number of output points and thus the size of the file");
		doLinePolygonSimplificationBtn.setSelection(true);
		doLinePolygonSimplificationBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		keepOriginalLinePolygonsBtn = new Button(cont, SWT.CHECK);
		keepOriginalLinePolygonsBtn.setText("Keep original line polygons");
		keepOriginalLinePolygonsBtn.setToolTipText("Keep the original line polygons after the recognition process, e.g. if they have been already corrected");
		keepOriginalLinePolygonsBtn.setSelection(false);
		keepOriginalLinePolygonsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		doStoreConfMatsBtn = new Button(cont, SWT.CHECK);
		doStoreConfMatsBtn.setText("Enable Keyword Spotting");
		doStoreConfMatsBtn.setToolTipText("The internal recognition result respresentation, needed for keyword spotting, will be stored in addition to the transcription.");
		doStoreConfMatsBtn.setSelection(true);
		doStoreConfMatsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		List<StructCustomTagSpec> tags = new ArrayList<>();
		tags = store.getStructCustomTagSpecs();
		
//		multiCombo = new MultiCheckSelectionCombo(cont, SWT.FILL,"Restrict on structure tags", 1, 200, 300 );
//		multiCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));		
//		
//		for(StructCustomTagSpec tag : tags) {
//			int itemCount = multiCombo.getItemCount();
//			List<String> items = new ArrayList<>();
//			for(int i = 0; i < itemCount; i++) {
//				items.add(multiCombo.getItem(i));
//			}	
//			if(!items.contains(tag.getCustomTag().getType())) {
//				multiCombo.add(tag.getCustomTag().getType());
//			}	
//		}
		
		structureBar = new ToolBar(cont, SWT.FILL);
		
		structureItem = new ToolItem(structureBar, SWT.CHECK);
		structureItem.setToolTipText("Choose structures...");
		structureItem.setText("Restrict on structure tags");
		
		structureBox = new ToolBox(getShell(), true, "Structures");
		structureBox.addTriggerWidget(structureItem);
		
		for(StructCustomTagSpec tag : tags) {
			Button button = structureBox.addButton(tag.getCustomTag().getType(), null, SWT.CHECK);
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					if(selectionArray.contains(tag.getCustomTag().getType())) {
						selectionArray.remove(tag.getCustomTag().getType());
					}else {
						selectionArray.add(tag.getCustomTag().getType());
					}
			      }
			});
			
		}
		
		SWTUtil.onSelectionEvent(keepOriginalLinePolygonsBtn, e -> {
			doLinePolygonSimplificationBtn.setEnabled(!keepOriginalLinePolygonsBtn.getSelection());
		});
		doLinePolygonSimplificationBtn.setEnabled(!keepOriginalLinePolygonsBtn.getSelection());
		
		Text configTxt = new Text(cont, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);
		configTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 6));
				
		Button configBtn = new Button(cont, SWT.PUSH);
		configBtn.setText("Select HTR model...");
		configBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		configBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(trcd == null) {
					trcd = new HtrTextRecognitionConfigDialog(parent.getShell(), config);
					if(trcd.open() == IDialogConstants.OK_ID) {
						logger.info("OK pressed");
						config = trcd.getConfig();
						configTxt.setText(config.toString());
						store.saveTextRecognitionConfig(config);
					}
					trcd = null;
				} else {
					trcd.setVisible();
				}
			}
		});
		
		config = store.loadTextRecognitionConfig();
		logger.debug("Config loaded:" + config);
		if(config != null) {
			configTxt.setText(config.toString());
		}
		return cont;
	}
	
	public boolean isDocsSelection(){
		return docsSelected;
	}
	
	public List<DocumentSelectionDescriptor> getDocs(){
		return selectedDocDescriptors;
	}
	
	@Override
	protected void okPressed() {

		if(dps.isCurrentTranscript()) {
			pages = ""+store.getPage().getPageNr();
		} else if(!dps.isDocsSelection()) {
			pages = dps.getPagesStr();
			if(pages == null || pages.isEmpty()) {
				DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Please specify pages for recognition.");
				return;
			}
			
			try {
				CoreUtils.parseRangeListStr(pages, store.getDoc().getNPages());
			} catch (IOException e) {
				DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Page selection is invalid.");
				return;
			}
		} else {
			docsSelected = dps.isDocsSelection();
			selectedDocDescriptors = dps.getDocumentsSelected();
			if(CollectionUtils.isEmpty(selectedDocDescriptors)) {
				DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "No documents selected for recognition.");
				return;
			}
		}
		
		if(config == null) {
			DialogUtil.showErrorMessageBox(getShell(), "Bad Configuration", "Please define a configuration.");
			return;
		}
		
		config.setStructures(selectionArray);
		config.setKeepOriginalLinePolygons(keepOriginalLinePolygonsBtn.getSelection());
		config.setDoLinePolygonSimplification(doLinePolygonSimplificationBtn.getSelection());
		config.setDoStoreConfMats(doStoreConfMatsBtn.getSelection());
		
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Text Recognition");
		if (Storage.getInstance().isAdminLoggedIn())
			newShell.setMinimumSize(300, 430);
		else{
			newShell.setMinimumSize(300, 400);
		}
	}

	@Override
	protected Point getInitialSize() {
//		return new Point(300, 400);
		return SWTUtil.getPreferredOrMinSize(getShell(), 300, 400);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
	
	public TextRecognitionConfig getConfig() {
		return this.config;
	}
	
	public String getPages() {
		return this.pages;
	}

}
