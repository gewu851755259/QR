#include <jni.h>
#include "../prebuilt/RK_IGHDSCClientC.h"
#include <string.h>


#ifndef NULL
#define NULL   ((void *) 0)
#endif

//HANDLE_GHDSCCLIENT ghHandle = NULL;

// 引入log头文件
#include <android/log.h>

// log标签
#define  TAG    "cloudsdk"
// 定义info信息
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
// 定义debug信息
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
// 定义error信息
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)

//发布库的开关
//#define _DEBUG

extern "C"
{
// 获取数组的大小
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

// 指定要注册的类，对应完整的java类名
#ifdef _DEBUG
	#define JNIREG_CLASS "com/hao/testdynicjni/cloudsdk"
	#define JNIPAR_CLASS "com/hao/testdynicjni/CloudHandle"
#else
	#define JNIREG_CLASS "com/my51c/see51/media/cloudsdk"
	#define JNIPAR_CLASS "com/my51c/see51/data/CloudHandle"
#endif

struct CloudParams
{
	jclass gcloudcls;
    jfieldID lCloudHandle;

    jfieldID strADKPath;
    jfieldID strParam;
    jfieldID strUsername;
    jfieldID strPassword;
    jfieldID strSN;

    jfieldID strQueryEventCountWhere;
    jfieldID lQueryEventCount;

    jfieldID strQueryDataEventWhere;
    jfieldID strQueryDataEventColumn;
    jfieldID lQueryDataEventPos;
    jfieldID lQueryDataEventCount;
    jfieldID lQueryDataEventRead;
    jfieldID bQueryDataEventData;
    jfieldID lQueryDataEventLenData;

    jfieldID strQueryVideoCountWhere;
    jfieldID lQueryVideoCount;


    jfieldID strQueryVideoMonthCountWhere;
    jfieldID strQueryVideoMonthCountColumn;
    jfieldID lQueryVideoMonthCountRead;
    jfieldID bQueryVideoMonthCountData;
    jfieldID lQueryVideoMonthCountLenData;

    jfieldID strQueryVideoDataWhere;
    jfieldID strQueryVideoDataColumn;
    jfieldID lQueryVideoDataPos;
    jfieldID lQueryVideoDataCount;
    jfieldID lQueryVideoDataRead;
    jfieldID bQueryVideoData;
    jfieldID lQueryVideoDataLenData;

    jfieldID lDownloadVideoInfoTime;
    jfieldID iDownloadVideoInfoType;
    jfieldID lDownloadVideoInfoSize;
    jfieldID lDownloadVideoInfoStatus;
    jfieldID lDownloadVideoInfoContex;

    jfieldID lDownloadVideoDataContex;
    jfieldID lDownloadVideoDataTime;
    jfieldID iDownloadVideoDataType;
    jfieldID lDownloadVideoDataPos;
    jfieldID lDownloadVideoDataCount;
    jfieldID lDownloadVideoDataRead;
    jfieldID bDownloadVideoData;
    jfieldID lDownloadVideoDataLenData;

    jfieldID lReleaseData;
} gCloudParam;


// 初始化函数，用于获取Java中各个成员对应的fieldID。
static void nativeClassInit (JNIEnv *env)
{
    jclass cloudcls = env->FindClass(JNIPAR_CLASS);
    gCloudParam.gcloudcls =(jclass)env->NewGlobalRef(cloudcls);
    gCloudParam.lCloudHandle = env->GetFieldID(cloudcls, "lCloudHandle", "J");

    gCloudParam.strADKPath = env->GetFieldID(cloudcls, "strADKPath", "Ljava/lang/String;");
    gCloudParam.strParam  = env->GetFieldID(cloudcls, "strParam", "Ljava/lang/String;");
    gCloudParam.strUsername = env->GetFieldID(cloudcls, "strUsername", "Ljava/lang/String;");
    gCloudParam.strPassword = env->GetFieldID(cloudcls, "strPassword", "Ljava/lang/String;");
    gCloudParam.strSN = env->GetFieldID(cloudcls, "strSN", "Ljava/lang/String;");

    gCloudParam.strQueryEventCountWhere = env->GetFieldID(cloudcls, "strQueryEventCountWhere", "Ljava/lang/String;");
    gCloudParam.lQueryEventCount = env->GetFieldID(cloudcls, "lQueryEventCount", "J");

    gCloudParam.strQueryDataEventWhere = env->GetFieldID(cloudcls, "strQueryDataEventWhere", "Ljava/lang/String;");
    gCloudParam.strQueryDataEventColumn = env->GetFieldID(cloudcls, "strQueryDataEventColumn", "Ljava/lang/String;");
    gCloudParam.lQueryDataEventPos = env->GetFieldID(cloudcls, "lQueryDataEventPos", "J");
    gCloudParam.lQueryDataEventCount = env->GetFieldID(cloudcls, "lQueryDataEventCount", "J");
    gCloudParam.lQueryDataEventRead = env->GetFieldID(cloudcls, "lQueryDataEventRead", "J");
    gCloudParam.bQueryDataEventData = env->GetFieldID(cloudcls, "bQueryDataEventData", "[B");
    gCloudParam.lQueryDataEventLenData = env->GetFieldID(cloudcls, "lQueryDataEventLenData", "J");

    gCloudParam.strQueryVideoCountWhere = env->GetFieldID(cloudcls, "strQueryVideoCountWhere", "Ljava/lang/String;");
    gCloudParam.lQueryVideoCount = env->GetFieldID(cloudcls, "lQueryVideoCount", "J");


    gCloudParam.strQueryVideoMonthCountWhere = env->GetFieldID(cloudcls, "strQueryVideoMonthCountWhere", "Ljava/lang/String;");
    gCloudParam.strQueryVideoMonthCountColumn = env->GetFieldID(cloudcls, "strQueryVideoMonthCountColumn", "Ljava/lang/String;");
    gCloudParam.lQueryVideoMonthCountRead = env->GetFieldID(cloudcls, "lQueryDataEventPos", "J");
    gCloudParam.bQueryVideoMonthCountData = env->GetFieldID(cloudcls, "bQueryDataEventData", "[B");
    gCloudParam.lQueryVideoMonthCountLenData = env->GetFieldID(cloudcls, "lQueryDataEventLenData", "J");


    gCloudParam.strQueryVideoDataWhere = env->GetFieldID(cloudcls, "strQueryVideoDataWhere", "Ljava/lang/String;");
    gCloudParam.strQueryVideoDataColumn = env->GetFieldID(cloudcls, "strQueryVideoDataColumn", "Ljava/lang/String;");
    gCloudParam.lQueryVideoDataPos = env->GetFieldID(cloudcls, "lQueryVideoDataPos", "J");
    gCloudParam.lQueryVideoDataCount = env->GetFieldID(cloudcls, "lQueryVideoDataCount", "J");
    gCloudParam.lQueryVideoDataRead = env->GetFieldID(cloudcls, "lQueryVideoDataRead", "J");
    gCloudParam.bQueryVideoData = env->GetFieldID(cloudcls, "bQueryVideoData", "[B");
    gCloudParam.lQueryVideoDataLenData = env->GetFieldID(cloudcls, "lQueryVideoDataLenData", "J");

    gCloudParam.lDownloadVideoInfoTime = env->GetFieldID(cloudcls, "lDownloadVideoInfoTime", "J");
    gCloudParam.iDownloadVideoInfoType = env->GetFieldID(cloudcls, "iDownloadVideoInfoType", "I");
    gCloudParam.lDownloadVideoInfoSize = env->GetFieldID(cloudcls, "lDownloadVideoInfoSize", "J");
    gCloudParam.lDownloadVideoInfoStatus = env->GetFieldID(cloudcls, "lDownloadVideoInfoStatus", "J");
    gCloudParam.lDownloadVideoInfoContex = env->GetFieldID(cloudcls, "lDownloadVideoInfoContex", "J");

    gCloudParam.lDownloadVideoDataContex = env->GetFieldID(cloudcls, "lDownloadVideoDataContex", "J");
    gCloudParam.lDownloadVideoDataTime = env->GetFieldID(cloudcls, "lDownloadVideoDataTime", "J");
    gCloudParam.iDownloadVideoDataType = env->GetFieldID(cloudcls, "iDownloadVideoDataType", "I");
    gCloudParam.lDownloadVideoDataPos = env->GetFieldID(cloudcls, "lDownloadVideoDataPos", "J");
    gCloudParam.lDownloadVideoDataCount = env->GetFieldID(cloudcls, "lDownloadVideoDataCount", "J");
    gCloudParam.lDownloadVideoDataRead = env->GetFieldID(cloudcls, "lDownloadVideoDataRead", "J");
    gCloudParam.bDownloadVideoData = env->GetFieldID(cloudcls, "bDownloadVideoData", "[B");
	gCloudParam.lDownloadVideoDataLenData = env->GetFieldID(cloudcls, "lDownloadVideoDataLenData", "J");

	gCloudParam.lReleaseData = env->GetFieldID(cloudcls, "lReleaseData", "J");
}


/*
 *云SDK初始化
 */

JNIEXPORT jint JNICALL native_ghdscclient_init(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	int ret;

	jfieldID sdkpath_field =  gCloudParam.strADKPath;
	jstring sdkpath = static_cast<jstring>(env->GetObjectField(cloudcls, sdkpath_field));

	const char *strpath = (env)->GetStringUTFChars(sdkpath, (unsigned char*)NULL);
	ret = GHDSCClient_Init(strpath);
	(env)->ReleaseStringUTFChars(sdkpath,strpath);
	return ret;
}

/*
 * 云SDK注销
 */
JNIEXPORT void JNICALL native_ghdscclient_fini(JNIEnv *env, jclass clazz, jobject obj)
{
	GHDSCClient_Fini();
}

/*
 * 网络句柄创建
 */
JNIEXPORT jint JNICALL native_ghdscclient_create(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	int ret;
	jfieldID cloudhandle_field  = gCloudParam.lCloudHandle;
	//jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));

	HANDLE_GHDSCCLIENT handle = NULL;
	ret = GHDSCClient_Create(handle);
	//cloudhandle = (jlong)handle;
	//LOGD("create handle:%ld", (long)handle);
	env->SetLongField(cloudcls, cloudhandle_field, (jlong)handle);
	return ret;
}

/*
 * 网路句柄销毁
 */
JNIEXPORT void JNICALL native_ghdscclient_destory(JNIEnv *env, jclass clazz, jobject cloudcls)
{

	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));

	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;
	GHDSCClient_Destroy(temphandle);
	env->SetLongField(cloudcls, cloudhandle_field, (jlong)temphandle);
	//cloudhandle = (jlong)temphandle;
}

