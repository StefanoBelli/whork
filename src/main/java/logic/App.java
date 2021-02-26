package logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import logic.controller.MailController;
import logic.controller.MailSender;
import logic.dao.ComuniDao;
import logic.exception.DataAccessException;
import logic.exception.DatabaseException;
import logic.util.Util;
import logic.dao.EmploymentStatusDao;

import java.util.ArrayList;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

final class App {
	// Logger tagged "WhorkStartup" for startup phase
	private static final Logger LOGGER = LoggerFactory.getLogger("WhorkStartup");

	// Cmdline opts
	private static final String BASEOPT = "base";
	private static final String WEBRESOPT = "webRes";
	private static final String WEBROOTOPT = "webRoot";
	private static final String HELPOPT = "help";
	private static final String PORTOPT = "port";
	private static final String DBCONNECTOPT = "dbConnect";
	private static final String DBUSEROPT = "dbUser";
	private static final String DBPWDOPT = "dbPwd";
	private static final String DESKTOPOPT = "desktop";
	private static final String MAILFROMOPT = "mailFrom";
	private static final String MAILPASSWORDOPT = "mailPassword";
	private static final String MAILSMTPPORTOPT = "mailSmtpPort";
	private static final String MAILNOTLSOPT = "mailNoTls";
	private static final String MAILHOSTOPT = "mailHost";

	// Self-extraction properties
	private static ArrayList<String> webResDirectory = new ArrayList<>();
	private static ArrayList<String> webResFiles = new ArrayList<>();

	// Dynamic config for whork
	private static String webRoot;
	private static String base = "";
	private static int port = 8080;
	private static boolean selfExtract = isJarPackaged();
	private static String dbConnect = "localhost:3306";
	private static String dbUser = null;
	private static String dbPwd = null;
	private static boolean launchDesktop = false;
	private static String mailTls = "true";
	private static String mailSmtpPort = "587";
	private static String mailFrom = null;
	private static String mailPwd = null;
	private static String mailHost = null;

	// Static config for whork
	private static final String DBNAME = "whorkdb";

	private static final String NOT_USING_PWDAUTH = "NOT using password authentication";
	private static final String HIDE_PWD = "[HIDDEN]";

	private static void setResources() {
		webResDirectory.add("WEB-INF");

		webResFiles.add("/WEB-INF/web.xml");
		webResFiles.add("/index.jsp");
	}

	private static void selfExtraction() throws IOException {
		if (selfExtract) {
			LOGGER.info("starting self extraction...");
			setResources();

			File f = new File(webRoot);
			f.mkdir();

			for (final String dir : webResDirectory) {
				StringBuilder builder = new StringBuilder();
				builder.append(webRoot);
				builder.append("/");
				builder.append(dir);

				File newDir = new File(builder.toString());
				newDir.mkdir();
			}

			for (String res : webResFiles) {
				StringBuilder builder = new StringBuilder();

				builder.append(webRoot);
				builder.append(res);

				String path = builder.toString();
				File resFile = new File(path);
				if (!resFile.createNewFile())
					LOGGER.info("{} already exists", path);

				BufferedInputStream istOrigin = new BufferedInputStream(App.class.getResourceAsStream(res));

				BufferedOutputStream ostDest = new BufferedOutputStream(new FileOutputStream(resFile));

				byte[] buffer = new byte[1024];
				int lengthRead;

				try {
					while ((lengthRead = istOrigin.read(buffer)) > 0) {
						ostDest.write(buffer, 0, lengthRead);
					}
				} finally {
					ostDest.close();
					istOrigin.close();
				}
			}
		} else {
			LOGGER.info("skipping self extraction...");
		}
	}

	private static boolean isJarPackaged() {
		return App.class.getResource("App.class").toString().charAt(0) == 'j';
	}

