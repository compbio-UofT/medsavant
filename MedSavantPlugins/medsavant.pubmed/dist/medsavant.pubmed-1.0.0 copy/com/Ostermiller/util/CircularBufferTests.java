/*
 * Regression tests for Circular Buffers.
 * Copyright (C) 2002 Stephen Ostermiller
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
import java.util.*;

/**
 * Regression test for circular buffers.  When run, this program
 * should output the Gettysburg Address and quite a few digits of
 * pi.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/CircularCharBuffer.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
class CircularBufferTests {

	private byte[] pi = {
		3,
		1,4,1,5,9,2,6,5,3,5,8,9,7,9,3,2,3,8,4,6,2,6,4,3,3,8,3,2,7,9,5,0,2,8,8,4,1,9,7,1,6,9,3,9,9,3,7,5,1,0,5,8,2,0,9,7,4,9,4,4,
		5,9,2,3,0,7,8,1,6,4,0,6,2,8,6,2,0,8,9,9,8,6,2,8,0,3,4,8,2,5,3,4,2,1,1,7,0,6,7,9,8,2,1,4,8,0,8,6,5,1,3,2,8,2,3,0,6,6,4,7,
		0,9,3,8,4,4,6,0,9,5,5,0,5,8,2,2,3,1,7,2,5,3,5,9,4,0,8,1,2,8,4,8,1,1,1,7,4,5,0,2,8,4,1,0,2,7,0,1,9,3,8,5,2,1,1,0,5,5,5,9,
		6,4,4,6,2,2,9,4,8,9,5,4,9,3,0,3,8,1,9,6,4,4,2,8,8,1,0,9,7,5,6,6,5,9,3,3,4,4,6,1,2,8,4,7,5,6,4,8,2,3,3,7,8,6,7,8,3,1,6,5,
		2,7,1,2,0,1,9,0,9,1,4,5,6,4,8,5,6,6,9,2,3,4,6,0,3,4,8,6,1,0,4,5,4,3,2,6,6,4,8,2,1,3,3,9,3,6,0,7,2,6,0,2,4,9,1,4,1,2,7,3,
		7,2,4,5,8,7,0,0,6,6,0,6,3,1,5,5,8,8,1,7,4,8,8,1,5,2,0,9,2,0,9,6,2,8,2,9,2,5,4,0,9,1,7,1,5,3,6,4,3,6,7,8,9,2,5,9,0,3,6,0,
		0,1,1,3,3,0,5,3,0,5,4,8,8,2,0,4,6,6,5,2,1,3,8,4,1,4,6,9,5,1,9,4,1,5,1,1,6,0,9,4,3,3,0,5,7,2,7,0,3,6,5,7,5,9,5,9,1,9,5,3,
		0,9,2,1,8,6,1,1,7,3,8,1,9,3,2,6,1,1,7,9,3,1,0,5,1,1,8,5,4,8,0,7,4,4,6,2,3,7,9,9,6,2,7,4,9,5,6,7,3,5,1,8,8,5,7,5,2,7,2,4,
		8,9,1,2,2,7,9,3,8,1,8,3,0,1,1,9,4,9,1,2,9,8,3,3,6,7,3,3,6,2,4,4,0,6,5,6,6,4,3,0,8,6,0,2,1,3,9,4,9,4,6,3,9,5,2,2,4,7,3,7,
		1,9,0,7,0,2,1,7,9,8,6,0,9,4,3,7,0,2,7,7,0,5,3,9,2,1,7,1,7,6,2,9,3,1,7,6,7,5,2,3,8,4,6,7,4,8,1,8,4,6,7,6,6,9,4,0,5,1,3,2,
		0,0,0,5,6,8,1,2,7,1,4,5,2,6,3,5,6,0,8,2,7,7,8,5,7,7,1,3,4,2,7,5,7,7,8,9,6,0,9,1,7,3,6,3,7,1,7,8,7,2,1,4,6,8,4,4,0,9,0,1,
		2,2,4,9,5,3,4,3,0,1,4,6,5,4,9,5,8,5,3,7,1,0,5,0,7,9,2,2,7,9,6,8,9,2,5,8,9,2,3,5,4,2,0,1,9,9,5,6,1,1,2,1,2,9,0,2,1,9,6,0,
		8,6,4,0,3,4,4,1,8,1,5,9,8,1,3,6,2,9,7,7,4,7,7,1,3,0,9,9,6,0,5,1,8,7,0,7,2,1,1,3,4,9,9,9,9,9,9,8,3,7,2,9,7,8,0,4,9,9,5,1,
	};

	private String theGettysburgAddress = "Four score and seven years ago our fathers brought forth, " +
		"upon this continent, a new nation, conceived in Liberty, and dedicated " +
		"to the proposition that all men are created equal.\n" +
		"Now we are engaged in a great civil war, testing whether that nation, " +
		"or any nation so conceived, and so dedicated, can long endure. We are " +
		"met here on a great battlefield of that war. We have come to dedicate " +
		"a portion of it as a final resting place for those who here gave their " +
		"lives that that nation might live. It is altogether fitting and proper " +
		"that we should do this.\n" +
		"But in a larger sense we can not dedicate -- we can not consecrate -- " +
		"we can not hallow this ground. The brave men, living and dead, who " +
		"struggled, here, have consecrated it far above our poor power to add " +
		"or detract. The world will little note, nor long remember, what we say here, " +
		"but can never forget what they did here. It is for us, the living, rather " +
		"to be dedicated here to the unfinished work which they have, thus far, so " +
		"nobly carried on. It is rather for us to be here dedicated to the great task " +
		"remaining before us -- that from these honored dead we take increased devotion " +
		"to that cause for which they here gave the last full measure of devotion -- " +
		"that we here highly resolve that these dead shall not have died in vain; that " +
		"this nation shall have a new birth of freedom; and that this government of the " +
		"people, by the people, for the people, shall not perish from the earth.";

	private StringWriter tgbaWriter = new StringWriter();
	private StringWriter tgbaWriter2 = new StringWriter();

	private ByteArrayOutputStream piOutputStream = new ByteArrayOutputStream();

	private Random rand = new Random();

	private CircularCharBuffer ccb = new CircularCharBuffer(20);
	private Reader ccbin;
	private Writer ccbout;

	private CircularByteBuffer cbb = new CircularByteBuffer(20);
	private InputStream cbbin;
	private OutputStream cbbout;

	private CircularObjectBuffer cob = new CircularObjectBuffer(20);

	public static void main(String args[]){
		try {
			new CircularBufferTests();
		} catch (Exception x){
			x.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

	private CircularBufferTests() throws Exception{

		ccbin = ccb.getReader();
		ccbout = ccb.getWriter();
		CCBProducer ccbp = new CCBProducer();
		CCBConsumer ccbc = new CCBConsumer();
		ccbc.start();
		ccbp.start();
		while (ccbc.isAlive() || ccbp.isAlive()){
			if(System.in.available() > 0){
				ccbc.interrupt();
				ccbp.interrupt();
				return;
			}
		}
		String s1 = tgbaWriter.toString();
		if (!theGettysburgAddress.equals(s1)){
			throw new Exception(s1);
		}

		cbbin = cbb.getInputStream();
		cbbout = cbb.getOutputStream();
		CBBProducer cbbp = new CBBProducer();
		CBBConsumer cbbc = new CBBConsumer();
		cbbc.start();
		cbbp.start();
		while (cbbc.isAlive() || cbbp.isAlive()){
			if(System.in.available() > 0){
				cbbc.interrupt();
				cbbp.interrupt();
				return;
			}
		}
		assertEqual(pi, piOutputStream.toByteArray());

		COBProducer cobp = new COBProducer();
		COBConsumer cobc = new COBConsumer();
		cobc.start();
		cobp.start();
		while (cobc.isAlive() || cobp.isAlive()){
			if(System.in.available() > 0){
				cobc.interrupt();
				cobp.interrupt();
				return;
			}
		}
		String s2 = tgbaWriter2.toString();
		if (!theGettysburgAddress.equals(s2)){
			throw new Exception(s2);
		}
	}

	private static void assertEqual(byte[] b1, byte[] b2) throws Exception {
		if (b1.length != b2.length) throw new Exception ("Length mismatch: " + b1.length + ", " + b2.length);
		for (int i=0; i<b1.length; i++){
			if (b1[i] != b2[i]){
				throw new Exception("Mismatch at position " + i + ": " + b1[i] + ", " + b2[i]);
			}
		}
	}

	private class CCBProducer extends Thread {
		public void run() {
			try {
				for(int position = 0; !isInterrupted() && position < theGettysburgAddress.length(); position++){
					int len = rand.nextInt(30);
					len = Math.min(len, theGettysburgAddress.length() - position);
					int off = rand.nextInt(10);
					switch (rand.nextInt(4)){
						case 0: {
							char[] writeBuf = new char[len];
							for (int i=0; i<len; i++){
								writeBuf[i] = theGettysburgAddress.charAt(position + i);
							}
							ccbout.write(writeBuf);
						} break;
						case 1: {
							char[] writeBuf = new char[off + len];
							for (int i=0; i<len; i++){
								writeBuf[i+off] = theGettysburgAddress.charAt(position + i);
							}
							ccbout.write(writeBuf, off, len);
						} break;
						case 2: {
							for (int i=0; !isInterrupted() && i<len; i++){
								ccbout.write(theGettysburgAddress.charAt(position + i));
							}
						} break;
						case 3: {
							ccbout.write(theGettysburgAddress.substring(position, position+len));
						} break;
						case 4: {
							ccbout.write(theGettysburgAddress, position, len);
						} break;
					}
					position += (len - 1);
					try {
						Thread.sleep(50 + rand.nextInt(100));
					} catch(Exception x){
						throw new IOException("Producer thread interrupted.");
					}
				}
				ccbout.close();
			} catch (IOException x){
				System.err.println(x.getMessage());
			}
		}
	}

	private class CCBConsumer extends Thread {
		public void run() {
			try {
				boolean done = false;
				while(!isInterrupted() && !done){
					int len = rand.nextInt(30);
					int off = rand.nextInt(10);
					switch (rand.nextInt(2)){
						case 0: {
							char[] readBuf = new char[len];
							int read = ccbin.read(readBuf);
							if (read == -1){
								done = true;
							} else {
								for (int i=0; i<read; i++){
									tgbaWriter.write(readBuf[i]);
								}
							}
						} break;
						case 1: {
							char[] readBuf = new char[off + len];
							int read = ccbin.read(readBuf, off, len);
							if (read == -1){
								done = true;
							} else {
								for (int i=0; i<read; i++){
									tgbaWriter.write(readBuf[i+off]);
								}
							}
						} break;
						case 2: {
							for (int i=0; !isInterrupted() && !done && i<len; i++){
								int read;
								read = ccbin.read();
								if (read == -1){
									done = true;
								} else {
									tgbaWriter.write((char)read);
								}
							}
						} break;
					}
					try {
						Thread.sleep(50 + rand.nextInt(100));
					} catch(Exception x){
						throw new IOException("Consumer thread interrupted.");
					}
				}
			} catch (IOException x){
				System.err.println(x.getMessage());
			}
		}
	}

	private class CBBProducer extends Thread {
		public void run() {
			try {
				for(int position = 0; !isInterrupted() && position < pi.length; position++){
					int len = rand.nextInt(30);
					len = Math.min(len, pi.length - position);
					int off = rand.nextInt(10);
					switch (rand.nextInt(2)){
						case 0: {
							byte[] writeBuf = new byte[len];
							for (int i=0; i<len; i++){
								writeBuf[i] = pi[position + i];
							}
							cbbout.write(writeBuf);
						} break;
						case 1: {
							byte[] writeBuf = new byte[off + len];
							for (int i=0; i<len; i++){
								writeBuf[i+off] = pi[position + i];
							}
							cbbout.write(writeBuf, off, len);
						} break;
						case 2: {
							for (int i=0; !isInterrupted() && i<len; i++){
								cbbout.write(pi[position + i]);
							}
						} break;
					}
					position += (len - 1);
					try {
						Thread.sleep(50 + rand.nextInt(100));
					} catch(Exception x){
						throw new IOException("Producer thread interrupted.");
					}
				}
				cbbout.close();
			} catch (IOException x){
				System.err.println(x.getMessage());
			}
		}
	}

	private class CBBConsumer extends Thread {
		public void run() {
			try {
				boolean done = false;
				while(!isInterrupted() && !done){
					int len = rand.nextInt(30);
					int off = rand.nextInt(10);
					switch (rand.nextInt(2)){
						case 0: {
							byte[] readBuf = new byte[len];
							int read = cbbin.read(readBuf);
							if (read == -1){
								done = true;
							} else {
								for (int i=0; i<read; i++){
									piOutputStream.write(readBuf[i]);
								}
							}
						} break;
						case 1: {
							byte[] readBuf = new byte[off + len];
							int read = cbbin.read(readBuf, off, len);
							if (read == -1){
								done = true;
							} else {
								for (int i=0; i<read; i++){
									piOutputStream.write(readBuf[i+off]);
								}
							}
						} break;
						case 2: {
							for (int i=0; !isInterrupted() && !done && i<len; i++){
								int read;
								read = cbbin.read();
								if (read == -1){
									done = true;
								} else {
									piOutputStream.write((byte)read);
								}
							}
						} break;
					}
					try {
						Thread.sleep(50 + rand.nextInt(100));
					} catch(Exception x){
						throw new IOException("Consumer thread interrupted.");
					}
				}
			} catch (IOException x){
				System.err.println(x.getMessage());
			}
		}
	}

	private class COBProducer extends Thread {
		public void run() {
			try {
				for(int position = 0; !isInterrupted() && position < theGettysburgAddress.length(); position++){
					int len = rand.nextInt(30);
					len = Math.min(len, theGettysburgAddress.length() - position);
					int off = rand.nextInt(10);
					switch (rand.nextInt(2)){
						case 0: {
							String[] writeBuf = new String[len];
							for (int i=0; i<len; i++){
								writeBuf[i] = Character.toString(theGettysburgAddress.charAt(position + i));
							}
							cob.write(writeBuf);
						} break;
						case 1: {
							String[] writeBuf = new String[off + len];
							for (int i=0; i<len; i++){
								writeBuf[i+off] = Character.toString(theGettysburgAddress.charAt(position + i));
							}
							cob.write(writeBuf, off, len);
						} break;
						case 2: {
							for (int i=0; !isInterrupted() && i<len; i++){
								cob.write(Character.toString(theGettysburgAddress.charAt(position + i)));
							}
						} break;
					}
					position += (len - 1);
					try {
						Thread.sleep(50 + rand.nextInt(100));
					} catch(Exception x){
						throw new IOException("Producer thread interrupted.");
					}
				}
				cob.done();
			} catch (Exception x){
				System.err.println(x.getMessage());
			}
		}
	}

	private class COBConsumer extends Thread {
		public void run() {
			try {
				boolean done = false;
				while(!isInterrupted() && !done){
					int len = rand.nextInt(30);
					int off = rand.nextInt(10);
					switch (rand.nextInt(2)){
						case 0: {
							Object[] readBuf = new Object[len];
							int read = cob.read(readBuf);
							if (read == -1){
								done = true;
							} else {
								for (int i=0; i<read; i++){
									tgbaWriter2.write((String)readBuf[i]);
								}
							}
						} break;
						case 1: {
							Object[] readBuf = new Object[off + len];
							int read = cob.read(readBuf, off, len);
							if (read == -1){
								done = true;
							} else {
								for (int i=0; i<read; i++){
									tgbaWriter2.write((String)readBuf[i+off]);
								}
							}
						} break;
						case 2: {
							for (int i=0; !isInterrupted() && !done && i<len; i++){
								Object read;
								read = cob.read();
								if (read == null){
									done = true;
								} else {
									tgbaWriter2.write((String)read);
								}
							}
						} break;
					}
					try {
						Thread.sleep(50 + rand.nextInt(100));
					} catch(Exception x){
						throw new IOException("Consumer thread interrupted.");
					}
				}
			} catch (Exception x){
				System.err.println(x.getMessage());
			}
		}
	}
}
