package eu.quelltext.mundraub.error;

import android.app.Activity;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Scanner;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.initialization.Initialization;

/*
    Copy STDOUT and STDERR to a file
 */
public class Logger implements UncaughtExceptionHandler, Initialization.ActivityInitialized {

    private static final int TAG_MAX_LEGTH = 23; // from https://stackoverflow.com/a/28168739/1320237
    private static final String TAG_DIVIDER = ": ";
    private static Logger logger; // initialize as soon as possible;
    private static final String PACKAGE_PATH = "/data/eu.quelltext.mundraub"; // package path
    private static final String LOG_FILE_NAME = "eu.quelltext.mundraub.log.txt";
    private static final String ERROR_FILE_NAME = "eu.quelltext.mundraub.error.txt";
    private static File logFile;
    private static Activity activity;
    private final UncaughtExceptionHandler defaultExceptionHandler;
    private final PrintStream logStream;
    private final String TAG = "LOGGER" + TAG_DIVIDER;

    static {
        // crate logger as soon as possible
        getInstance();
    }

    public static Logger getInstance() {
        if (logger == null) {
            logger = new Logger();
        }
        return logger;
    }

    private Logger() {
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        File outputFile = getLogFile();
        PrintStream log1;
        try {
            log1 = new PrintStream(new FileOutputStream(outputFile, false));
            System.setOut(log1);
            System.setErr(log1);
        } catch (FileNotFoundException e) {
            // this should not happen as we are using internal file when external is not accessed
            printStackTrace(TAG, e);
            d(TAG, "A FileNotFoundException usually happens when the user did not give permission to log the output to EXTERNAL_STORAGE. Nothing to worry about.");
            log1 = null;
        }

        logStream = log1;
        Initialization.provideActivityFor(this);
        i(TAG, "-------------- App started --------------");
    }

    private final static File getLogFile() {
        // Create logfile only once -> on start of application
        if (logFile != null)
            return logFile;

        // from https://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage#7887114
        String root = Environment.getExternalStorageDirectory().toString();

        File outputFile = new File(root, LOG_FILE_NAME);
        if (!outputFile.exists() || !outputFile.canWrite()) {
            // we should use file from internal storage
            // we delete it on every start and transmitting error report, so it is safe to leak
            String path = Environment.getDataDirectory().getAbsolutePath(); // this get us to "/data" directory
            path += PACKAGE_PATH; // append package path
            outputFile = new File(path, LOG_FILE_NAME);
            // delete existing file and create new because we need clear content and prevent leaks
            if (outputFile.exists())
                outputFile.delete();

            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logFile = outputFile;
        return outputFile;
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        if (logStream != null) {
            printStackTrace(TAG, e);
            if (Settings.class == null || Settings.useErrorReport()) {
                makeErrorReport();
            }
        }
        if (defaultExceptionHandler != null) {
            defaultExceptionHandler.uncaughtException(t, e);
        }
    }

    private void makeErrorReport() {
        File report = getErrorReport();
        if (report.exists()) {
            report.delete();
        }
        getLogFile().renameTo(report);
    }

    private final static File getErrorReport() {
        return new File(getLogFile().getParentFile(), ERROR_FILE_NAME);
    }

    public static Log newFor(Loggable loggable) {
        return new Log(getInstance(), loggable.getTag());
    }

    public static Log newFor(String tag) {
        return new Log(getInstance(), tag);
    }

    public static Log newFor(Object o) {
        return new Log(getInstance(), o.getClass().getSimpleName());
    }

    @Override
    public void setActivity(Activity newActivity) {
        if (!hasContext()) {
            activity = newActivity;
            checkErrorReport();
        }
    }

    private void checkErrorReport() {
        if (hasErrorReport()) {
            String messageTemplate = activity.getResources().getString(R.string.error_app_crashed);
            String message = String.format(messageTemplate, getErrorReport().getAbsolutePath());
            new Dialog(activity).askYesNo(message, R.string.ask_error_report_is_needed, new Dialog.YesNoCallback() {
                @Override
                public void yes() {
                    getErrorReport().delete();
                }
                @Override
                public void no() {
                    getErrorReport().delete();
                }
            });
        }
    }

    private static boolean hasErrorReport() {
        return getErrorReport().exists();
    }

    private static boolean hasContext() {
        return activity != null;
    }

    public interface Loggable {
        String getTag();
    }

    public static class Log {

        private final String tag;

        private Log(Logger logger, String tag) {
            this.tag = tag.substring(0, tag.length() < TAG_MAX_LEGTH? tag.length() : TAG_MAX_LEGTH);
            //d("LOG", "INITIALIZED");
        }

        public void d(String tag, String s) {
            logger.d(this.tag, tag + TAG_DIVIDER + s);
        }
        public void d(String tag, boolean b) {
            logger.d(this.tag, tag + TAG_DIVIDER + Boolean.toString(b));
        }
        public void d(String tag, int i) {
            logger.d(this.tag, tag + TAG_DIVIDER + Integer.toString(i));
        }
        public void d(String tag, double d) {
            logger.d(this.tag, tag + TAG_DIVIDER + Double.toString(d));
        }

        public void e(String tag, String s) {
            logger.e(this.tag, tag + TAG_DIVIDER + s);
        }

        public void i(String tag, String s) {
            logger.i(this.tag, tag + TAG_DIVIDER + s);
        }

        public void printStackTrace(Exception e) {
            logger.printStackTrace(tag, e);
        }

        public void secure(String tag, String secret) {
            logger.secure(this.tag, tag, secret);
        }
    }

    private void printStackTrace(String tag, Throwable e) {
        e.printStackTrace();
        if (logStream != null) {
            logStream.print(tag);
            e.printStackTrace(logStream);
            logStream.flush();
        }
    }

    private void d(String tag, String s) {
        android.util.Log.d(tag, s);
        print("DEBUG" + TAG_DIVIDER + tag, s);
    }

    private void e(String tag, String s) {
        android.util.Log.d(tag, s);
        print("ERROR" + TAG_DIVIDER + tag, s);
    }

    private void i(String tag, String s) {
        android.util.Log.i(tag, s);
        print("INFO" + TAG_DIVIDER + tag, s);
    }

    private void secure(String tag1, String tag2, String secret) {
        android.util.Log.i(tag1, tag2 + TAG_DIVIDER + secret);
        print("SECURE" + TAG_DIVIDER + tag1 + TAG_DIVIDER + tag2 + TAG_DIVIDER, fillString(secret.length(), "*"));
    }


    private void print(String tag, String s) {
        if ( logStream == null) {
            return;
        }
        Scanner scanner = new Scanner(s);
        tag = tag + ": ";
        String spaces = fillString(tag.length(), " ");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            logStream.print(tag);
            logStream.print(line + "\n");
            tag = spaces;
        }
        scanner.close();
        logStream.flush();
    }

    private String fillString(int length, String character) {
        return new String(new char[length]).replace("\0", character); // from https://stackoverflow.com/a/16812721/1320237;
    }
}