/*
 * 网络句柄链接
 *
 */

JNIEXPORT jint JNICALL native_ghdscclient_connect(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	int ret;

	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jfieldID param_field = gCloudParam.strParam ;
	jfieldID username_field = gCloudParam.strUsername;
	jfieldID password_field = gCloudParam.strPassword;
	jfieldID sn_field = gCloudParam.strSN;

	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;

	jstring jParam = static_cast<jstring>(env->GetObjectField(cloudcls, param_field));
	jstring jusername = static_cast<jstring>(env->GetObjectField(cloudcls, username_field));
	jstring jpassword = static_cast<jstring>(env->GetObjectField(cloudcls, password_field));
	jstring jsn = static_cast<jstring>(env->GetObjectField(cloudcls, sn_field));

	const char *param = (env)->GetStringUTFChars(jParam, (unsigned char*)NULL);
	const char *username = (env)->GetStringUTFChars(jusername, (unsigned char*)NULL);
	const char *passwd = (env)->GetStringUTFChars(jpassword, (unsigned char*)NULL);
	const char *sn = (env)->GetStringUTFChars(jsn, (unsigned char*)NULL);

	ret = GHDSCClient_Connect(temphandle, param, username, passwd, sn);

	(env)->ReleaseStringUTFChars(jParam,param);
	(env)->ReleaseStringUTFChars(jusername,username);
	(env)->ReleaseStringUTFChars(jpassword,passwd);
	(env)->ReleaseStringUTFChars(jsn,sn);

	return ret;
}

