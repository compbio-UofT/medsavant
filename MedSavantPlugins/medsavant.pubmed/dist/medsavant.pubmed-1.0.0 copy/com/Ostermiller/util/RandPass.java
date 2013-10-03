/*
 * Generate random passwords.
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

import java.security.SecureRandom;
import gnu.getopt.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Vector;

/**
 * Generates a random String using a cryptographically
 * secure random number generator.
 * <p>
 * The alphabet (characters used in the passwords generated)
 * may be specified, and the random number generator can be
 * externally supplied.
 * <p>
 * Care should be taken when using methods that limit the types
 * of passwords may be generated.  Using an alphabet that is too
 * small, using passwords that are too short, requiring too many
 * of a certain type of character, or not allowing repetition,
 * may decrease security.
 * <p>
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/RandPass.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class RandPass {

	/**
	 * Version number of this program
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static final String version = "1.1";

	/**
	 * Locale specific strings displayed to the user.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected static ResourceBundle labels = ResourceBundle.getBundle("com.Ostermiller.util.RandPass",  Locale.getDefault());


	/**
	 * Default length for passwords
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static final int DEFAULT_PASSWORD_LENGTH = 8;

	/**
	 * Alphabet consisting of upper and lowercase letters A-Z and
	 * the digits 0-9.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static final char[] NUMBERS_AND_LETTERS_ALPHABET = {
		'A','B','C','D','E','F','G','H',
		'I','J','K','L','M','N','O','P',
		'Q','R','S','T','U','V','W','X',
		'Y','Z','a','b','c','d','e','f',
		'g','h','i','j','k','l','m','n',
		'o','p','q','r','s','t','u','v',
		'w','x','y','z','0','1','2','3',
		'4','5','6','7','8','9',
	};

	/**
	 * Alphabet consisting of all the printable ASCII symbols.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static final char[] SYMBOLS_ALPHABET = {
		'!','\"','#','$','%','&','\'','(',
		')','*','+',',','-','.','/',':',
		';','<','?','@','[','\\',']','^',
		'_','`','{','|','}','~',
	};

	/**
	 * Alphabet consisting of all the printable ASCII characters.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static final char[] PRINTABLE_ALPHABET = {
		'!','\"','#','$','%','&','\'','(',
		')','*','+',',','-','.','/','0',
		'1','2','3','4','5','6','7','8',
		'9',':',';','<','?','@','A','B',
		'C','D','E','F','G','H','I','J',
		'K','L','M','N','O','P','Q','R',
		'S','T','U','V','W','X','Y','Z',
		'[','\\',']','^','_','`','a','b',
		'c','d','e','f','g','h','i','j',
		'k','l','m','n','o','p','q','r',
		's','t','u','v','w','x','y','z',
		'{','|','}','~',
	};

	/**
	 * Alphabet consisting of the lowercase letters A-Z.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static final char[] LOWERCASE_LETTERS_ALPHABET = {
		'a','b','c','d','e','f','g','h',
		'i','j','k','l','m','n','o','p',
		'q','r','s','t','u','v','w','x',
		'y','z',
	};

	/**
	 * Alphabet consisting of the lowercase letters A-Z and
	 * the digits 0-9.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static final char[] LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET = {
		'a','b','c','d','e','f','g','h',
		'i','j','k','l','m','n','o','p',
		'q','r','s','t','u','v','w','x',
		'y','z','0','1','2','3','4','5',
		'6','7','8','9',
	};

	/**
	 * Alphabet consisting of upper and lowercase letters A-Z.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static final char[] LETTERS_ALPHABET = {
		'A','B','C','D','E','F','G','H',
		'I','J','K','L','M','N','O','P',
		'Q','R','S','T','U','V','W','X',
		'Y','Z','a','b','c','d','e','f',
		'g','h','i','j','k','l','m','n',
		'o','p','q','r','s','t','u','v',
		'w','x','y','z',
	};

	/**
	 * Alphabet consisting of the upper letters A-Z.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static final char[] UPPERCASE_LETTERS_ALPHABET = {
		'A','B','C','D','E','F','G','H',
		'I','J','K','L','M','N','O','P',
		'Q','R','S','T','U','V','W','X',
		'Y','Z',
	};

	/**
	 * Alphabet consisting of upper and lowercase letters A-Z and
	 * the digits 0-9 but with characters that are often mistaken
	 * for each other when typed removed. (I,L,O,U,V,i,l,o,u,v,0,1)
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static final char[] NONCONFUSING_ALPHABET = {
		'A','B','C','D','E','F','G','H',
		'J','K','M','N','P','Q','R','S',
		'T','W','X','Y','Z','a','b','c',
		'd','e','f','g','h','j','k','m',
		'n','p','q','r','s','t','w','x',
		'y','z','2','3','4','5','6','7',
		'8','9',
	};

	/**
	 * Random number generator used.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected SecureRandom rand;

	/**
	 * One less than the maximum number of repeated characters
	 * that are allowed in a password.
	 * Set to -1 to disable this feature.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected int repetition = -1;

	/**
	 * Set of characters which may be
	 * used in the generated passwords.
	 * <p>
	 * This value may not be null or have
	 * no elements.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected char[] alphabet;

	/**
	 * Set of characters which may be
	 * used for the first character
	 * in the generated passwords.
	 * <p>
	 * This value may be null but it mus
	 * have at least one element otherwise.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected char[] firstAlphabet;

	/**
	 * Set of characters which may be
	 * used for the last character
	 * in the generated passwords.
	 * <p>
	 * This value may be null but it mus
	 * have at least one element otherwise.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected char[] lastAlphabet;

	/**
	 * Create a new random password generator
	 * with the default secure random number generator
	 * and default NONCONFUSING alphabet for all characters.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public RandPass(){
		this(new SecureRandom(), NONCONFUSING_ALPHABET);
	}

	/**
	 * Create a new random password generator
	 * with the given secure random number generator
	 * and default NONCONFUSING alphabet for all characters.
	 *
	 * @param rand Secure random number generator to use when generating passwords.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public RandPass(SecureRandom rand){
		this(rand, NONCONFUSING_ALPHABET);
	}

	/**
	 * Create a new random password generator
	 * with the default secure random number generator
	 * and given alphabet for all characters.
	 *
	 * @param alphabet Characters allowed in generated passwords.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public RandPass(char[] alphabet){
		this(new SecureRandom(), alphabet);
	}

	/**
	 * Create a new random password generator
	 * with the given secure random number generator
	 * and given alphabet for all characters.
	 *
	 * @param rand Secure random number generator to use when generating passwords.
	 * @param alphabet Characters allowed in generated passwords.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public RandPass(SecureRandom rand, char[] alphabet){
		this.rand = rand;
		this.alphabet = alphabet;
	}

	private class Requirement {
		public Requirement(char[] alphabet, int num){
			this.alphabet = alphabet;
			this.num = num;
		}
		public char[] alphabet;
		public int num;
	}

	/**
	 * Generate a random passwords.
	 * Run with --help argument for more information.
	 *
	 * @param args Command line arguments.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void main(String[] args) throws Exception {
		// create the command line options that we are looking for
		LongOpt[] longopts = {
			new LongOpt(labels.getString("help.option"), LongOpt.NO_ARGUMENT, null, 1),
			new LongOpt(labels.getString("version.option"), LongOpt.NO_ARGUMENT, null, 2),
			new LongOpt(labels.getString("about.option"), LongOpt.NO_ARGUMENT, null, 3),
			new LongOpt(labels.getString("alphabet.option"), LongOpt.REQUIRED_ARGUMENT, null, 'a'),
			new LongOpt(labels.getString("first.alphabet.option"), LongOpt.REQUIRED_ARGUMENT, null, 'F'),
			new LongOpt(labels.getString("last.alphabet.option"), LongOpt.REQUIRED_ARGUMENT, null, 'L'),
			new LongOpt(labels.getString("number.option"), LongOpt.REQUIRED_ARGUMENT, null, 'n'),
			new LongOpt(labels.getString("maxrep.option"), LongOpt.REQUIRED_ARGUMENT, null, 'r'),
			new LongOpt(labels.getString("length.option"), LongOpt.REQUIRED_ARGUMENT, null, 'l'),
			new LongOpt(labels.getString("require.option"), LongOpt.REQUIRED_ARGUMENT, null, 'R'),
			new LongOpt(labels.getString("verify.option"), LongOpt.REQUIRED_ARGUMENT, null, 'v'),
		};
		String oneLetterOptions = "a:n:F:L:r:l:R:v:";
		Getopt opts = new Getopt(labels.getString("randpass"), args, oneLetterOptions, longopts);
		int number = 1;
		char[] alphabet = NONCONFUSING_ALPHABET;
		char[] firstAlphabet = null;
		char[] lastAlphabet = null;
		Vector reqs = new Vector();
		Vector ver = new Vector();
		int maxreps = 0;
		int length = 8;
		int c;
		while ((c = opts.getopt()) != -1){
			switch(c){
				case 1:{
					// print out the help message
					String[] helpFlags = new String[]{
						"--" + labels.getString("help.option"),
						"--" + labels.getString("version.option"),
						"--" + labels.getString("about.option"),
						"-a --" + labels.getString("alphabet.option") + " " + labels.getString("alphabet.argument"),
						"-n --" + labels.getString("number.option") + " " + labels.getString("number.argument"),
						"-F --" + labels.getString("first.alphabet.option") + " " + labels.getString("alphabet.argument"),
						"-L --" + labels.getString("last.alphabet.option") + " " + labels.getString("alphabet.argument"),
						"-l --" + labels.getString("length.option") + " " + labels.getString("number.argument"),
						"-r --" + labels.getString("maxrep.option") + " " + labels.getString("number.argument"),
						"-R --" + labels.getString("require.option") + " " + labels.getString("alphabet.argument"),
						"-v --" + labels.getString("verify.option") + " " + labels.getString("class.argument"),
					};
					int maxLength = 0;
					for (int i=0; i<helpFlags.length; i++){
						maxLength = Math.max(maxLength, helpFlags[i].length());
					}
					maxLength += 2;
					System.out.println(
						labels.getString("randpass") + " [-" + StringHelper.replace(oneLetterOptions, ":", "") + "]\n" +
						labels.getString("purpose.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[0] ,maxLength, ' ') + labels.getString("help.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[1] ,maxLength, ' ') + labels.getString("version.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[2] ,maxLength, ' ') + labels.getString("about.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[3] ,maxLength, ' ') + labels.getString("a.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[4] ,maxLength, ' ') + labels.getString("n.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[5] ,maxLength, ' ') + labels.getString("F.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[6] ,maxLength, ' ') + labels.getString("L.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[7] ,maxLength, ' ') + labels.getString("l.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[8] ,maxLength, ' ') + labels.getString("r.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[9] ,maxLength, ' ') + labels.getString("R.message") + "\n" +
						"  " + StringHelper.postpad(helpFlags[10] ,maxLength, ' ') + labels.getString("v.message") + "\n"
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
						labels.getString("randpass") + " -- " + labels.getString("purpose.message") + "\n" +
						MessageFormat.format(labels.getString("copyright"), new String[] {"2001-2002", "Stephen Ostermiller (http://ostermiller.org/contact.pl?regarding=Java+Utilities)"}) + "\n\n" +
						labels.getString("license")
					);
					System.exit(0);
				} break;
				case 'a':{
					String alph = opts.getOptarg();
					if (alph.length() == 0){
						alphabet = NONCONFUSING_ALPHABET;
					} else {
						alphabet = alph.toCharArray();
					}
				} break;
				case 'F':{
					String alph = opts.getOptarg();
					if (alph.length() == 0){
						firstAlphabet = null;
					} else {
						firstAlphabet = alph.toCharArray();
					}
				} break;
				case 'L':{
					String alph = opts.getOptarg();
					if (alph.length() == 0){
						lastAlphabet = null;
					} else {
						lastAlphabet = alph.toCharArray();
					}
				} break;
				case 'R':{
					String alph = opts.getOptarg();
					if (alph.length() != 0){
						reqs.add(alph);
					}
				} break;
				case 'v':{
					ver.add(opts.getOptarg());
				} break;
				case 'n':{
					try {
						number = Integer.parseInt(opts.getOptarg());
						if (number <= 0) throw new NumberFormatException();
					} catch (NumberFormatException nfe){
						System.err.println(labels.getString("number.bad_argument"));
						System.exit(0);
					}
				} break;
				case 'r':{
					try {
						maxreps = Integer.parseInt(opts.getOptarg());
						if (maxreps < 0) throw new NumberFormatException();
					} catch (NumberFormatException nfe){
						System.err.println(labels.getString("number.bad_argument"));
						System.exit(0);
					}
				} break;
				case 'l':{
					try {
						length = Integer.parseInt(opts.getOptarg());
						if (length < 0) throw new NumberFormatException();
					} catch (NumberFormatException nfe){
						System.err.println(labels.getString("number.bad_argument"));
						System.exit(0);
					}
				} break;
				default:{
					System.exit(0);
				}
			}
		}
		RandPass randPass = new RandPass();
		randPass.setAlphabet(alphabet);
		randPass.setFirstAlphabet(firstAlphabet);
		randPass.setLastAlphabet(lastAlphabet);
		randPass.setMaxRepetition(maxreps);
		for (int i=0; i<reqs.size(); i++){
			randPass.addRequirement(((String)(reqs.elementAt(i))).toCharArray(), 1);
		}
		for (int i=0; i<ver.size(); i++){
			randPass.addVerifier((PasswordVerifier)((Class.forName((String)(ver.elementAt(i)))).newInstance()));
		}
		for (int i=0; i<number; i++){
			System.out.println(randPass.getPass(length));
		}
	}


	private Vector requirements = null;

	/**
	 * Require that a certain number of characters from an
	 * alphabet be present in generated passwords.
	 *
	 * @param alphabet set of letters that must be present
	 * @param num number of letters from the alphabet that must be present.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void addRequirement(char[] alphabet, int num){
		if (requirements == null) requirements = new Vector();
		requirements.add(new Requirement(alphabet, num));
	}


	/**
	 * Set the alphabet used by this random password generator.
	 *
	 * @param alphabet Characters allowed in generated passwords.
	 * @throws NullPointerException if the alphabet is null.
	 * @throws ArrayIndexOutOfBoundsException if the alphabet has no elements.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setAlphabet(char[] alphabet){
		if (alphabet == null) throw new NullPointerException("Null alphabet");
		if (alphabet.length == 0) throw new ArrayIndexOutOfBoundsException("No characters in alphabet");
		this.alphabet = alphabet;
	}

	/**
	 * Set the random number generator used by this random password generator.
	 *
	 * @param rand Secure random number generator to use when generating passwords.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setRandomGenerator(SecureRandom rand){
		this.rand = rand;
	}

	/**
	 * Set the alphabet used by this random password generator for the first character
	 * of passwords.
	 * <p>
	 * If the alphabet for the first character is set to null or has no elements, the main alphabet will
	 * be used for the first character.
	 *
	 * @param alphabet Characters allowed for the first character of the passwords.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setFirstAlphabet(char[] alphabet){
		if (alphabet == null || alphabet.length == 0){
			this.firstAlphabet = null;
		} else {
			this.firstAlphabet = alphabet;
		}
	}

	/**
	 * Set the alphabet used by this random password generator for the last character
	 * of passwords.
	 * <p>
	 * If the alphabet for the last character is set to null or has no elements, the main alphabet will
	 * be used for the last character.
	 *
	 * @param alphabet Characters allowed for the last character of the passwords.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setLastAlphabet(char[] alphabet){
		if (alphabet == null || alphabet.length == 0){
			this.lastAlphabet = null;
		} else {
			this.lastAlphabet = alphabet;
		}
	}

	/**
	 * Set the maximum number of characters that may appear in sequence more than
	 * once in a password.  Your alphabet must be large enough to handle this
	 * option.  If your alphabet is {'a', 'b'} and you want 8 character passwords
	 * in which no character appears twice (repetition 1) you are out of luck.
	 * In such instances your request for no repetition will be ignored.
	 * <p>
	 * For example setRepetition(3) will allow a password ababab but not allow
	 * abcabc.
	 * <p>
	 * Using this method can greatly reduce the pool of passwords that are generated.
	 * For example if only one repetition is allowed then the pool of passwords
	 * is the permutation of the alphabet rather than the combination.
	 *
	 * @param rep Maximum character repetition.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setMaxRepetition(int rep){
		this.repetition = rep - 1;
	}

	/**
	 * Fill the given buffer with random characters.
	 * <p>
	 * Using this method, the password character array can easily
	 * be reused for efficiency, or overwritten with new random
	 * characters for security.
	 * <p>
	 * NOTE: If it is possible for a hacker to examine memory to find passwords,
	 * the password should be overwritten in memory as soon as possible after i
	 * is no longer in use.
	 *
	 * @param pass buffer that will hold the password.
	 * @return the buffer, filled with random characters.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public char[] getPassChars(char[] pass){
		boolean verified = false;
		while (!verified){
			int length = pass.length;
			for (int i=0; i<length; i++){
				char[] useAlph = alphabet;
				if (i == 0 && firstAlphabet != null){
					useAlph = firstAlphabet;
				} else if (i == length - 1 && lastAlphabet != null){
					useAlph = lastAlphabet;
				}
				int size = avoidRepetition(useAlph, pass, i, repetition, useAlph.length);
				pass[i] = useAlph[rand.nextInt(size)];
			}
			if (requirements != null) applyRequirements(pass);
			verified = true;
			for (int i=0; verified && verifiers != null && i<verifiers.size(); i++){
				verified = ((PasswordVerifier)verifiers.elementAt(i)).verify(pass);
			}
		}
		return(pass);
	}

	private Vector verifiers = null;

	/**
	 * Add a class that will verify passwords.  No password
	 * will be returned unless all verifiers approve of it.
	 *
	 * @param verifier class that performs verification of password.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void addVerifier(PasswordVerifier verifier){
		if (verifiers == null) verifiers = new Vector();
		verifiers.add(verifier);
	}

	private boolean[] touched = null;
	private int[] available = null;

	private void applyRequirements(char[] pass){
		int size = requirements.size();
		if (size > 0){
			int length = pass.length;
			if (touched == null || touched.length < length) touched = new boolean[length];
			if (available == null || available.length < length) available = new int[length];
			for (int i=0; i<length; i++){
				touched[i] = false;
			}
			for (int reqNum=0; reqNum<size; reqNum++){
				Requirement req = (Requirement)requirements.elementAt(reqNum);
				// set the portion of this alphabet available for use.
				int reqUsedInd = req.alphabet.length;
				// figure out how much of this requirement is already fulfilled
				// and what is available to fulfill the rest of it.
				int fufilledInd = 0;
				int availableInd = 0;
				for (int i=0; i<length; i++){
					if (arrayContains(req.alphabet, pass[i]) && fufilledInd < req.num){
						fufilledInd++;
						touched[i] = true;
						if (repetition >= 0){
							// move already used characters so they can'
							// be used again.  This prevents repetition.
							reqUsedInd -= moveto(req.alphabet, reqUsedInd, pass[i]);
							// allow repetition if we have no other choice
							if(reqUsedInd < 0) reqUsedInd = req.alphabet.length;
						}
					} else if (!touched[i]){
						available[availableInd] = i;
						availableInd++;
					}
				}
				// fulfill the requirement
				int toDo = req.num - fufilledInd;
				for (int i=0; i<toDo && availableInd>0; i++){
					// pick a random available slot
					// and a random member of the available alphabet
					int slot = rand.nextInt(availableInd);
					char passChar = req.alphabet[rand.nextInt(reqUsedInd)];
					pass[available[slot]] = passChar;
					touched[available[slot]] = true;
					// make the slot no longer available
					availableInd--;
					available[slot] = available[availableInd];
					if (repetition >= 0){
						// move already used characters so they can'
						// be used again.  This prevents repetition.
						reqUsedInd -= moveto(req.alphabet, reqUsedInd, passChar);
						// allow repetition if we have no other choice
						if(reqUsedInd < 0) reqUsedInd = req.alphabet.length;
					}
				}
			}
		}
	}

	private static boolean arrayContains(char[] alph, char c){
		for (int i=0; i<alph.length; i++){
			if (alph[i] == c) return true;
		}
		return false;
	}

	/**
	 * Avoid repetition (if possible) by moving all characters that would cause repetition to
	 * the end of the alphabet and returning the size of the alphabet that may be used.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static int avoidRepetition(char[] alph, char[] pass, int passSize, int repetition, int alphSize){
		if (repetition > -1){
			// limit the alphabet to those characters that
			// will not cause repeating sequences
			int repPos = 0;
			while ((repPos = findRep(pass, repPos, passSize, repetition)) != -1){
				// shuffle characters that would cause repetition
				// to the end of the alphabet and adjust the size
				// so that they will not be used.
				alphSize -= moveto(alph, alphSize, pass[repPos+repetition]);
				repPos++;
			}
			if (alphSize == 0) alphSize = alph.length;
		}
		return alphSize;
	}

	/**
	 * Find a repetition of the desired length.  The characters to search
	 * for are located at pass[end-length] to pass[end]
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static int findRep(char[] pass, int start, int end, int length){
		for (int i=start; i<end-length; i++){
			boolean onTrack = true;
			for (int j=0; onTrack && j<length; j++){
				if (pass[i+j] != pass[end-length+j]) onTrack = false;
			}
			if(onTrack) return i;
		}
		return -1;
	}

	/**
	 * move all of the given character to the end of the array
	 * and return the number of characters moved.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static int moveto(char[] alph, int numGood, char c){
		int count = 0;
		for (int i=0; i<numGood; i++){
			if (alph[i] == c){
				numGood--;
				char temp = alph[numGood];
				alph[numGood] = alph[i];
				alph[i] = temp;
				count++;
			}
		}
		return count;
	}

	/**
	 * Generate a random password of the given length.
	 * <p>
	 * NOTE: If it is possible for a hacker to examine memory to find passwords,
	 * the password should be overwritten in memory as soon as possible after i
	 * is no longer in use.
	 *
	 * @param length The desired length of the generated password.
	 * @return a random password
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public char[] getPassChars(int length){
		return(getPassChars(new char[length]));
	}

	/**
	 * Generate a random password of the default length (8).
	 * <p>
	 * NOTE: If it is possible for a hacker to examine memory to find passwords,
	 * the password should be overwritten in memory as soon as possible after i
	 * is no longer in use.
	 *
	 * @return a random password
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public char[] getPassChars(){
		return(getPassChars(DEFAULT_PASSWORD_LENGTH));
	}

	/**
	 * Generate a random password of the given length.
	 * <p>
	 * NOTE: Strings can not be modified.  If it is possible
	 * for a hacker to examine memory to find passwords, getPassChars()
	 * should be used so that the password can be zeroed out of memory
	 * when no longer in use.
	 *
	 * @param length The desired length of the generated password.
	 * @return a random password
	 *
	 * @see #getPassChars(int)
	 * @since ostermillerutils 1.00.00
	 */
	public String getPass(int length){
		return(new String(getPassChars(new char[length])));
	}

	/**
	 * Generate a random password of the default length (8).
	 * <p>
	 * NOTE: Strings can not be modified.  If it is possible
	 * for a hacker to examine memory to find passwords, getPassChars()
	 * should be used so that the password can be zeroed out of memory
	 * when no longer in use.
	 *
	 * @return a random password
	 *
	 * @see #getPassChars()
	 * @since ostermillerutils 1.00.00
	 */
	public String getPass(){
		return(getPass(DEFAULT_PASSWORD_LENGTH));
	}
}