	private static boolean startTomcat() {
		Tomcat tomcat = new Tomcat();

		tomcat.setPort(port);
		tomcat.addWebapp(base, webRoot);

		try {
			tomcat.start();
		} catch (LifecycleException e) {
			e.printStackTrace();
			return false;
		}

		LOGGER.info("startup successful: up and running!");

		tomcat.getServer().await();

		return true;
	}

	private static Options createOptions() {
		Options opt = new Options();

		// webapp
		opt.addOption(PORTOPT, true, new StringBuilder()
				.append("Provide different port from the standard one (default: ").append(port).append(")").toString());

		// webapp
		opt.addOption(BASEOPT, true, new StringBuilder().append("Provide base path for website (default: ")
				.append(base.isEmpty() ? "/" : base).append(")").toString());

		// webapp
		opt.addOption(WEBROOTOPT, true,
				new StringBuilder()
						.append("Provide different root directory for web resource extraction and usage (default: ")
						.append(webRoot == null ? "webRes must be provided" : webRoot).append(")").toString());

		opt.addOption(DBCONNECTOPT, true,
				new StringBuilder()
						.append("Where we can find your DB? \"hostname:[0-65535]\". We don't check syntax. (default: ")
						.append(dbConnect).append(")").toString());

		opt.addOption(DBPWDOPT, true, new StringBuilder().append("Password for \"dbUser\". (default: ")
				.append(dbPwd == null ? NOT_USING_PWDAUTH : dbPwd).append(")").toString());

		// webapp
		opt.addOption(WEBRESOPT, true,
				"Provide web resources on your own (required if no self-extraction, default: none)");

		opt.addOption(DBUSEROPT, true,
				"User which has sufficient privileges to manage whork's DB. (required, default: none)");

		opt.addOption(DESKTOPOPT, false, "Launch desktop application");

		opt.addOption(MAILFROMOPT, true, "From email address (required)");

		opt.addOption(MAILPASSWORDOPT, true, "Mail provider password authentication");

		opt.addOption(MAILHOSTOPT, true, "Mail provider host (required, hostname or IP address)");

		opt.addOption(MAILSMTPPORTOPT, true, new StringBuilder().append("Mail provider SMTP port (default: ")
				.append(mailSmtpPort).append(")").toString());

		opt.addOption(MAILNOTLSOPT, false, "Disable TLS for mail provider");

		opt.addOption(HELPOPT, false, "Print this help and immediately exit");

		return opt;
	}

	private static boolean assignBase(String arg, String value) {
		base = value;
		if (base.equals("/")) {
			base = "";
		} else if (base.charAt(0) != '/' || base.endsWith("/")) {
			LOGGER.error("{} must start with / *AND* not end with /", arg);
			return false;
		}

		return true;
	}

	private static boolean assignServerPort(String arg, String value) {
		port = Integer.parseInt(value);
		if (!Util.isValidPort(port)) {
			LOGGER.error("{} number must be within range [0-65535]", arg);
			return false;
		}

		return true;
	}

	private static boolean assignSmtpPort(String arg, String value) {
		mailSmtpPort = value;
		if (!Util.isValidPort(Integer.parseInt(mailSmtpPort))) {
			LOGGER.error("{} number must be within range [0-65535]", arg);
			return false;
		}

		return true;
	}

	private static void assignWebRootOnRes(String arg, String value) {
		if (selfExtract) {
			LOGGER.warn("ignoring {} value because self extraction is enabled...", arg);
		} else {
			webRoot = new File(value).getAbsolutePath();
		}
	}

	private static void assignWebRootOnRoot(String arg, String value) {
		if (!selfExtract) {
			LOGGER.warn("ignoring {} property because self extraction is disabled...", arg);
		} else {
			webRoot = new File(value).getAbsolutePath();
		}
	}