/*
 * 网络是否连接
 *
 */

JNIEXPORT jboolean JNICALL native_ghdscclient_isconnect(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;
	return GHDSCClient_IsConnect(temphandle);
}

/*
 * 断开网络连接
 */

JNIEXPORT void JNICALL native_ghdscclient_disconnect(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;
	GHDSCClient_Disconnect(temphandle);
	env->SetLongField(cloudcls, cloudhandle_field, (jlong)temphandle);
}

/*
 * 查询事件个数
 *
 */
JNIEXPORT jint JNICALL native_ghdscclient_query_count_event(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	int ret;

	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jfieldID where_field = gCloudParam.strQueryEventCountWhere;
	jfieldID queryeventcount_field = gCloudParam.lQueryEventCount;

	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;

	jstring jwhere = static_cast<jstring>(env->GetObjectField(cloudcls, where_field));
	//jlong queryeventcount = (jlong)(env->GetLongField(cloudcls, queryeventcount_field));
	unsigned long  nqueryeventcount = 0;
	const char *where = (const char*)(env)->GetStringUTFChars(jwhere, (unsigned char*)NULL);
	ret = GHDSCClient_Query_Count_Event(temphandle, where, nqueryeventcount);
	//queryeventcount = (jlong)nqueryeventcount;
	env->SetLongField(cloudcls, queryeventcount_field, (jlong)nqueryeventcount);
	(env)->ReleaseStringUTFChars(jwhere,where);
	return ret;
}

