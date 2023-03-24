#include <jni.h>
#include <string>
#include <string.h>

extern "C" {
// Environment

float maxTemperature, minTemperature;
float maxHumidity, minHumidity;
float vocThreshold = 150;

JNIEXPORT jint JNICALL
Java_goodmonit_monit_com_kao_devices_EnvironmentCheckManager_getVocStage(JNIEnv *env, jobject, jfloat voc) {
    if (voc < 0) {
        return -1;
    } else if (voc < 51) {
        return 0;
    } else if (voc < 151) {
        return 1;
    } else if (voc < 301) {
        return 2;
    } else {
        return 3;
    }
}

JNIEXPORT jboolean JNICALL
Java_goodmonit_monit_com_kao_devices_EnvironmentCheckManager_isVocWarning(JNIEnv *env, jobject, jfloat voc) {
    if (voc > vocThreshold) {
        return true;
    } else {
        return false;
    }
}

JNIEXPORT void JNICALL
Java_goodmonit_monit_com_kao_devices_EnvironmentCheckManager_setTemperatureThres(JNIEnv *env, jobject, jfloat min, jfloat max) {
    maxTemperature = max;
    minTemperature = min;
}

JNIEXPORT void JNICALL
Java_goodmonit_monit_com_kao_devices_EnvironmentCheckManager_setHumidityThres(JNIEnv *env, jobject, jfloat min, jfloat max) {
    maxHumidity = max;
    minHumidity = min;
}

JNIEXPORT jint JNICALL
Java_goodmonit_monit_com_kao_devices_EnvironmentCheckManager_getScoreStage(JNIEnv *env, jobject, jfloat score) {
    if (score >= 90) {
        return 0;
    } else if (score >= 70) {
        return 1;
    } else if (score >= 50) {
        return 2;
    } else {
        return 3;
    }
}

JNIEXPORT jint JNICALL
Java_goodmonit_monit_com_kao_devices_EnvironmentCheckManager_updateScore(JNIEnv *env, jobject, jfloat temperature, jfloat humidity, jfloat voc) {
    jfloat score = 100;
    jfloat avg = 0;
    jfloat diff = 0;

    if (voc < 0) {
        avg = (minTemperature + maxTemperature) / 2;
        if (temperature < minTemperature) {
            score -= 12;
            score -= (minTemperature - temperature) * 15;
        } else if (temperature > maxTemperature) {
            score -= 12;
            score -= (temperature - maxTemperature) * 15;
        } else {
            diff = temperature - avg;
            if (diff > 0) score -= diff * 7;
            else score += diff * 7;
        }

        avg = (minHumidity + maxHumidity) / 2;
        if (humidity < minHumidity) {
            score -= 12;
            score -= (minHumidity - humidity) * 3;
        } else if (humidity > maxHumidity) {
            score -= 12;
            score -= (humidity - maxHumidity) * 3;
        } else {
            diff = humidity - avg;
            if (diff > 0) score -= diff;
            else score += diff;
        }
    } else {
        avg = (minTemperature + maxTemperature) / 2;
        if (temperature < minTemperature) {
            score -= 10;
            score -= (minTemperature - temperature) * 10;
        } else if (temperature > maxTemperature) {
            score -= 10;
            score -= (temperature - maxTemperature) * 10;
        } else {
            diff = temperature - avg;
            if (diff > 0) score -= diff * 5;
            else score += diff * 5;
        }

        avg = (minHumidity + maxHumidity) / 2;
        if (humidity < minHumidity) {
            score -= 10;
            score -= (minHumidity - humidity) * 2;
        } else if (humidity > maxHumidity) {
            score -= 10;
            score -= (humidity - maxHumidity) * 2;
        } else {
            diff = humidity - avg;
            if (diff > 0) score -= diff;
            else score += diff;
        }

        if (voc > vocThreshold) {
            score -= 10;
            score -= (voc - vocThreshold) / 4 + 1;
        } else {
            score -= voc / 4 + 1;
        }
    }

    if (score < 0) {
        score = 0;
    }

    return score;
}
} // End Extern "C"