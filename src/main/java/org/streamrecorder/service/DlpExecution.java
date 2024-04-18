package org.streamrecorder.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.*;

@Component
public class DlpExecution {

    @Value("${ytdlp.location}")
    protected String YT_DLP_LOCATION;
    @Value("${ytdlp.output.location}")
    private String VOD_OUT_LOCATION;

    @PostConstruct
    private void setHomeFolderLocation(){
        YT_DLP_LOCATION=YT_DLP_LOCATION.replace("%userprofile%", System.getenv("USERPROFILE"));
        VOD_OUT_LOCATION=VOD_OUT_LOCATION.replace("%userprofile%", System.getenv("USERPROFILE"));
    }

    public void execute(String streamLink, String ytDlpLocation, String vodOutLocation) {

        File ytDlpOld = new File(YT_DLP_LOCATION);
        if(!ytDlpOld.exists() && ytDlpLocation==null)
            throw new RuntimeException("Please enter optional argument for ytdlp.exe location or place the executable in the default location");
        if(ytDlpLocation != null){
            File ytDlpNew = new File(ytDlpLocation);
            if(ytDlpNew.exists()) YT_DLP_LOCATION=ytDlpNew.getAbsolutePath();
            if(!ytDlpOld.exists() && !ytDlpNew.exists())
                throw new RuntimeException("Please enter optional argument for ytdlp.exe location or place the executable on the default location");
        }

        File vodOutLocationOld = new File(VOD_OUT_LOCATION);
        if(vodOutLocation!=null) {
            File vodOutLocationNew = new File(vodOutLocation);
            if(vodOutLocationNew.exists()) VOD_OUT_LOCATION=vodOutLocationNew.getAbsolutePath();
            else vodOutLocationNew.mkdirs();
        }
        else if(!vodOutLocationOld.exists()) vodOutLocationOld.mkdirs();

        ProcessBuilder pb = new ProcessBuilder(
                YT_DLP_LOCATION,
                "--wait-for-video", "60",
                streamLink);
        pb.directory(new File(VOD_OUT_LOCATION));

        // redirect yt-dlp child processes stdout/stderr to main so they can be captured
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        while(true){
            try {
                Thread.sleep(1000*60);
                Process p = pb.start();
                p.getInputStream().transferTo(System.out);
                p.getErrorStream().transferTo(System.out);

                int exitValue = p.waitFor(); // Wait for the process to finish (stream has ended or an error occurred)
                if (exitValue == 0) { // successfully finished
                    System.out.printf("YT-DLP successfully exited for stream %s\n", streamLink);
                }
                else {
                    System.out.printf("YT-DLP failed for stream %s\n", streamLink);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static final String NEWLINE = System.lineSeparator();

    /**
     * @param inputStream InputStream of the process in question
     * @return the output of the command
     * @throws IOException if an I/O error occurs
     */
    public static String getProcessOutput(InputStream inputStream) throws IOException
    {
        StringBuilder result = new StringBuilder(80);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream)))
        {
            while (true)
            {
                String line = in.readLine();
                System.out.println(line);
                if (line == null){
                    break;
                }
                result.append(line).append(NEWLINE);
            }
        }
        return result.toString();
    }
}
