/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.mycompany.imagej;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.*;
import ij.io.OpenDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import sc.fiji.multiKymograph.MultipleKymograph_;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

/**
 * A template for processing each pixel of either
 * GRAY8, GRAY16, GRAY32 or COLOR_RGB images.
 *
 * @author Johannes Schindelin
 */
public class Process_Pixels extends JFrame implements ActionListener, KeyListener,MouseListener,PlugIn {
	protected ImagePlus image;
	public ImagePlus imp_scaled;
	public ImageProcessor ip_scaled;
	protected ImagePlus imp_big;

	public MultipleKymograph_ Kymo = new MultipleKymograph_();

	double[] getKymo(ImagePlus imp, ImageProcessor ip, Roi roi, int linewidth, int proflength) {
		return Kymo.sKymo(imp, ip, roi, linewidth, proflength) ;
	}

	// image property members
	private int width;
	private int height;




	// plugin parameters
	public double value;





	public void run(java.lang.String arg)  {
		// get width and height

		doDialog();

	}

	/*
//-------------------------------------------------------------------------------
//-------------------------------------------------------------------------------
// Build the dialog box.
//-------------------------------------------------------------------------------
//-------------------------------------------------------------------------------
 */
	private BoxLayout mainLayout;
	static ImagePlus i1;
	static ImageCanvas ic;
	static int selected_cell;					// id of the selected balloon candidate to modification

	private GridBagLayout ActionLayout;
	private GridBagConstraints 	constraint;
	// Layout for Sub components of the Action panel
	private GridLayout 	boundsLayout;
	private GridLayout 	centerLayout;

	private JButton Ckymo;
	private JButton Skymo;
	private JButton updtkymo;

	//private JButton 		bnNeig;
	private JTextField magscalfield;
	private JTextField timescalfield;

	private JButton showresults;
	private JToggleButton Cntrselection;
	private JButton 		bnClear;
	private JTextField	txtRem;
	private JTextField	txtCellID;
	private PolygonRoi ROIkym;

	/*private CheckboxGroup choiceVisu = new CheckboxGroup();
	private Checkbox ChkBounds;
	private Checkbox ChkCentres;
	private Checkbox ChkTopo;
	private Checkbox ChkArchi;*/
	private JTextField Lwidthfield;
	private JTextField widthfield;

	private Checkbox ChkProcessAll2;

	//private Scrollbar	scrpar1;
	private JComboBox editOptionsList;



	RoiManager roiMng = new RoiManager();