/*
 * 查询内容
 *
 */
JNIEXPORT jint JNICALL native_ghdscclient_query_data_event(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	int ret;

	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jfieldID where_field = gCloudParam.strQueryDataEventWhere;
	jfieldID column_field = gCloudParam.strQueryDataEventColumn;
	jfieldID pos_field = gCloudParam.lQueryDataEventPos;
	jfieldID count_field = gCloudParam.lQueryDataEventPos;
	jfieldID read_field = gCloudParam.lQueryDataEventRead ;
	jfieldID lendata_field =  gCloudParam.lQueryDataEventLenData ;
	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;

	jmethodID data_method = env->GetMethodID(gCloudParam.gcloudcls, "setbQueryDataEventData", "([B)V");

	jstring jwhere = static_cast<jstring>(env->GetObjectField(cloudcls, where_field));
	jstring jcolumn= static_cast<jstring>(env->GetObjectField(cloudcls, column_field));
	jlong jpos = (jlong)(env->GetLongField(cloudcls, pos_field));
	jlong jcount = (jlong)(env->GetLongField(cloudcls, count_field));
	//jlong jread = (jlong)(env->GetLongField(cloudcls, read_field));
	//jlong jlen = (jlong)(env->GetLongField(cloudcls, lendata_field));

	unsigned long pos = (unsigned long) jpos;
	unsigned long count = (unsigned long) jcount;


	unsigned long read = 0;
	void* data = 0;
	unsigned long len = 0;
	const char *where = (const char*)(env)->GetStringUTFChars(jwhere, (unsigned char*)NULL);
	const char *column = (const char*)(env)->GetStringUTFChars(jcolumn, (unsigned char*)NULL);
	ret = GHDSCClient_Query_Data_Event(temphandle, where, column, pos, count, read, data, len);

	env->SetLongField(cloudcls, read_field, (jlong)read);
	env->SetLongField(cloudcls, lendata_field, (jlong)len);

	jbyte* pdata = (jbyte*) data;
	jbyteArray jbarray = (env)->NewByteArray((jsize)len);
	(env)->SetByteArrayRegion(jbarray, 0, len, pdata);            //将Jbyte 转换为jbarray数组
	(env)->CallVoidMethod(cloudcls, data_method,jbarray);         //回调java set方法
	if(data != NULL)
	{
		GHDSClient_Release_Data(temphandle, data);
	}

	(env)->ReleaseStringUTFChars(jwhere,where);
	(env)->ReleaseStringUTFChars(jcolumn,column);

	return ret;
}

