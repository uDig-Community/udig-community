package net.refractions.linecleaner;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.Feature;

/*
 * level, stage name, process name, action, comments
 */

public class LoggingSystem {
	
	private Logger logger;
	private long startTime;
	private LogAction currentAction;
	
	private static LoggingSystem instance = new LoggingSystem();
	
	private LoggingSystem() {
		this.logger = Logger.getLogger(LoggingSystem.class.getName());
		this.logger.addHandler(new LineCleanerLogHandler());
	}
	
	public static LoggingSystem getInstance() {
        instance.setLevel(Level.OFF);
		return instance;
	}
	
	public void setLevel(Level level) {
		logger.setLevel(level);
	}

	public Logger getLogger() {
		return logger;
	}
	
	public void setCurrentAction(LogAction action) {
		this.currentAction = action;
	}

	public static String featureToString(Feature feature) {
		//TODO if loggin is level.finest, print geom too - no, do that elsewhere?
		return feature.getID();
	}
	
	public void begin() {
		this.logger.info(currentAction+SEPARATOR+BEGIN);
		startTime = System.currentTimeMillis();
	}
	
	
	public void delete(Feature feature) {
		this.logger.info(currentAction+SEPARATOR + DELETE + SEPARATOR + featureToString(feature));
	}
	
	public void modify(Feature newFeature) {
		modify(newFeature, null);
	}
	
	public void modify(Feature newFeature, String comment) {
		if (comment != null && comment.length() != 0) {
			comment = SEPARATOR + comment;
		} else {
			comment = "";
		}
		this.logger.info(currentAction+SEPARATOR + MODIFY + SEPARATOR + featureToString(newFeature)
				+ comment);
	}

	public void info(String message) {
		message(Level.INFO, message);
	}
	
	public void warning(String message) {
		message(Level.WARNING, message);
	}
	
	public void severe(String message) {
		message(Level.SEVERE, message);
	}
	
	public void fine(String message) {
		message(Level.FINE, message);
	}
	
	public void finest(String message) {
		message(Level.FINEST, message);
	}
	
	public void message(Level level, String message) {
		this.logger.log(level, currentAction+SEPARATOR+ INFORMATION + SEPARATOR + message);
	}
	
	public void finish() {
		long finishTime = System.currentTimeMillis() - startTime;
		this.logger.info(currentAction+SEPARATOR+FINISH+SEPARATOR+finishTime);
	}
	
	public void finish(int metric) {
		long finishTime = System.currentTimeMillis() - startTime;
		this.logger.info(currentAction+SEPARATOR+FINISH+SEPARATOR+finishTime+SEPARATOR+metric);
	}
	
	protected final static String SEPARATOR = ",";
	protected final static String CLEANSING = "Cleansing";
    protected final static String CLEANING = "Cleaning";
	protected final static String BEGIN = "begin";
	protected final static String FINISH = "finish";
	protected final static String DELETE = "delete";
	protected final static String MODIFY = "modify";
	protected final static String INFORMATION = "information";
	
	public static final LogAction PSEUDO_NODES = new LogAction() {
	
		@Override
		public String getStageName() {
			return CLEANSING;
		}
	
		@Override
		public String getProcessName() {
			return "Pseudo Nodes";
		}
	
	};

	public static final LogAction CYCLES = new LogAction() {
		
		@Override
		public String getStageName() {
			return CLEANSING;
		}
	
		@Override
		public String getProcessName() {
			return "Cycles";
		}
	
	};
	public static final LogAction END_NODES = new LogAction() {
	
		@Override
		public String getStageName() {
			return CLEANSING;
		}
	
		@Override
		public String getProcessName() {
			return "End Nodes";
		}
	
	};
	public static final LogAction DOUGLAS_PEUCKER = new LogAction() {
	
		@Override
		public String getStageName() {
			return CLEANSING;
		}
	
		@Override
		public String getProcessName() {
			return "Douglas-Peucker";
		}
	
	};
	public static final LogAction MINIMUM_LENGTH = new LogAction() {
	
		@Override
		public String getStageName() {
			return CLEANSING;
		}
	
		@Override
		public String getProcessName() {
			return "Minimum Length";
		}
	
	};
	public static final LogAction SIMILAR_FEATURES = new LogAction() {
        
        @Override
	    public String getStageName() {
         return CLEANING;   
        }
        
        @Override
        public String getProcessName() {
            return "Similar Features";
        }
    };
	public static final LogAction NODE_INSERTION = new LogAction() {
        
        @Override
        public String getStageName() {
         return CLEANING;   
        }
        
        @Override
        public String getProcessName() {
            return "Node Insertion";
        }
    };
}
