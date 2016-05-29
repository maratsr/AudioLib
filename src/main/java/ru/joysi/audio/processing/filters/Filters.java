package ru.joysi.audio.processing.filters;

import ru.joysi.audio.ExceptionHandler;
import ru.joysi.audio.GlobalHelper;
import ru.joysi.audio.exceptions.AudioFilterException;

/**
 * Created by 886 on 26.05.2016.
 */
public class Filters {
//      --- ___
//    /         \
//   /           ------
//  /                   \----___
//    1 |   2   |   3    |   4
// 1 - Attack(нарастание), 2 - Decay(ослабление), 3- Sustain(поддержка), 4-Release (затухание)
    /**
     * Построение значений нормализованной функции ADSL на для Attack и Delay участков
     * @param attackMaxTime время наступления пикового уровня амлитуды (=1) в миллисек
     * @param deсayEndTime время окончания Delay-стадии в миллисек
     * @param deсayEndLevel уровень громкости Delay-стадии в конце (<1)
     * @return
     */
    public static double[] getASDRarray(double attackMaxTime, double deсayEndTime, double deсayEndLevel) {

        if (attackMaxTime > deсayEndTime || deсayEndLevel >= 1 || attackMaxTime <0 || deсayEndTime < 0 || deсayEndLevel <0) {
            ExceptionHandler.log(new AudioFilterException(), GlobalHelper.props.getProperty("error.filter.incorrectParameters"));
            return null;
        }

        // Итоговый нормализованный массив коэффициентов фильтра
        double[] res = new double[(GlobalHelper.bitsPerSample * GlobalHelper.samplePerSec * ((int)deсayEndTime / 1000)) >>> 4];

        double atk   = attackMaxTime / 1000.0; // время атаки в секундах
        double decay = deсayEndTime / 1000.0; // время от начала семпла до окончания decay фазы
        // Нормализуем максимум функции в единицу (double-Значение максимума ненормализованной функции)
        double normCoeff = 1.0 * Math.exp(atk) / Math.pow(atk, atk);


        // Вычислим участок Attack
        int idxMaxAttack = (int) (atk * GlobalHelper.samplePerSec); // Индекс соответствующий максимуму атаке
        for (int i = 0; i < idxMaxAttack ; i++)
            res[i] = normCoeff * Math.exp(-i *1. / GlobalHelper.samplePerSec) * Math.pow(i *1./ GlobalHelper.samplePerSec, atk);

        // Коэффиент конечного значения decay в точке 2*atk
        double cDeltaDecay =  deсayEndLevel / (Math.exp(-2*atk) * Math.pow(2*atk, atk) *normCoeff);

        // Коэффициенты линейного растяжения функции для участка (atk, 1*atk) на участок (atk, decay)
        double alpha = (cDeltaDecay - 1) / atk;
        double beta = 2 - cDeltaDecay;

        // Вычислим участок Decay
        double x = atk;
        double dx = atk / ((decay - atk)*GlobalHelper.samplePerSec);
        for (int i = idxMaxAttack; i < res.length ; i++) {
            res[i] = normCoeff * Math.exp(-x) * Math.pow(x, atk) * (alpha*x+beta);
            x += dx;
        }
        return res;
    }
//
//    public static void main(String[] args) {
//        asdr(500, 4000, 0.8);
//    }
}
