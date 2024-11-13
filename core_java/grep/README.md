# Introduction
The Java Grep App is designed to mimic the functionality of the Linux Grep command by letting users search for specific patterns within files. Given a regex pattern, a directory path, and a specified output file path, the app searches all files in the given directory, creates a list of lines that match the given regex pattern, and writes all matched lines to the provided output file. The Java Grep App implements 2 different approaches. The first approach uses standard file I/O and for loops, which is sufficient for smaller files. The second approach uses Lambda & Stream APIs to increase efficiency when handling larger datasets, requiring much less memory to function properly. The app utilizes core Java libraries for file I/O, SLF4J for logging, Stream API for more efficient memory usage, Maven for dependency management and build automation, and is deployed using Docker.


# Quick Start
## Package the app with Maven:
```
mvn clean compile package
```
## Run the app:
```
java -Xms{min-memory}m -Xmx{max-memory}m -cp target/grep-1.0-SNAPSHOT.jar ca.jrvs.apps.grep.JavaGrepImp {regex-pattern} {root-directory-path} {output-file-path}
```


## Pseudocode
`process` method pseudocode:
```
matchedLines = []
for file in listFilesRecursively(rootDir)
  for line in readLines(file)
      if containsPattern(line)
        matchedLines.add(line)
writeToFile(matchedLines)
```

## Performance Issue
When trying to run the app with a memory limit of 5mb with the following command:
```
java -Xms5m -Xmx5m -cp target/grep-1.0-SNAPSHOT.jar ca.jrvs.apps.grep.JavaGrepImp .*Romeo.*Juliet.* ./data ./out/grep.txt
```
The app fails to execute correctly, throwing an `OutOfMemoryError`. This is due to the standard approach of using core Java libraries for file I/O and for loops to iterate through lines in files not being very memory efficient, which causes problems when dealing with larger files. Utilizing Lambda and the Stream API for more efficient file handling fixed this issue by loading the data in chunks instead of loading it all at once, reducing the required memory to run the application by over 66.67%. 

# Test
The application was manually tested using sample text files and running the application several times with different regex patterns. SLF4J was used for logging. Each test was compared with the standard Linux grep command (`egrep -r {regex} {rootPath} > {outFile}`) to ensure the app was producing the correct output.  

# Deployment
The app is Dockerized for easier distribution. A Dockerfile was created to make a new Docker image for the Java Grep app, which can be built and run using the following commands:
## build a new docker image locally
`docker build -t ${docker_user}/grep .`
## run docker container 
`docker run --rm \
-v `pwd`/data:/data -v `pwd`/log:/log \
${docker_user}/grep .*Romeo.*Juliet.* /data /log/grep.out`


# Improvement
- implement automated unit tests and integration tests to make the testing process repeatable and reliable. Can use JUnit for testing.
- expand the app to handle different file types (pdf, docx, csv, json, etc)
- add more in depth logging / error handling
