package org.streamrecorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.streamrecorder.service.DlpExecution;

@SpringBootApplication
public class StreamRecorder {

    public static void main(String[] args) {
        ApplicationContext appContext = SpringApplication.run(StreamRecorder.class, args);
        if(args.length > 0) {
            String arg1 = args[0];
            String arg2 = args.length>1 ? args[1] : null;
            String arg3 =  args.length>2 ? args[2] : null;

            appContext.getBean(DlpExecution.class).execute(arg1, arg2, arg3);
        }
        else System.out.println("""
                Please enter the following arguments:\s
                Argument 1 (required): The stream url
                Argument 2 (optional): Ytdlp.exe location, default location is: %userprofile%\\StreamRecorder\\yt-dlp.exe
                Argument 3 (optional): Ytdlp.exe output folder location, default location is %userprofile%\\StreamRecorder\\out
                """);
    }
}