/*
 *查询视频个数
 */
JNIEXPORT jint JNICALL native_ghdscclient_query_count_video(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	int ret;

	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jfieldID where_field = gCloudParam.strQueryVideoCountWhere;
	jfieldID queryvideocount_field = gCloudParam.lQueryVideoCount;


	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;

	jstring jwhere = static_cast<jstring>(env->GetObjectField(cloudcls, where_field));
	//jlong queryvideocount = (jlong)(env->GetLongField(cloudcls, queryvideocount_field));
	const char *where = (const char*)(env)->GetStringUTFChars(jwhere, (unsigned char*)NULL);
	unsigned long count = 0;
	ret = GHDSCClient_Query_Count_Video(temphandle, where, count);

	(env)->ReleaseStringUTFChars(jwhere,where);
	//queryvideocount = (jlong) count;
	env->SetLongField(cloudcls, queryvideocount_field, (jlong)count);

	return ret;
}


/*
 *查询某个月视频数
 */
JNIEXPORT jint JNICALL native_ghdscclient_query_month_count_video(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	int ret;

	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jfieldID where_field = gCloudParam.strQueryVideoMonthCountWhere;
	jfieldID column_field =  gCloudParam.strQueryVideoMonthCountColumn;
	jfieldID read_field = gCloudParam.lQueryVideoMonthCountRead;
	jfieldID lendata_field = gCloudParam.lQueryVideoMonthCountLenData;

	jmethodID data_method = env->GetMethodID(gCloudParam.gcloudcls, "setbQueryVideoMonthCountData", "([B)V");

	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;

	jstring jwhere = static_cast<jstring>(env->GetObjectField(cloudcls, where_field));
	const char *where = (const char*)(env)->GetStringUTFChars(jwhere, (unsigned char*)NULL);

	jstring jcolumn = static_cast<jstring>(env->GetObjectField(cloudcls, column_field));
	const char *column = (const char*)(env)->GetStringUTFChars(jcolumn, (unsigned char*)NULL);

	unsigned long read = 0;
	void* data = 0;
	unsigned long len = 0;

	ret = GHDSCClient_Query_Month_Count_Video(temphandle, where, column, read, data, len);

	(env)->ReleaseStringUTFChars(jwhere,where);
	(env)->ReleaseStringUTFChars(jcolumn,column);

	env->SetLongField(cloudcls, read_field, (jlong)read);
	env->SetLongField(cloudcls, lendata_field, (jlong)len);

	jbyte* pdata = (jbyte*) data;
	jbyteArray jbarray = (env)->NewByteArray((jsize)len);
	(env)->SetByteArrayRegion(jbarray, 0, len, pdata);            //将Jbyte 转换为jbarray数组
	(env)->CallVoidMethod(cloudcls, data_method,jbarray);         //回调java set方法

	if(data != NULL)
	{
		GHDSClient_Release_Data(temphandle, data);
	}

	return ret;
}

/*
 *查询视频
 */
