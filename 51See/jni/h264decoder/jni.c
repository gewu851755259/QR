//jni.c
//#define TAG "h264_jni"
#include <android/log.h>
#include "jniUtils.h"

static const char *kClassPathName = "com/my51c/see51/media/Arm7videoDecoder";

void nativeInitGlobal(JNIEnv* env, jobject thiz){
	initGlobal();
}

jint nativeInitDecoder(JNIEnv* env, jobject thiz){
	return initDecoder();
}

jint nativeInitRecord(JNIEnv* env, jobject thiz,jstring filename){
	int ret;
	char *filename_char = (*env)->GetStringUTFChars(env,filename, NULL);
	ret = initRecord(filename_char);
	(*env)->ReleaseStringUTFChars(env,filename,filename_char);
	return ret;
}

jint nativeDecoderNal(JNIEnv* env, jobject thiz, jbyteArray in, jint nalLen, jbyteArray out, jintArray resolution){
	jbyte * Buf = (jbyte*)(*env)->GetByteArrayElements(env, in, 0);
	jbyte * Pixel= (jbyte*)(*env)->GetByteArrayElements(env, out, 0);
	jint * Res = (jint*)(*env)->GetIntArrayElements(env, resolution, 0);	
	
	int nImageSize;
	nImageSize = decoderNal(Buf,nalLen,Pixel,Res);
	(*env)->ReleaseIntArrayElements(env, resolution, Res, 0);
    (*env)->ReleaseByteArrayElements(env, in, Buf, 0);    
    (*env)->ReleaseByteArrayElements(env, out, Pixel, 0);
    
    return nImageSize;
}

jint nativeWriteAudio(JNIEnv* env, jobject thiz,jbyteArray in, jint len){
	jbyte * Buf = (jbyte*)(*env)->GetByteArrayElements(env, in, 0);
	int ret = writeAudio(Buf,len);
	(*env)->ReleaseByteArrayElements(env, in, Buf, 0);
	return ret;
}

jint nativeWriteVideo(JNIEnv* env, jobject thiz,jbyteArray in, jint len){
	jbyte * Buf = (jbyte*)(*env)->GetByteArrayElements(env, in, 0);
	int ret = writeVideo(Buf,len);
	(*env)->ReleaseByteArrayElements(env, in, Buf, 0);	
	return ret;
}

void nativeUninitDecoder(JNIEnv* env, jobject thiz){
	uninitDecoder();	
}

void nativeUninitRecord(JNIEnv* env, jobject thiz){
	uninitRecord();	
}

void nativeUninitGlobal(JNIEnv* env, jobject thiz){
	uninitGlobal();	
}

static JNINativeMethod gMethods[] = {     
  {"nativeInitGlobal", "()V", (void*)nativeInitGlobal},
  {"nativeInitDecoder", "()I", (void*)nativeInitDecoder},
  {"nativeInitRecord", "(Ljava/lang/String;)I", (void*)nativeInitRecord},
  
  {"nativeDecoderNal", "([BI[B[I)I", (void*)nativeDecoderNal},
  
  {"nativeWriteAudio", "([BI)I",(void*)nativeWriteAudio},
  {"nativeWriteVideo", "([BI)I",(void*)nativeWriteVideo},
  
  {"nativeUninitDecoder", "()V", (void*)nativeUninitDecoder},
  {"nativeUninitRecord", "()V", (void*)nativeUninitRecord},
  {"nativeUninitGlobal", "()V", (void*)nativeUninitGlobal},     
};

int registerNativeMethods(JNIEnv *env) {  
    return jniRegisterNativeMethods(env, kClassPathName, gMethods, sizeof(gMethods) / sizeof(gMethods[0]));
}
