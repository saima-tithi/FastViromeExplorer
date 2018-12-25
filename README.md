# FastViromeExplorer
Indentify the viruses/phages and their abundance in the viral metagenomics data. The paper  describing FastViromeExplorer is available from here: https://peerj.com/articles/4227/.

# Installation
FastViromeExplorer requires JAVA (JDK) 1.8 or later, Samtools 1.4 or later, and Kallisto 0.43.0 or 0.43.1 installed in the user's machine. As in later versions of Kallisto, the output format of pseudoalignments is different, please use Kallisto version 0.43.0 or 0.43.1.
## Download FastViromeExplorer
You can download FastViromeExplorer directly from github and extract it. You can also download it using the following command:
```bash
git clone https://github.com/saima-tithi/FastViromeExplorer.git
```
From now on, we will refer the FastViromeExplorer directory in the user's local machine as `project directory`. The `project directory` will contain 5 folders: src, bin, test, tools-linux, and tools-mac. It will also contain two text files: ncbi-viruses-list and imgvr-viruses-list.txt.
## Install Java
If Java is not already installed, you need to install Java (JDK) 1.8 or later from the following link: http://www.oracle.com/technetwork/java/javase/downloads/index.html. From this link, download the appropriate jdk installation file (for linux or macOS), and then install Java by double-clicking the downloaded installation file.
## Install Kallisto and Samtools
If Kallisto or Samtools is not installed, you can install it from the executables distributed with FastViromeExplorer. 
In terminal, go into the project directory. Then go into the `tools-linux` folder if you are using a linux machine or go into the `tools-mac` folder if you are using macOS. Copy the kallisto and samtools executables from this directory to the /usr/local/bin directory.

```bash
cd /path-to-FastViromeExplorer/tools-linux
sudo cp kallisto /usr/local/bin/
sudo cp samtools /usr/local/bin/
```
Or

```bash
cd /path-to-FastViromeExplorer/tools-mac
sudo cp kallisto /usr/local/bin/
sudo cp samtools /usr/local/bin/
```
## Install FastViromeExplorer
In terminal, go into the project directory, which should contain `src` and `bin` folders. From the project directory, run the following command:
```bash
javac -d bin src/*.java
```
# Run FastViromeExplorer using test data
From the project directory, run the following commands:
```bash
mkdir test-output
java -cp bin FastViromeExplorer -1 test/reads_1.fq -2 test/reads_2.fq -i test/testset-kallisto-index.idx -o test-output
```
The test input files are given in the `test` folder. Here, the input files are:
1. *reads_1.fq* and *reads_2.fq* : paired-end reads in fastq format
2. *testset-kallisto-index.idx* : kallisto index file generated for a small set of NCBI RefSeq viruses

The output files will be generated in the `test-output` directory. The output files are:
1. *FastViromeExplorer-reads-mapped-sorted.sam* : aligned/mapped reads in sam format
2. *FastViromeExplorer-final-sorted-abundance.tsv* : virus abundance result in tab-delimited format

In a similar manner, we can run FastViromeExplorer for single-end reads without specifying the "-2" parameter. An example of running FastViromeExplorer for single-end reads:
```bash
mkdir test-output
java -cp bin FastViromeExplorer -1 test/reads_1.fq -i test/testset-kallisto-index.idx -o test-output
```

# Run FastViromeExplorer using NCBI RefSeq database
Some pre-computed kallisto index files are given in the following link: http://bench.cs.vt.edu/FastViromeExplorer/.
Download the kallisto index file for NCBI RefSeq database "ncbi-virus-kallisto-index-k31.idx" and save it. In terminal, go into the project directory. From the project directory, run the following command:
```bash
java -cp bin FastViromeExplorer -1 $read1File -2 $read2File -i /path-to-index-file/ncbi-virus-kallisto-index-k31.idx -o $outputDirectory
```
# Run FastViromeExplorer using IMG/VR database
Download the kallisto index file for IMG/VR database "imgvr-virus-kallisto-index-k31.idx" from http://bench.cs.vt.edu/FastViromeExplorer/ and save it. In terminal, go into the project directory. From the project directory, run the following command:
```bash
java -cp bin FastViromeExplorer -1 $read1File -2 $read2File -i /path-to-index-file/imgvr-virus-kallisto-index-k31.idx -l imgvr-viruses-list.txt -o $outputDirectory
```
For running FastViromeExplorer using IMG/VR database, we need to specify the kallisto index file and the list of viruses in the database along with their genome length, which is given in the file "imgvr-viruses-list.txt".
 
# Run FastViromeExplorer using custom database
For running FastViromeExplorer using any custom database, please look at our detailed manual at http://fastviromeexplorer.readthedocs.io/en/latest/.

# Usage
java -cp bin FastViromeExplorer -1 $read1File -2 $read2File -i $indexFile -o $outputDirectory

The full parameter list of FastViromeExplorer:
1. -1: input .fastq file or .fastq.gz file for read sequences (paired-end 1), mandatory field.
2. -2: input .fastq file or .fastq.gz file for read sequences (paired-end 2).
3. -i: kallisto index file, mandatory field.
4. -db: reference database file in fasta/fa format.
5. -o: output directory, default option is the project directory.
6. -l: virus list containing all viruses present in the reference database along with their length.
7. -cr: the value of ratio criteria, default: 0.3.
8. -co: the value of coverage criteria, default: 0.1.
9. -cn: the value of number of reads criteria, default: 10.
10. -salmon: use salmon instead of kallisto, default: false. To use salmon pass '-salmon true' as parameter.

# Support
If you are having issues, please look at the detailed manual at http://fastviromeexplorer.readthedocs.io/en/latest/ or contact us at saima5@vt.edu
# License
This project is licensed under the BSD 2-clause "Simplified" License.
# Citation
If you are using our tool, please cite us:

Saima Sultana Tithi, Frank O. Aylward, Roderick V. Jensen, and Liqing Zhang. "FastViromeExplorer: a pipeline for virus and phage identification and abundance profiling in metagenomics data." PeerJ 6 (2018): e4227.