JNIEXPORT jint JNICALL native_ghdscclient_query_data_video(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	int ret;

	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jfieldID where_field = gCloudParam.strQueryVideoDataWhere;
	jfieldID column_field = gCloudParam.strQueryVideoDataColumn;
	jfieldID pos_field = gCloudParam.lQueryVideoDataPos;
	jfieldID count_field = gCloudParam.lQueryVideoDataCount;
	jfieldID read_field = gCloudParam.lQueryVideoDataRead ;
	jfieldID lendata_field = gCloudParam.lQueryVideoDataLenData ;
	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;

	jmethodID data_method = env->GetMethodID(gCloudParam.gcloudcls, "setbQueryVideoData", "([B)V");

	jstring jwhere = static_cast<jstring>(env->GetObjectField(cloudcls, where_field));
	jstring jcolumn= static_cast<jstring>(env->GetObjectField(cloudcls, column_field));
	jlong jpos = (jlong)(env->GetLongField(cloudcls, pos_field));
	jlong jcount = (jlong)(env->GetLongField(cloudcls, count_field));
	//jlong jread = (jlong)(env->GetLongField(cloudcls, read_field));
	//jlong jlen = (jlong)(env->GetLongField(cloudcls, lendata_field));

	unsigned long pos = (unsigned long) jpos;
	unsigned long count = (unsigned long) jcount;


	unsigned long read = 0;
	void* data = 0;
	unsigned long len = 0;
	const char *where = (const char*)(env)->GetStringUTFChars(jwhere, (unsigned char*)NULL);
	const char *column = (const char*)(env)->GetStringUTFChars(jcolumn, (unsigned char*)NULL);
	ret = GHDSCClient_Query_Data_Video(temphandle, where, column, pos, count, read, data, len);
	//jread = (jlong)read;
	//jlen = (jlong) len;

	env->SetLongField(cloudcls, read_field, (jlong)read);
	env->SetLongField(cloudcls, lendata_field, (jlong)len);

	jbyte* pdata = (jbyte*) data;
	//LOGD("query video data:%s\n", pdata);
	//LOGD("query video data length:%d, read:%d\n", len, read);
	jbyteArray jbarray = (env)->NewByteArray((jsize)len);
	(env)->SetByteArrayRegion(jbarray, 0, len, pdata);            //将Jbyte 转换为jbarray数组
	(env)->CallVoidMethod(cloudcls, data_method,jbarray);         //回调java set方法

	if(data != NULL)
	{
		GHDSClient_Release_Data(temphandle, data);
	}

	(env)->ReleaseStringUTFChars(jwhere,where);
	(env)->ReleaseStringUTFChars(jcolumn,column);
	return ret;
}

/**
 *
 *获取视频信息
 */

JNIEXPORT jint JNICALL native_ghdscclient_download_info_video(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	int ret;

	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jfieldID time_field = gCloudParam.lDownloadVideoInfoTime;
	jfieldID type_field = gCloudParam.iDownloadVideoInfoType;
	jfieldID size_field = gCloudParam.lDownloadVideoInfoSize;
	jfieldID status_field = gCloudParam.lDownloadVideoInfoStatus;
	jfieldID contex_field = gCloudParam.lDownloadVideoInfoContex;

	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;


	jlong jtime = (jlong)(env->GetLongField(cloudcls, time_field));
	jint jtype = (jint)(env->GetIntField(cloudcls, type_field));
	//jlong jsize = (jlong)(env->GetLongField(cloudcls, size_field));
	//jlong jstatus = (jlong)(env->GetLongField(cloudcls, status_field));
	//jlong jcontex = (jlong)(env->GetLongField(cloudcls, contex_field));

	unsigned long time = (unsigned long) jtime;
	unsigned int type = (unsigned int) jtype;
	unsigned long size = 0;
	unsigned long status = 0;
	unsigned long contex = 0;


	ret = GHDSCClient_Download_Info_Video(temphandle, time, type, size, status, contex);

	//jsize = size;
	//jstatus = status;
	//jcontex = contex;

	env->SetLongField(cloudcls, size_field, (jlong)size);
	env->SetLongField(cloudcls, status_field, (jlong)status);
	env->SetLongField(cloudcls, contex_field, (jlong)contex);

	return ret;
}