	private static boolean optCheck(Option opt) {
		String argName = opt.getOpt();

		if (argName == null) {
			return true;
		}

		if (argName.equals(BASEOPT) && 
				!assignBase(argName, opt.getValue())) {
			return false;
		} else if (argName.equals(PORTOPT) && 
				!assignServerPort(argName, opt.getValue())) {
			return false;
		} else if (argName.equals(WEBRESOPT)) {
			assignWebRootOnRes(argName, opt.getValue());
		} else if (argName.equals(WEBROOTOPT)) {
			assignWebRootOnRoot(argName, opt.getValue());
		} else if (argName.equals(DBCONNECTOPT)) {
			dbConnect = opt.getValue();
		} else if (argName.equals(DBUSEROPT)) {
			dbUser = opt.getValue();
		} else if (argName.equals(DBPWDOPT)) {
			dbPwd = opt.getValue();
		} else if (argName.equals(DESKTOPOPT)) {
			launchDesktop = true;
		} else if (argName.equals(MAILHOSTOPT)) {
			mailHost = opt.getValue();
		} else if (argName.equals(MAILNOTLSOPT)) {
			mailTls = "false";
		} else if (argName.equals(MAILSMTPPORTOPT) && 
				!assignSmtpPort(argName, opt.getValue())) {
			return false;
		} else if (argName.equals(MAILFROMOPT)) {
			mailFrom = opt.getValue();
		} else if (argName.equals(MAILPASSWORDOPT)) {
			mailPwd = opt.getValue();
		}

		return true;
	}

	private static String getPassword(String pwd) {
		return pwd == null ? NOT_USING_PWDAUTH : HIDE_PWD;
	}

	private static void logParsedConfig() {
		if (launchDesktop) {
			LOGGER.info(
					"Settings for Whork desktop:\n--> db: {}\n |--> dbuser:"
							+ " {}\n |--> dbpwd: {}\n--> mailfrom: {}\n--> mailhost:"
							+ " {}\n--> mailpwd: {}\n--> mailtls: {}\n--> smtpport: {}",
					dbConnect, dbUser, getPassword(dbPwd), mailFrom, mailHost, getPassword(mailPwd), mailTls,
					mailSmtpPort);
		} else {
			LOGGER.info(
					"{}:\n--> port: {}\n--> base: {}\n--> webroot: {}\n"
							+ "--> self-extract? {}\n--> db: {}\n |--> dbuser: {}\n"
							+ " |--> dbpwd: {}\n--> mailfrom: {}\n--> mailhost: {}\n"
							+ "--> mailpwd: {}\n--> mailtls: {}\n--> smtpport: {}",
					"Settings for Whork webapp", port, base.isEmpty() ? "/" : base, webRoot, selfExtract, dbConnect,
					dbUser, getPassword(dbPwd), mailFrom, mailHost, getPassword(mailPwd), mailTls, mailSmtpPort);
		}
	}

	private static boolean configRequirementsChecks() {
		if (dbUser == null) {
			LOGGER.error("you must pass -{} in order to access DB", DBUSEROPT);
			return false;
		}

		if (mailHost == null || mailFrom == null) {
			LOGGER.error("you must pass -{} and -{} in order to be able to send emails", MAILFROMOPT, MAILHOSTOPT);
			return false;
		}

		if (!launchDesktop && webRoot == null && !selfExtract) {
			LOGGER.error("you must pass -{} in order to specify where to locate web resources", WEBRESOPT);
			return false;
		}

		return true;
	}

	private static boolean propertySetup(String[] args) throws ParseException {
		Options options = createOptions();
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("whork-webapp", options);
			return false;
		}

		for (Option option : cmd.getOptions()) {
			if (!optCheck(option))
				return false;
		}

		if (!configRequirementsChecks()) {
			return false;
		}

		logParsedConfig();

