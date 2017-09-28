package eu.transkribus.swt_gui.htr;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.mihalis.opal.propertyTable.PTProperty;
import org.mihalis.opal.propertyTable.PropertyTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.GsonUtil;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.CurrentDocPagesSelector;

public class Text2ImageConfComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(Text2ImageConfComposite.class);
	
	LabeledText epochsTxt;
	LabeledText subsamplingTxt;
	Button removeLineBreaksCheck;
	LabeledText numberOfThreadsTxt;
	
	LabeledText trainSizePerEpochTxt;
	CitlabNoiseParamCombo noiseCmb;
	LearningRateCombo learningRateCombo;
	
	HtrModelChooserButton baseModelBtn;
	
	Text additonalParameters;
	PropertyTable advancedPropertiesTable;
	
	CurrentDocPagesSelector currentDocPagesSelector;
	
	public Text2ImageConfComposite(Composite parent, int flags) {
		super(parent, flags);
		this.setLayout(new GridLayout(2, false));
		
		currentDocPagesSelector = new CurrentDocPagesSelector(this, 0, true, true, true);
		currentDocPagesSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label labelBaseModel = new Label(this, 0);
		labelBaseModel.setText("Base model:");
		
		baseModelBtn = new HtrModelChooserButton(this);
		baseModelBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		epochsTxt = new LabeledText(this, "Epochs: ");
		epochsTxt.setText(CitLabSemiSupervisedHtrTrainConfig.DEFAULT_TRAINING_EPOCHS);
		epochsTxt.setToolTipText("A series of training epochs per iteration divided by semicolons - enter an empty string for no training at all");
		epochsTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		subsamplingTxt = new LabeledText(this, "Subsampling: ");
		subsamplingTxt.setText(""+CitLabSemiSupervisedHtrTrainConfig.DEFAULT_SUBSAMPLING);
		subsamplingTxt.setToolTipText("The number of subsets the document is divided into - max is the number of pages");
		subsamplingTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		removeLineBreaksCheck = new Button(this, SWT.CHECK);
		removeLineBreaksCheck.setText("Remove line breaks");
		removeLineBreaksCheck.setToolTipText("If checked line breaks in the input text are not respected");
		removeLineBreaksCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label noiseLbl = new Label(this, SWT.NONE);
		noiseLbl.setText("Noise:");
		noiseCmb = new CitlabNoiseParamCombo(this, 0);
		noiseCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		trainSizePerEpochTxt = new LabeledText(this, "Train size per epoch:");
		trainSizePerEpochTxt.setText(""+CitLabSemiSupervisedHtrTrainConfig.DEFAULT_TRAIN_SIZE_PER_EPOCH);
		trainSizePerEpochTxt.setToolTipText("The number of lines per epoch that is used for training");
		trainSizePerEpochTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label lrLbl = new Label(this, SWT.NONE);
		lrLbl.setText("Learning rate:");
		learningRateCombo = new LearningRateCombo(this, 0);
		learningRateCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		numberOfThreadsTxt = new LabeledText(this, "Number of threads:");
		numberOfThreadsTxt.setText(""+CitLabSemiSupervisedHtrTrainConfig.DEFAULT_NUMBER_OF_THREADS);
		numberOfThreadsTxt.setToolTipText("The number of threads that is used on the server to process this job");
		numberOfThreadsTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		initAdditionalParametersUi();
	}
		
	private void initAdditionalParametersUi() {
		ExpandableComposite exp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
		exp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		additonalParameters = new Text(exp, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	    additonalParameters.setLayoutData(new GridData(GridData.FILL_BOTH));
		additonalParameters.setToolTipText("Advanced parameters for Text2Image - use key=value format in each line!");
//		advancedParameters.setText("hyphen=null\n");
		
//		advancedPropertiesTable = buildPropertyTable(exp, true);
		
		exp.setClient(additonalParameters);
		exp.setText("Additional Parameters");
//		Fonts.setBoldFont(exp);
		exp.setExpanded(true);
		exp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				layout();
			}
		});
	}
	
	@Deprecated
    private static PropertyTable buildPropertyTable(Composite parent, /*boolean showButton, boolean showAsCategory,*/ boolean showDescription) {
        PropertyTable table = new PropertyTable(parent, SWT.CHECK);

//        if (showButton) {
//                table.showButtons();
//        } else {
//                table.hideButtons();
//        }
//
//        if (showAsCategory) {
//                table.viewAsCategories();
//        } else {
//                table.viewAsFlatList();
//        }
        
        
        table.hideButtons();
        table.viewAsFlatList();

        if (showDescription) {
                table.showDescription();
        } else {
                table.hideDescription();
        }
        
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_HYPHEN, null);
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_HYPHEN_LANG, null);
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_JUMP_BASELINE, null);
//        
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_SKIP_WORD, "3.0");
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_SKIP_BASELINE, "0.3");
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_BEST_PATHES, "200.0");
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_THRESH, "0.01");
        
        PTProperty hyphenProperty = new PTProperty("hyphen", "hyphen", "Description for identifier", "value");
        
        table.addProperty(new PTProperty("hyphen", "hyphen", "Description for identifier", "value"));