JNIEXPORT jint JNICALL native_ghdscclient_download_data_video(JNIEnv *env, jclass clazz, jobject cloudcls)
{
	int ret;

	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jfieldID contex_field = gCloudParam.lDownloadVideoDataContex;
	jfieldID time_field = gCloudParam.lDownloadVideoDataTime;
	jfieldID type_field = gCloudParam.iDownloadVideoDataType;
	jfieldID pos_field = gCloudParam.lDownloadVideoDataPos;
	jfieldID count_field = gCloudParam.lDownloadVideoDataCount;
	jfieldID read_field = gCloudParam.lDownloadVideoDataRead;
	jfieldID len_field = gCloudParam.lDownloadVideoDataLenData;


	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;

	jmethodID data_method = env->GetMethodID(gCloudParam.gcloudcls, "setbDownloadVideoData", "([B)V");

	jlong jcontex = (jlong)(env->GetLongField(cloudcls, contex_field));
	jlong jtime = (jlong)(env->GetLongField(cloudcls, time_field));
	jint jtype = (jint)(env->GetIntField(cloudcls, type_field));
	jlong jpos = (jlong)(env->GetLongField(cloudcls, pos_field));
	jlong jcount = (jlong)(env->GetLongField(cloudcls, count_field));
	//jlong jread = (jlong)(env->GetLongField(cloudcls, read_field));
	//jlong jlen = (jlong)(env->GetLongField(cloudcls, len_field));

	unsigned long contex = (unsigned long) jcontex;
	unsigned long time = (unsigned long) jtime;
	unsigned long type = (unsigned long) jtype;
	unsigned long pos = (unsigned long) jpos;
	unsigned long count = (unsigned long) jcount;
	unsigned long read = 0;
	void* data = 0;
	unsigned long len = 0;


	ret = GHDSCClient_Download_Data_Video(temphandle, contex, time, type, pos, count, read, data, len);

	//jread = read;
	//jlen = len;
	env->SetLongField(cloudcls, read_field, (jlong)read);
	env->SetLongField(cloudcls, len_field, (jlong)len);

	jbyte* pdata = (jbyte*) data;
	jbyteArray jbarray = (env)->NewByteArray((jsize)len);
	(env)->SetByteArrayRegion(jbarray, 0, len, pdata);            //将Jbyte 转换为jbarray数组
	(env)->CallVoidMethod(cloudcls, data_method,jbarray);         //回调java set方法

	if(data != NULL)
	{
		GHDSClient_Release_Data(temphandle, data);
	}

	return ret;
}


/**
 * 释放数据
 *
 */

JNIEXPORT void JNICALL native_ghdscclient_release_data(JNIEnv *env, jclass clazz, jobject cloudcls, jbyteArray data)
{


	jfieldID cloudhandle_field = gCloudParam.lCloudHandle;
	jlong cloudhandle = (jlong)(env->GetLongField(cloudcls, cloudhandle_field));
	HANDLE_GHDSCCLIENT temphandle = (HANDLE_GHDSCCLIENT)cloudhandle;

	int size=(env)->GetArrayLength(data);
	void *p;
	p = (void*)(env)->GetByteArrayElements(data,JNI_FALSE);
	GHDSClient_Release_Data(temphandle, p);
	data = NULL;
}



// Java和JNI函数的绑定表
#ifdef _DEBUG
	static JNINativeMethod method_table[] = {
		{ "Native_GHDSCClient_Init", "(Lcom/hao/testdynicjni/CloudHandle;)I", (void*)native_ghdscclient_init },
		{ "Native_GHDSCClient_Fini", "(Lcom/hao/testdynicjni/CloudHandle;)V", (void*)native_ghdscclient_fini },
		{ "Native_GHDSCClient_Create", "(Lcom/hao/testdynicjni/CloudHandle;)I", (void*)native_ghdscclient_create },
		{ "Native_GHDSCClient_Destory", "(Lcom/hao/testdynicjni/CloudHandle;)V", (void*)native_ghdscclient_destory },
		{ "Native_GHDSCClient_Connect", "(Lcom/hao/testdynicjni/CloudHandle;)I", (void*)native_ghdscclient_connect },
		{ "Native_GHDSCClient_IsConnect", "(Lcom/hao/testdynicjni/CloudHandle;)Z", (void*)native_ghdscclient_isconnect },
		{ "Native_GHDSCClient_DisConnect", "(Lcom/hao/testdynicjni/CloudHandle;)V", (void*)native_ghdscclient_disconnect },
		{ "Native_GHDSCClient_Query_Count_Event", "(Lcom/hao/testdynicjni/CloudHandle;)I", (void*)native_ghdscclient_query_count_event },
		{ "Native_GHDSCClient_Query_Data_Event", "(Lcom/hao/testdynicjni/CloudHandle;)I", (void*)native_ghdscclient_query_data_event },
		{ "Native_GHDSCClient_Query_Count_Video", "(Lcom/hao/testdynicjni/CloudHandle;)I", (void*)native_ghdscclient_query_count_video },
		{ "Native_GHDSCClient_Query_Month_Count_Video", "(Lcom/hao/testdynicjni/CloudHandle;)I", (void*)native_ghdscclient_query_month_count_video },
		{ "Native_GHDSCClient_Query_Data_Video", "(Lcom/hao/testdynicjni/CloudHandle;)I", (void*)native_ghdscclient_query_data_video },
		{ "Native_GHDSCClient_Download_Info_Video", "(Lcom/hao/testdynicjni/CloudHandle;)I", (void*)native_ghdscclient_download_info_video },
		{ "Native_GHDSCClient_Download_data_Video", "(Lcom/hao/testdynicjni/CloudHandle;)I", (void*)native_ghdscclient_download_data_video },
		{ "Native_GHDSCClient_Release_Data", "(Lcom/hao/testdynicjni/CloudHandle;[B)V", (void*)native_ghdscclient_release_data },
	};
