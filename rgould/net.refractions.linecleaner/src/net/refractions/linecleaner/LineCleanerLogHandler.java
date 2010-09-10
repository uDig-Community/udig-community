package net.refractions.linecleaner;

import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LineCleanerLogHandler extends Handler {

	PrintStream out = System.out; //change this to write to a file, if desired.
	
	@Override
	public void publish(LogRecord record) {
		out.println(record.getLevel().getName()+","+record.getMessage());
	}

	@Override
	public void flush() {
		out.flush();
	}

	@Override
	public void close() throws SecurityException {
		out.close();
	}

}
