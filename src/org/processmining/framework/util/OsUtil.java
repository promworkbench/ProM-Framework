package org.processmining.framework.util;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.processmining.framework.boot.Boot;

import sun.management.ManagementFactoryHelper;

import com.sun.management.OperatingSystemMXBean;

public class OsUtil {

	public static final String OS_WIN32 = "Windows 32 bit";
	public static final String OS_WIN64 = "Windows 64 bit";
	public static final String OS_MACOSX = "Mac OS X";
	public static final String OS_MACOSCLASSIC = "Mac OS 7-9";
	public static final String OS_LINUX = "Linux";
	public static final String OS_BSD = "BSD";
	public static final String OS_RISCOS = "RISC OS";
	public static final String OS_BEOS = "BeOS";
	public static final String OS_UNKNOWN = "unknown";

	private static String currentOs = null;

	public static String determineOS() {
		if (currentOs == null) {
			String osString = System.getProperty("os.name").trim().toLowerCase();
			if (osString.startsWith("windows")) {
				currentOs = OS_WIN32;
			} else if (osString.startsWith("mac os x")) {
				currentOs = OS_MACOSX;
			} else if (osString.startsWith("mac os")) {
				currentOs = OS_MACOSCLASSIC;
			} else if (osString.startsWith("risc os")) {
				currentOs = OS_RISCOS;
			} else if ((osString.indexOf("linux") >= 0) || (osString.indexOf("debian") >= 0)
					|| (osString.indexOf("redhat") >= 0) || (osString.indexOf("lindows") >= 0)) {
				currentOs = OS_LINUX;
			} else if ((osString.indexOf("freebsd") >= 0) || (osString.indexOf("openbsd") >= 0)
					|| (osString.indexOf("netbsd") >= 0) || (osString.indexOf("irix") >= 0)
					|| (osString.indexOf("solaris") >= 0) || (osString.indexOf("sunos") >= 0)
					|| (osString.indexOf("hp/ux") >= 0) || (osString.indexOf("risc ix") >= 0)
					|| (osString.indexOf("dg/ux") >= 0)) {
				currentOs = OS_BSD;
			} else if (osString.indexOf("beos") >= 0) {
				currentOs = OS_BEOS;
			} else {
				currentOs = OS_UNKNOWN;
			}
		}
		return currentOs;
	}

	public static boolean is64Bit() {
		return System.getProperty("sun.arch.data.model").equals("64");
	}

	public static boolean is32Bit() {
		return System.getProperty("sun.arch.data.model").equals("32");
	}

	public static boolean isRunningWindows() {
		return determineOS() == OS_WIN32;
	}

	public static boolean isRunningMacOsX() {
		return determineOS() == OS_MACOSX;
	}

	public static boolean isRunningLinux() {
		return determineOS() == OS_LINUX;
	}

	public static boolean isRunningUnix() {
		String os = determineOS();
		return (os == OS_BSD) || (os == OS_LINUX) || (os == OS_MACOSX);
	}

	public static void setWorkingDirectoryAtStartup() {
		if (isRunningMacOsX()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			File here = new File(".");
			try {
				if (new File(here.getAbsolutePath() + "/ProM.app").exists()) {
					System.out.println("--> Mac OS X: running from application bundle (1).");
					File nextHere = new File(here.getCanonicalPath() + "/ProM.app/Contents/Resources/ProMhome");
					System.setProperty("user.dir", nextHere.getCanonicalPath());
				} else if (here.getAbsolutePath().matches("^(.*)ProM\\.app(/*)$")) {
					System.out.println("--> Mac OS X: running from application bundle (2).");
					File nextHere = new File(here.getCanonicalPath() + "/Contents/Resources/ProMhome");
					System.setProperty("user.dir", nextHere.getCanonicalPath());
				}
				System.out.println("Mac OS X: Working directory set to " + System.getProperty("user.dir") + " (from "
						+ here.getAbsolutePath() + ")");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Mac OS X: Working directory set to " + System.getProperty("user.dir") + " (from "
					+ here.getAbsolutePath() + ")");
		}
	}

	//	public static File getProMUserDirectory() {
	//		File dir = new File(Boot.PROM_USER_FOLDER);
	//		//System.getProperty("user.home", ""), ".ProM");
	//		dir.mkdirs();
	//		return dir;
	//	}

	/**
	 * Returns a handle to the ProM package folder. Creates the folder in case
	 * it does not yet exist.
	 * 
	 * @return The handle to the folder.
	 */
	public static File getProMPackageDirectory() {
		return getDirectory(Boot.PACKAGE_FOLDER);
	}

	/**
	 * Returns whether the ProM package folder exists on this system.
	 * 
	 * @return Whether the folder exists.
	 */
	public static boolean hasProMPackageDirectory() {
		return hasDirectory(Boot.PACKAGE_FOLDER);
	}

	/**
	 * Returns a handle to the ProM workspace folder. Creates the folder in case
	 * it does not yet exist.
	 * 
	 * @return The handle to the folder.
	 */
	public static File getProMWorkspaceDirectory() {
		return getDirectory(Boot.WORKSPACE_FOLDER);
	}

	/**
	 * Returns whether the ProM workspace folder exists on this system.
	 * 
	 * @return Whether the folder exists.
	 */
	public static boolean hasProMWorkspaceDirectory() {
		return hasDirectory(Boot.WORKSPACE_FOLDER);
	}

	/*
	 * Returns a handle to the folder with provided name. Creates the folder in
	 * case it does not yet exist.
	 * 
	 * @return The handle to the folder.
	 */
	private static File getDirectory(String dirName) {
		File dir = new File(dirName);
		dir.mkdirs();
		return dir;
	}

	/*
	 * Returns whether the folder with provided name exists on this system.
	 * 
	 * @return Whether the folder exists.
	 */
	private static boolean hasDirectory(String dirName) {
		File dir = new File(dirName);
		return dir.exists();
	}

	public static long getPhysicalMemory() {
		try {
			OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactoryHelper
					.getOperatingSystemMXBean();
			return operatingSystemMXBean.getTotalPhysicalMemorySize();
		} catch (Exception e) {
			// Does not work, try something else.
		}
		try {
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			Object attribute = mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"),
					"TotalPhysicalMemorySize");
			return Long.parseLong(attribute.toString());
		} catch (Exception e) {
			// Does not work, try something else.
		}
		// If all else fails, assume thare is just 1 GB of RAM.
		return 1024 * 1024 * 1024;
	}
}
