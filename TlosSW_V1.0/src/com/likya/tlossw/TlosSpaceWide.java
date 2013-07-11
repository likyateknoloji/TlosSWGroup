package com.likya.tlossw;

import org.apache.log4j.Logger;

import com.likya.tlossw.exceptions.TlosRecoverException;
import com.likya.tlossw.jmx.JMXTLSServer;
import com.likya.tlossw.utils.SpaceWideRegistry;

public class TlosSpaceWide extends TlosSpaceWideBase {

	private static String assassinFlag = "";

	private static Logger logger = SpaceWideRegistry.getGlobalLogger();

	public TlosSpaceWide() {
		super();
	}

	public static void main(String[] args) {
		setSpaceWideRegistry(SpaceWideRegistry.getInstance());
		logger.info("********************** Start of main *********************");
		/*
		 * logger.trace("TRACE"); logger.debug("DEBUG"); logger.info("INFO");
		 * logger.warn("WARN"); logger.error("ERROR"); logger.fatal("FATAL");
		 */
		parseArguments(args);

		TlosSpaceWide tlosSpaceWide = new TlosSpaceWide();
		getSpaceWideRegistry().setSpaceWideReference(tlosSpaceWide);
		tlosSpaceWide.startAssassin();
		tlosSpaceWide.startTlosSpaceWide();
		logger.info("");
		logger.info("********************** End of main *********************");

	}

	private static void parseArguments(String[] args) {

		String USAGE_MSG = "Kullan�m: TlosSpaceWide [-normalize] [-standby ]";

		String arg = "";
		int i = 0;

		while (i < args.length && args[i].startsWith("-")) {

			arg = args[i++];

			// use this type of check for "wordy" arguments
			if (arg.equals("-normalize")) {

			} else if (arg.equals("-standby")) {

			} else {
				System.err.println(USAGE_MSG);
				System.exit(0);
			}

		}

	}

