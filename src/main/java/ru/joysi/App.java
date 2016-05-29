package ru.joysi;

import ru.joysi.audio.GlobalHelper;
import ru.joysi.audio.file.WavFile;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println(GlobalHelper.samplePerSec);
        WavFile.testSave(440);
    }
}
