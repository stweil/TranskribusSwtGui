package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.xml.bind.JAXBException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.swt.ChartComposite;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset;
import org.jfree.date.DateUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpComputeSample;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.rest.JobParameters;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.rest.RESTConst;
import eu.transkribus.core.util.DescriptorUtils;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProvider;
import eu.transkribus.swt_gui.htr.DataSetTableWidget;
import eu.transkribus.swt_gui.htr.TreeViewerDataSetSelectionSashForm.DataSetEntry;
import eu.transkribus.swt_gui.htr.TreeViewerDataSetSelectionSashForm.DataSetMetadata;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;


public class SamplesCompareDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(SamplesCompareDialog.class);
	
	private final int colId;

	private CTabFolder paramTabFolder;
	private CTabItem samplesTabItem, computeSampleTabItem;
	
	LabeledText nrOfLinesTxt;
	
	private Text modelNameTxt, descTxt;

	private Storage store = Storage.getInstance();

	private List<TrpDocMetadata> docList;
	
	private static final RGB BLUE_RGB = new RGB(0, 0, 140);
	private static final RGB LIGHT_BLUE_RGB = new RGB(0, 140, 255);
	private static final RGB CYAN_RGB = new RGB(85, 240, 240);
	
	private static final Color BLUE = Colors.createColor(BLUE_RGB);
	private static final Color LIGHT_BLUE = Colors.createColor(LIGHT_BLUE_RGB);
	private static final Color CYAN = Colors.createColor(CYAN_RGB);
	private static final Color WHITE = Colors.getSystemColor(SWT.COLOR_WHITE);
	private static final Color BLACK = Colors.getSystemColor(SWT.COLOR_BLACK);
	
	private TreeViewer tv, tvCompute;
	private CollectionContentProvider contentProv;
	private CollectionLabelProvider labelProv;
	private Composite buttonComp,buttonComputeComp, chartResultComp, samplesConfComposite ;
	private ChartComposite jFreeChartComp;
	private Button addToSampleSetBtn, removeFromSampleSetBtn, computeSampleBtn;
	private ParameterMap params = new ParameterMap();
	Combo comboRef,comboHyp;
	Label labelRef,labelHyp, chartText;
	TrpDocMetadata docMd;
	JFreeChart chart;
	private Label previewLbl;
	private DataSetTableWidget sampleSetOverviewTable;
	private Map<TrpDocMetadata, List<TrpPage>> sampleDocMap;
	
	ResultLoader rl;
	

	public SamplesCompareDialog(Shell parentShell) {
		super(parentShell);
		docList = store.getDocList();
		colId = store.getCollId();
		docMd = new TrpDocMetadata();
		
	}
	
	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);

		SashForm sash = new SashForm(cont, SWT.VERTICAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new GridLayout(1, false));

		Composite paramCont = new Composite(sash, SWT.BORDER);
		paramCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		paramCont.setLayout(new GridLayout(1, false));

		Label modelNameLbl = new Label(paramCont, SWT.FLAT);
		modelNameLbl.setText("Sample Name:");
		modelNameTxt = new Text(paramCont, SWT.BORDER);
		modelNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label descLbl = new Label(paramCont, SWT.FLAT);
		descLbl.setText("Description:");
		descTxt = new Text(paramCont, SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint = 20;
		descTxt.setLayoutData(gd);
		
		nrOfLinesTxt = new LabeledText(paramCont, "Nr. of lines", true);
		nrOfLinesTxt.setText("100");
		nrOfLinesTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true , false ));

		paramTabFolder = new CTabFolder(paramCont, SWT.BORDER | SWT.FLAT);
		paramTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		SamplesMethodUITab tab = createSampelsTab(0);
		CTabItem selection = tab.getTabItem();
		
		paramTabFolder.setSelection(selection);		
		paramCont.pack();
		
		
		createSampleTreeViewer(samplesConfComposite, SWT.HORIZONTAL);
		
		//treeViewerSelector = new TreeViewerDataSetSelectionSashForm(sash, SWT.HORIZONTAL, colId, docList);
		
		
		return cont;
	}
	
	private void createSampleTreeViewer(Composite parent, int style) {
		
		SashForm sampleTreeViewer = new SashForm(parent,style);
		sampleDocMap = new TreeMap<>();
		sampleTreeViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sampleTreeViewer.setLayout(new GridLayout(1, false));
		
		Group treeViewerCont = new Group(sampleTreeViewer, SWT.NONE);
		treeViewerCont.setText("Sample Set");
		treeViewerCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewerCont.setLayout(new GridLayout(1, false));
		
		
		tv = new TreeViewer(treeViewerCont, SWT.BORDER | SWT.MULTI);
		contentProv = new CollectionContentProvider();
		labelProv = new CollectionLabelProvider();
		tv.setContentProvider(contentProv);
		tv.setLabelProvider(labelProv);
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(this.docList);
		
		buttonComp = new Composite(sampleTreeViewer, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, true));
		
		previewLbl = new Label(buttonComp, SWT.NONE);
		GridData gd2 = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		previewLbl.setLayoutData(gd2);
		
		addToSampleSetBtn = new Button(buttonComp, SWT.PUSH);
		addToSampleSetBtn.setImage(Images.ADD);
		addToSampleSetBtn.setText("Sample Set");
		addToSampleSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Group trainOverviewCont = new Group(sampleTreeViewer, SWT.NONE);
		trainOverviewCont.setText("Overview");
		trainOverviewCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trainOverviewCont.setLayout(new GridLayout(1, false));
		
		GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout tableGl = new GridLayout(1, true);
		
		Group sampleSetGrp = new Group(trainOverviewCont, SWT.NONE);
		sampleSetGrp.setText("Sample Set");
		sampleSetGrp.setLayoutData(tableGd);
		sampleSetGrp.setLayout(tableGl);

		sampleSetOverviewTable = new DataSetTableWidget(sampleSetGrp, SWT.BORDER);
		sampleSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridData buttonGd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		removeFromSampleSetBtn = new Button(sampleSetGrp, SWT.PUSH);
		removeFromSampleSetBtn.setLayoutData(buttonGd);
		removeFromSampleSetBtn.setImage(Images.CROSS);
		removeFromSampleSetBtn.setText("Remove selected entries from train set");
		
		sampleTreeViewer.setWeights(new int[] {45,15,45});
		
		treeViewerCont.pack();
		buttonComp.pack();
		trainOverviewCont.pack();
		sampleSetGrp.pack();
		addListeners();
		
	}

	private SamplesMethodUITab createSampelsTab(final int tabIndex) {
		samplesTabItem = new CTabItem(paramTabFolder, SWT.NONE);
		samplesTabItem.setText("Create Sample");
		
		samplesConfComposite = new Composite(paramTabFolder,0);
		samplesConfComposite.setLayout(new GridLayout(1,false));
		
		samplesTabItem.setControl(samplesConfComposite);
		
		computeSampleTabItem = new CTabItem(paramTabFolder, SWT.NONE);
		computeSampleTabItem.setText("Compute");
		
		SashForm samplesComputesash = new SashForm(paramTabFolder, SWT.HORIZONTAL);
		samplesComputesash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		samplesComputesash.setLayout(new GridLayout(3, false));
		
		tvCompute = new TreeViewer(samplesComputesash, SWT.BORDER | SWT.MULTI);
		contentProv = new CollectionContentProvider();
		labelProv = new CollectionLabelProvider();
		tvCompute.setContentProvider(contentProv);
		tvCompute.setLabelProvider(labelProv);
		tvCompute.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tvCompute.setInput(this.docList);
		
		buttonComputeComp = new Composite(samplesComputesash, SWT.NONE);
		buttonComputeComp.setLayout(new GridLayout(1, true));
		
		labelRef = new Label(buttonComputeComp,SWT.NONE );
		labelRef.setText("Select reference:");
		labelRef.setVisible(false);
		comboRef = new Combo(buttonComputeComp, SWT.DROP_DOWN);
		comboRef.setItems(new String[] {"GT","1st IN_PROGRESS","Last IN_PROGRESS","1st DONE","Last DONE"});
		comboRef.setVisible(false);
		labelHyp = new Label(buttonComputeComp,SWT.NONE );
		labelHyp.setText("Select hypothese by toolname:");
		labelHyp.setVisible(false);
		comboHyp = new Combo(buttonComputeComp, SWT.DROP_DOWN);
		final GridData gd_combo = new GridData(SWT.FILL,SWT.FILL, true, false);
		gd_combo.widthHint = 250;
		comboHyp.setLayoutData(gd_combo);
		comboHyp.setVisible(false);
		
		computeSampleBtn = new Button(buttonComputeComp, SWT.PUSH);
		computeSampleBtn.setImage(Images.ARROW_RIGHT);
		computeSampleBtn.setText("Compute");
		computeSampleBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		chartResultComp = new Composite(samplesComputesash, SWT.NONE);
		chartResultComp.setLayout(new GridLayout(1, true));
		Date date = new Date();
		BoxAndWhiskerXYDataset dataset = createDataset(0,0,0,date);
		chart = createChart(dataset);
		jFreeChartComp = new ChartComposite(chartResultComp, SWT.FILL);
		jFreeChartComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		jFreeChartComp.setChart(chart);
		chartText = new Label(chartResultComp, SWT.FILL);
		
		
		computeSampleTabItem.setControl(samplesComputesash);
		
		
		return new SamplesMethodUITab(tabIndex, samplesTabItem, samplesConfComposite);
	}
	
	private JFreeChart createChart(BoxAndWhiskerXYDataset dataset) {
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
				"Confidence Interval for CER", "Sample", "CER",  dataset, true);
		return chart;

	}
	
	private BoxAndWhiskerXYDataset createDataset(double meanDoub, double minDoub, double maxDoub, Date date) {

		DefaultBoxAndWhiskerXYDataset dataset = new DefaultBoxAndWhiskerXYDataset("Upper and lower bound for CER");
		
		Number mean = meanDoub;
		Number median = 0;
		Number q1 = 0;
		Number q3 = 0;
		Number minRegularValue = minDoub;
		Number maxRegularValue = maxDoub;
		Number minOutlier = 0;
		Number maxOutlier = 0;
		List outliers = null;
		
		BoxAndWhiskerItem item = new BoxAndWhiskerItem(mean, median, q1, q3, minRegularValue, maxRegularValue, minOutlier, maxOutlier, outliers);
		dataset.add(date,item);
		  
		return dataset;

	}

	@Override
	protected void okPressed() {
		String msg = "";
		DataSetMetadata sampleSetMd = getSampleSetMetadata();
		msg += "Sample set size:\n \t\t\t\t" + sampleSetMd.getPages() + " pages\n";
		msg += "\t\t\t\t" + sampleSetMd.getLines() + " lines\n";
		msg += "\t\t\t\t" + sampleSetMd.getWords() + " words\n";
		msg += "Samples Options:\n ";
		msg += "\t\t\t\t" + nrOfLinesTxt.getText()  + " lines\n";
		
		int result = DialogUtil.showYesNoDialog(this.getShell(), "Start?", msg);
		
		if (result == SWT.YES) {
			
			try {
				logger.debug("Nr of Lines : "+nrOfLinesTxt.getText());
				store.createSample(sampleDocMap, Integer.parseInt(nrOfLinesTxt.getText()), modelNameTxt.getText(), descTxt.getText());
			} catch (SessionExpiredException | ServerErrorException | ClientErrorException
					| IllegalArgumentException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Samples Compare");
		newShell.setMinimumSize(1000, 800);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
	}
	
	private void addListeners() {
		
		
		comboHyp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Hyp Selction "+comboHyp.getItem(comboHyp.getSelectionIndex()));
				params.addParameter("hyp", comboHyp.getItem(comboHyp.getSelectionIndex()));
			}
		});
		
		comboRef.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Ref Selction "+comboRef.getItem(comboRef.getSelectionIndex()));
				params.addParameter("ref", comboRef.getItem(comboRef.getSelectionIndex()));
			}
		});
		
		computeSampleBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int docId = docMd.getDocId();
				params.addParameter("computeSample", "computeSample");
				try {
					store.computeSampleRate(docId,params);
					rl = new ResultLoader();
					
				} catch (TrpServerErrorException | TrpClientErrorException | SessionExpiredException e1) {
					e1.printStackTrace();
				}
				
			}
			
		});

		tvCompute.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object o = selection.getFirstElement();
				if (o instanceof TrpDocMetadata) {
					docMd = (TrpDocMetadata) o;
					labelRef.setVisible(true);
					comboRef.setVisible(true);
					labelHyp.setVisible(true);
					comboHyp.setVisible(true);
					try {
						Object[] pageObjArr = contentProv.getChildren(docMd);
						for (Object obj : pageObjArr) {
							TrpPage page = (TrpPage) obj;
							List<TrpTranscriptMetadata> transcripts = page.getTranscripts();
							for(TrpTranscriptMetadata transcript : transcripts){
								if(transcript.getToolName() != null) {
									String[] items = comboHyp.getItems();
									if(!Arrays.stream(items).anyMatch(transcript.getToolName()::equals)) {
										comboHyp.add(transcript.getToolName());
									}
								}
								
							}
							
						}
						Integer docId = docMd.getDocId();
						List<TrpJobStatus> jobs = new ArrayList<>();
						jobs = store.getConnection().getJobs(true, null, JobImpl.ComputeSampleJob.getLabel(), docId, 0, 0, null, null);
						for(TrpJobStatus job : jobs) {
							if(job.isFinished()) {
								TrpProperties props = job.getJobDataProps();
								final String xmlStr = props.getString(JobConst.PROP_RESULT);
								TrpComputeSample res = new TrpComputeSample();
								if(xmlStr != null) {
									try {
										res = JaxbUtils.unmarshal(xmlStr, TrpComputeSample.class);
										BoxAndWhiskerXYDataset dataset = createDataset(res.getMean(),res.getMinProp(),res.getMaxProp(),job.getCreated());
										chart = createChart(dataset);
										jFreeChartComp.setChart(chart);
										chart.fireChartChanged();
										chartText.setText("With the probability of 0.95 the CER of the given recognition is in the interval ["+res.getMinProp()+" "+res.getMaxProp()+"] with the mean : "+res.getMean());
									} catch (JAXBException e) {
										logger.error("Could not unmarshal error result result from job!");
									}
								}
							}
						}
						
					} catch (ServerErrorException | IllegalArgumentException | SessionExpiredException | ClientErrorException e) {
						e.printStackTrace();
					}
					
				}

			}
		});

		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (o instanceof TrpDocMetadata) {
					for (TreeItem i : tv.getTree().getItems()) {
						if (i.getData().equals(o)) {
							tv.setExpandedState(o, !i.getExpanded());
							break;
						}
					}
					updateColors();
				} else if (o instanceof TrpPage) {
					TrpPage p = (TrpPage)o;
					TrpLocation loc = new TrpLocation();
					loc.collId = colId;
					loc.docId = p.getDocId();
					loc.pageNr = p.getPageNr();
					TrpMainWidget.getInstance().showLocation(loc);
				}
			}
		});

		tv.getTree().addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {
				updateColors();
			}
		});

		addToSampleSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				Iterator<?> it = sel.iterator();
				while (it.hasNext()) {
					Object o = it.next();
					if (o instanceof TrpDocMetadata) {
						TrpDocMetadata docMd = (TrpDocMetadata) o;
						Object[] pageObjArr = contentProv.getChildren(docMd);
						List<TrpPage> pageList = new LinkedList<>();
						for (Object page : pageObjArr) {
							pageList.add((TrpPage) page);
						}

						sampleDocMap.put(docMd, pageList);

					} else if (o instanceof TrpPage) {
						TrpPage p = (TrpPage) o;
						TrpDocMetadata parent = (TrpDocMetadata) contentProv.getParent(p);
						if (sampleDocMap.containsKey(parent) && !sampleDocMap.get(parent).contains(p)) {
							sampleDocMap.get(parent).add(p);
						} else if (!sampleDocMap.containsKey(parent)) {
							List<TrpPage> pageList = new LinkedList<>();
							pageList.add(p);
							sampleDocMap.put(parent, pageList);
						}

					}
				}
				updateTable(sampleSetOverviewTable, sampleDocMap);
				updateColors();
			}
		});

		removeFromSampleSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<DataSetEntry> entries = sampleSetOverviewTable.getSelectedDataSets();
				if (!entries.isEmpty()) {
					for (DataSetEntry entry : entries) {
						sampleDocMap.remove(entry.getDoc());
					}
					updateTable(sampleSetOverviewTable, sampleDocMap);
					updateColors();
				}
			}
		});

	}
	
	private void updateColors() {
		List<TrpPage> trainPages;
		for (TreeItem i : tv.getTree().getItems()) {
			TrpDocMetadata doc = (TrpDocMetadata) i.getData();

			// default color set
			Color fgColor = BLACK;
			Color bgColor = WHITE;

			if (sampleDocMap.containsKey(doc)) {
				fgColor = WHITE;
				bgColor = CYAN;
			} else if (sampleDocMap.containsKey(doc)) {
				fgColor = WHITE;
				if (doc.getNrOfPages() == sampleDocMap.get(doc).size()) {
					bgColor = BLUE;
				} else {
					bgColor = LIGHT_BLUE;
				}
			} 
			i.setBackground(bgColor);
			i.setForeground(fgColor);

			trainPages = sampleDocMap.containsKey(doc) ? sampleDocMap.get(doc) : new ArrayList<>(0);

			for (TreeItem child : i.getItems()) {
				TrpPage page = (TrpPage) child.getData();
				if (trainPages.contains(page)) {
					child.setBackground(BLUE);
					child.setForeground(WHITE);
				}else {
					child.setBackground(WHITE);
					child.setForeground(BLACK);
				}
			}
		}
	}
	
	private void updateTable(DataSetTableWidget t, Map<TrpDocMetadata, List<TrpPage>> map) {
		List<DataSetEntry> list = new ArrayList<>(map.entrySet().size());
		for (Entry<TrpDocMetadata, List<TrpPage>> entry : map.entrySet()) {
			TrpDocMetadata doc = entry.getKey();

			List<TrpPage> pageList = entry.getValue();

			list.add(new DataSetEntry(doc, pageList));
		}
		Collections.sort(list);
		t.setInput(list);
	}
	
	public DataSetMetadata getSampleSetMetadata() {
		return computeDataSetSize(getTrainDocMap());
	}
	
	public Map<TrpDocMetadata, List<TrpPage>> getTrainDocMap() {
		return sampleDocMap;
	}
	
	private DataSetMetadata computeDataSetSize(Map<TrpDocMetadata, List<TrpPage>> map) {
		int pages = 0;
		int lines = 0;
		int words = 0;
		for (Entry<TrpDocMetadata, List<TrpPage>> e : map.entrySet()) {
			for (TrpPage p : e.getValue()) {
				TrpTranscriptMetadata tmd = p.getCurrentTranscript();
					for (TrpTranscriptMetadata t : p.getTranscripts()) {
							tmd = t;
							break;
					}
					pages++;
					lines += tmd.getNrOfTranscribedLines();
					words += tmd.getNrOfWordsInLines();
				}
				
			
		}
		return new DataSetMetadata(pages, lines, words);
	}
	
	private class ResultLoader extends Thread{
		private final static int SLEEP = 2000;
		private boolean stopped = false;
		@Override
		public void run() {
			logger.debug("Starting result polling.");
			while(!stopped) {
				List<TrpJobStatus> jobs;
				jobs = this.getSampleJob();
				for(TrpJobStatus job : jobs) {
					if(job.isFinished()) {
						TrpProperties props = job.getJobDataProps();
						final String xmlStr = props.getString(JobConst.PROP_RESULT);
						TrpComputeSample res = new TrpComputeSample();
						if(xmlStr != null) {
							try {
								res = JaxbUtils.unmarshal(xmlStr, TrpComputeSample.class);
								BoxAndWhiskerXYDataset dataset = createDataset(res.getMean(),res.getMinProp(),res.getMaxProp(),job.getCreated());
								chart = createChart(dataset);
								chartResultComp.setVisible(true);
								chartText.setText("With the probability of 0.95 the CER of the given recognition is in the interval ["+res.getMinProp()+" "+res.getMaxProp()+"] with the mean : "+res.getMean());
							} catch (JAXBException e) {
								logger.error("Could not unmarshal error result result from job!");
							}
						}
					}
				}
				try {
					Thread.sleep(SLEEP);
				} catch (InterruptedException e) {
					logger.error("Sleep interrupted.", e);
				}
			}
		}
		private List<TrpJobStatus> getSampleJob() {
			Integer docId = docMd.getDocId();
			List<TrpJobStatus> jobs = new ArrayList<>();
			if (store != null && store.isLoggedIn()) {
				try {
					jobs = store.getConnection().getJobs(true, null, JobImpl.ComputeSampleJob.getLabel(), docId, 0, 0, null, null);
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException
						| IllegalArgumentException e) {
					logger.error("Could not load Jobs!");
				}
			}
			return jobs;
		}
		public void setStopped() {
			logger.debug("Stopping result polling.");
			stopped = true;
		}
		
	}
	
	private class SamplesMethodUITab {
		final int tabIndex;
		final CTabItem tabItem;
		final Composite configComposite;
		private SamplesMethodUITab(int tabIndex, CTabItem tabItem, Composite configComposite) {
			this.tabIndex = tabIndex;
			this.tabItem = tabItem;
			this.configComposite = configComposite;
		}
		public int getTabIndex() {
			return tabIndex;
		}
		public CTabItem getTabItem() {
			return tabItem;
		}
		public Composite getConfigComposite() {
			return configComposite;
		}
	}

}
