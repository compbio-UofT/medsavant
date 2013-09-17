/*
 * Control a web browser from your java application.
 * Copyright (C) 2001-2002 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */
package com.Ostermiller.util;

import java.io.*;
import java.text.MessageFormat;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLClassLoader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Properties;
import java.util.Vector;
import java.util.ResourceBundle;
import java.util.Locale;
import java.io.StringReader;
import java.lang.reflect.*;

/**
 * Allows URLs to be opened in the system browser on Windows and Unix.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/Browser.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class Browser {

	/**
	 * The dialog that allows user configuration of the options for this class.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected static BrowserDialog dialog;

	/**
	 * Locale specific strings displayed to the user.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected static ResourceBundle labels = ResourceBundle.getBundle("com.Ostermiller.util.Browser",  Locale.getDefault());

	/**
	 * Set the locale used for getting localized
	 * strings.
	 *
	 * @param locale Locale used to for i18n.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void setLocale(Locale locale){
		labels = ResourceBundle.getBundle("com.Ostermiller.util.Browser",  locale);
	}

	/**
	 * A list of commands to try in order to display the url.
	 * The url is put into the command using MessageFormat, so
	 * the URL will be specified as {0} in the command.
	 * Some examples of commands to try might be:<br>
	 * <code>rundll32 url.dll,FileProtocolHandler {0}</code></br>
	 * <code>netscape {0}</code><br>
	 * These commands are passed in order to exec until something works
	 * when displayURL is used.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String[] exec = null;

	/**
	 * Determine appropriate commands to start a browser on the current
	 * operating system.  On windows: <br>
	 * <code>rundll32 url.dll,FileProtocolHandler {0}</code></br>
	 * On other operating systems, the "which" command is used to
	 * test if Mozilla, netscape, and lynx(xterm) are available (in that
	 * order).
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void init(){
		exec = defaultCommands();
	}

	/**
	 * Retrieve the default commands to open a browser for this system.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String[] defaultCommands(){
		String[] exec = null;
		if ( System.getProperty("os.name").startsWith("Windows")){
			exec = new String[]{
				"rundll32 url.dll,FileProtocolHandler {0}",
			};
		} else if (System.getProperty("os.name").startsWith("Mac")){
			Vector browsers = new Vector();
			try {
				Process p = Runtime.getRuntime().exec("which open");
				if (p.waitFor() == 0){
					browsers.add("open {0}");
				}
			} catch (IOException e){
			} catch (InterruptedException e){
			}
			if (browsers.size() == 0){
				exec = null;
			} else {
				exec = (String[])browsers.toArray(new String[0]);
			}
		} else {
			Vector browsers = new Vector();
			try {
				Process p = Runtime.getRuntime().exec("which firebird");
				if (p.waitFor() == 0){
					browsers.add("firebird -remote openURL({0})");
					browsers.add("firebird {0}");
				}
			} catch (IOException e){
			} catch (InterruptedException e){
			}try {
				Process p = Runtime.getRuntime().exec("which mozilla");
				if (p.waitFor() == 0){
					browsers.add("mozilla -remote openURL({0})");
					browsers.add("mozilla {0}");
				}
			} catch (IOException e){
			} catch (InterruptedException e){
			}
			try {
				Process p = Runtime.getRuntime().exec("which opera");
				if (p.waitFor() == 0){
					browsers.add("opera -remote openURL({0})");
					browsers.add("opera {0}");
				}
			} catch (IOException e){
			} catch (InterruptedException e){
			}
			try {
				Process p = Runtime.getRuntime().exec("which galeon");
				if (p.waitFor() == 0){
					browsers.add("galeon {0}");
				}
			} catch (IOException e){
			} catch (InterruptedException e){
			}
			try {
				Process p = Runtime.getRuntime().exec("which konqueror");
				if (p.waitFor() == 0){
					browsers.add("konqueror {0}");
				}
			} catch (IOException e){
			} catch (InterruptedException e){
			}
			try {
				Process p = Runtime.getRuntime().exec("which netscape");
				if (p.waitFor() == 0){
					browsers.add("netscape -remote openURL({0})");
					browsers.add("netscape {0}");
				}
			} catch (IOException e){
			} catch (InterruptedException e){
			}
			try {
				Process p = Runtime.getRuntime().exec("which xterm");
				if (p.waitFor() == 0){
					p = Runtime.getRuntime().exec("which lynx");
					if (p.waitFor() == 0){
						browsers.add("xterm -e lynx {0}");
					}
				}
			} catch (IOException e){
			} catch (InterruptedException e){
			}
			if (browsers.size() == 0){
				exec = null;
			} else {
				exec = (String[])browsers.toArray(new String[0]);
			}
		}
		return exec;
	}

	/**
	 * Save the options used to the given properties file.
	 * Property names used will all start with com.Ostermiller.util.Browser
	 * Properties are saved in such a way that a call to load(props); will
	 * restore the state of this class.
	 * If the default commands to open a browser are being used then
	 * they are not saved in the properties file, assuming that the user
	 * will want to use the defaults next time even if the defaults change.
	 *
	 * @param props properties file to which configuration is saved.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void save(Properties props){
		boolean saveBrowser = false;
		if (Browser.exec != null && Browser.exec.length > 0){
			String[] exec = Browser.defaultCommands();
			if (exec != null && exec.length == Browser.exec.length){
				for (int i=0; i<exec.length; i++){
					if (!exec[i].equals(Browser.exec[i])){
						saveBrowser = true;
					}
				}
			} else {
				saveBrowser = true;
			}
		}
		if (saveBrowser){
			StringBuffer sb = new StringBuffer();
			for (int i=0; Browser.exec != null && i < Browser.exec.length; i++){
				sb.append(Browser.exec[i]).append('\n');
			}
			props.put("com.Ostermiller.util.Browser.open", sb.toString());
		} else {
			props.remove("com.Ostermiller.util.Browser.open");
		}
	}

	/**
	 * Load the options for this class from the given properties file.
	 * This method is designed to work with the save(props) method.  All
	 * properties used will start with com.Ostermiller.util.Browser.  If
	 * no configuration is found, the default configuration will be used.
	 * If this method is used, a call to Browser.init(); is not needed.
	 *
	 * @param props properties file from which configuration is loaded.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void load(Properties props){
		if (props.containsKey("com.Ostermiller.util.Browser.open")){
			java.util.StringTokenizer tok = new java.util.StringTokenizer(props.getProperty("com.Ostermiller.util.Browser.open"), "\r\n", false);
			int count = tok.countTokens();
			String[] exec = new String[count];
			for (int i=0; i < count; i++){
				exec[i] = tok.nextToken();
			}
			Browser.exec = exec;
		} else {
			Browser.init();
		}
	}

	/**
	 * Display a URL in the system browser.
	 *
	 * Browser.init() should be called before calling this function or
	 * Browser.exec should be set explicitly.
	 *
	 * For security reasons, the URL will may not be passed directly to the
	 * browser as it is passed to this method.  The URL may be made safe for
	 * the exec command by URLEncoding the URL before passing it.
	 *
	 * @param url the url to display
	 * @throws IOException if the url is not valid or the browser fails to star
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void displayURL(String url) throws IOException {
		if (exec == null || exec.length == 0){
			if (System.getProperty("os.name").startsWith("Mac")){
				boolean success = false;
			try {
				Class nSWorkspace;
					if (new File("/System/Library/Java/com/apple/cocoa/application/NSWorkspace.class").exists()){
						 // Mac OS X has NSWorkspace, but it is not in the classpath, add it.
						 ClassLoader classLoader = new URLClassLoader(new URL[]{new File("/System/Library/Java").toURL()});
						 nSWorkspace = Class.forName("com.apple.cocoa.application.NSWorkspace", true, classLoader);
					} else {
						 nSWorkspace = Class.forName("com.apple.cocoa.application.NSWorkspace");
					}
					Method sharedWorkspace = nSWorkspace.getMethod("sharedWorkspace", new Class[] {});
					Object workspace = sharedWorkspace.invoke(null, new Object[] {});
					Method openURL = nSWorkspace.getMethod("openURL", new Class[] {Class.forName("java.net.URL")});
					success = ((Boolean)openURL.invoke(workspace, new Object[] {new java.net.URL(url)})).booleanValue();
				//success = com.apple.cocoa.application.NSWorkspace.sharedWorkspace().openURL(new java.net.URL(url));
			} catch (Exception x) {}
				if (!success){
					try {
						 Class mrjFileUtils = Class.forName("com.apple.mrj.MRJFileUtils");
						 Method openURL = mrjFileUtils.getMethod("openURL", new Class[] {Class.forName("java.lang.String")});
						 openURL.invoke(null, new Object[] {url});
						 //com.apple.mrj.MRJFileUtils.openURL(url);
					} catch (Exception x){
						 System.err.println(x.getMessage());
						 throw new IOException(labels.getString("failed"));
					}
				}
			} else {
				throw new IOException(labels.getString("nocommand"));
			}
		} else {
			// for security, see if the url is valid.
			// this is primarily to catch an attack in which the url
			// starts with a - to fool the command line flags, bu
			// it could catch other stuff as well, and will throw a
			// MalformedURLException which will give the caller of this
			// function useful information.
			new URL(url);
			// escape any weird characters in the url.  This is primarily
			// to prevent an attacker from putting in spaces
			// that might fool exec into allowing
			// the attacker to execute arbitrary code.
			StringBuffer sb = new StringBuffer(url.length());
			for (int i=0; i<url.length(); i++){
				char c = url.charAt(i);
				if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
						|| c == '.' || c == ':' || c == '&' || c == '@' || c == '/' || c == '?'
						|| c == '%' || c =='+' || c == '=' || c == '#' || c == '-' || c == '\\'){
					//characters that are necessary for URLs and should be safe
					//to pass to exec.  Exec uses a default string tokenizer with
					//the default arguments (whitespace) to separate command line
					//arguments, so there should be no problem with anything bu
					//whitespace.
					sb.append(c);
				} else {
					c = (char)(c & 0xFF); // get the lowest 8 bits (URLEncoding)
					if (c < 0x10){
						sb.append("%0" + Integer.toHexString(c));
					} else {
						sb.append("%" + Integer.toHexString(c));
					}
				}
			}
			String[] messageArray = new String[1];
			messageArray[0] = sb.toString();
			String command = null;
			boolean found = false;
			// try each of the exec commands until something works
			try {
				for (int i=0; i<exec.length && !found; i++){
					try {
						// stick the url into the command
						command = MessageFormat.format(exec[i], messageArray);
						// parse the command line.
						Vector argsVector = new Vector();
						BrowserCommandLexer lex = new BrowserCommandLexer(new StringReader(command));
						String t;
						while ((t = lex.getNextToken()) != null) {
							argsVector.add(t);
						}
						String[] args = new String[argsVector.size()];
						args = (String[])argsVector.toArray(args);
						// the windows url protocol handler doesn't work well with file URLs.
						// Correct those problems here before continuing
						// Java File.toURL() gives only one / following file: bu
						// we need two.
						// If there are escaped characters in the url, we will have
						// to create an Internet shortcut and open that, as the command
						// line version of the rundll doesn't like them.
						boolean useShortCut = false;
						if (args[0].equals("rundll32") && args[1].equals("url.dll,FileProtocolHandler")){
							if (args[2].startsWith("file:/")){
								if (args[2].charAt(6) != '/'){
									args[2] = "file://" + args[2].substring(6);
								}
								if (args[2].charAt(7) != '/'){
									args[2] = "file:///" + args[2].substring(7);
								}
								useShortCut = true;
							} else if (args[2].toLowerCase().endsWith("html") || args[2].toLowerCase().endsWith("htm")){
								useShortCut = true;
							}
						}
						if (useShortCut){
							File shortcut = File.createTempFile("OpenInBrowser", ".url");
							shortcut = shortcut.getCanonicalFile();
							shortcut.deleteOnExit();
							PrintWriter out = new PrintWriter(new FileWriter(shortcut));
							out.println("[InternetShortcut]");
							out.println("URL=" + args[2]);
							out.close();
							args[2] = shortcut.getCanonicalPath();
						}
						// start the browser
						Process p = Runtime.getRuntime().exec(args);

						// give the browser a bit of time to fail.
						// I have found that sometimes sleep doesn't work
						// the first time, so do it twice.  My tests
						// seem to show that 1000 milliseconds is enough
						// time for the browsers I'm using.
						for (int j=0; j<2; j++){
							 try{
									Thread.currentThread().sleep(1000);
							 } catch (InterruptedException inte){
							 }
						}
						if (p.exitValue() == 0){
							 // this is a weird case.  The browser exited after
							 // a couple seconds saying that it successfully
							 // displayed the url.  Either the browser is lying
							 // or the user closed it *really* quickly.  Oh well.
							 found = true;
						}
					} catch (IOException x){
						// the command was not a valid command.
						System.err.println(labels.getString("warning") + " " + x.getMessage());
					}
				}
				if (!found){
					// we never found a command that didn't terminate with an error.
					throw new IOException(labels.getString("failed"));
				}
			} catch (IllegalThreadStateException e){
				// the browser is still running.  This is a good sign.
				// lets just say that it is displaying the url right now!
			}
		}
	}

	/**
	 * Display the URLs, each in their own window, in the system browser.
	 *
	 * Browser.init() should be called before calling this function or
	 * Browser.exec should be set explicitly.
	 *
	 * If more than one URL is given an HTML page containing JavaScript will
	 * be written to the local drive, that page will be opened, and it will
	 * open the rest of the URLs.
	 *
	 * @param urls the list of urls to display
	 * @throws IOException if the url is not valid or the browser fails to star
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void displayURLs(String[] urls) throws IOException {
		if (urls == null || urls.length == 0){
			return;
		}
		if (urls.length == 1){
			displayURL(urls[0]);
			return;
		}
		File shortcut = File.createTempFile("DisplayURLs", ".html");
		shortcut = shortcut.getCanonicalFile();
		shortcut.deleteOnExit();
		PrintWriter out = new PrintWriter(new FileWriter(shortcut));
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" + labels.getString("html.openurls") + "</title>");
		out.println("<script language=\"javascript\" type=\"text/javascript\">");
		out.println("function displayURLs(){");
		for (int i=1; i<urls.length; i++){
			out.println("window.open(\"" + urls[i] + "\", \"_blank\", \"toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes\");");
		}
		out.println("location.href=\"" + urls[0] + "\";");
		out.println("}");
		out.println("</script>");
		out.println("</head>");
		out.println("<body onload=\"javascript:displayURLs()\">");
		out.println("<noscript>");
		for (int i=0; i<urls.length; i++){
			out.println("<a target=\"_blank\" href=\"" + urls[i] + "\">" + urls[i] + "</a><br>");
		}
		out.println("</noscript>");
		out.println("</body>");
		out.println("</html>");
		out.close();
		displayURL(shortcut.toURL().toString());
	}

	/**
	 * Display the URL in a new window.
	 *
	 * Uses javascript to check history.length to determine if the browser opened a
	 * new window already.  If it did, the url is shown in that window, if not, it is
	 * shown in new window.
	 *
	 * Some browsers do not allow the length of history to be viewed by a web page.  In that
	 * case, the url will be displayed in the current window.
	 *
	 * Browser.init() should be called before calling this function or
	 * Browser.exec should be set explicitly.
	 *
	 * @param url the url to display in a new window.
	 * @throws IOException if the url is not valid or the browser fails to star
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void displayURLinNew(String url) throws IOException {
		displayURLsinNew (new String[] {url});
	}

	/**
	 * Display the URLs, each in their own window, in the system browser and the first in
	 * the named window.
	 *
	 * The first URL will only be opened in the named window if the browser did no
	 * open it in a new window to begin with.
	 *
	 * Browser.init() should be called before calling this function or
	 * Browser.exec should be set explicitly.
	 *
	 * An html page containing javascript will
	 * be written to the local drive, that page will be opened, and it will
	 * open all the urls.
	 *
	 * @param urls the list of urls to display
	 * @throws IOException if the url is not valid or the browser fails to star
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void displayURLsinNew(String[] urls) throws IOException {
		if (urls == null || urls.length == 0){
			return;
		}
		File shortcut = File.createTempFile("DisplayURLs", ".html");
		shortcut.deleteOnExit();
		shortcut = shortcut.getCanonicalFile();
		PrintWriter out = new PrintWriter(new FileWriter(shortcut));
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" + labels.getString("html.openurls") + "</title>");
		out.println("<script language=\"javascript\" type=\"text/javascript\">");
		out.println("function displayURLs(){");
		out.println("var hlength = 0;");
		out.println("try {");
		out.println("hlength = history.length;");
		out.println("} catch (e) {}");
		out.println("if (hlength>0) {");
		out.println("window.open(\"" + urls[0] + "\", \"_blank\", \"toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes\");");
		out.println("}");
		for (int i=1; i<urls.length; i++){
			out.println("window.open(\"" + urls[i] + "\", \"_blank\", \"toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes\");");
		}
		out.println("if (hlength==0) {");
		out.println("location.href=\"" + urls[0] + "\";");
		out.println("} else {");
		out.println("history.back()");
		out.println("}");
		out.println("}");
		out.println("</script>");
		out.println("</head>");
		out.println("<body onload=\"javascript:displayURLs()\">");
		out.println("<noscript>");
		for (int i=0; i<urls.length; i++){
			out.println("<a target=\"_blank\" href=\"" + urls[i] + "\">" + urls[i] + "</a><br>");
		}
		out.println("</noscript>");
		out.println("</body>");
		out.println("</html>");
		out.close();
		displayURL(shortcut.toURL().toString());
	}

	/**
	 * Display the URL in the named window.
	 *
	 * If the browser opens a new window by default, this will likely cause a duplicate window
	 * to be opened.
	 *
	 * Browser.init() should be called before calling this function or
	 * Browser.exec should be set explicitly.
	 *
	 * @param url the url to display
	 * @param namedWindow the name of the desired window.
	 * @throws IOException if the url is not valid or the browser fails to star
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void displayURL(String url, String namedWindow) throws IOException {
		displayURLs (new String[] {url}, new String[] {namedWindow});
	}

	/**
	 * Display the URLs in the named windows.
	 *
	 * If the browser opens a new window by default, this will likely cause a duplicate window
	 * to be opened.  This method relies on the browser to support javascript.
	 *
	 * Browser.init() should be called before calling this function or
	 * Browser.exec should be set explicitly.
	 *
	 * Extra names for windows will be ignored, and if there are too few names, the remaining
	 * windows will be named "_blank".
	 *
	 * @param urls the list of urls to display
	 * @param namedWindows the list of names for the windows.
	 * @throws IOException if the url is not valid or the browser fails to star
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void displayURLs(String[] urls, String[] namedWindows) throws IOException {
		if (urls == null || urls.length == 0){
			return;
		}
		File shortcut = File.createTempFile("DisplayURLs", ".html");
		shortcut.deleteOnExit();
		shortcut = shortcut.getCanonicalFile();
		PrintWriter out = new PrintWriter(new FileWriter(shortcut));
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" + labels.getString("html.openurls") + "</title>");
		out.println("<base target=\"" + ((namedWindows==null||namedWindows.length==0||namedWindows[0]==null)?"_blank":namedWindows[0]) + "\">");
		out.println("<script language=\"javascript\" type=\"text/javascript\">");
		for (int i=1; i<urls.length; i++){
			out.println("window.open(\"" + urls[i] + "\", \"" + ((namedWindows==null||namedWindows.length<=i||namedWindows[i]==null)?"_blank":namedWindows[i]) + "\", \"toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes\");");
		}
		out.println("location.href=\"" + urls[0] + "\";");
		out.println("</script>");
		out.println("</head>");
		out.println("<body onload=\"javascript:displayURLs()\">");
		out.println("<noscript>");
		for (int i=0; i<urls.length; i++){
			out.println("<a target=\"" + ((namedWindows==null||namedWindows.length==0||namedWindows[0]==null)?"_blank":namedWindows[0]) + "\" href=\"" + urls[i] + "\">" + urls[i] + "</a><br>");
		}
		out.println("</noscript>");
		out.println("</body>");
		out.println("</html>");
		out.close();
		displayURL(shortcut.toURL().toString());
	}

	/**
	 * Display the URLs the first in the given named window.
	 *
	 * If the browser opens a new window by default, this will likely cause a duplicate window
	 * to be opened.  This method relies on the browser to support javascript.
	 *
	 * Browser.init() should be called before calling this function or
	 * Browser.exec should be set explicitly.
	 *
	 * @param urls the list of urls to display
	 * @param namedWindow the name of the first window to use.
	 * @throws IOException if the url is not valid or the browser fails to star
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void displayURLs(String[] urls, String namedWindow) throws IOException {
		displayURLs(urls, new String[] {namedWindow});
	}

	/**
	 * Open the url(s) specified on the command line in your browser.
	 *
	 * @param args Command line arguments (URLs)
	 */
	public static void main(String[] args){
		try {
			Browser.init();
			if (Browser.dialogConfiguration(null)){
				if (args.length == 0){
					Browser.displayURLs(new String[]{
						"http://www.google.com/",
						"http://dmoz.org/",
						"http://ostermiller.org",
					}, "fun");
				} else if (args.length == 1){
					Browser.displayURL(args[0], "fun");
				} else {
					Browser.displayURLs(args, "fun");
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException x){
			}
		} catch (IOException e){
			System.err.println(e.getMessage());
		}
		System.exit(0);
	}

	/**
	 * Show a dialog that allows the user to configure the
	 * command lines used for starting a browser on their system.
	 *
	 * @param owner The frame that owns the dialog.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static boolean dialogConfiguration(Frame owner){
		dialogConfiguration(owner, null);
		return Browser.dialog.changed();
	}

	/**
	 * Show a dialog that allows the user to configure the
	 * command lines used for starting a browser on their system.
	 * String used in the dialog are taken from the given
	 * properties.  This dialog can be customized or displayed in
	 * multiple languages.
	 * <P>
	 * Properties that are used:
	 * com.Ostermiller.util.BrowserDialog.title<br>
	 * com.Ostermiller.util.BrowserDialog.description<br>
	 * com.Ostermiller.util.BrowserDialog.label<br>
	 * com.Ostermiller.util.BrowserDialog.defaults<br>
	 * com.Ostermiller.util.BrowserDialog.browse<br>
	 * com.Ostermiller.util.BrowserDialog.ok<br>
	 * com.Ostermiller.util.BrowserDialog.cancel<br>
	 *
	 * @param owner The frame that owns this dialog.
	 * @param props contains the strings used in the dialog.
	 * @deprecated  Use the com.Ostermiller.util.Browser resource bundle to set strings for the given locale.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static boolean dialogConfiguration(Frame owner, Properties props){
		if (Browser.dialog == null){
			Browser.dialog = new BrowserDialog(owner);
		}
		if (props != null){
			Browser.dialog.setProps(props);
		}
		Browser.dialog.show();
		return Browser.dialog.changed();
	}

	/**
	 * Where the command lines are typed.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static JTextArea description;

	/**
	 * Where the command lines are typed.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static JTextArea commandLinesArea;

	/**
	 * The reset button.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static JButton resetButton;

	/**
	 * The browse button.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static JButton browseButton;

	/**
	 * The label for the field in which the name is typed.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static JLabel commandLinesLabel;

	/**
	 * File dialog for choosing a browser
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static JFileChooser fileChooser;

	/**
	 * A panel used in the options dialog.  Null until getDialogPanel() is called.
	 */
	private static JPanel dialogPanel = null;
	private static Window dialogParent = null;

	/**
	 * If you wish to add to your own dialog box rather than have a separate
	 * one just for the browser, use this method to get a JPanel that can
	 * be added to your own dialog.
	 *
	 * mydialog.add(Browser.getDialogPanel(mydialog));
	 * Browser.initPanel();
	 * mydialog.show();
	 * if (ok_pressed){
	 * &nbsp;&nbsp;Browser.userOKedPanelChanges();
	 * }
	 *
	 * @param parent window into which panel with eventually be placed.
	 * @since ostermillerutils 1.02.22
	 */
	public static JPanel getDialogPanel(Window parent){
		dialogParent = parent;
		if (dialogPanel == null){
			commandLinesArea = new JTextArea("", 8, 40);
			JScrollPane scrollpane = new JScrollPane(commandLinesArea);
			resetButton = new JButton(labels.getString("dialog.reset"));
			browseButton = new JButton(labels.getString("dialog.browse"));
			commandLinesLabel = new JLabel(labels.getString("dialog.commandLines"));
			description = new JTextArea(labels.getString("dialog.description"));
			description.setEditable(false);
			description.setOpaque( false );

			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e){
					Object source = e.getSource();
					if (source == resetButton){
						setCommands(Browser.defaultCommands());
					} else if (source == browseButton){
						if (fileChooser == null){
							fileChooser = new JFileChooser();
						}
						if (fileChooser.showOpenDialog(dialogParent) == JFileChooser.APPROVE_OPTION){
							String app = fileChooser.getSelectedFile().getPath();
							StringBuffer sb = new StringBuffer(2 * app.length());
							for (int i=0; i<app.length(); i++){
								char c = app.charAt(i);
								// escape these two characters so that we can later parse the stuff
								if (c == '\"' || c == '\\') {
									sb.append('\\');
								}
								sb.append(c);
							}
							app = sb.toString();
							if (app.indexOf(" ") != -1){
								app = '"' + app + '"';
							}
							String commands = commandLinesArea.getText();
							if (commands.length() != 0 && !commands.endsWith("\n") && !commands.endsWith("\r")){
								commands += "\n";
							}
							commandLinesArea.setText(commands + app + " {0}");
						}
					}
				}
			};

			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			c.insets.top = 5;
			c.insets.bottom = 5;
			dialogPanel = new JPanel(gridbag);
			dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
			JLabel label;


			c.gridwidth = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.WEST;
			gridbag.setConstraints(description, c);
			dialogPanel.add(description);

			c.gridy = 1;
			c.gridwidth = GridBagConstraints.RELATIVE;
			gridbag.setConstraints(commandLinesLabel, c);
			dialogPanel.add(commandLinesLabel);
			JPanel buttonPanel = new JPanel();
			c.anchor = GridBagConstraints.EAST;
			browseButton.addActionListener(actionListener);
			buttonPanel.add(browseButton);
			resetButton.addActionListener(actionListener);
			buttonPanel.add(resetButton);
			gridbag.setConstraints(buttonPanel, c);
			dialogPanel.add(buttonPanel);

			c.gridy = 2;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.WEST;
			gridbag.setConstraints(scrollpane, c);
			dialogPanel.add(scrollpane);
		}
		return dialogPanel;
	}


	/**
	 * A modal dialog that presents configuration option for this class.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static class BrowserDialog extends JDialog {

		/**
		 * The OK button.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		private JButton okButton;

		/**
		 * The cancel button.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		private JButton cancelButton;

		/**
		 * The label for the field in which the name is typed.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		private JLabel commandLinesLabel;

		/**
		 * update this variable when the user makes an action
		 *
		 * @since ostermillerutils 1.00.00
		 */
		private boolean pressed_OK = false;


		/**
		 * Properties that are used:
		 * com.Ostermiller.util.BrowserDialog.title<br>
		 * com.Ostermiller.util.BrowserDialog.description<br>
		 * com.Ostermiller.util.BrowserDialog.label<br>
		 * com.Ostermiller.util.BrowserDialog.defaults<br>
		 * com.Ostermiller.util.BrowserDialog.browse<br>
		 * com.Ostermiller.util.BrowserDialog.ok<br>
		 * com.Ostermiller.util.BrowserDialog.cancel<br>
		 *
		 * @deprecated  Use the com.Ostermiller.util.Browser resource bundle to set strings for the given locale.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		private void setProps(Properties props){
			if (props.containsKey("com.Ostermiller.util.BrowserDialog.title")){
				setTitle(props.getProperty("com.Ostermiller.util.BrowserDialog.title"));
			}
			if (props.containsKey("com.Ostermiller.util.BrowserDialog.description")){
				description.setText(props.getProperty("com.Ostermiller.util.BrowserDialog.description"));
			}
			if (props.containsKey("com.Ostermiller.util.BrowserDialog.label")){
				commandLinesLabel.setText(props.getProperty("com.Ostermiller.util.BrowserDialog.label"));
			}
			if (props.containsKey("com.Ostermiller.util.BrowserDialog.defaults")){
				resetButton.setText(props.getProperty("com.Ostermiller.util.BrowserDialog.defaults"));
			}
			if (props.containsKey("com.Ostermiller.util.BrowserDialog.browse")){
				browseButton.setText(props.getProperty("com.Ostermiller.util.BrowserDialog.browse"));
			}
			if (props.containsKey("com.Ostermiller.util.BrowserDialog.ok")){
				okButton.setText(props.getProperty("com.Ostermiller.util.BrowserDialog.ok"));
			}
			if (props.containsKey("com.Ostermiller.util.BrowserDialog.cancel")){
				cancelButton.setText(props.getProperty("com.Ostermiller.util.BrowserDialog.cancel"));
			}
			pack();
		}

		/**
		 * Whether the user pressed the applied changes.
		 * true if OK was pressed or the user otherwise applied new changes,
		 * false if cancel was pressed or dialog was closed with no changes.
		 * If called before the dialog is displayed and closed, the results
		 * are not defined.
		 *
		 * @returns if the user made changes to the browser configuration.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public boolean changed() {
			return pressed_OK;
		}

		/**
		 * Create this dialog with the given parent and title.
		 *
		 * @param parent window from which this dialog is launched
		 * @param title the title for the dialog box window
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public BrowserDialog(Frame parent) {
			super(parent, labels.getString("dialog.title"), true);
			setLocationRelativeTo(parent);
			// super calls dialogInit, so we don't need to do it again.
		}

		/**
		 * Called by constructors to initialize the dialog.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		protected void dialogInit(){

			super.dialogInit();

			getContentPane().setLayout(new BorderLayout());

			getContentPane().add(getDialogPanel(this), BorderLayout.CENTER);

			JPanel panel = new JPanel(new FlowLayout());
			okButton = new JButton(labels.getString("dialog.ok"));
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					pressed_OK = true;
					BrowserDialog.this.hide();
				}
			});
			panel.add(okButton);
			cancelButton = new JButton(labels.getString("dialog.cancel"));
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					pressed_OK = false;
					BrowserDialog.this.hide();
				}
			});
			panel.add(cancelButton);

			getContentPane().add(panel, BorderLayout.SOUTH);

			pack();
		}

		/**
		 * Shows the dialog.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public void show(){
			initPanel();
			super.show();
			if (pressed_OK){
				userOKedPanelChanges();
			}
		}
	}

	private static void setCommands(String[] newExec){
		StringBuffer sb = new StringBuffer();
		for (int i=0; newExec != null && i < newExec.length; i++){
			sb.append(newExec[i]).append('\n');
		}
		commandLinesArea.setText(sb.toString());
	}

	/**
	 * If you are using the getDialogPanel() method to create your own dialog, this
	 * method should be called every time before you display the dialog.
	 *
	 * mydialog.add(Browser.getDialogPanel(mydialog));
	 * Browser.initPanel();
	 * mydialog.show();
	 * if (ok_pressed){
	 * &nbsp;&nbsp;Browser.userOKedPanelChanges();
	 * }
	 *
	 * @since ostermillerutils 1.02.22
	 */
	public static void initPanel(){
		setCommands(exec);
	}

	/**
	 * If you are using the getDialogPanel() method to create your own dialog, this
	 * method should be called after you display the dialog if the user pressed ok.
	 *
	 * mydialog.add(Browser.getDialogPanel(mydialog));
	 * Browser.initPanel();
	 * mydialog.show();
	 * if (ok_pressed){
	 * &nbsp;&nbsp;Browser.userOKedPanelChanges();
	 * }
	 *
	 * @since ostermillerutils 1.02.22
	 */
	public static void userOKedPanelChanges(){
		java.util.StringTokenizer tok = new java.util.StringTokenizer(commandLinesArea.getText(), "\r\n", false);
		int count = tok.countTokens();
		String[] exec = new String[count];
		for (int i=0; i < count; i++){
			exec[i] = tok.nextToken();
		}
		Browser.exec = exec;
	}
}
