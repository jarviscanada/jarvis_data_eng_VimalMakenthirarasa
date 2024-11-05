package ca.jrvs.apps.practice;

public class RegexExcImp implements RegexExc {

    @Override
    public boolean matchJpeg(String filename) {
        // Match file extension .jpg or .jpeg (case insensitive)
        return filename.toLowerCase().matches(".*\\.(jpg|jpeg)$");
    }

    @Override
    public boolean matchIp(String ip) {
        // Match IP in the range 0.0.0.0 to 999.999.999.999
        return ip.matches("\\b([0-9]{1,3}\\.){3}[0-9]{1,3}\\b");
    }

    @Override
    public boolean isEmptyLine(String line) {
        // Match an empty line, including spaces, tabs, etc.
        return line.trim().isEmpty();
    }
}