#else
	static JNINativeMethod method_table[] = {
		{ "Native_GHDSCClient_Init", "(Lcom/my51c/see51/data/CloudHandle;)I", (void*)native_ghdscclient_init },
		{ "Native_GHDSCClient_Fini", "(Lcom/my51c/see51/data/CloudHandle;)V", (void*)native_ghdscclient_fini },
		{ "Native_GHDSCClient_Create", "(Lcom/my51c/see51/data/CloudHandle;)I", (void*)native_ghdscclient_create },
		{ "Native_GHDSCClient_Destory", "(Lcom/my51c/see51/data/CloudHandle;)V", (void*)native_ghdscclient_destory },
		{ "Native_GHDSCClient_Connect", "(Lcom/my51c/see51/data/CloudHandle;)I", (void*)native_ghdscclient_connect },
		{ "Native_GHDSCClient_IsConnect", "(Lcom/my51c/see51/data/CloudHandle;)Z", (void*)native_ghdscclient_isconnect },
		{ "Native_GHDSCClient_DisConnect", "(Lcom/my51c/see51/data/CloudHandle;)V", (void*)native_ghdscclient_disconnect },
		{ "Native_GHDSCClient_Query_Count_Event", "(Lcom/my51c/see51/data/CloudHandle;)I", (void*)native_ghdscclient_query_count_event },
		{ "Native_GHDSCClient_Query_Data_Event", "(Lcom/my51c/see51/data/CloudHandle;)I", (void*)native_ghdscclient_query_data_event },
		{ "Native_GHDSCClient_Query_Count_Video", "(Lcom/my51c/see51/data/CloudHandle;)I", (void*)native_ghdscclient_query_count_video },
		{ "Native_GHDSCClient_Query_Month_Count_Video", "(Lcom/my51c/see51/data/CloudHandle;)I", (void*)native_ghdscclient_query_month_count_video },
		{ "Native_GHDSCClient_Query_Data_Video", "(Lcom/my51c/see51/data/CloudHandle;)I", (void*)native_ghdscclient_query_data_video },
		{ "Native_GHDSCClient_Download_Info_Video", "(Lcom/my51c/see51/data/CloudHandle;)I", (void*)native_ghdscclient_download_info_video },
		{ "Native_GHDSCClient_Download_data_Video", "(Lcom/my51c/see51/data/CloudHandle;)I", (void*)native_ghdscclient_download_data_video },
		{ "Native_GHDSCClient_Release_Data", "(Lcom/my51c/see51/data/CloudHandle;[B)V", (void*)native_ghdscclient_release_data },
	};
#endif
// 注册native方法到java中
static int registerNativeMethods(JNIEnv* env, const char* className,
        JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;
    clazz = (env)->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if ((env)->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

int register_ndk_load(JNIEnv *env)
{
	nativeClassInit(env);
    // 调用注册方法
    return registerNativeMethods(env, JNIREG_CLASS,
            method_table, NELEM(method_table));
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if ((vm)->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    register_ndk_load(env);

    // 返回jni的版本
    return JNI_VERSION_1_4;
}


}
