import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

public class FastViromeExplorer {
    private static String outDir = "";
    private static String read1 = "";
    private static String read2 = "";
    private static String kallistoIndexFile = "";
    private static String refDbFile = "";
    private static String virusListFile = "";
    private static double ratioCriteria = 0.3;
    private static double coverageCriteria = 0.1;
    private static int numReadsCriteria = 10;
    private static double avgReadLen = 0;
    private static boolean ASC = true;
    private static boolean DESC = false;
    private static Map<String, Integer> virusLength;
    private static Map<String, String> virusLineage;
    private static Set<String> virusRatio;
    
    // sort a map
    private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order) {
        List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
    
    private static void parseArguments(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }
        else {
            for (int i = 0; i < args.length; i++) {
                if(args[i].startsWith("-")) {
                    if ((i+1) >= args.length) {
                        System.out.println("Missing argument after " + args[i] + " .");
                        printUsage();
                        System.exit(1);
                    }
                    else {
                        if (args[i].equals("-o")) {
                            outDir = args[i+1];
                        }
                        else if (args[i].equals("-1")) {
                            read1 = args[i+1];
                        }
                        else if (args[i].equals("-2")) {
                            read2 = args[i+1];
                        }
                        else if (args[i].equals("-i")) {
                            kallistoIndexFile = args[i+1];
                        }
                        else if (args[i].equals("-db")) {
                            refDbFile = args[i+1];
                        }
                        else if (args[i].equals("-l")) {
                            virusListFile = args[i+1];
                        }
                        else if (args[i].equals("-cr")) {
                            ratioCriteria = Double.parseDouble(args[i+1]);
                        }
                        else if (args[i].equals("-co")) {
                            coverageCriteria = Double.parseDouble(args[i+1]);
                        }
                        else if (args[i].equals("-cn")) {
                            numReadsCriteria = Integer.parseInt(args[i+1]);
                        }
                        else {
                            System.out.println("Invalid argument.");
                            printUsage();
                            System.exit(1);
                        }
                    }
                }
            }
        } //finish parsing arguments
        if (read1.isEmpty()) {
            System.out.println("Please provide the read file.");
            printUsage();
            System.exit(1);
        }
        if (kallistoIndexFile.isEmpty() && refDbFile.isEmpty()) {
            System.out.println("Please provide the reference database or kallisto index file.");
            printUsage();
            System.exit(1);
        }
        if (virusListFile.isEmpty()) {
            virusListFile = "ncbi-viruses-list.txt";
        }
        if (outDir.isEmpty()) {
            outDir = Paths.get(".").toAbsolutePath().normalize().toString();
        }
        if (ratioCriteria < 0.0 || ratioCriteria > 1.0) {
            System.out.println("The ratio criteria should be between 0.0 and 1.0. "
                + "Using the default value: 0.3.");
            ratioCriteria = 0.3;
        }
        if (coverageCriteria < 0.0 || coverageCriteria > 1.0) {
            System.out.println("The coverage criteria should be between 0.0 and 1.0. "
                + "Using the default value: 0.1.");
            coverageCriteria = 0.1;
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("java -cp bin FastViromeExplorer -1 $read1File -2 $read2File -i $indexFile -o $outputDirectory");
        System.out.println("-1: input .fastq file for read sequences (paired-end 1), mandatory field.");
        System.out.println("-2: input .fastq file for read sequences (paired-end 2).");
        System.out.println("-i: kallisto index file, mandatory field.");
        System.out.println("-db: reference database file in fasta/fa format.");
        System.out.println("-o: output directory. Default option is the project directory.");
        System.out.println("-l: virus list containing "
            + "all viruses present in the reference database along with their length.");        
        System.out.println("-cr: the value of ratio criteria, default: 0.3.");
        System.out.println("-co: the value of coverage criteria, default: 0.1.");
        System.out.println("-cn: the value of number of reads criteria, default: 10.");
    }
    
    private static void callKallisto() {
        File f1 = new File(virusListFile);
        if(!f1.isFile()) { 
            System.out.println("Could not find the list of viruses (ncbi-viruses-list.txt) "
                + "in the project directory.");
            printUsage();
            System.exit(1);
        }
        try{
            String command = "";
            //index file is given
            if(!kallistoIndexFile.isEmpty()) {
                if (read2.isEmpty()) {
                    command = "kallisto quant -i " + kallistoIndexFile
                        + " -o " + outDir + " --single -l 200 -s 50 --pseudobam " 
                        + read1
                        + " | samtools view -bS - | samtools view -h -F 0x04 -b - | "
                        + "samtools sort - -o " + outDir + "/FastViromeExplorer-reads-mapped-sorted.sam"
                        + "\n" + "awk 'NR%4 == 2 {lenSum+=length($0); readCount++;} END {print lenSum/readCount}' "
                        + read1;
                }
                else {
                    command = "kallisto quant -i " + kallistoIndexFile
                        + " -o " + outDir + " --pseudobam " + read1 + " " + read2
                        + " | samtools view -bS - | samtools view -h -F 0x04 -b - | "
                        + "samtools sort - -o " + outDir + "/FastViromeExplorer-reads-mapped-sorted.sam"
                        + "\n" + "awk 'NR%4 == 2 {lenSum+=length($0); readCount++;} END {print lenSum/readCount}' "
                        + read1;
                }
            }
            else if (!refDbFile.isEmpty()) {
                if (read2.isEmpty()) {
                    command = "kallisto index -i kallisto-index.idx "
                        + refDbFile + "\n"
                        + "kallisto quant -i kallisto-index.idx "
                        + "-o " + outDir + " --single -l 200 -s 50 --pseudobam " 
                        + read1
                        + " | samtools view -bS - | samtools view -h -F 0x04 -b - | "
                        + "samtools sort - -o " + outDir + "/FastViromeExplorer-reads-mapped-sorted.sam"
                        + "\n" + "awk 'NR%4 == 2 {lenSum+=length($0); readCount++;} END {print lenSum/readCount}' "
                        + read1;
                }
                else {
                    command = "kallisto index -i kallisto-index.idx "
                        + refDbFile + "\n"
                        + "kallisto quant -i kallisto-index.idx "
                        + "-o " + outDir + " --pseudobam " + read1 + " " + read2
                        + " | samtools view -bS - | samtools view -h -F 0x04 -b - | "
                        + "samtools sort - -o " + outDir + "/FastViromeExplorer-reads-mapped-sorted.sam"
                        + "\n" + "awk 'NR%4 == 2 {lenSum+=length($0); readCount++;} END {print lenSum/readCount}' "
                        + read1;
                }
            }
            
            FileWriter shellFileWriter = new FileWriter("run.sh");
            shellFileWriter.write("#!/bin/bash\n");
            shellFileWriter.write(command);
            shellFileWriter.close();
            
            ProcessBuilder builder = new ProcessBuilder("sh","run.sh");
            builder.redirectError(new File("log.txt"));
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = "";
            while ((str = reader.readLine()) != null) {
                avgReadLen = Double.parseDouble(str);
            }
            process.waitFor();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        
        if (avgReadLen == 0) {
            System.out.println("Could not extract average read length from read file.");
            System.exit(1);
        }
    }
    
    private static void getVirusLength() {
        virusLength = new HashMap<String, Integer>();
        virusLineage = new HashMap<String, String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(virusListFile));
            String str = "";
            while ((str = br.readLine()) != null) {
                String[] results = str.split("\t");
                if (results.length == 4) {
                    int length = Integer.parseInt(results[3]);
                    virusLength.put(results[0].trim(), length);
                    virusLineage.put(results[0].trim(), results[1].trim() + "\t" + results[2].trim());
                }
                else {
                    int length = Integer.parseInt(results[1]);
                    virusLength.put(results[0].trim(), length);
                    virusLineage.put(results[0].trim(), "N/A\tN/A");
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }       
    }
    
    private static void getRatio() {
        virusRatio = new HashSet<String>();
        // read from sam
        TreeSet<Read> readSet = new TreeSet<Read>();
        int numReads = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(outDir + "/FastViromeExplorer-reads-mapped-sorted.sam"));
            String str = "";
            String prevVirusName = null;
            double ratio = 0.0;
            //read sam file
            while ((str = br.readLine()) != null) 
            {
                if (!str.startsWith("@")) {
                    String[] results = str.split("\t");
                    String virusName = results[2].trim();
                    int startPos = Integer.parseInt(results[3].trim());
                    int endPos = results[9].trim().length() + startPos - 1;
                    if (prevVirusName != null && !virusName.equals(prevVirusName)) 
                    {
                        // finish calculating ratio for prev virus
                        double coveredBps = 0.0;
                        double genomeLen = virusLength.get(prevVirusName);

                        for (int i = 1; i <= genomeLen; i++) {
                            Read tempRead = new Read(i, i+(2*(int)avgReadLen));
                            NavigableSet<Read> smallSet = readSet.headSet(tempRead, true);
                            Iterator<Read> it = smallSet.descendingIterator();
                            while(it.hasNext()) {
                                tempRead = it.next();
                                if (i >= tempRead.getStartPos() && i <= tempRead.getEndPos()) {
                                    coveredBps++;
                                    break;
                                }
                                if (tempRead.getEndPos()+2*avgReadLen < i) {
                                    break;
                                }
                            }
                        }
                        
                        double support = coveredBps / genomeLen;
                        double cov = (numReads * avgReadLen) / genomeLen;
                        double predictedSupport = 1 - Math.exp(-cov);
                        ratio = 0.0;
                        if (support < predictedSupport) {
                            ratio = support / predictedSupport;
                        }
                        else {
                            ratio = predictedSupport / support;
                        }
                        if (ratio >= ratioCriteria && support >= coverageCriteria) {
                            /*virusRatio.put(prevVirusName, support + "\t" + predictedSupport + "\t" 
                                + new DecimalFormat("##.####").format(ratio));*/
                            virusRatio.add(prevVirusName);
                        }
                        // create new readSet
                        readSet = new TreeSet<Read>();
                        numReads = 0;
                        Read read = new Read(startPos, endPos);
                        numReads++;
                        readSet.add(read);
                    }
                    else {
                        Read read = new Read(startPos, endPos);
                        numReads++;
                        readSet.add(read);
                    }
                    prevVirusName = virusName;
                }
            }
            //calculate ratio for the last virus
            double coveredBps = 0.0;
            double genomeLen = virusLength.get(prevVirusName);
            for (int i = 1; i <= genomeLen; i++) {
                Read tempRead = new Read(i, i+(2*(int)avgReadLen));
                NavigableSet<Read> smallSet = readSet.headSet(tempRead, true);
                Iterator<Read> it = smallSet.descendingIterator();
                while(it.hasNext()) {
                    tempRead = it.next();
                    if (i >= tempRead.getStartPos() && i <= tempRead.getEndPos()) {
                        coveredBps++;
                        break;
                    }
                    if (tempRead.getEndPos()+2*avgReadLen < i) {
                        break;
                    }
                }
            }
            
            double support = coveredBps / genomeLen;
            double cov = (numReads * avgReadLen) / genomeLen;
            double predictedSupport = 1 - Math.exp(-cov);
            ratio = 0.0;
            if (support < predictedSupport) {
                ratio = support / predictedSupport;
            }
            else {
                ratio = predictedSupport / support;
            }
            
            if (ratio >= ratioCriteria && support >= coverageCriteria) {
                /*virusRatio.put(prevVirusName, support + "\t" + predictedSupport + "\t" 
                        + new DecimalFormat("##.####").format(ratio));*/
                virusRatio.add(prevVirusName);
            }
            
            readSet = null;
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void getSortedAbundanceRatio() {
        Map<String, Double> map = new HashMap<String, Double>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(outDir + "/abundance.tsv"));
            br.readLine();
            String str = "";
            while ((str = br.readLine()) != null) {
                String[] results = str.split("\t");
                double est_count = Double.parseDouble(results[3]);
                if (est_count != 0.0) {
                    map.put(results[0], est_count);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        map = sortByComparator(map, DESC);

        // write in file
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(outDir + "/FastViromeExplorer-final-sorted-abundance.tsv"));
            
            //bw.write("#NCBIAccession\tName\tkingdom;phylum;class;order;family;genus;species\tEstimatedAbundance\tSupport\tPredictedSupport\tRatio\n");
            bw.write("#VirusIdentifier\tVirusName\tkingdom;phylum;class;order;family;genus;species\tEstimatedAbundance\n");
            for (Entry<String, Double> entry : map.entrySet()) {
                if (virusRatio.contains(entry.getKey()) 
                    && entry.getValue() >= numReadsCriteria) {
                    bw.write(entry.getKey() + "\t" + virusLineage.get(entry.getKey()) 
                    + "\t" + entry.getValue() + "\n");
                }
            }
            bw.close();
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        parseArguments(args);  
        callKallisto();
        getVirusLength();
        getRatio();
        getSortedAbundanceRatio();
    }
}