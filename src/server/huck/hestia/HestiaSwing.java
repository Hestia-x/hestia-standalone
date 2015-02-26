package huck.hestia;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class HestiaSwing extends JFrame {
	private static final long serialVersionUID = -8356722256097914614L;

	private JTextArea textArea;
	
	public HestiaSwing() throws IOException, BadLocationException {
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		
		JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);
		
		this.addWindowListener(new EventListener());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(600, 300);
		this.setVisible(true);
	}

	public void addLog(String log) throws BadLocationException, IOException {
		textArea.append(log);
		System.out.println(textArea.getText());
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}
	
	private class EventListener implements WindowListener {
		@Override
		public void windowOpened(WindowEvent e) {
			Thread th = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Hestia.main(new String[]{});
					} catch( Exception ex ) {
						Logger.getLogger("hestia").fatal("stopped!", ex);
					}
				}
			});
			th.setDaemon(true);
			th.start();
		}

		@Override
		public void windowClosing(WindowEvent e) {
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}
	}

	volatile static HestiaSwing instance;	
	public static void main(String[] args) {
		PropertyConfigurator.configure(Thread.currentThread().getContextClassLoader().getResource("log4j_swing.properties"));
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					instance = new HestiaSwing();
				} catch( Exception ex ) {
					ex.printStackTrace();
				}
			}
		});
	}
}
