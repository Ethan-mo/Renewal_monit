#include <jni.h>
#include <stdlib.h>
#include <time.h>
#include <memory.h>
#include "aes/aes.h"

#define MAX_LEN (2 * 1024 * 1024)
#define ENCRYPT 0
#define DECRYPT 1
#define AES_KEY_SIZE 256

extern "C" {
char candidate[63] = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
char gen[65] = "4090C7138BEE7DBCD44E3BBD3DCCD78EB484A0174B1D19350194F0FF076955D1";
char appData[65] = {0, };
static unsigned char aes_key[33] = {0, };
static unsigned char aes_iv[17] = {0, };
char padding[33] = {0, };
char keyIndex[3] = {0, };

char localAppData[65] = {0, };
static unsigned char local_aes_key[33] = {0, };
static unsigned char local_aes_iv[17] = {0, };
//static unsigned char local_aes_key[33] = "49C18E7BD43B3CD8B8A1411309FF065D";
//static unsigned char local_aes_iv[17] = "73BEDC4EBDDC7E44";
char localPadding[33] = {0, };
char localKeyIndex[3] = {0, };

JNIEXPORT jstring JNICALL
Java_goodmonit_monit_com_kao_managers_em_genKey(JNIEnv *env, jobject,
                                                                    jboolean newData) {
    if (newData) {
        srand(time(NULL));

        for (int i = 0 ; i < 64; i++) {
            gen[i] = candidate[rand() % 62];
        }
    }
    return env->NewStringUTF((const char*)gen);
}

JNIEXPORT jboolean JNICALL
Java_goodmonit_monit_com_kao_managers_em_isValid(JNIEnv *env, jobject,
                                                                  jstring data) {
    strcpy(appData, env->GetStringUTFChars(data, 0));

    if (strlen(appData) != 64) return false;

    int cntKey = 0;
    int cntPadding = 0;
    for (int i = 0; i < strlen(appData); i++) {
        if (i % 2 == 0) {
            aes_key[cntKey] = appData[i];
            cntKey++;
        } else {
            padding[cntPadding] = appData[i];
            cntPadding++;
        }
    }

    keyIndex[0] = padding[0];
    keyIndex[1] = padding[1];

    for (int i = 0; i < 16; i++) {
        aes_iv[i] = padding[i + 2];
    }

    return true;
}

JNIEXPORT jboolean JNICALL
Java_goodmonit_monit_com_kao_managers_em_isValidLocal(JNIEnv *env, jobject,
                                                    jstring data) {
    strcpy(localAppData, env->GetStringUTFChars(data, 0));

    if (strlen(localAppData) != 64) return false;

    int cntKey = 0;
    int cntPadding = 0;
    for (int i = 0; i < strlen(localAppData); i++) {
        if (i % 2 == 0) {
            local_aes_key[cntKey] = localAppData[i];
            cntKey++;
        } else {
            localPadding[cntPadding] = localAppData[i];
            cntPadding++;
        }
    }

    localKeyIndex[0] = localPadding[0];
    localKeyIndex[1] = localPadding[1];

    for (int i = 0; i < 16; i++) {
        local_aes_iv[i] = localPadding[i + 2];
    }

    return true;
}

JNIEXPORT jstring JNICALL
Java_goodmonit_monit_com_kao_managers_em_getLA(JNIEnv *env, jobject) {
    return env->NewStringUTF((const char*)local_aes_key); // 키 가져오기
}

JNIEXPORT jstring JNICALL
Java_goodmonit_monit_com_kao_managers_em_getLB(JNIEnv *env, jobject) {
    return env->NewStringUTF((const char*)local_aes_iv); // InitialVector 가져오기
}

JNIEXPORT jstring JNICALL
Java_goodmonit_monit_com_kao_managers_em_getLC(JNIEnv *env, jobject) {
    return env->NewStringUTF(localKeyIndex); // 키 Index 가져오기
}

JNIEXPORT jstring JNICALL
Java_goodmonit_monit_com_kao_managers_em_getA(JNIEnv *env, jobject) {
    return env->NewStringUTF((const char*)aes_key); // 키 가져오기
}

JNIEXPORT jstring JNICALL
Java_goodmonit_monit_com_kao_managers_em_getB(JNIEnv *env, jobject) {
    return env->NewStringUTF((const char*)aes_iv); // InitialVector 가져오기
}

JNIEXPORT jstring JNICALL
Java_goodmonit_monit_com_kao_managers_em_getC(JNIEnv *env, jobject) {
    return env->NewStringUTF(keyIndex); // 키 Index 가져오기
}

jbyteArray aescrypto(JNIEnv *env, jbyteArray jarray, unsigned char* key, unsigned char* iv, jint jmode) {
    //check input data
    unsigned int len = (unsigned int) (env->GetArrayLength(jarray));
    if (len <= 0 || len >= MAX_LEN) {
        return NULL;
    }

    unsigned char *data = (unsigned char *) (env->GetByteArrayElements(jarray, NULL));
    if (!data) {
        return NULL;
    }

    //패딩길이 계산하기(DESede/CBC/PKCS5Padding)
    unsigned int mode = (unsigned int) jmode;
    unsigned int rest_len = len % AES_BLOCK_SIZE;
    unsigned int padding_len = (
            (ENCRYPT == mode) ? (AES_BLOCK_SIZE - rest_len) : 0);
    unsigned int src_len = len + padding_len;

    //입력값설정
    unsigned char *input = (unsigned char *) malloc(src_len);
    memset(input, 0, src_len);
    memcpy(input, data, len);
    if (padding_len > 0) {
        memset(input + len, (unsigned char) padding_len, padding_len);
    }
    //더이상 사용하지 않음, Release
    env->ReleaseByteArrayElements(jarray, (jbyte *) data, 0);

    //Output설정
    unsigned char *buff = (unsigned char *) malloc(src_len);
    if (!buff) {
        free(input);
        return NULL;
    }
    memset(buff, 0, src_len);

    //set key & iv
    unsigned int key_schedule[AES_BLOCK_SIZE * 4] = {0}; //>=53(这里取64)
    aes_key_setup(key, key_schedule, AES_KEY_SIZE);

    //암호화 진행(CBC mode)
    if (mode == ENCRYPT) {
        aes_encrypt_cbc(input, src_len, buff, key_schedule, AES_KEY_SIZE,
                        iv);
    } else {
        aes_decrypt_cbc(input, src_len, buff, key_schedule, AES_KEY_SIZE,
                        iv);
    }

    //Decoding 시 해독 길이 채우기 계산
    if (ENCRYPT != mode) {
        unsigned char *ptr = buff;
        ptr += (src_len - 1);
        padding_len = (unsigned int) *ptr;
        if (padding_len > 0 && padding_len <= AES_BLOCK_SIZE) {
            src_len -= padding_len;
        }
        ptr = NULL;
    }

    //반환
    jbyteArray bytes = env->NewByteArray(src_len);
    env->SetByteArrayRegion(bytes, 0, src_len, (jbyte *) buff);

    //메모리해제
    free(input);
    free(buff);

    return bytes;
}

JNIEXPORT jbyteArray JNICALL
Java_goodmonit_monit_com_kao_managers_em_getE(JNIEnv *env, jobject, jbyteArray jarray) {
    //반환
    jbyteArray bytes = aescrypto(env, jarray, aes_key, aes_iv, ENCRYPT);

    // Base64 인코딩
    jclass base64Class = env->FindClass("android/util/Base64");
    jmethodID encodeToStringMid = env->GetStaticMethodID( base64Class,"encodeToString", "([BI)Ljava/lang/String;");
    jstring base64String = (jstring)env->CallStaticObjectMethod( base64Class, encodeToStringMid, bytes, 0);

    // UTF8 인코딩
    const char *enc2Data = env->GetStringUTFChars(base64String, 0);
    int len = strlen(enc2Data);

    jbyte *retBytes = new jbyte[len + 2];
    retBytes[0] = enc2Data[0];
    retBytes[1] = keyIndex[0];
    retBytes[2] = enc2Data[1];
    retBytes[3] = keyIndex[1];
    for (int i = 0; i < len - 2; i++) {
        retBytes[i + 4] = enc2Data[i + 2];
    }
    jbyteArray retByteArray = env->NewByteArray(len + 2);
    env->SetByteArrayRegion(retByteArray, 0, len + 2, retBytes);

    //더이상 사용하지 않음, Release
    env->ReleaseStringUTFChars(base64String, enc2Data);

    return retByteArray;
}

JNIEXPORT jbyteArray JNICALL
Java_goodmonit_monit_com_kao_managers_em_getD(JNIEnv *env, jobject, jbyteArray jarray) {
    return aescrypto(env, jarray, aes_key, aes_iv, DECRYPT);
}

JNIEXPORT jbyteArray JNICALL
Java_goodmonit_monit_com_kao_managers_em_getLE(JNIEnv *env, jobject, jbyteArray jarray) {
    //반환
    jbyteArray bytes = aescrypto(env, jarray, local_aes_key, local_aes_iv, ENCRYPT);

    // Base64 인코딩
    jclass base64Class = env->FindClass("android/util/Base64");
    jmethodID encodeToStringMid = env->GetStaticMethodID( base64Class,"encodeToString", "([BI)Ljava/lang/String;");
    jstring base64String = (jstring)env->CallStaticObjectMethod( base64Class, encodeToStringMid, bytes, 0);

    // UTF8 인코딩
    const char *enc2Data = env->GetStringUTFChars(base64String, 0);
    int len = strlen(enc2Data);

    jbyte *retBytes = new jbyte[len];
    for (int i = 0; i < len; i++) {
        retBytes[i] = enc2Data[i];
    }
    jbyteArray retByteArray = env->NewByteArray(len);
    env->SetByteArrayRegion(retByteArray, 0, len, retBytes);

    //더이상 사용하지 않음, Release
    env->ReleaseStringUTFChars(base64String, enc2Data);

    return retByteArray;
}

JNIEXPORT jbyteArray JNICALL
Java_goodmonit_monit_com_kao_managers_em_getLD(JNIEnv *env, jobject, jbyteArray jarray) {
    return aescrypto(env, jarray, local_aes_key, local_aes_iv, DECRYPT);
}

}