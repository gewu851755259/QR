#ifndef __H264_H
#define __H264_H

#define DEBUG 1

#if DEBUG
#include <android/log.h>
#ifndef TAG
#define TAG "native"
#endif
#  define  LOGV(x...)  __android_log_print(ANDROID_LOG_VERBOSE,TAG,x)
#else
#  define  LOGV(...)  do {} while (0)
#endif

#ifndef byte
#define byte unsigned char
#endif

void initGlobal();
int initDecoder();
int initRecord(char*name);

int decoderNal(byte* Buf, int nalLen, byte* Pixel, int* Res);
int writeAudio(byte* Buf, int len);
int writeVideo(byte* Buf, int len);

void uninitDecoder();	
void uninitRecord();	
void uninitGlobal();

#endif
