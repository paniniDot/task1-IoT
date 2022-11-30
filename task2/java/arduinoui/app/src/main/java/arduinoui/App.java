/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package arduinoui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.fazecast.jSerialComm.SerialPort;

public class App {
	public static void main(String[] args) {
		JFrame win = new JFrame();
		win.setTitle("Arduino UI");
		win.setSize(800, 600);
		win.setLayout(new BorderLayout());
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComboBox<String> portList = new JComboBox<>();
		JButton connectBtn = new JButton("Connect");
		JCheckBox overridejcb = new JCheckBox("Manual Servo");
		JPanel topPanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 180, 0);
		slider.setPaintTrack(true);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setMajorTickSpacing(50);
		slider.setMinorTickSpacing(5);

		JLabel labelwater = new JLabel("water : ");
		JLabel labellight = new JLabel("light : ");

		topPanel.add(portList);
		topPanel.add(connectBtn);
		topPanel.add(slider);
		topPanel.add(overridejcb);
		bottomPanel.add(labelwater);
		bottomPanel.add(labellight);
		win.add(topPanel, BorderLayout.NORTH);
		win.add(bottomPanel, BorderLayout.SOUTH);

		Arrays.stream(SerialPort.getCommPorts())
				.map(SerialPort::getSystemPortName)
				.forEach(name -> portList.addItem(name));

		XYSeries series = new XYSeries("Water Level Readings");
		XYDataset dataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("Water Level Readings", "Time (Seconds)", "Water Level (cm)",
				dataset, PlotOrientation.VERTICAL, true, false, false);

		connectBtn.addActionListener(new ActionListener() {

			SerialPort port;
			int x;
			boolean connected = false;

			@Override
			public void actionPerformed(ActionEvent e) {

				if (portList.getItemCount() > 0 && !connected) {
					connected = true;
					port = SerialPort.getCommPort(portList.getSelectedItem().toString());
					port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					if (port.openPort()) {
						connectBtn.setText("Disconnect");
						portList.setEnabled(false);
					}
					slider.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(javax.swing.event.ChangeEvent e) {
							String msg = Integer.toString(slider.getValue());
							char[] array = (msg + "a\n").toCharArray();
							byte[] bytes = new byte[array.length];
							for (int i = 0; i < array.length; i++) {
								bytes[i] = (byte) array[i];
							}
							try {
								synchronized (port) {
									port.writeBytes(bytes, bytes.length);
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					});
					overridejcb.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							String msg = overridejcb.isSelected() ? "YES" : "NO";
							char[] array = (msg + "a\n").toCharArray();
							byte[] bytes = new byte[array.length];
							for (int i = 0; i < array.length; i++) {
								bytes[i] = (byte) array[i];
							}
							try {
								synchronized (port) {
									port.writeBytes(bytes, bytes.length);
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					});
					// create a new thread that listens for incoming text and populates the graph
					Thread thread = new Thread() {
						@Override
						public void run() {
							Scanner scanner = new Scanner(port.getInputStream());
							while (scanner.hasNextLine()) {
								try {
									String line = scanner.nextLine();
									System.out.println("line " + line);
									if (line.equals("ALARM")) {
										labelwater.setText("water : ALARM");
									} else if (line.equals("PRE_ALARM")) {
										labelwater.setText("water : PRE ALARM");
									} else if (line.equals("NORMAL")) {
										labelwater.setText("water : NORMAL");
									} else if (line.equals("LIGHT_ON")) {
										labellight.setText("light : LIGHT ON");
									} else if (line.equals("LIGHT_OFF")) {
										labellight.setText("light : LIGHT OFF");
									}
									double number = Double.parseDouble(line);
									series.add(x++, number);
									win.repaint();
								} catch (Exception e) {
								}
							}
							scanner.close();
						}
					};
					thread.start();
				} else {
					// disconnect from the serial port
					port.closePort();
					portList.setEnabled(true);
					connectBtn.setText("Connect");
					series.clear();
					x = 0;
					connected = false;
				}
			}

		});

		win.add(new ChartPanel(chart), BorderLayout.CENTER);

		win.setVisible(true);
	}
}
