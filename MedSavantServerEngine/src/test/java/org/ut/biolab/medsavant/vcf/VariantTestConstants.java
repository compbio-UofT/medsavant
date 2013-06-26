/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.vcf;

/**
 * Hacky way to store some example values for the variant terms.
 *
 * Taken from the schema browser.
 *
 * //FIXME there has to be a better way to this
 */
public class VariantTestConstants {

    public static String[] ids = new String[] {
            ".",
            "rs1014010",
            "rs10225678",
            "rs1015680",
            "rs1019080",
            "rs1019081",
            "rs1014011",
            "rs10227845",
            "rs10230092",
            "rs10236205"
    };

    public static String[] dna_ids = new String[] {
            "HG01551",
            "HG01617",
            "HG01618",
            "HG01619",
            "HG01620",
            "HG01623",
            "HG01624",
            "HG01625",
            "HG01626",
            "NA06984",
            "NA06986",
            "NA06989",
            "NA06994",
            "NA07000",
            "NA07037",
            "NA07048",
            "NA07051",
            "NA07056",
            "NA07347",
            "NA07357",
            "NA10847",
            "NA10851"
    };

    public static String[] chroms = new String[] { "7", "17", "10"};

    public static String[] alts = new String[] {
            "T",
            "A",
            "G",
            "C",
            "GT",
            "TA",
            "ATG",
            "TATCTATCTATC",
            "CG",
            "TG"
    };

    public static String[] refs = new String[] {
            "C",
            "G",
            "T",
            "A",
            "TG",
            "TC",
            "CA",
            "AG",
            "ATTTAC",
            "CTG"
    };

    public static String[] zygosities = new String[] {
            "HomoRef", "HomoAlt", "Hetero", "HeteroTriallelic", "Missing"
    };

}
