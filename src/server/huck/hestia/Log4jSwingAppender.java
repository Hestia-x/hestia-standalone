package huck.hestia;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class Log4jSwingAppender extends AppenderSkeleton {
	protected void append(LoggingEvent event) {
		try {
			if (!performChecks()) {
				return;
			}
			String logOutput = this.layout.format(event);
			HestiaSwing.instance.addLog(logOutput);
			if (layout.ignoresThrowable()) {
				String[] lines = event.getThrowableStrRep();
				if (lines != null) {
					int len = lines.length;
					for (int i = 0; i < len; i++) {
						HestiaSwing.instance.addLog(lines[i]);
						HestiaSwing.instance.addLog(Layout.LINE_SEP);
					}
				}
			}
		} catch( Exception ex ) {
			ex.printStackTrace();
		}
	}

	public void close() {
	}

	public boolean requiresLayout() {
		return true;
	}

	private boolean performChecks() {
		return !closed && layout != null;
	}
}
