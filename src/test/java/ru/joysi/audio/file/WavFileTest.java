package ru.joysi.audio.file;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
/**
 * Created by 886 on 24.05.2016.
 */
public class WavFileTest {
    @Test
    public void saveMonoToFile() throws Exception {
        Assert.assertFalse(WavFile.saveMonoToFile(
                Files.createTempFile("tmp.wav", null), new double[]{ 2, 2}));
    }

    @Test
    public void saveStereoToFile() throws Exception {
        Assert.assertFalse(WavFile.saveStereoToFile(
                Files.createTempFile("tmp.wav", null), new double[]{ 2, 2}, new double[]{2,2,2}));
    }

}