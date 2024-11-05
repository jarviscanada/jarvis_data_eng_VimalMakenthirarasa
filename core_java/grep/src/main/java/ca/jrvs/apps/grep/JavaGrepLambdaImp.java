package ca.jrvs.apps.grep;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaGrepLambdaImp implements JavaGrepLambda {

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

        // get stream of files and matched lines
        writeToFile(
                listFiles(rootPath)
                        .flatMap(this::readLines)
                        .filter(this::containsPattern)
        );
    }

    @Override
    public Stream<File> listFiles(String rootDir) {
        File root = new File(rootDir);
        if (!root.exists()) {
            logger.error("Root directory does not exist: {}", rootDir);
            return Stream.empty();
        }

        File[] fileList = root.listFiles();
        if (fileList == null) {
            return Stream.empty();
        }

        return Arrays.stream(fileList)
                .filter(File::isFile);
    }

    @Override
    public Stream<String> readLines(File inputFile) {
        try {
            return Files.lines(inputFile.toPath());
        } catch (IOException e) {
            logger.error("Error reading file: {}", inputFile.getAbsolutePath(), e);
            return Stream.empty();
        }
    }

    @Override
    public boolean containsPattern(String line) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    @Override
    public void writeToFile(Stream<String> lines) throws IOException {
        File outputFile = new File(getOutFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            lines.forEach(line -> {
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            logger.error("Error writing to file: {}", outFile, e.getCause());
            throw e.getCause();
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
