package ru.joysi.audio.processing.generators;

import ru.joysi.audio.GlobalHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Генераторы звука, реализованные в виде static функций
 * @author MaratSR
 */
public final class Generators {
    public static final double[] musNotesFrequency; // значение частот нот ( от `до` субконтр до `си` 5-й)
    public static final Map<String,Double> notes;

    static { // Вычисляем частоту музыкальных нот
        musNotesFrequency = new double[9*12];
        for (int i = 0; i < 108; i++) {
            musNotesFrequency[i] = 440.0 * Math.pow(2, (i-57)/12.0);
        }
        Map<String,Double> notesF = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            notesF.put("C"  + i, musNotesFrequency[i*12]);
            notesF.put("C#" + i, musNotesFrequency[i*12+1]);
            notesF.put("Db" + i, musNotesFrequency[i*12+1]);
            notesF.put("D"  + i, musNotesFrequency[i*12+2]);
            notesF.put("D#" + i, musNotesFrequency[i*12+3]);
            notesF.put("Eb" + i, musNotesFrequency[i*12+3]);
            notesF.put("E"  + i, musNotesFrequency[i*12+4]);
            notesF.put("F"  + i, musNotesFrequency[i*12+5]);
            notesF.put("F#" + i, musNotesFrequency[i*12+6]);
            notesF.put("Gb" + i, musNotesFrequency[i*12+6]);
            notesF.put("G"  + i, musNotesFrequency[i*12+7]);
            notesF.put("G#" + i, musNotesFrequency[i*12+8]);
            notesF.put("Ab" + i, musNotesFrequency[i*12+8]);
            notesF.put("A"  + i, musNotesFrequency[i*12+9]);
            notesF.put("A#" + i, musNotesFrequency[i*12+10]);
            notesF.put("Hb" + i, musNotesFrequency[i*12+10]);
            notesF.put("H"  + i, musNotesFrequency[i*12+11]);
            notesF.put("B"  + i, musNotesFrequency[i*12+11]);
    }
        notes = Collections.unmodifiableMap(notesF);
    }

    /**
     * Генератор синусоидальной волны
     * @param freq частота
     * @param time продолжительность в мсек
     * @param amplitude амплитуда ( 0...1.0)
     * @return семпл
     */
    public static double[] sineGenerator(double freq, int time, double amplitude) {

        if (amplitude<=0 || amplitude >1)
            return null;

        int freqSampling = GlobalHelper.samplePerSec;
        double[] data = new double[(GlobalHelper.bitsPerSample * freqSampling * (time / 1000)) >>> 4];

        // Переменные для избежания повторного вычисления в цикле
        double piAngle = 2.0 * freq * Math.PI / freqSampling;
        int rndOffset = (int) (freqSampling * 0.5 * Math.random() / freq); // Начальное смещение фазы

        for (int i = 0; i < data.length; i++)
            data[i] =  (amplitude * Math.sin(piAngle * (i+rndOffset)));
        return data;
    }

    /**
     * Генератор синусоидальной волны максимальной амплитуды
     * @param freq частота
     * @param time продолжительность в мсек
     * @return семпл
     */
    public static double[] sineGenerator(double freq, int time) {
        return sineGenerator(freq, time, 1);
    }

    /**
     * Генератор синусоидальной волны максимальной амплитуды стандартной продолжительности
     * @param freq частота
     * @return семпл
     */
    public static double[] sineGenerator(double freq) {
        return sineGenerator(freq, GlobalHelper.sampleMsTime, 1);
    }

    /**
     * Генератор пилообразной волны
     * @param freq частота
     * @param time продолжительность в мсек
     * @param amplitude амплитуда ( 0...1.0)
     * @return семпл
     */
    public static double[] sawGenerator(double freq, int time, double amplitude) {
        if (amplitude<0 || amplitude >1)
            return null;

        int freqSampling = GlobalHelper.samplePerSec;
        double[] data = new double[(GlobalHelper.bitsPerSample * freqSampling * (time /1000)) >>> 4];
        // Переменные для избежания повторного вычисления в цикле
        double doubleDia = (freqSampling*1.0/freq);
        int intDia = (int) (freqSampling/freq);
        int rndOffset = (int) (freqSampling * 0.5 * Math.random() / freq); // Начальное смещение фазы

        for (int i = 0; i < data.length; i++)
            data[i] =  (amplitude * (2 * ((i+rndOffset) % intDia)/doubleDia - 1));
        return data;
    }

    /**
     * Генератор пилообразной волны
     * @param freq частота
     * @param time продолжительность в мсек
     * @return семпл
     */
    public static double[] sawGenerator(double freq, int time) {
        return sawGenerator(freq, time, 1);
    }

    /**
     * Генератор пилообразной волны
     * @param freq частота
     * @return семпл
     */
    public static double[] sawGenerator(double freq) {
        return sawGenerator(freq, GlobalHelper.sampleMsTime, 1);
    }

    /**
     * Генератор треугольной волны
     * @param freq частота
     * @param time продолжительность в мсек
     * @param amplitude амплитуда ( 0...1.0)
     * @return семпл
     */
    public static double[] triangleGenerator(double freq, int time, double amplitude) {
        if (amplitude<0 || amplitude >1)
            return null;

        int freqSampling = GlobalHelper.samplePerSec;
        double[] data = new double[(GlobalHelper.bitsPerSample * freqSampling * (time /1000)) >>> 4];

        // Переменные для избежания повторного вычисления в цикле
        double twiceFreq = freqSampling/freq/2;
        int rndOffset = (int) (freqSampling * 0.5 * Math.random() / freq); // Начальное смещение фазы

        for (int i = 0; i < data.length; i++)
            data[i] = (amplitude * (0.5 + Math.abs((i+rndOffset)%(2*twiceFreq)-twiceFreq)/twiceFreq));
        return data;
    }

    /**
     * Генератор треугольной волны
     * @param freq частота
     * @param time продолжительность в мсек
     * @return семпл
     */
    public static double[] triangleGenerator(double freq, int time) {
        return triangleGenerator(freq, time, 1);
    }

    /**
     * Генератор треугольной волны
     * @param freq частота
     * @return семпл
     */
    public static double[] triangleGenerator(double freq) {
        return triangleGenerator(freq, GlobalHelper.sampleMsTime, 1);
    }

    /**
         * Генератор прямоугольной волны
         * @param freq частота
         * @param time продолжительность в мсек
         * @param amplitude амплитуда ( 0...1.0)
         * @return семпл
         */
        public static double[] rectangleGenerator(double freq, int time, double amplitude) {
            if (amplitude<0 || amplitude >1)
                return null;

            int freqSampling = GlobalHelper.samplePerSec;
            double[] data = new double[(GlobalHelper.bitsPerSample * freqSampling * (time / 1000)) >>> 4];

            // Переменные для избежания повторного вычисления в цикле
            double piAngle = 2.0 * freq * Math.PI / freqSampling;

            for (int i = 0; i < data.length; i++)
                data[i] =  amplitude * Math.signum(Math.sin(piAngle * i));
            return data;
    }

    /**
     * Генератор прямоугольной волны
     * @param freq частота
     * @param time продолжительность в мсек
     * @return семпл
     */
    public static double[] rectangleGenerator(double freq, int time) {
        return rectangleGenerator(freq, time, 1);
    }

    /**
     * Генератор прямоугольной волны
     * @param freq частота
     * @return семпл
     */
    public static double[] rectangleGenerator(double freq) {
        return rectangleGenerator(freq, GlobalHelper.sampleMsTime, 1);
    }


    /**
     * Генератор шумовой волны
     * @param time продолжительность в мсек
     * @param amplitude амплитуда ( 0...1.0)
     * @return семпл
     */
    public static double[] randomGenerator( int time, double amplitude) {
        if (amplitude<0 || amplitude >1)
            return null;

        int freqSampling = GlobalHelper.samplePerSec;
        double[] data = new double[(GlobalHelper.bitsPerSample * freqSampling * (time / 1000)) >>> 4];

        for (int i = 0; i < data.length; i++)
            data[i] =  (short) (amplitude * Math.random());
        return data;
    }

    /**
     * Генератор шумовой волны
     * @param amplitude амплитуда ( 0...1.0)
     * @return семпл
     */
    public static double[] randomGenerator(double amplitude) {
        return randomGenerator(GlobalHelper.sampleMsTime, amplitude);
    }

    /**
     * Генератор шумовой волны
     * @return семпл
     */
    public static double[] randomGenerator() {
        return randomGenerator(GlobalHelper.sampleMsTime, 1);
    }


    public static double getNoteFrequency(String name ) {return 0;}
}
