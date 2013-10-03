/*
 * Adjusts line endings.
 * Copyright (C) 2001 Stephen Ostermiller
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
import gnu.getopt.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * Stream editor to alter the line separators on text to match
 * that of a given platform.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/LineEnds.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class LineEnds {

	/**
	 * Version number of this program
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static final String version = "1.2";

	/**
	 * Locale specific strings displayed to the user.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected static ResourceBundle labels = ResourceBundle.getBundle("com.Ostermiller.util.LineEnds",  Locale.getDefault());

	/**
	 * Converts the line ending on files, or standard input.
	 * Run with --help argument for more information.
	 *
	 * @param args Command line arguments.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void main(String[] args){
		// create the command line options that we are looking for
		LongOpt[] longopts = {
			new LongOpt(labels.getString("help.option"), LongOpt.NO_ARGUMENT, null, 1),
			new LongOpt(labels.getString("version.option"), LongOpt.NO_ARGUMENT, null, 2),
			new LongOpt(labels.getString("about.option"), LongOpt.NO_ARGUMENT, null, 3),
			new LongOpt(labels.getString("windows.option"), LongOpt.NO_ARGUMENT, null, 'd'),
			new LongOpt(labels.getString("dos.option"), LongOpt.NO_ARGUMENT, null, 'd'),
			new LongOpt(labels.getString("unix.option"), LongOpt.NO_ARGUMENT, null, 'n'),
			new LongOpt(labels.getString("java.option"), LongOpt.NO_ARGUMENT, null, 'n'),
			new LongOpt(labels.getString("mac.option"), LongOpt.NO_ARGUMENT, null, 'r'),
			new LongOpt(labels.getString("system.option"), LongOpt.NO_ARGUMENT, null, 's'),
			new LongOpt(labels.getString("force.option"), LongOpt.NO_ARGUMENT, null, 'f'),
			new LongOpt(labels.getString("quiet.option"), LongOpt.NO_ARGUMENT, null, 'q'),
			new LongOpt(labels.getString("reallyquiet.option"), LongOpt.NO_ARGUMENT, null, 'Q'),
			new LongOpt(labels.getString("verbose.option"), LongOpt.NO_ARGUMENT, null, 'v'),
			new LongOpt(labels.getString("reallyverbose.option"), LongOpt.NO_ARGUMENT, null, 'V'),
			new LongOpt(labels.getString("noforce.option"), LongOpt.NO_ARGUMENT, null, 4),
		};
		String oneLetterOptions = "dnrsfVvqQ";
		Getopt opts = new Getopt(labels.getString("lineends"), args, oneLetterOptions, longopts);
		int style = STYLE_SYSTEM;
		boolean force = false;
		boolean printMessages = false;
		boolean printExtraMessages = false;
		boolean printErrors = true;
		int c;
		while ((c = opts.getopt()) != -1){
			switch(c){
					case 1:{
					// print out the help message
					String[] helpFlags = new String[]{
						"--" + labels.getString("help.option"),
						"--" + labels.getString("version.option"),
						"--" + labels.getString("about.option"),
						"-d --" + labels.getString("windows.option") + " --" + labels.getString("dos.option"),
						"-n --" + labels.getString("unix.option") + " --" + labels.getString("java.option"),
						"-r --" + labels.getString("mac.option"),
						"-s --" + labels.getString("system.option"),
						"-f --" + labels.getString("force.option"),
						"--" + labels.getString("noforce.option"),
						"-V --" + labels.getString("reallyverbose.option"),
						"-v --" + labels.getString("verbose.option"),
						"-q --" + labels.getString("quiet.option"),
						"-Q --" + labels.getString("reallyquiet.option"),
					};
					int maxLength = 0;
					for (int i=0; i<helpFlags.length; i++){
						maxLength = Math.max(maxLength, helpFlags[i].length());
					}
					maxLength += 2;
					System.out.println(
						labels.getString("lineends") + " [-" + oneLetterOptions + "] <" + labels.getString("files") + ">\n" +
						labels.getString("purpose.message") + "\n" +
						"  " + labels.getString("stdin.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[0] ,maxLength, ' ') + labels.getString("help.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[1] ,maxLength, ' ') + labels.getString("version.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[2] ,maxLength, ' ') + labels.getString("about.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[3] ,maxLength, ' ') + labels.getString("d.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[4] ,maxLength, ' ') + labels.getString("n.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[5] ,maxLength, ' ') + labels.getString("r.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[6] ,maxLength, ' ') + labels.getString("s.message") + " (" + labels.getString("default") + ")\n" +
						"  " + StringHelper.postpad(helpFlags[7] ,maxLength, ' ') + labels.getString("f.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[8] ,maxLength, ' ') + labels.getString("noforce.message") + " (" + labels.getString("default") + ")\n" +
						"  " + StringHelper.postpad(helpFlags[9] ,maxLength, ' ') + labels.getString("V.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[10] ,maxLength, ' ') + labels.getString("v.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[11] ,maxLength, ' ') + labels.getString("q.message") + " (" + labels.getString("default") + ")\n" +
						"  " + StringHelper.postpad(helpFlags[12] ,maxLength, ' ') + labels.getString("Q.message") + "\n"
					);
					System.exit(0);
				} break;
				case 2:{
					// print out the version message
					System.out.println(MessageFormat.format(labels.getString("version"), new String[] {version}));
					System.exit(0);
				} break;
				case 3:{
					System.out.println(
						labels.getString("lineends") + " -- " + labels.getString("purpose.message") + "\n" +
						MessageFormat.format(labels.getString("copyright"), new String[] {"2001", "Stephen Ostermiller (http://ostermiller.org/contact.pl?regarding=Java+Utilities)"}) + "\n\n" +
						labels.getString("license")
					);
					System.exit(0);
				} break;
				case 'd':{
					style = STYLE_RN;
				} break;
				case 'n':{
					style = STYLE_N;
				} break;
				case 'r':{
					style = STYLE_R;
				} break;
				case 's':{
					style = STYLE_SYSTEM;
				} break;
				case 'f':{
					force = true;
				} break;
				case 4:{
					force = false;
				} break;
				case 'V':{
					printExtraMessages = true;
					printMessages = true;
					printErrors = true;
				} break;
				case 'v':{
					printExtraMessages = false;
					printMessages = true;
					printErrors = true;
				} break;
				case 'q':{
					printExtraMessages = false;
					printMessages = false;
					printErrors = true;
				} break;
				case 'Q':{
					printExtraMessages = false;
					printMessages = false;
					printErrors = false;
				} break;
				default:{
					System.exit(1);
				}
			}
		}

		int exitCond = 0;
		boolean done = false;
		for (int i=opts.getOptind(); i<args.length; i++){
			boolean modified = false;
			done = true;
			File source = new File(args[i]);
			if (!source.exists()){
				if(printErrors){
					System.err.println(MessageFormat.format(labels.getString("doesnotexist"), new String[] {args[i]}));
				}
				exitCond = 1;
			} else if (!source.canRead()){
				if(printErrors){
					System.err.println(MessageFormat.format(labels.getString("cantread"), new String[] {args[i]}));
				}
				exitCond = 1;
			} else if (!source.canWrite()){
				if(printErrors){
					System.err.println(MessageFormat.format(labels.getString("cantwrite"), new String[] {args[i]}));
				}
				exitCond = 1;
			} else {
				try {
					if(convert (source, style, !force)){
						if (printMessages){
							System.out.println(MessageFormat.format(labels.getString("modified"), new String[] {args[i]}));
						}
					} else {
						if (printExtraMessages){
							System.out.println(MessageFormat.format(labels.getString("alreadycorrect"), new String[] {args[i]}));
						}
					}
				} catch (IOException x){
					if(printErrors){
						System.err.println(args[i] + ": " + x.getMessage());
					}
					exitCond = 1;
				}
			}
		}
		if (!done){
			try {
				convert (System.in, System.out, style, !force);
			} catch (IOException x){
				System.err.println(x.getMessage());
				exitCond = 1;
			}
		}
		System.exit(exitCond);
	}

	/**
	 * The system line ending as determined
	 * by System.getProperty("line.separator")
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public final static int STYLE_SYSTEM = 0;
	/**
	 * The Windows and DOS line ending ("\r\n")
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public final static int STYLE_WINDOWS = 1;
	/**
	 * The Windows and DOS line ending ("\r\n")
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public final static int STYLE_DOS = 1;
	/**
	 * The Windows and DOS line ending ("\r\n")
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public final static int STYLE_RN = 1;
	/**
	 * The UNIX and Java line ending ("\n")
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public final static int STYLE_UNIX = 2;
	/**
	 * The UNIX and Java line ending ("\n")
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public final static int STYLE_N = 2;
	/**
	 * The UNIX and Java line ending ("\n")
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public final static int STYLE_JAVA = 2;
	/**
	 * The Macintosh line ending ("\r")
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public final static int STYLE_MAC = 3;
	/**
	 * The Macintosh line ending ("\r")
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public final static int STYLE_R = 3;

	/**
	 * Buffer size when reading from input stream.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private final static int BUFFER_SIZE = 1024;
	private final static int STATE_INIT = 0;
	private final static int STATE_R = 1;

	private final static int MASK_N = 0x01;
	private final static int MASK_R = 0x02;
	private final static int MASK_RN = 0x04;

	/**
	 * Change the line endings of the text on the input stream and write
	 * it to the output stream.
	 *
	 * The current system's line separator is used.
	 *
	 * @param in stream that contains the text which needs line number conversion.
	 * @param out stream where converted text is written.
	 * @return true if the output was modified from the input, false if it is exactly the same
	 * @throws BinaryDataException if non-text data is encountered.
	 * @throws IOException if an input or output error occurs.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static boolean convert(InputStream in, OutputStream out) throws IOException {
		return convert(in, out, STYLE_SYSTEM, true);
	}

	/**
	 * Change the line endings of the text on the input stream and write
	 * it to the output stream.
	 *
	 * @param in stream that contains the text which needs line number conversion.
	 * @param out stream where converted text is written.
	 * @param style line separator style.
	 * @return true if the output was modified from the input, false if it is exactly the same
	 * @throws BinaryDataException if non-text data is encountered.
	 * @throws IOException if an input or output error occurs.
	 * @throws IllegalArgumentException if an unknown style is requested.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static boolean convert(InputStream in, OutputStream out, int style) throws IOException {
		return convert(in, out, style, true);
	}

	/**
	 * Change the line endings of the text on the input stream and write
	 * it to the output stream.
	 *
	 * The current system's line separator is used.
	 *
	 * @param in stream that contains the text which needs line number conversion.
	 * @param out stream where converted text is written.
	 * @param binaryException throw an exception and abort the operation if binary data is encountered and binaryExcepion is false.
	 * @return true if the output was modified from the input, false if it is exactly the same
	 * @throws BinaryDataException if non-text data is encountered.
	 * @throws IOException if an input or output error occurs.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static boolean convert(InputStream in, OutputStream out, boolean binaryException) throws IOException {
		return convert(in, out, STYLE_SYSTEM, binaryException);
	}

	/**
	 * Change the line endings of the text on the input stream and write
	 * it to the output stream.
	 *
	 * @param in stream that contains the text which needs line number conversion.
	 * @param out stream where converted text is written.
	 * @param style line separator style.
	 * @param binaryException throw an exception and abort the operation if binary data is encountered and binaryExcepion is false.
	 * @return true if the output was modified from the input, false if it is exactly the same
	 * @throws BinaryDataException if non-text data is encountered.
	 * @throws IOException if an input or output error occurs.
	 * @throws IllegalArgumentException if an unknown style is requested.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static boolean convert(InputStream in, OutputStream out, int style, boolean binaryException) throws IOException {
		byte[] lineEnding;
		switch (style) {
			case STYLE_SYSTEM: {
				 lineEnding = System.getProperty("line.separator").getBytes();
			} break;
			case STYLE_RN: {
				 lineEnding = new byte[]{(byte)'\r',(byte)'\n'};
			} break;
			case STYLE_R: {
				 lineEnding = new byte[]{(byte)'\r'};
			} break;
			case STYLE_N: {
				 lineEnding = new byte[]{(byte)'\n'};
			} break;
			default: {
				throw new IllegalArgumentException("Unknown line break style: " + style);
			}
		}
		byte[] buffer = new byte[BUFFER_SIZE];
		int read;
		int state = STATE_INIT;
		int seen = 0x00;
		while((read = in.read(buffer)) != -1){
			for (int i=0; i<read; i++){
				byte b = buffer[i];
				if (state==STATE_R){
					if(b!='\n'){
						out.write(lineEnding);
						seen |= MASK_R;
					}
				}
				if (b=='\r'){
					state = STATE_R;
				} else {
					if (b=='\n'){
						if (state==STATE_R){
							seen |= MASK_RN;
						} else {
							seen |= MASK_N;
						}
						out.write(lineEnding);
					} else if(binaryException && b!='\t' && b!='\f' && (b & 0xff)<32){
						throw new BinaryDataException(labels.getString("binaryexcepion"));
					} else {
						out.write(b);
					}
					state = STATE_INIT;
				}
			}
		}
		if (state==STATE_R){
			out.write(lineEnding);
			seen |= MASK_R;
		}
		if (lineEnding.length==2 && lineEnding[0]=='\r' && lineEnding[1]=='\n'){
			return ((seen & ~MASK_RN)!=0);
		} else if (lineEnding.length==1 && lineEnding[0]=='\r'){
			return ((seen & ~MASK_R)!=0);
		} else if (lineEnding.length==1 && lineEnding[0]=='\n'){
			return ((seen & ~MASK_N)!=0);
		} else {
			return true;
		}
	}

	/**
	 * Change the line endings on given file.
	 *
	 * The current system's line separator is used.
	 *
	 * @param f File to be converted.
	 * @return true if the file was modified, false if it was already in the correct format
	 * @throws BinaryDataException if non-text data is encountered.
	 * @throws IOException if an input or output error occurs.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static boolean convert(File f) throws IOException {
		return convert(f, STYLE_SYSTEM, true);
	}

	/**
	 * Change the line endings on given file.
	 *
	 * @param f File to be converted.
	 * @param style line separator style.
	 * @return true if the file was modified, false if it was already in the correct format
	 * @throws BinaryDataException if non-text data is encountered.
	 * @throws IOException if an input or output error occurs.
	 * @throws IllegalArgumentException if an unknown style is requested.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static boolean convert(File f, int style) throws IOException {
		return convert(f, style, true);
	}

	/**
	 * Change the line endings on given file.
	 *
	 * The current system's line separator is used.
	 *
	 * @param f File to be converted.
	 * @param binaryException throw an exception and abort the operation if binary data is encountered and binaryExcepion is false.
	 * @return true if the file was modified, false if it was already in the correct format
	 * @throws BinaryDataException if non-text data is encountered.
	 * @throws IOException if an input or output error occurs.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static boolean convert(File f, boolean binaryException) throws IOException {
		return convert(f, STYLE_SYSTEM, binaryException);
	}

	/**
	 * Change the line endings on given file.
	 *
	 * @param f File to be converted.
	 * @param style line separator style.
	 * @param binaryException throw an exception and abort the operation if binary data is encountered and binaryExcepion is false.
	 * @return true if the file was modified, false if it was already in the correct format
	 * @throws BinaryDataException if non-text data is encountered.
	 * @throws IOException if an input or output error occurs.
	 * @throws IllegalArgumentException if an unknown style is requested.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static boolean convert(File f, int style, boolean binaryException) throws IOException {
		File temp = null;
		InputStream in = null;
		OutputStream out = null;
		boolean modified = false;
		try {
			in = new FileInputStream(f);
			temp = File.createTempFile("LineEnds", null, null);
			out = new FileOutputStream(temp);
			modified = convert (in, out, style, binaryException);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			if (modified){
				FileHelper.move(temp, f, true);
			} else {
				if (!temp.delete()){
					throw new IOException(
						MessageFormat.format(
							labels.getString("tempdeleteerror"),
							new String[] {temp.toString()}
						)
					);
				}
			}
		} finally {
			if (in != null){
				in.close();
				in = null;
			}
			if (out != null){
				out.flush();
				out.close();
				out = null;
			}
		}
		return modified;
	}
}
