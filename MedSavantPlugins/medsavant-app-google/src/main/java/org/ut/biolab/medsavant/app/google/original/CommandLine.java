/*
Copyright 2014 Google Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.ut.biolab.medsavant.app.google.original;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Command line options handler for GenomicsSample
 */
class CommandLine {

  CmdLineParser parser;

  @Argument
  public List<String> remainingArgs = new ArrayList<String>();

  @Option(name = "--help",
      usage = "display this help message")
  public boolean help = false;

  @Option(name = "--root_url",
      metaVar = "<url>",
      usage = "set the Genomics API root URL")
  public String rootUrl = "https://www.googleapis.com/";

  @Option(name = "--client_secrets_filename",
      metaVar = "<client_secrets_filename>",
      usage = "Path to client_secrets.json")
  public String clientSecretsFilename = "client_secrets.json";

  @Option(name = "--dataset_id",
      metaVar = "<dataset_id>",
      usage = "The Genomics API dataset ID.")
  public String datasetId = "";

  @Option(name = "--job_id",
      metaVar = "<job_id>",
      usage = "The Genomics API job ID.")
  public String jobId = "";

  @Option(name = "--readset_id",
      metaVar = "<readsetId>",
      usage = "The Genomics API readset ID.")
  public String readsetId = "";

  @Option(name = "--page_token",
      metaVar = "<page_token>",
      usage = "The token used to retrieve additional pages in paginated API methods.")
  public String pageToken = "";

  @Option(name = "--fields",
      metaVar = "<field>",
      usage = "The fields to be returned with this query. " +
      "Leaving this blank returns all fields.")
  public String fields = "";

  @Option(name = "--dataset_ids",
      metaVar = "<datasetIds>",
      usage = "The list of dataset ids whose readsets are returned by this query.")
  public List<String> datasetIds = new ArrayList<String>();

  @Option(name = "--readset_ids",
      metaVar = "<readsetIds>",
      usage = "The list of readset ids whose reads are returned by this query.")
  public List<String> readsetIds = new ArrayList<String>();

  @Option(name = "--bam_file",
      metaVar = "<bamFile>",
      usage = "A BAM file (as Google Cloud Storage gs:// URL) to be be imported." +
          " Use the flag multiple times to import multiple BAM files at a time.")
  public List<String> bamFiles = new ArrayList<String>();

  @Option(name = "--sequence_name",
      metaVar = "<sequenceName>",
      usage = "The sequence name to query over (e.g. 'X', '23')")
  public String sequenceName = "";

  @Option(name = "--sequence_start",
      metaVar = "<sequenceStart>",
      usage = "The start position (1-based) of this query.")
  public Integer sequenceStart = 0;

  @Option(name = "--sequence_end",
      metaVar = "<sequenceEnd>",
      usage = "The end position (1-based, inclusive) of this query.")
  public Integer sequenceEnd = 0;

  public CommandLine() {
    // For testing
  }

  public CommandLine(String[] args) throws CmdLineException {
    parser = new CmdLineParser(this);
    parser.parseArgument(args);
  }

  public boolean showHelp() {
    return help;
  }

  public void printHelp(String headline, Appendable out) throws IOException {
    out.append(headline);
    out.append(getUsage());
  }

  public String getUsage() {
    StringWriter sw = new StringWriter();
    sw.append("Usage: GenomicsSample request_type [flags...]\n");
    parser.printUsage(sw, null);
    return sw.toString();
  }

}
