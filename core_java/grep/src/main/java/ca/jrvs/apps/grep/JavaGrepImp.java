package ca.jrvs.apps.grep;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaGrepImp implements JavaGrep {

    final Logger logger = LoggerFactory.getLogger(JavaGrep.class);

    private String regex;
    private String rootPath;
    private String outFile;


    @Override
    public String getRegex() {
        return regex;
    }

    @Override
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String getRootPath() {
        return rootPath;
    }

    @Override
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public String getOutFile() {
        return outFile;
    }

    @Override
    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }

    @Override
    public void process() throws IOException {
        logger.info("Processing with regex: {}, rootPath: {}, outFile: {}", regex, rootPath, outFile);

        // get list of files, create empty list of matched lines
        List<File> files = listFiles(rootPath);
        List<String> matchedLines = new ArrayList<>();

        // iterate through each file and search for given pattern
        for (File file : files) {
            List<String> lines = readLines(file);
            for (String line : lines) {
                if (containsPattern(line)) {
                    matchedLines.add(line);
                }
            }
        }

        // write all matched lines to output file
        writeToFile(matchedLines);
    }

    @Override
    public List<File> listFiles(String rootDir) {
        List<File> files = new ArrayList<>();
        File root = new File(rootDir);
        File[] fileList = root.listFiles();

        if (!root.exists()) {
            logger.error("Root directory does not exist: {}", rootDir);
            return files;
        }

        // add files from fileList to files
        if (fileList != null) {
            for (File file : fileList) {
                if(file.isFile()){
                    files.add(file);
                }
            }
        }

        return files;
    }

    @Override
    public List<String> readLines(File inputFile) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            logger.error("Error reading file: {}", inputFile.getAbsolutePath(), e);
        }

        return lines;
    }

    @Override
    public boolean containsPattern(String line) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    @Override
    public void writeToFile(List<String> lines) throws IOException {
        File outputFile = new File(getOutFile());

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(outputFile))) {

            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error("Error writing to file: {}", outFile, e);
            throw e;
        }
    }

    public static void main(String[] args) {
        if (args.length != 3){
            throw new IllegalArgumentException("USAGE: JavaGrep regex rootPath outFile");
        }

        //Use default logger config
        BasicConfigurator.configure();

        JavaGrepImp javaGrepImp = new JavaGrepImp();
        javaGrepImp.setRegex(args[0]);
        javaGrepImp.setRootPath(args[1]);
        javaGrepImp.setOutFile(args[2]);

        try {
            javaGrepImp.process();
        } catch (Exception ex){
            javaGrepImp.logger.error("Error: Unable to process", ex);
        }
    }
}