//        table.addProperty(new PTProperty("id", "Identifier", "Description for identifier", "My id")).setCategory("General");
//        table.addProperty(new PTProperty("text", "Description", "Description for the description field", "blahblah...")).setCategory("General");
//        table.addProperty(new PTProperty("url", "URL:", "This is a nice <b>URL</b>", "http://www.google.com").setCategory("General")).setEditor(new PTURLEditor());
//        table.addProperty(new PTProperty("password", "Password", "Enter your <i>password</i> and keep it secret...", "password")).setCategory("General").setEditor(new PTPasswordEditor());
//
//        table.addProperty(new PTProperty("int", "An integer", "Type any integer", "123")).setCategory("Number").setEditor(new PTIntegerEditor());
//        table.addProperty(new PTProperty("float", "A float", "Type any float", "123.45")).setCategory("Number").setEditor(new PTFloatEditor());
//        table.addProperty(new PTProperty("spinner", "Another integer", "Use a spinner to enter an integer")).setCategory("Number").setEditor(new PTSpinnerEditor(0, 100));
//
//        table.addProperty(new PTProperty("directory", "Directory", "Select a directory")).setCategory("Directory/File").setEditor(new PTDirectoryEditor());
//        table.addProperty(new PTProperty("file", "File", "Select a file")).setCategory("Directory/File").setEditor(new PTFileEditor());
//
//        table.addProperty(new PTProperty("comboReadOnly", "Combo (read-only)", "A simple combo with seasons")).setCategory("Combo").setEditor(new PTComboEditor(true, new Object[] {"Spring", "Summer", "Autumn", "Winter"} ) );
//        table.addProperty(new PTProperty("combo", "Combo", "A combo that is not read-only")).setCategory("Combo").setEditor(new PTComboEditor("Value 1", "Value 2", "Value 3"));
//
//        table.addProperty(new PTProperty("cb", "Checkbox", "A checkbox")).setCategory("Checkbox").setEditor(new PTCheckboxEditor()).setCategory("Checkbox");
////        table.addProperty(new PTProperty("cb", "Checkbox", "A checkboxxx")).setCategory("Checkbox").setEditor(new PTCheckboxEditor()).setCategory("Checkbox");
//        table.addProperty(new PTProperty("cb2", "Checkbox (disabled)", "A disabled checkbox...")).setEditor(new PTCheckboxEditor()).setCategory("Checkbox").setEnabled(false);
//
//        table.addProperty(new PTProperty("color", "Color", "Pick it !")).setCategory("Misc").setEditor(new PTColorEditor());
//        table.addProperty(new PTProperty("font", "Font", "Pick again my friend")).setEditor(new PTFontEditor()).setCategory("Misc");
//        table.addProperty(new PTProperty("dimension", "Dimension", "A dimension is composed of a width and a height")).setCategory("Misc").setEditor(new PTDimensionEditor());
//        table.addProperty(new PTProperty("rectangle", "Rectangle", "A rectangle is composed of a position (x,y) and a dimension(width,height)")).setCategory("Misc").setEditor(new PTRectangleEditor());
//        table.addProperty(new PTProperty("inset", "Inset", "An inset is composed of the following fields:top,left,bottom,right)")).setCategory("Misc").setEditor(new PTInsetsEditor());
//        table.addProperty(new PTProperty("date", "Date", "Well, is there something more to say ?")).setCategory("Misc").setEditor(new PTDateEditor());
        
//        table.getProperties().put("cb", "true");
        
//        for (PTProperty p : table.getPropertiesAsList()) {
//        	if (p.getName().equals("cb2")) {
//        		p.setValue(true);
//        	}
//        }
        

        return table;
    }
		
	public CitLabSemiSupervisedHtrTrainConfig getConfig() throws IOException {
		CitLabSemiSupervisedHtrTrainConfig config = new CitLabSemiSupervisedHtrTrainConfig();
		
		// add current document as train document:
		Storage store = Storage.getInstance();
		if (store.isRemoteDoc()) {
			logger.debug("pages str: "+currentDocPagesSelector.getPagesStr());
			config.getTrain().add(DocumentSelectionDescriptor.fromDocAndPagesStr(store.getDoc(), currentDocPagesSelector.getPagesStr()));
			config.setColId(store.getCurrentDocumentCollectionId());
		} else {
			throw new IOException("No remote document loaded!");
		}
		
		if (baseModelBtn.getModel() != null) {
			config.setBaseModelId(baseModelBtn.getModel().getHtrId());
		} else {
			throw new IOException("No base model chosen!");
		}
		
		if (CitLabSemiSupervisedHtrTrainConfig.isValidTrainingEpochsString(epochsTxt.getText())) {
			config.setTrainEpochs(epochsTxt.getText());
		} else {
			throw new IOException("Cannot parse epochs string: "+epochsTxt.getText());
		}
		
		if (subsamplingTxt.toIntVal()!=null) {
			config.setSubSampling(subsamplingTxt.toIntVal());
		} else {
			throw new IOException("Cannot parse subsampling parameter: "+epochsTxt.getText());
		}
		
		if (numberOfThreadsTxt.toIntVal()!=null) {
			config.setnThreads(numberOfThreadsTxt.toIntVal());
		} else {
			throw new IOException("Cannot parse number of threads parameter: "+epochsTxt.getText());
		}
		
		config.setNoise(noiseCmb.getNoise());
		
		try {
			logger.debug("lr = "+learningRateCombo.getLearningRate());
			Double.valueOf(learningRateCombo.getLearningRate());
			config.setLearningRate(learningRateCombo.getLearningRate());
		} catch (NumberFormatException e) {
			throw new IOException("Cannot parse learning rate: "+learningRateCombo.getLearningRate());
		}
		
		try {
			Properties props = CoreUtils.readPropertiesFromString(additonalParameters.getText());
			config.setJsonProps(GsonUtil.toJson(GsonUtil.toJson(props)));
		} catch (IOException e) {
			throw new IOException("Cannot parse advanced properties - use key=value format for each line!");
		}

		return config;
	}
	
	public static void main(String[] args) throws IOException {
		Properties props = CoreUtils.readPropertiesFromString("key1=value1\nkey2=value2");
		System.out.println(GsonUtil.toJson(props));
	}

}