	private void doDialog() {
		// Layout
		ActionLayout = new GridBagLayout();
		constraint = new GridBagConstraints();

		boundsLayout = new GridLayout(3,3,10,10);
		centerLayout = new GridLayout(3,3,10,10);



		// buttons
		Ckymo = new JButton(" Create Kymograph ");
		Ckymo.setBackground(new Color(255,200,200));
		Skymo = new JButton(" Select Kymograph polyline");
		Skymo.setBackground(new Color(255,200,200));
		updtkymo = new JButton(" Update scale");
		updtkymo.setBackground(new Color(255,200,200));


		showresults = new JButton("Show results");
		showresults.setBackground(Color.LIGHT_GRAY);
		magscalfield = new JTextField("Image Scale (px/mm)",1);
		timescalfield = new JTextField("Recording frequency (frame/s)",1);

		Cntrselection = new JToggleButton(" Select Contractions ");
		Cntrselection.setBackground(Color.LIGHT_GRAY);




		bnClear = new JButton(" Clear ");

		txtRem = new JTextField("0", 1);
		txtCellID = new JTextField("0", 1);

		Lwidthfield = new JTextField("Line Width (odd number)", 1);
		widthfield = new JTextField("Kymograph Width", 1);




		//ComboBox for Edit Option/////////////////////////////////////////////////////////////////////
		String[] editOptions = { "Antegrade", "Retrograde"};
		editOptionsList = new JComboBox(editOptions);
		editOptionsList.setSelectedIndex(0);
		/////////////////////////////////////////////////////////////////////////////////////////////////

		// Panel parameters
		JPanel pnMain = new JPanel();
		mainLayout = new BoxLayout(pnMain, BoxLayout.X_AXIS);
		JPanel pnAction = new JPanel();

		JPanel pnKymo = new JPanel();
		Border bd = BorderFactory.createBevelBorder(BevelBorder.RAISED); //BorderFactory.createEtchedBorder(); //
		pnKymo.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(bd, "Kymograph"), BorderFactory.createEmptyBorder(7, 7, 7, 7)));
		JPanel pnROIstats = new JPanel();
		pnROIstats.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(bd, "Contraction profiles"), BorderFactory.createEmptyBorder(7, 7, 7, 7)));

		JPanel pnButtons = new JPanel();


		// Panel for the slider

		pnMain.setLayout(mainLayout);
		pnAction.setLayout(ActionLayout);


		// Add components
		//addComponent(pnAction, 30, 0, 1, 1, 2, new JLabel("BOUNDS"));
		pnKymo.add(Lwidthfield);
		pnKymo.add(widthfield);
		pnKymo.add(Ckymo);
		pnKymo.add(Skymo);
		pnKymo.add(updtkymo);

		pnKymo.setLayout(boundsLayout);
		//addComponent(pnCenters, 55, 0, 1, 1, 5, new JLabel("CENTERS"));
		pnROIstats.add(editOptionsList);
		pnROIstats.add(Cntrselection);
		pnROIstats.add(magscalfield);
		pnROIstats.add(showresults);
		pnROIstats.add(timescalfield);

		pnROIstats.add(new Label(""));
		pnROIstats.setLayout(centerLayout);

		// Implement the listeners
		// close listener
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				((JFrame)e.getSource()).setState(JFrame.ICONIFIED);
			}
		});

		// buttons listeners
		Ckymo.addActionListener(this);
		Skymo.addActionListener(this);
		updtkymo.addActionListener(this);
		showresults.addActionListener(this);
		bnClear.addActionListener(this);
		Cntrselection.addActionListener(this);
		if (WindowManager.getCurrentImage()==null)
		{
			OpenDialog od = new OpenDialog("Open image ...");
			String fileName = od.getPath();
			if (fileName == null) return;
			i1 = IJ.openImage(fileName);
			i1.setTitle("image");
			i1.show();
			}

		else i1 = WindowManager.getCurrentImage();

		ic = i1.getCanvas();

		ic.addKeyListener(this);
		ic.addMouseListener(this);

		////////////////////////////////////////////////////////////////
		// Build panel
		////////////////////////////////////////////////////////////////
		addComponent(pnAction, 1, 0, 1, 1, 5, pnKymo);
		addComponent(pnAction, 2, 0, 1, 1, 5, pnROIstats);
		addComponent(pnAction, 5, 0, 1, 1, 5, pnButtons);

		pnMain.add(pnAction);


		add(pnMain);
		pack();
		setResizable(true);
		GUI.center(this);
		setVisible(true);
		Point IJ_location = (IJ.getInstance()).getLocation();
		int Dialog_width = getWidth();
		int XX = (int)(IJ_location.getX()  + (IJ.getInstance()).getWidth()- Dialog_width) ;
		int YY = (int)(IJ_location.getY()  + (IJ.getInstance()).getHeight());
		Point Dialog_pos = new Point(XX, YY);
		setLocation(Dialog_pos);
	}

	final private void addComponent(
			final JPanel pn,
			final int row, final int col,
			final int width, final int height,
			final int space,
			final Component comp) {
		constraint.gridx = col;
		constraint.gridy = row;
		constraint.gridwidth = width;
		constraint.gridheight = height;
		constraint.anchor = GridBagConstraints.NORTHWEST;
		constraint.insets = new Insets(space, space, space, space);
		constraint.weightx = IJ.isMacintosh()?90:100;
		constraint.fill = constraint.HORIZONTAL;
		ActionLayout.setConstraints(comp, constraint);
		pn.add(comp);
	}

	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		// check is slice number has changed

		notify();
	}
	public synchronized  void actionPerformed(ActionEvent e) {

		Toolbar Tb = Toolbar.getInstance();
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////
		//
		//		B O U N D A R I E S
		//
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////
		if (e.getSource() == Skymo) {
			IJ.setTool("polyline");



			Roi[] KymROI = getRois();
			ROIkym = (PolygonRoi) KymROI[0];




			ROIkym.setStrokeWidth(Float.valueOf(Lwidthfield.getText()).floatValue());


		}
		if (e.getSource() == Ckymo) {

			ImagePlus impp = WindowManager.getCurrentImage();



			ImageProcessor iip = impp.getProcessor();


			double[] kyminst = this.getIrregularProfile(ROIkym, iip, 0);
			int proflength = kyminst.length;

			int thickness = Integer.valueOf(Lwidthfield.getText()).intValue();





			double[] average = getKymo(impp, iip, ROIkym, thickness ,proflength);
			FloatProcessor nip = new FloatProcessor(proflength, impp.getStackSize(), average);
			ImagePlus kymoimp = new ImagePlus("Kymograph", nip);
			kymoimp.show();





			imp_big = WindowManager.getCurrentImage();
			imp_scaled = imp_big.duplicate();
			ip_scaled = imp_scaled.getProcessor();
			imp_scaled.setTitle("scaled");
			//imp_scaled.show();



		}


		if (e.getSource() == updtkymo) {

			int kymolength = (int) Float.valueOf(widthfield.getText()).floatValue();

			imp_scaled = imp_big.duplicate();
			int  kymowitdh =  imp_scaled.getWidth();
			ip_scaled = ip_scaled.resize(kymowitdh,kymolength);
			if ( imp_scaled.isVisible())
			{
				IJ.log( "false");
				}
			imp_scaled.setProcessor(ip_scaled);


			IJ.log("here");
			imp_scaled.show();
			imp_scaled.updateAndRepaintWindow();


//			Roi[] KymROI = getRois();

//			PolygonRoi ROIkym = (PolygonRoi) KymROI[0];
//
//
//
//
//			ROIkym.setStrokeWidth(Float.valueOf(Lwidthfield.getText()).floatValue());


		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////
		//
		//		S E E D
		//
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////


		if (e.getSource() == Cntrselection) {

			RoiManager.getInstance().runCommand("delete");





			roiMng.setVisible(true);
			roiMng.runCommand("Show All");
			IJ.setTool("line");




		}

		if (e.getSource() == showresults) {


			Roi[] rois = getRois();

			double[] Ypositions = new double[rois.length];
			double[] interval = new double[rois.length];
			double[] ROIduration = new double[rois.length];
			double[] angle = new double[rois.length];
			double[] velocity = new double[rois.length];
			double[] distance = new double[rois.length];
			interval[0]=0;


			ResultsTable resulty = new ResultsTable();

			float magscale = Float.valueOf(magscalfield.getText()).floatValue();
			float timescale = 1 / Float.valueOf(timescalfield.getText()).floatValue();


			for (int i = 0; i < rois.length; i++) {

				Roi ROId = rois[i];
				i1.setRoi(rois[i]);


				angle[i] = ROId.getAngle();
				Line line = ( Line ) ROId;
				String editType = (String)(editOptionsList.getSelectedItem());



				if (editType == "Antegrade"){

					Ypositions[i] = Math.min(line.y1,line.y2)*timescale;



				}				//
				else if (editType == "retrograde"){

					Ypositions[i] = Math.max(line.y1,line.y2)*timescale;

				}

				if (i > 0) {
					interval[i]= Ypositions[i]-Ypositions[i-1];
				}
				distance[i]= Math.abs(line.x1-line.x2)/magscale;
				ROIduration[i] = Math.abs(line.y1-line.y2)*timescale;  //  possibly add scale
				velocity[i] = distance[i]/ROIduration[i];


				resulty.incrementCounter();
				resulty.addValue("Duration (s)", (double) ROIduration[i]);
				resulty.addValue("velocity (mm/s)", (double) velocity[i]);
				resulty.addValue("distance (mm)", (double) distance[i]);
				resulty.addValue("Interval (s)", (double) interval[i] );






				resulty.show("results");


			}



			if (e.getSource() == bnClear) {

				i1.killRoi();

				// SET THE TOOLBAR TO HAND
				Tb.setTool(Toolbar.HAND);

				// SET COMBOBOX VIEW
				//showOptionsList.setSelectedIndex(0);

				// CHECK THAT EDIT NOT SELECTED
				Cntrselection.setSelected(false);
				IJ.showProgress(1.0);
				IJ.showStatus("");
			}

			notify();
		}
	}




			// CHECK THAT EDIT NOT SELECTED




		/**
         * Process an image.
         * <p>
         * Please provide this method even if {@link ij.plugin.filter.PlugInFilter} does require it;
         * the method {@link ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)} can only
         * handle 2-dimensional data.
         * </p>
         * <p>
         * If your plugin does not change the pixels in-place, make this method return the results and
         * change the {@link #)} method to return also the
         * <i>DOES_NOTHING</i> flag.
         * </p>
         *
         * @param image the image (possible multi-dimensional)
         */
	public void process(ImagePlus image) {
		// slice numbers start with 1 for historical reasons
		for (int i = 1; i <= image.getStackSize(); i++)
			process(image.getStack().getProcessor(i));
	}

	// Select processing method depending on image type
	public void process(ImageProcessor ip) {
		int type = image.getType();
		if (type == ImagePlus.GRAY8)
			process( (byte[]) ip.getPixels() );
		else if (type == ImagePlus.GRAY16)
			process( (short[]) ip.getPixels() );
		else if (type == ImagePlus.GRAY32)
			process( (float[]) ip.getPixels() );
		else if (type == ImagePlus.COLOR_RGB)
			process( (int[]) ip.getPixels() );
		else {
			throw new RuntimeException("not supported");
		}
	}

	// processing of GRAY8 images
	public void process(byte[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (byte)value;
			}
		}
	}

	// processing of GRAY16 images
	public void process(short[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (short)value;
			}
		}
	}

	// processing of GRAY32 images
	public void process(float[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (float)value;
			}
		}
	}

	// processing of COLOR_RGB images
	public void process(int[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (int)value;
			}
		}
	}

	public void showAbout() {
		IJ.showMessage("ProcessPixels",
			"a template for processing each pixel of an image"
		);
	}

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = Process_Pixels.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);


		// start ImageJ
		final ImageJ ij = new ImageJ();

		// open the Clown sample
		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
		IJ.setTool("polyline");




	}




	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//		C L E A R
	//
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////


	public synchronized void itemStateChanged(ItemEvent e) { notify(); 	}
	public void windowActivated(WindowEvent e) {}
	public void windowClosing(WindowEvent e) { dispose(); }
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) { 	}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
	public Roi[] getRois() {
		RoiManager rmanager = RoiManager.getInstance();
		if (rmanager == null || rmanager.getCount() == 0) {
			IJ.log("add ROIs to the RoiManager first (select a region then press [t]).");
			return null;
		}
		return rmanager.getRoisAsArray();
	}


	// *************************
	// Mouse event manager
	// *************************
	public void mouseWheelMoved(MouseWheelEvent event) {
		//synchronized(this) {
	}
	public void mouseReleased(MouseEvent e)
	{
		// modify the coordinates of the corresponding balloon if a cell is selected when mouse is released
		Roi roi = i1.getRoi();
		if (roi !=null)
		{
			if (roi.getType() == roi.POLYGON & selected_cell>-1) // modify the selected cell
			{
				Polygon p = roi.getPolygon();
				int[] XXi = p.xpoints;
				int[] YYi = p.ypoints;
				//bal1.setXX(XXi);
				//bal1.setYY(YYi);


			}

		}
	}

	public void mousePressed(MouseEvent e)  	{
		if (Cntrselection.isSelected()){
			int x = ic.offScreenX(e.getX());
			int y = ic.offScreenY(e.getY());
			int xx=0;
		}

	}
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}


	// *************************
	// KeyPress event manager
	// *************************
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e)
	{
		Roi roi = i1.getRoi();
		Polygon p = roi.getPolygon();
		int[] XXi = p.xpoints;
		int[] YYi = p.ypoints;

		PolygonRoi Proi = new PolygonRoi(XXi,YYi,(XXi).length,Roi.POLYGON);
		i1.setRoi(Proi);
	}
	public String modifiers(int flags) {
		String s = " [ ";
		if (flags == 0) return "";
		if ((flags & KeyEvent.SHIFT_MASK) != 0) s += "Shift ";
		if ((flags & KeyEvent.CTRL_MASK) != 0) s += "Control ";
		if ((flags & KeyEvent.META_MASK) != 0) s += "Meta ";
		if ((flags & KeyEvent.ALT_MASK) != 0) s += "Alt ";
		s += "] ";
		return s;
	}

	// from  MultipleKymograph plugin
	double[] getIrregularProfile(Roi roi, ImageProcessor ip, int shift) {
		int n = ((PolygonRoi)roi).getNCoordinates();
		int[] x = ((PolygonRoi)roi).getXCoordinates();
		int[] y = ((PolygonRoi)roi).getYCoordinates();

		for(int r = 0; r < n; ++r) {
			x[r] += shift;
			y[r] += shift;
		}

		Rectangle var42 = roi.getBoundingRect();
		int xbase = var42.x;
		int ybase = var42.y;
		double length = 0.0D;
		double[] segmentLengths = new double[n];
		int[] dx = new int[n];
		int[] dy = new int[n];

		for(int values = 0; values < n - 1; ++values) {
			int xdelta = x[values + 1] - x[values];
			int ydelta = y[values + 1] - y[values];
			double segmentLength = Math.sqrt((double)(xdelta * xdelta + ydelta * ydelta));
			length += segmentLength;
			segmentLengths[values] = segmentLength;
			dx[values] = xdelta;
			dy[values] = ydelta;
		}

		double[] var43 = new double[(int)length];
		double leftOver = 1.0D;
		double distance = 0.0D;

		for(int i = 0; i < n; ++i) {
			double len = segmentLengths[i];
			if(len != 0.0D) {
				double xinc = (double)dx[i] / len;
				double yinc = (double)dy[i] / len;
				double start = 1.0D - leftOver;
				double rx = (double)(xbase + x[i]) + start * xinc;
				double ry = (double)(ybase + y[i]) + start * yinc;
				double len2 = len - start;
				int n2 = (int)len2;

				for(int j = 0; j <= n2; ++j) {
					int index = (int)distance + j;
					if(index < var43.length) {
						var43[index] = ip.getInterpolatedValue(rx, ry);
					}

					rx += xinc;
					ry += yinc;
				}

				distance += len;
				leftOver = len2 - (double)n2;
			}
		}

		return var43;
	}


}