	private Runnable assassin = new Runnable() {
		public void run() {
			synchronized (assassinFlag) {
				try {
					assassinFlag.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				shutdownTlosSpaceWide();
				System.exit(0);
			}
		}
	};

	public void startAssassin() {
		Thread t = new Thread(assassin);
		t.setName("assassin");
		t.setDaemon(true);
		t.start();
	}

	public void processQueueStarters() {

		try {
			if (!loadGlobalstateDefinitions()) {
				/**
				 * Close application reason : persisted states and db states
				 * does not match !!!!
				 */
				logger.info("   > Close application reason : persisted states and db states does not match !!!!");
				logger.error("   >  Close application reason : persisted states and db states does not match !!! (ERROR NO:2102)");
				System.exit(-1);
			}
		} catch (TlosRecoverException e1) {
			e1.printStackTrace();
			logger.info("   > Close application reason : cannot recover GlobalstateDefinitions !");
			logger.info("   > Clean tmp folder or set persistent to false and restart the application !");
			System.exit(-1);
		}

		/**
		 * isAgentEnabled de�i�keni test �al��t�rlmas� yan�nda
		 * lisans ile de ilgili olarak se�imlik olmal�d�r.
		 * �imdilik el yordam� ile de�i�iklik yap�ls�n.
		 * 
		 * @author serkan ta�
		 *         22.09.2012
		 */

		boolean isAgentEnabled = true;
		if (isAgentEnabled) {
			try {
				startAgentManager();
			} catch (Exception e) {
				logger.info("   > Close application reason : cannot recover AgentCache !");
				logger.info("   > Clean tmp folder or set persistent to false and restart the application !");
				System.exit(-1);
			}
		}

		/**
		 * isNagisoEnabled de�i�keni test �al��t�rlmas� yan�nda
		 * lisans ile de ilgili olarak se�imlik olmal�d�r.
		 * �imdilik el yordam� ile de�i�iklik yap�ls�n.
		 * 
		 * @author serkan ta�
		 *         22.09.2012
		 */

		boolean isNagisoEnabled = true;
		if (isNagisoEnabled) {
			/** Start Nagios Server */
			startNagiosServer();
		}

		/**
		 * isPerformanceManagerEnabled de�i�keni test �al��t�rlmas� yan�nda
		 * lisans ile de ilgili olarak se�imlik olmal�d�r.
		 * �imdilik el yordam� ile de�i�iklik yap�ls�n.
		 * 
		 * @author serkan ta�
		 *         22.09.2012
		 */

		boolean isPerformanceManagerEnabled = true;
		if (isPerformanceManagerEnabled) {
			/** Start Performance Manager */
			startPerformanceManager();
		}

		/** License Server */
		// startLicenseManager();

		/**************************************************************************************/

		/** Start Info bus manager */
		try {
			startInfoBusSystem();
		} catch (TlosRecoverException e) {
			logger.error("   > Close application reason : cannot recover InfoBusQueue !");
			logger.error("   > Clean tmp folder or set persistent to false and restart the application !");
			System.exit(-1);
		}

		/** Start alert servers (Log, Mail, SMS, ...) */
		boolean isEmailEnabled = getSpaceWideRegistry().getTlosSWConfigInfo().getSettings().getMailOptions().getUseMail().getValueBoolean();
		if (isEmailEnabled) {
			/** Mail Server */
			startMailSystem();
		}

		/**************************************************************************************/
	}

	private void startTlosSpaceWide() {

		/** Initialize startup conditions **/
		initApplication();

		/** Start database server */
		startExistDBSystem();

		/** Start access and user manager */

		/** Start Comm Interface */

		/** Start GUI Manager */

		/**
		 * program�n normal olmayan bir �ekilde sonlanmas� durumunda, son halini
		 * diskten y�kleyip tekrar baslayabilir. ValueBoolean in default degeri
		 * #false#
		 */
		/**
		 * @author serkan gun donumunun �zerinden bir peryod zaman ge�ip
		 *         ge�medi�ini kontrol edelim Sistemde persistent = true ise
		 *         veya b�t�n persist dosyalar� temp dizine �nceden yaz�lm�� ise
		 *         sistem daha �nce �al��m�� oldu�undan recover edilmelidir bu
		 *         nedenle isFIRST_TIME = false olur.
		 * 
		 *         Ancak persisten = true ise ve en az bir tane persistence
		 *         dosyas� mevcut de�il ise bu durumda sistem ger�ekten ilk defa
		 *         �al��m�� oldu�undan ve ge�mi�i olmad���ndan persistent
		 *         de�ilmi� gibi �al���r ve bu nedenle de FIRST_TIME = true
		 *         olarak kabul edilebilir. A�a��daki if kontrol�ne girmez.
		 **/

		if (TlosSpaceWide.isRecoverable()) {
			getSpaceWideRegistry().setFIRST_TIME(false);
			initG�nD�n�m�PeryodPassed();
		}

		/** gun donumunun gecip gecmedigini kontrol edelim **/
		initSolsticePassed();

		/**
		 * @author serkan recover etme durumunda kullan�c� onay� sonras�
		 *         �al��mas� gerekiyor.
		 * 
		 */
		if (!isRecoverable() || getSpaceWideRegistry().isSolsticePassed()) {
			processQueueStarters();
		}
		
		/** Start Jmx Server */
		// Prototip MC baslatmak icin gerekiyor.
		startJmxServer();
		startJmxTLSServer();

		/** Start HTTP server */
		//startWebSystem();

		/** Baslatma Yoneticisi : Start Central Process Controller (CPC) */
		logger.info("");
		logger.info(" Baslatma kosullari kontrol ediliyor ...");
		logger.info(" Kosullar;");

		if (getSpaceWideRegistry().isFIRST_TIME()) {

			logger.info(" 1 - ilk kez calisiyor ...");

			if (!getSpaceWideRegistry().isSolsticePassed()) {

				/**
				 * Uygulama ilk defa �al��t���ndan ya da s�re� bilgilerini
				 * saklamayacak �ekilde �al��t�rl�d���ndan, ve de hen�z g�n
				 * d�n�m� saati ge�medi�inden uygulama beklemeye ge�er ve g�n
				 * d�n�m� saaati gelince �al���r.
				 */
				logger.info(" 2 - Gundonumu gecmedi.");
				logger.info("");
				logger.info("Aksiyon;");
				logger.info(" 1 - Gundonumu Takipcisini baslat .");

				startDayKeeper();
				logger.info("");
				logger.info("   CPC STATE : GUNDONUMUNU BEKLIYORUM ...");
				logger.info("");

				getSpaceWideRegistry().setWaitConfirmOfGUI(false);

			} else {

				logger.info(" 2 - Gundonumu gecti.");
				logger.info("");
				logger.info("   KULLANICIYA SORUYORUM (HEMEN CALISTIR / BIR SONRAKI GUNDONUMUNU BEKLE) ");

				/**
				 * Ne yap�laca�� ekran �zerinden kullan�c�ya sorulacak :
				 * 
				 * 1. DURUM : Hemen ��lemler �al��t�r�lmaya ba�layabilir
				 * 
				 * 2. DURUM : Bir sonraki g�n d�n�m�ne uymas� istenebilir.
				 */
				getSpaceWideRegistry().setWaitConfirmOfGUI(true);

			}

		} else {

			logger.info(" 1 - ilk calismasi degil.");

			if (!getSpaceWideRegistry().isSolsticePassed()) {
				logger.info(" 2 - Gundonumu gecmedi.");
				logger.info("");
				logger.info("   RECOVERY YAPIYORUM (GUNDONUMU GECMEDI) kullaniciya soruyorum ...");
				logger.info("");

				/**
				 * Ne yap�laca�� ekran �zerinden kullan�c�ya sorulacak :
				 * 
				 * 1. DURUM s�reci kald��� yerden devam ettirebilir (RECOVER).
				 * 
				 * 2. DURUM Eski i�leri (raporlay�p veya raporlamay�p) S�reci
				 * yeninde ba�latabilir.
				 * 
				 * 3. DURUM Eski i�leri (raporlay�p veya raporlamay�p) uygulama
				 * beklemeye ge�er ve g�n d�n�m� saaati gelince �al���r
				 */
				getSpaceWideRegistry().setWaitConfirmOfGUI(true);

			} else if (getSpaceWideRegistry().isSolsticePassed()) {

				logger.info(" 2 - Gundonumu gecti. Son gun donumu okumasi uzerinden bir peryod gecti.");
				logger.info("");
				logger.info("Aksiyon;");
				logger.info(" 1 - Gundonumu Takipcisini baslat .");
				logger.info(" 2 - islerin guncel durumunu VT nina sakla.");

				/**
				 * Uygulama daha �nce �al��m�� oldu�undan ve uygulaman�n son g�n
				 * d�n�m� okudu�u zaman �zerinden bir peryod zaman�ndan fazla
				 * zaman ge�ti�inden, eldeki islerin bilgileri VT'na raporlan�p,
				 * uygulama beklemeye ge�er ve g�n d�n�m� saaati gelince
				 * �al���r.
				 * 
				 * NOT : Burada g�n d�n�m� ge�mi� ise �al��ma bir sonraki g�n
				 * d�n�m�ne g�re olu�ur, ge�memi� ise o g�n i�indeki g�n
				 * d�n�m�ne g�re olu�ur, k�saca en yak�n g�n d�n�m�ne g�re
				 * planlan�r.
				 */

				logger.info("TODO Report recovered info to db");
				startDayKeeper();
				logger.info("");
				logger.info("   CPC STATE : RECOVERY YAPIYORUM ...");
				logger.info("");

				getSpaceWideRegistry().setWaitConfirmOfGUI(false);

			} else {

				logger.fatal("Baslatma Yoneticisi icin beklenmeyen bir durum olustu... !! Kod: 0001");

				System.exit(-1);
			}

		}

		logger.info("#############################################");
		logger.info("Startign CpcTester...");
		startCpcTester();
		logger.info("Startign CpcTester...Done");
		logger.info("#############################################");

		logger.info("#############################################");
		logger.info("");

	}

	public static void stopSpacewide() {
		synchronized (assassinFlag) {
			assassinFlag.notifyAll();
		}
	}

	private void shutdownTlosSpaceWide() {
		/**
		 * stop HTTP server
		 */

		// shutDownHttpServer();

		/**
		 * Stop alert servers (Log, Mail, SMS, ...)
		 */

		/**
		 * Stop Mail Server
		 */
		shutDownMailServer();

		/**
		 * Stop Info bus manager
		 */
		shutDownInfobusManager();

		/**
		 * Stop database server, connection pool and manager
		 */

		shutDownDBServer();

		/**
		 * stop persistence manager
		 */

		/**
		 * stop performance manager
		 */

		/**
		 * stop event manager
		 */

		/**
		 * stop grid manager
		 */

		/**
		 * Stop access and user manager
		 */

		/**
		 * BEY�N Stop CPC (Central process controller)
		 */
		shutDownDayKeeper();

		shutDownCpcServer();

		/**
		 * Stop Comm Interface
		 */

		/*
		 * tlosCommInterface = new TlosCommInterface(this);
		 * 
		 * schedulerLogger.info("�leti�im arabirimi ba�lat�l�yor...");
		 * tlosCommInterface = new TlosCommInterface(this);
		 */

		/**
		 * Stop Remote Manager
		 */

		/*
		 * schedulerLogger.info("Web arabirimi ba�lat�l�yor ...");
		 * TlosWebConsole tlosWebConsole = new TlosWebConsole(this);
		 * schedulerLogger.info("Hostname : " + tlosWebConsole.getHostName() +
		 * " " + "Port : " + tlosWebConsole.getHttpPort());
		 * tlosWebConsole.initServer();
		 * schedulerLogger.info("Web arabirimi ba�lat�ld� !");
		 * 
		 * schedulerLogger.info("Y�netim Konsolu ba�lat�l�yor..."); try {
		 * managementConsoleHandler =
		 * ManagementConsole.initComm(tlosCommInterface,
		 * tlosParameters.getManagementPort(),
		 * tlosParameters.getManagementBufferSize()); new
		 * Thread(managementConsoleHandler).start(); } catch (SocketException e)
		 * { schedulerLogger.fatal("Y�netim Konsolu ba�lat�lamad� !");
		 * schedulerLogger.fatal("Program sona erdi."); e.printStackTrace();
		 * System.exit(-1); }
		 * schedulerLogger.info("Y�netim Konsolu ba�lat�ld� !");
		 */

		/**
		 * Stop GUI Manager
		 */

		/**
		 * Stop Jmx Server
		 */
		if (useJmx) {
			JMXTLSServer.disconnect();
		}

		println("TlosSpaceWide terminated successfully !");
	}

}
