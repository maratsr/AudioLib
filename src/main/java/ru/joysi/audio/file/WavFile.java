package ru.joysi.audio.file;

import org.apache.commons.io.FilenameUtils;
import ru.joysi.audio.ExceptionHandler;
import ru.joysi.audio.GlobalHelper;
import ru.joysi.audio.processing.combines.Combines;
import ru.joysi.audio.processing.filters.Filters;
import ru.joysi.audio.processing.generators.Generators;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InputMismatchException;


/**
 *  Класс для работы с Wav-файлами, содержит только static методы
 *  @author MaratSR
 */
public class WavFile {
    /**
     * Записать аудиомассив (16бит, моно) в Wav-файл
     * @param path Путь к файлу
     * @param audio Массив 16 битных аудиоданных
     * @return true - если запись произведена, false - иначе
     */
    public static boolean saveMonoToFile(Path path, double[] audio) {
        if (checkAndDeleteWavFile(path)) return false;
        try(DataOutputStream wavFile = new DataOutputStream(new FileOutputStream(path.toString()))) {

            short audioData[] = new short[audio.length];
            for (int i = 0; i < audio.length; i++)
                audioData[i] = (short) (audio[i] *Short.MAX_VALUE);

            saveWavFileHeader(wavFile, 1, audioData.length);
            // offset 44: Data - собственно массив аудио (приведенный из short к byte)
            wavFile.write(convertShortToByteArray(audioData));
        } catch (IOException e) {
            ExceptionHandler.log(e, GlobalHelper.props.getProperty("error.wav.FileSave"));
            return false;
        }
        return true;
    }

    /**
     * Записать аудиомассивы (16бит, стерео) в Wav-файл
     * @param path Путь к файлу
     * @param audioRightChannel Массив 16 битных аудиоданных (правый канал)
     * @param audioLeftChannel Массив 16 битных аудиоданных (левый канал)
     * @return true - если запись произведена, false - иначе
     */
    public static boolean saveStereoToFile(Path path, double[] audioRightChannel,double[] audioLeftChannel ) {
        if (audioLeftChannel.length != audioRightChannel.length) {
            ExceptionHandler.log(new InputMismatchException(),GlobalHelper.props.getProperty("error.wav.stereoSave"));
            return false;
        }

        if (checkAndDeleteWavFile(path)) return false;

        try(DataOutputStream wavFile = new DataOutputStream(new FileOutputStream(path.toString()))) {

            short audioDataRight[] = new short[audioRightChannel.length];
            short audioDataLeft[] = new short[audioLeftChannel.length];
            for (int i = 0; i < audioDataLeft.length; i++) {
                audioDataRight[i] = (short) (audioRightChannel[i] * Short.MAX_VALUE);
                audioDataLeft[i]  = (short) (audioLeftChannel[i]  * Short.MAX_VALUE);
            }

            saveWavFileHeader(wavFile, 2, audioLeftChannel.length);
            // offset 44: Data - собственно массив аудио (приведенный из short к byte) 2-х канальный
            ByteBuffer byteBuf = ByteBuffer.allocate(4*audioRightChannel.length);
            for (int i = 0; i < audioRightChannel.length; i++) {
                byteBuf.putShort(Short.reverseBytes(audioDataLeft[i]));
                byteBuf.putShort(Short.reverseBytes(audioDataRight[i]));
            }
            wavFile.write(byteBuf.array());
        } catch (IOException e) {
            ExceptionHandler.log(e, GlobalHelper.props.getProperty("error.wav.FileSave"));
            return false;
        }
        return true;
    }

