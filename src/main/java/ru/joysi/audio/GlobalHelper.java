package ru.joysi.audio;

import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Инициализация глобальных переменных из property
 * @author MaratSR
 */

public class GlobalHelper {
    public static final int samplePerSec;
    public static final int bitsPerSample;
    public static final int sampleMsTime;
    public static final Properties props;

    static {
        int tmpSampleRate = 22050;
        int tmpBitsPerSample = 16;
        int tmpSampleMsTime = 1000;
        boolean flError = true;
        Properties properties = new Properties();
        Locale.setDefault(Locale.ENGLISH);
        try {
            FileInputStream fis = new FileInputStream("src/resources/config.properties");
            properties.load(fis);
            tmpSampleMsTime = Integer.parseInt(properties.getProperty("audio.sampleMsTime"));
            tmpSampleRate   = Integer.parseInt(properties.getProperty("audio.samplePerSec"));
            tmpBitsPerSample = Integer.parseInt(properties.getProperty("audio.bitsPerSample"));

            fis.close();
            flError = false;
        } catch (Exception e) {
            //TODO FileNotFoundException or IOException or NumberFormatException - залогировать сообщение об ошибке
            e.printStackTrace();
        } finally {
            props = properties;
            samplePerSec = tmpSampleRate;
            bitsPerSample = tmpBitsPerSample;
            sampleMsTime  = tmpSampleMsTime;
        }
        if (flError) System.exit(0);
    }
}
