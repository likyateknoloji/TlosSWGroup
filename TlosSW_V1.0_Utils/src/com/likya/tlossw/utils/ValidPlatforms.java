/*
 * TlosFaz2
 * com.likya.tlos.core.spc.helpers : ValidPlatforms.java
 * @author Serkan Ta�
 * Tarih : 09.Kas.2008 23:40:05
 */

package com.likya.tlossw.utils;

public class ValidPlatforms {
	
	public static boolean isOSValid() {
		
		if (getCommand("") != null) {
			return true;
		}
		
		return false;
		
	}

		/**
	 * Windows'da bulunan farkl� davran�� bi�imi nedeni ile, a�a��daki davran�� matrisi
	 * ge�erlidir.
	 * 
	 *  Windows i�in komut davran�� matrisi
	 *  
	 *  *-----------------------------------------------------------------------------------------------*---------------------------*
	 *  *						|	Hatal� Jar	|	Hatal� ��leri Durdurma	| Hatas�z ��leri Durdurma	|	��in biti�ini anlama	*
	 *  *-----------------------------------------------------------------------------------------------*---------------------------*
	 *  *	[0] = cmd			|				|							|							*
	 *  *	[1] = /c			|	Ba�ar�l�	|			Ba�ar�l�		|		Ba�ar�s�z			*							*
	 *  *	[2] = jobCommand	|				|							|							*
	 *  *-----------------------------------------------------------------------------------------------*---------------------------*
	 *  *	[1] = "cmd"			|				|							|							*
	 *  *	[2] = ""			|	Ba�ar�s�z	|			Ba�ar�l�		|		Ba�ar�l�			*		Ba�ar�s�z			*
	 *  *	[3] = jobCommand	|				|							|							*
	 *  *-----------------------------------------------------------------------------------------------*---------------------------*
	 *  *	[1] = "cmd"			|				|							|							*
	 *  *	[2] = jobCommand	|	         	|			        		|		        			*		         			*
	 *  *	[3] = ""			|				|							|							*
	 *  *-----------------------------------------------------------------------------------------------*---------------------------*
	 *  *	[0] = jobCommand	|				|							|							*
	 *  *	[1] = ""			|	Ba�ar�l�	|			Ba�ar�l�		|		Ba�ar�s�z			*							*
	 *  *	[2] = ""			|				|							|							*
	 *  *-----------------------------------------------------------------------------------------------*---------------------------*
	 *  *	[0] = cmd			|				|							|							*
	 *  *	[1] = /k			|	Ba�ar�s�z	|			Ba�ar�l�		|		Ba�ar�s�z			*							*
	 *  *	[2] = jobCommand	|				|							|							*
	 *  *-----------------------------------------------------------------------------------------------*---------------------------*
	 */

	/**
	 * Windows'da bulunan farkl� davran�� bi�imi nedeni ile, a�a��daki
	 * davran�� matrisi ge�erlidir.
	 * 
	 * Windows i�in komut davran�� matrisi
	 * 
	 * *----------------------------------------------------------------
	 * -------------------------------*---------------------------* * |
	 * Hatal� Jar | Hatal� ��leri Durdurma | Hatas�z ��leri Durdurma |
	 * ��in biti�ini anlama *
	 * *------------------------------------------
	 * ------------------------
	 * -----------------------------*---------------------------* * [0]
	 * = cmd | | | * * [1] = /c | Ba�ar�l� | Ba�ar�l� | Ba�ar�s�z * * *
	 * [2] = jobCommand | | | *
	 * *----------------------------------------
	 * --------------------------
	 * -----------------------------*---------------------------* * [1]
	 * = "cmd" | | | * * [2] = "" | Ba�ar�s�z | Ba�ar�l� | Ba�ar�l� *
	 * Ba�ar�s�z * * [3] = jobCommand | | | *
	 * *--------------------------
	 * ----------------------------------------
	 * -----------------------------*---------------------------* * [1]
	 * = "cmd" | | | * * [2] = jobCommand | | | * * * [3] = "" | | | *
	 * *--
	 * ----------------------------------------------------------------
	 * -----------------------------*---------------------------* * [0]
	 * = jobCommand | | | * * [1] = "" | Ba�ar�l� | Ba�ar�l� | Ba�ar�s�z
	 * * * * [2] = "" | | | *
	 * *------------------------------------------
	 * ------------------------
	 * -----------------------------*---------------------------* * [0]
	 * = cmd | | | * * [1] = /k | Ba�ar�s�z | Ba�ar�l� | Ba�ar�s�z * * *
	 * [2] = jobCommand | | | *
	 * *----------------------------------------
	 * --------------------------
	 * -----------------------------*---------------------------*
	 */
	
	// TODO Bu k�s�m de�i�meli!
	/**
	 * Asl�nda a�a��daki gibi bir liste olmamal�. Sadece deploy edilecek sisteme
	 * ait lisans olmal�
	 */
	public static String[] getCommand(String jobCommand) {

		String osName = System.getProperty("os.name");
		String[] cmd = new String[3];

		if (osName.equals("Windows Vista")) {
			
			/*
			 * cmd[0] = "cmd.exe"; cmd[1] = "/C"; cmd[2] = jobCommand;
			 */

			/*
			 * cmd[0] = "cmd.exe"; cmd[1] = "/K"; cmd[2] = jobCommand;
			 */

			/*
			 * cmd[0] = "cmd.exe"; cmd[1] = ""; cmd[2] = jobCommand;
			 */

			cmd[0] = "cmd.exe";
			cmd[1] = "/C";
			cmd[2] = jobCommand;

			/*
			 * cmd[0] = "cmd.exe"; cmd[1] = jobCommand; cmd[2] = "";
			 */

		} else if (osName.equals("Windows NT")) {
			cmd[0] = "cmd.exe";
			cmd[1] = "/C";
			cmd[2] = jobCommand;
		} else if (osName.equals("Windows 95")) {
			cmd[0] = "command.com";
			cmd[1] = "/C";
			cmd[2] = jobCommand;
		} else if (osName.equals("Windows XP")) {
			cmd[0] = "cmd.exe";
			cmd[1] = "/C";
			cmd[2] = jobCommand;
		} else if (osName.equals("Windows 7")) {
			cmd[0] = "cmd.exe";
			cmd[1] = "/C";
			cmd[2] = jobCommand;
		} else if (osName.equals("HP-UX")) {
			cmd[0] = "/bin/sh";
			cmd[1] = "-c";
			cmd[2] = jobCommand;
		} else if (osName.equals("HP-UX")) {
			cmd[0] = "/bin/sh";
			cmd[1] = "-c";
			cmd[2] = jobCommand;
		} else if (osName.equals("SunOS")) {
			cmd[0] = "/bin/sh";
			cmd[1] = "-c";
			cmd[2] = jobCommand;
		} else if(osName.equals("Mac OS X")) {
			cmd[0] = "/bin/sh";
			cmd[1] = "-c";
			cmd[2] = jobCommand;
		} else {
//			System.out.println("----------->windows");
//			System.out.println("----------->" + osName.toLowerCase());
//			System.out.println("----------->" + osName.toLowerCase().indexOf("windows"));
			if (osName.toLowerCase().indexOf("windows") != -1) {
				cmd[0] = "cmd.exe";
				cmd[1] = "/C";
				cmd[2] = jobCommand;
			} else {
				cmd[0] = "/bin/sh";
				cmd[1] = "-c";
				cmd[2] = jobCommand;
			}
			// TlosServer.getLogger().error(osName +
			// " sistemi desteklenmiyor !");
		}

		return cmd;
	}

}