    /**
     * Запуск монофонического проигрывания аудиомассива
     * @param audio аудиомассив
     */
    public static void playMonoAudio(double[] audio) {
        Runnable playAudioTask = () -> {
            try {
                short[] audioMono = new short[audio.length];
                for (int i = 0; i < audio.length; i++)
                    audioMono[i] = (short) (audio[i] *Short.MAX_VALUE);

                byte audioData[] = convertShortToByteArray(audioMono);
                byte tempBuffer[] = new byte[Short.MAX_VALUE+1];

                InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
                AudioFormat audioFormat = new AudioFormat(GlobalHelper.samplePerSec, GlobalHelper.bitsPerSample, 1, true, false);
                AudioInputStream audioInputStream = new AudioInputStream(
                        byteArrayInputStream, audioFormat, audioData.length / audioFormat.getFrameSize());
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

                SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                sourceDataLine.open(audioFormat);
                sourceDataLine.start();

                int cnt;
                while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1)
                    if (cnt > 0)
                        sourceDataLine.write(tempBuffer, 0, cnt);
                sourceDataLine.drain();
                sourceDataLine.close();
            } catch(Exception e) {
                ExceptionHandler.log(e, GlobalHelper.props.getProperty("error.wav.playAudio"));
            }
        };
        new Thread(playAudioTask).start();
    }

    /**
     * Запуск стереофонического проигрывания аудиомассива
     * @param audioDataRightChannel аудиомассив для правого канала
     * @param audioDataLeftChannel аудиомассив для левого канала
     */
    public static void playStereoAudio(double[] audioDataRightChannel,double[] audioDataLeftChannel) {
        Runnable playAudioTask = () -> {
            try {
                short audioDataRight[] = new short[audioDataRightChannel.length];
                short audioDataLeft[] = new short[audioDataLeftChannel.length];
                for (int i = 0; i < audioDataLeftChannel.length; i++) {
                    audioDataRight[i] = (short) (audioDataRightChannel[i] * Short.MAX_VALUE);
                    audioDataLeft[i]  = (short) (audioDataLeftChannel[i]  * Short.MAX_VALUE);
                }

                ByteBuffer byteBuf = ByteBuffer.allocate(4*audioDataRightChannel.length);
                for (int i = 0; i < audioDataRightChannel.length; i++) {
                    byteBuf.putShort(Short.reverseBytes(audioDataLeft[i]));
                    byteBuf.putShort(Short.reverseBytes(audioDataRight[i]));
                }
                byte audioData[] = byteBuf.array();
                byte tempBuffer[] = new byte[Short.MAX_VALUE+1];

                InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
                AudioFormat audioFormat = new AudioFormat(GlobalHelper.samplePerSec, GlobalHelper.bitsPerSample, 1, true, false);
                AudioInputStream audioInputStream = new AudioInputStream(
                        byteArrayInputStream, audioFormat, audioData.length / audioFormat.getFrameSize());
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

                SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                sourceDataLine.open(audioFormat);
                sourceDataLine.start();

                int cnt;
                while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1)
                    if (cnt > 0)
                        sourceDataLine.write(tempBuffer, 0, cnt);
                sourceDataLine.drain();
                sourceDataLine.close();
            } catch(Exception e) {
                ExceptionHandler.log(e, GlobalHelper.props.getProperty("error.wav.playAudio"));
            }
        };
        new Thread(playAudioTask).start();
    }

    /**
     * Проверка возможности перезаписи файла и отсутствия одноименной директории
     * @param path путь до файла
     * @return true - если путь корректен, false - иначе
     */
    private static boolean checkAndDeleteWavFile(Path path) {
        if (!"wav".equals(FilenameUtils.getExtension(path.getFileName().toString())))
            return true;
        try {
            if (Files.isDirectory(path))
                return true;
            Files.deleteIfExists(path);
        } catch (IOException e) {
            ExceptionHandler.log(e, GlobalHelper.props.getProperty("error.wav.fileSave"));
            return true;
        }
        return false;
    }

    /**
     * Запись заголовка Wav-файла
     * @param output поток для вывода файла
     * @param numChannels количество каналов (1-моно, 2 - стерео)
     * @param audioLength длина массива аудиоданных
     * @throws IOException
     */
    private static void saveWavFileHeader(DataOutputStream output, int numChannels, int audioLength) throws IOException {
        int shift = 4 - numChannels;
        output.writeInt(Integer.reverseBytes(0x46464952));                      // offset 00: ChinkId ="RIFF"
        output.writeInt(Integer.reverseBytes(36+audioLength*numChannels*2));    // offset 04: ChunkSize длину файла (с учетом заголовка)
        output.writeInt(Integer.reverseBytes(0x45564157));                      // offset 08: Format = "WAVE"
        output.writeInt(Integer.reverseBytes(0x20746D66));                      // offset 12: Subchunk1Id = "frm"
        output.writeInt(Integer.reverseBytes(16));                              // offset 16: Subchunk1Size = 16
        output.writeShort(Short.reverseBytes((short) 1));                       // offset 20: AudioFormat = 1 (PCM)
        output.writeShort(Short.reverseBytes((short) numChannels));             // offset 22: NumChannels = 2 (Stereo) - кол-во дорожек
        output.writeInt(Integer.reverseBytes(GlobalHelper.samplePerSec));       // offset 24: SampleRate (частота дискретизации)
        output.writeInt(Integer.reverseBytes(GlobalHelper.samplePerSec  *       // offset 28: ByteRate (частота дискретизации,
                GlobalHelper.bitsPerSample >>> shift));                         // приведенная к байтам)
        output.writeShort(Short.reverseBytes((short) (GlobalHelper.bitsPerSample >>> shift)));  // offset 32: FrameSize = 16 / 8
        output.writeShort(Short.reverseBytes((short) GlobalHelper.bitsPerSample));              // offset 34: BlockAlign
        output.writeInt(Integer.reverseBytes(0x61746164));                      // offset 36: Subchunk2Id   = "DATA" (заголовок)
        output.writeInt(Integer.reverseBytes(audioLength*2*numChannels));       // offset 40: Subchunk2Size = размер массива данных в байтах
    }

    /**
     * Преобразование массива short[] в byte[]
     * @param data исходный массив
     * @return байтовый массив (длина которого в 2 раза больше)
     */
    private static byte[] convertShortToByteArray(short[] data) {
        ByteBuffer byteBuf = ByteBuffer.allocate(2*data.length);
        for (short s : data)
            byteBuf.putShort(Short.reverseBytes(s));
        return byteBuf.array();
    }

    public static void testSave(int freq) {

        double[] c4=Generators.sineGenerator(Generators.notes.get("C4"), 1000, 0.2);
        double[] e4=Generators.sineGenerator(Generators.notes.get("E4"), 1000, 0.2);
        double[] g4=Generators.sineGenerator(Generators.notes.get("G4"), 1000, 0.2);

        double[] data  = Combines.combineWithLnDynaRangeCompression( 0.8,
                c4, e4, g4);

        double[] data2  = Combines.combineWithNormalize(
                c4, e4, g4);

//        double[] data2  = Combines.combineWithNormalize(
//                Generators.sineGenerator(Generators.notes.get("A1"), 1000),
//                Generators.rectangleGenerator(Generators.notes.get("C4"), 1000,1),
//                Generators.sineGenerator(Generators.notes.get("E4"), 1000,1));

        double[] data3  = Combines.combineWithNormalize(
                Generators.sineGenerator(Generators.notes.get("F1"), 1000),
                Generators.sineGenerator(Generators.notes.get("A3"), 1000,0.5),
                Generators.sineGenerator(Generators.notes.get("C4"), 1000,0.5));

        double[] data4  = Combines.combineWithNormalize(
                Generators.sineGenerator(Generators.notes.get("G1"), 1000),
                Generators.sineGenerator(Generators.notes.get("B3"), 1000,0.5),
                Generators.sineGenerator(Generators.notes.get("D4"), 1000,0.5));


        double[] asdr = Filters.getASDRarray(150, 1000, 0.1);
        for (int i = 0; i < data.length; i++) data[i] =  (data[i]* asdr[i]);
        for (int i = 0; i < data.length; i++) data2[i] = (data2[i]* asdr[i]);
        for (int i = 0; i < data.length; i++) data3[i] = (data3[i]* asdr[i]);
        for (int i = 0; i < data.length; i++) data4[i] = (data4[i]* asdr[i]);

        //saveMonoToFile(Paths.get("c:/javalib/out/sineaccord.wav"), Combines.getSequenceByAdd(data, data2, data3, data4));
        playMonoAudio( Combines.getSequenceByAdd(data, data2, data3, data4, data, data2, data3, data4));

        double[] datax= Generators.sineGenerator( 110, 1000, 1);
//        short[] data3= Generators.triangleGenerator( freq/2, 10000, 1);
//        short[] data4= Generators.rectangleGenerator( freq/2, 10000, 1);
//        short[] data5= Generators.randomGenerator(10000, 1);
        saveMonoToFile(Paths.get("c:/java/out/1.wav"), datax);
        saveStereoToFile(Paths.get("c:/java/out/2.wav"), data, data2);
        //saveMonoToFile(Paths.get("c:/java/out/2.wav"), data2);
//        saveMonoToFile(Paths.get("c:/javalib/out/sawmono.wav"), data2);
//        saveMonoToFile(Paths.get("c:/javalib/out/trianglemono.wav"), data3);
//        saveMonoToFile(Paths.get("c:/javalib/out/rectanglemono.wav"), data4);
//        saveMonoToFile(Paths.get("c:/javalib/out/randomwave.wav"), data5);
//        saveStereoToFile(Paths.get("c:/javalib/out/3.wav"), data2, data3);
//        saveStereoToFile(Paths.get("c:/javalib/out/2.wav"), data, data2);
//        saveStereoToFile(Paths.get("c:/javalib/out/4.wav"), data, data4);
    }

}