		return true;
	}

	private static void utilDeleteDirectoryRecursion(Path path) throws IOException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					utilDeleteDirectoryRecursion(entry);
				}
			}
		}
		Files.delete(path);
	}

	private static void cleanup() {
		Logger cleanupLogger = LoggerFactory.getLogger("WhorkCleanup");

		if (!launchDesktop && selfExtract) {
			cleanupLogger.info("deleting webroot...");
			try {
				utilDeleteDirectoryRecursion(Paths.get(webRoot));
			} catch (IOException e) {
				e.getMessage();
				cleanupLogger.error("unable to delete webroot @ {}", webRoot);
			}
		}

		cleanupLogger.info("closing DB connection...");

		try {
			Database.getInstance().closeConnection();
		} catch (DatabaseException e) {
			cleanupLogger.warn(e.getMessage());
			cleanupLogger.warn("please report this, we don't expect this at this point. Something is broken :S");
		}

		cleanupLogger.info("bye bye!");
	}

	private static void exceptionMessageBeforeStart(Exception e, String msg) {
		LOGGER.error("{}: {}", e.getMessage(), msg);
		LOGGER.error("Whork will not start!");
	}

	private static boolean prePopulatePools() {
		LOGGER.info("Prepopulating pools with default values (presets)...");

		try {
			ComuniDao.populatePool();
			EmploymentStatusDao.populatePool();
		} catch (DataAccessException e) {
			exceptionMessageBeforeStart(e, e.getCause().getMessage());
			return false;
		}

		return true;
	}

	private static void setMailSender() {
		LOGGER.info("Setting mail sender...");

		MailSender sender = new MailSender();
		sender.setFrom(mailFrom);
		sender.setHost(mailHost);
		sender.setPassword(mailPwd);
		sender.setTls(mailTls);
		sender.setPort(mailSmtpPort);

		MailController.setMailSender(sender);
	}

	private static boolean attemptToEstablishDbConnection() {
		LOGGER.info("Checking if we can correctly talk to DB...");
		LOGGER.info("Driver: {}", Database.DRIVER);

		DatabaseName.setDbName(DBNAME);

		try {
			Connection conn = Database.getInstance(dbConnect, dbUser, dbPwd).getConnection();
			conn.setCatalog(DatabaseName.getDbName());
			LOGGER.info("Yes, we're able to talk to DB!");
		} catch (ClassNotFoundException e) {
			exceptionMessageBeforeStart(e, "unable to load driver class, this SHOULD be reported!");
			return false;
		} catch (SQLException e) {
			exceptionMessageBeforeStart(e, "unable to correctly establish a DB connection!");
			return false;
		}

		return true;
	}

	private static boolean initialize() {
		if (!attemptToEstablishDbConnection()) {
			return false;
		}

		if (!prePopulatePools()) {
			return false;
		}

		setMailSender();

		return true;
	}

	private static boolean parse(String[] args) {
		try {
			if (selfExtract) {
				webRoot = new File("whork_webroot").getAbsolutePath();
			} else {
				webRoot = null;
			}

			if (!propertySetup(args)) {
				return false;
			}

			if (!launchDesktop) {
				selfExtraction();
			} else {
				LOGGER.info("launching desktop application: any of {},{},{},{} options are ignored", BASEOPT, PORTOPT,
						WEBRESOPT, WEBROOTOPT);
				LOGGER.info("launching desktop application: self extraction is ignored anyway");
			}
		} catch (ParseException e) {
			exceptionMessageBeforeStart(e, "unable to parse command line");
			return false;
		} catch (IOException e) {
			exceptionMessageBeforeStart(e, "unable to correclty self-extract");
			return false;
		}

		return true;
	}

	private static void setRuntimeHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				App.cleanup();
			}
		});
	}

	private static void finalizeLaunch(String[] args) {
		if (!launchDesktop) {
			LOGGER.info("Welcome to Whork webapp! Starting up...");

			if (!startTomcat()) {
				LOGGER.error("unable to start tomcat, details above");
			}
		} else {
			LOGGER.info("Welcome to Whork desktop! Starting up...");
			WhorkDesktopLauncher.launchApp(args);
		}
	}

	public static void main(String[] args) {
		if (parse(args)) {
			setRuntimeHooks();
			if (initialize()) {
				finalizeLaunch(args);
			}
		}
	}
}
