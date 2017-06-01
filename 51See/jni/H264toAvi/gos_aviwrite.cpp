/*
作者： 刘志强
时间： 2011-08-23
描述： 将网络来的视频流(*.H264)和音频流打包成AVI文件，边收边写文件，这里完成全部模块功能的实现，有且仅有一个接口gos_package()
	   为尽量缩减写文件时间，及方便手持端回放时解封装，在保证最常用播放器能播放的前提下，只写movi和idx1块，其它块省略，因此可
	   能会存在某些播放器不能打开的情况。
	   目前验证过的播放器是暴风影音和QVOD，其它播放器没有测试。
要求： 要求输入视频文件是H.264标准码流，音频采样率为8k的g711。
*/

//#include "stdafx.h"
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "gos_avi_write.h"
#include "gos_avi_global.h"

//#define CONCAT(X,X)      X##X

//#include "GVAPClient.h"
//#include "GVAPPlayer.h"


#if 0//just for refer

            case DVR_RECORD_START:
				{
					printf("Get message DVR_RECORD_START ");
					//++多路录像时命令连续发，连续创建多个线程
					gCur_ch = msg.string[0]; //++录像通道号				
					strcpy(channel_procparm[gCur_ch].filename, &msg.string[1]);//++录像文件名
					printf("and avi name is %s\n", channel_procparm[gCur_ch].filename);
					channel_procparm[gCur_ch].avi_enable = 1;
					if(0 != (pthread_create(&pthreadaviw[gCur_ch], NULL, (void *)avi_write_process, &channel_procparm[gCur_ch])))
					{
						OSA_ERROR("avi_write thread create error!!!\n");
					}


#endif



int signlist[10] = {RIFFID, AVIID, LIST, HDRL, AVIHFORMAT, STRL, STRH, STRFFORMAT, MOVI, IDX1FORMAT};

short int Bitrate[2][14] = {32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320,       8, 16, 24, 32, 64, 80, 56, 64, 128, 160, 112, 128, 256, 320};

float Sampling_rate[2][3] = {44.1, 48, 32, 22.05, 24, 16}; 

//unsigned int	g_total_vframe;
// unsigned int	g_total_aframe;  //++视频帧总数应该等于音频帧总数

static channel_ProcParm* pProcParm;


static	int  readBytesFromFile( byteStream_s * str )
{
	int  n;
	
	if( str->bitbufLen - str->bitbufDataLen < NALBUF_BLOCK_SIZE )
	{	

		str->bitbuf = (unsigned char*) realloc( str->bitbuf, str->bitbufLen + NALBUF_BLOCK_SIZE );
		
		if( str->bitbuf == NULL )
		{
			printf( "Cannot resize bitbuffer\n" );
			return -1;
		}
		str->bitbufLen += NALBUF_BLOCK_SIZE;
	}
	
	/* Read block of data */
	n = ( int ) fread( str->bitbuf + str->bitbufDataLen, 1, NALBUF_BLOCK_SIZE, str->fn );
	
	str->bytesRead	   += n;
	str->bitbufDataLen += n;
	
	return n;
}
static	int  findStartCode( byteStream_s * str )
{
	int  numZeros;
	int  startCodeFound;
	int  i;
	int  currByte;
	
	numZeros	   = 0;
	startCodeFound = 0;
	
	i = str->bitbufDataPos;
	
	while( !startCodeFound )
	{		
		if( i == str->bitbufDataLen )
		{																	    		
			int  n = readBytesFromFile( str );
			
			if( n == 0 )								
				break;
		}
		
	
		while( i < str->bitbufDataLen )
		{			
			currByte = str->bitbuf[ i ];
			i++;
			
			if( currByte > 1 )											    
				numZeros = 0;
			else
				if( currByte == 0 )  								    
					numZeros++;
				else
				{															    
					if( numZeros > 1 )
					{														    
						startCodeFound = 1;
						break;
					}
					numZeros = 0;
				}
		}
	}
	
	str->bitbufDataPos = i;
	
	if( startCodeFound )
		return ( numZeros + 1 );
	else
		return 0;
}

static int  getNalunitBits_ByteStream( void * strIn )
{
	byteStream_s  * str;
	int  			numRemainingBytes;
	int  			startCodeLen;
	int  			nalUnitStartPos;

	str = ( byteStream_s * ) strIn;


	numRemainingBytes = str->bitbufDataLen - str->bitbufDataPos;

	if( numRemainingBytes > 0 )
	{
		memcpy( str->bitbuf, str->bitbuf + str->bitbufDataPos, numRemainingBytes );
	}


	str->bitbufDataLen = numRemainingBytes;
	str->bitbufDataPos = 0;


	startCodeLen = findStartCode( str );

	if( startCodeLen == 0 )
		return 0;

	nalUnitStartPos = str->bitbufDataPos - startCodeLen;

	startCodeLen = findStartCode( str );

	if( startCodeLen != 0 )
		str->bitbufDataPos -= startCodeLen;

	str->bitbufNalunit	  = str->bitbuf + nalUnitStartPos;
	str->bitbufNalunitLen = str->bitbufDataPos;

	return 1;
}

FILE *fa = NULL;
FILE *fv = NULL;
static int gos_movi_write(FILE *outFile, video_frame *video_head, audio_frame *audio_head, int *movi_size, int MicroSecPerFrame, void *pRecorder)
{
	int				status = 0;
	video_frame    *video_move=video_head;    
	audio_frame	   *audio_move=audio_head;   
	int				sign_list[2]={DCID, WBID};
	char			zero=0;
	byteStream_s	strByte;
	int				nalHeaderByte, nalType;
	char		   *Ps = NULL;
	FILE           *Fa = NULL;
	int             read_count   = 0;
	unsigned int    audioF_head  = 0;

	int		        nopadsize;        
	unsigned int    bitrateindex;         
	unsigned int    sampleindex;         
	unsigned int    mpegversion;
	float           frame_dur;          
	int				vframe_over = 0;
	int				aframe_over = 0;

	channel_ProcParm *pRec = (channel_ProcParm *) pRecorder;
	stFrameHeader 	  *pFrameHead;
	int				  ret = 0;
	int 			  av_type = 0;  //++1:V, 2: A
	Uint8             iFlag = 0;
	int               A = 0;
	int               V = 0;
	AVFrameData		  pFrameData ;


//fa = fopen("a.g711", "wb");
//fv = fopen("v.264",  "wb");

//	msg_send(DVR_RECORD_START_OK);
TRACE("codec send DVR_RECORD_START_OK and enable = %d\n", pRec->avi_enable);

	//++循环读取当前摄像头套接字，获取音频帧或视频帧数据，读一帧写一帧，音视频是严格按照时间顺序来的，
	//++解析帧头，将帧大小存储下来，并分别计算音视频帧的时间戳
	//++这里是以读文件来模拟的，假定音频文件的第一帧信息已经获得，由此可以计算出时间戳，与同时读出的V帧时间戳比较，决定AV的写文件顺序
	//++实际情况下，应该先接收第一帧音频数据，解析完头部信息后再回头写音频的strf结构体。这可能会导致一个bug!!!
//	pRec->m_pRecFrameData = (AVFrameData*)calloc(1,sizeof(AVFrameData));
//	pRec->m_pRecFrameData->pszBuf = (char*)calloc(1,0x2ffff);
//	AVFrameData * pFrameData = pRec->m_pRecFrameData;
	pFrameData.pszBuf = (CHAR *)pRec->av_tbuf;

	while (pRec->avi_enable)
	{
		ret = -1;
		//ret = gs_GetOneAviframe( channel, channel->av_tbuf, &FrameHead);
		//H264Frame pData = channel->m_bufAviQueue;
		if (!pRec->m_bufAviQueue.GetBuf(&pFrameData))
		{	Sleep(1);
			continue;
		}
		
		if(&pFrameData != NULL)
		{
#if 1			
			av_type = pFrameData.fheader.nAVType;//avtype
			//TRACE("************current frame type: %d, frame length:%d\n",pFrameData.fheader.nIFrame,pFrameData.fheader.dwSize);
			pFrameHead = &pFrameData.fheader;
			if(av_type == 1)
			{
				if(pFrameHead->nAVType == 1)  
					iFlag = 1;

				if(iFlag)
				{
					video_move->nextframe=(video_frame*)malloc(sizeof(video_frame));
					video_move = video_move->nextframe;
					memset(video_move, 0, sizeof(video_frame));
					video_move->buf = NULL;	
					
					video_move->frame_size = (int)pFrameHead->dwSize;

					/*video_move->frame_time = pRec->g_total_vframe*MicroSecPerFrame/1000;*/ 
					video_move->frame_time = pRec->g_total_vframe*(1000000/pFrameHead->dwFrameRate)/1000;
					TRACE("****** frame rate:%d\n", pFrameHead->dwFrameRate);
					fwrite(&sign_list[0], 4, 1, outFile);
					fwrite(&video_move->frame_size, 4, 1, outFile);
					fwrite(pFrameData.pszBuf, video_move->frame_size, 1, outFile);
					//fwrite(pFrameData.pszBuf, video_move->frame_size, 1, fv);
					if(video_move->frame_size%2){
						fwrite(&zero, 1, 1, outFile);
					}
					
					fflush(outFile);
					*movi_size+=8;             
					*movi_size+=video_move->frame_size+video_move->frame_size%2;
					video_move->keyframe = (pFrameHead->nIFrame&0xff==1) ? 2 : 0;
					pRec->g_total_vframe++;	
				}
			}
			else if(av_type == 2)
			{
				if(pFrameHead->dwSize != 1024)
				{
					printf("audio frame size is %d not equal 1024!!!\n", pFrameHead->dwSize);
				}
				audio_move->nextframe = (audio_frame*)malloc(sizeof(audio_frame));
				audio_move = audio_move->nextframe;
				memset(audio_move, 0, sizeof(audio_frame));				
				audio_move->buf = NULL;
							
				frame_dur = 1000/(float)40;		//以毫秒为单位
				
				audio_move->version			  = 0;
				audio_move->Bitrate_index	  = 0;
				audio_move->duration_perframe = frame_dur;
				audio_move->Sampling_index	  = 0;					   
				
				audio_move->frame_size  = pFrameHead->dwSize;
				audio_move->frame_time  = frame_dur * pRec->g_total_aframe;

				fwrite(&sign_list[1], 4, 1, outFile);
				fwrite(&audio_move->frame_size, 4, 1,outFile);
				fwrite(pRec->av_tbuf, audio_move->frame_size, 1, outFile);
				if(audio_move->frame_size%2){
					fwrite(&zero, 1, 1, outFile);
				}
				fflush(outFile);
				*movi_size += 8;  //01wb和它的大小
				*movi_size += audio_move->frame_size + audio_move->frame_size%2;

				pRec->g_total_aframe++;
			}
			else
			{ printf("Error, not supported av_type!!!\n"); }
		}
#endif	
//	TRACE("++++channel->avi_enalbe = %d, movi_size = %d \n", pRec->avi_enable, *movi_size);
	}

//TRACE("**********pRec->avi_enalbe = %d, movi_size = %d \n", pRec->avi_enable, *movi_size);
	fflush(outFile);
//fclose(fa);
//fclose(fv);
	return 1;
}

static int gos_idx1_write(FILE *outFile, audio_frame *audio_head, video_frame *video_head, int *idx1_size)
{
	audio_frame   *audio_move=audio_head->nextframe;       
	video_frame   *video_move=video_head->nextframe;  
	gos_AVIINDEXENTRY  myindex;                               
	int sign_list[2];
	int current_off = 4;                     

	sign_list[0]=DCID;
	sign_list[1]=WBID;
	
	while(audio_move && video_move){
		if(audio_move->frame_time<video_move->frame_time){
			myindex.ckid=WBID;
			myindex.dwFlags=2;
			myindex.dwChunkOffset=current_off;
			myindex.dwChunkLength=audio_move->frame_size;
			
			fwrite(&myindex, sizeof(gos_AVIINDEXENTRY), 1, outFile);
			fflush(outFile);
			
			current_off+=8;    
			current_off+=myindex.dwChunkLength+audio_move->frame_size%2;
			
			*idx1_size+=sizeof(gos_AVIINDEXENTRY);
			audio_move=audio_move->nextframe;
		}//if
		else{
			myindex.ckid=DCID;
			myindex.dwChunkOffset=current_off;
			myindex.dwChunkLength=video_move->frame_size;
			if(video_move->keyframe){
				myindex.dwFlags=2;
			}
			else{
				myindex.dwFlags=0;
			}
			
			fwrite(&myindex, sizeof(gos_AVIINDEXENTRY), 1, outFile);
			fflush(outFile);
			
			current_off+=8;
			current_off+=myindex.dwChunkLength+video_move->frame_size%2;
			
			*idx1_size+=sizeof(gos_AVIINDEXENTRY);
			video_move=video_move->nextframe;
		}//else
	}//while(audio_move && video_move)
	while(audio_move){
		myindex.ckid=WBID;
		myindex.dwFlags=2;
		myindex.dwChunkOffset=current_off;
		myindex.dwChunkLength=audio_move->frame_size;
		
		fwrite(&myindex, sizeof(gos_AVIINDEXENTRY), 1, outFile);
		fflush(outFile);
		
		current_off+=8;    
		current_off+=myindex.dwChunkLength+audio_move->frame_size%2;
		
		*idx1_size+=sizeof(gos_AVIINDEXENTRY);
		audio_move=audio_move->nextframe;
	}//while(audio_move)
	while(video_move){
		myindex.ckid=DCID;
		myindex.dwChunkOffset=current_off;
		myindex.dwChunkLength=video_move->frame_size;
		if(video_move->keyframe){
			myindex.dwFlags=2;
		}
		else{
			myindex.dwFlags=0;
		}
		
		fwrite(&myindex, sizeof(gos_AVIINDEXENTRY), 1, outFile);
		fflush(outFile);
		
		current_off+=8;
		current_off+=myindex.dwChunkLength+video_move->frame_size%2;
		
		*idx1_size+=sizeof(gos_AVIINDEXENTRY);
		video_move=video_move->nextframe;
	}//while(video_move)
	return 1;
}

int GOS_aviWriteRun(FILE *outAviFile, channel_ProcParm *pRecorder)
{
	int status = 0;
	int file_size;                    
	int hdrl_size;                    
	int	movi_size=0;                 
	int strl_size;                   
	int avihsize=0x38;                
	int strfsize_video=0x28;          
	int strfsize_audio=0x1e;         
	char access_suc;
	int movi_locate;  
	int idx1_locate;
	int idx1_size=0; 
	int junk_size=0x1014;            
	int junk4=0x4;
	int junk_dc=DCID;
	int junk_wb=WBID;
	int myodml=ODMLID;
	int mydmlh=DMLHID;
	int f8=248;
	int zero=0;
	char *zerobuf;
	int  junk=JUNKFORMAT;
	int  junk_locate;

	unsigned char infocontext[12] = {0x4c, 0x61, 0x76, 0x66, 0x35, 0x31, 0x2e, 0x31, 0x32, 0x2e, 0x32, 0x0};  
	int info_size=24;
	int myinfo=INFOID;
	int myisft=ISFTID;
	int isft_size=12;

	int Width;                        
	int Height;                      
    int MicroSecPerFrame;           
	int FramesPerSec;                
	video_frame    *video_head;      
	video_frame    *video_free;

//++*****************************audio(mpeg1 mpeg2 layer3)****************
	unsigned char extrainfo[12]={1, 0, 2, 0, 0, 0, 128, 4, 1, 0, 113, 5};  
	audio_frame   *audio_head;      
	audio_frame   *audio_free;
	unsigned int  audio_version;

//*******************************avi************************************
	gos_MainAVIHeader    gs_avih;          
	gos_AVIStreamHeader  gs_strh_video;    
	gos_AVIStreamHeader  gs_strh_audio;    
	gos_BITMAPINFOHEADER gs_strf_video;    
	gos_WAVEFORMATEX     gs_strf_audio;    
	
	video_head = (video_frame*)malloc(sizeof(video_frame));
	audio_head = (audio_frame*)malloc(sizeof(audio_frame));
	memset(video_head, 0, sizeof(video_frame));
	memset(audio_head, 0, sizeof(audio_frame));
	pRecorder->g_total_vframe = 0;
	pRecorder->g_total_aframe = 0;

	FramesPerSec = 8;
	Width  = IMG_W;
	Height = IMG_H;
	MicroSecPerFrame=1000000/FramesPerSec;

	gs_avih.dwFlags = 2;         
	gs_avih.dwHeight = Height;
	gs_avih.dwInitialFrames = 0;
	gs_avih.dwMaxBytesPerSec = 41000;
	gs_avih.dwMicroSecPerFrame = MicroSecPerFrame;  
	gs_avih.dwPaddingGranularity = 0;
	gs_avih.dwReserved[0] = 0;
	gs_avih.dwReserved[1] = 0;
	gs_avih.dwReserved[2] = 0;
	gs_avih.dwReserved[3] = 0;
	gs_avih.dwStreams = 2;
	gs_avih.dwSuggestedBufferSize = 1048576;
	gs_avih.dwTotalFrames = 0; //VideoTotalFrames;	   //++视频的总帧数是未知的...	先写0
	gs_avih.dwWidth = Width;
	

	gs_strh_video.dwFlags = 0;
	gs_strh_video.dwInitialFrames = 0;
	gs_strh_video.dwLength = 0;//VideoTotalFrames;	   //++视频的总帧数是未知的...
	gs_strh_video.dwQuality = -1;
	gs_strh_video.dwRate = 1000000/MicroSecPerFrame;
	gs_strh_video.dwSampleSize = 0;
	gs_strh_video.dwScale = 1;
	gs_strh_video.dwStart = 0;
	gs_strh_video.dwSuggestedBufferSize = 1048576;   //0x 10 00 00
	gs_strh_video.fccHandler = H264ID;
	gs_strh_video.fccType = VIDEOID;
	gs_strh_video.rcFrame.bottom = Height;
	gs_strh_video.rcFrame.left = 0;
	gs_strh_video.rcFrame.right = Width;
	gs_strh_video.rcFrame.top = 0;
	gs_strh_video.wLanguage = 0;
	gs_strh_video.wPriority = 0;
	
	//填充strh audio	
	gs_strh_audio.fccType = AUDIOID;
	gs_strh_audio.fccHandler = G711ID;
	gs_strh_audio.dwFlags = 0;
	gs_strh_audio.wPriority = 0;
	gs_strh_audio.wLanguage = 0;
	gs_strh_audio.dwInitialFrames = 0;
	gs_strh_audio.dwScale = 1;
	gs_strh_audio.dwRate = 8000;
	gs_strh_audio.dwStart = 0;
	gs_strh_audio.dwLength = 1024;
	gs_strh_audio.dwSuggestedBufferSize = 32768;       //0x 30 00
	gs_strh_audio.dwQuality = 0;
	gs_strh_audio.dwSampleSize = 8000;
	gs_strh_audio.rcFrame.top = 0;
	gs_strh_audio.rcFrame.left = 0;
	gs_strh_audio.rcFrame.bottom = 0;
	gs_strh_audio.rcFrame.right = 0;


	gs_strf_video.biBitCount = 24;
	gs_strf_video.biClrImportant = 0;
	gs_strf_video.biClrUsed = 0;
	gs_strf_video.biCompression = H264ID;
	gs_strf_video.biHeight = Height;
	gs_strf_video.biPlanes = 1;
	gs_strf_video.biSize = 40;
	gs_strf_video.biSizeImage = 391680;
	gs_strf_video.biWidth = Width;
	gs_strf_video.biXPelsPerMeter = 0;
	gs_strf_video.biYPelsPerMeter = 0;
	
	
	gs_strf_audio.wFormatTag = 0x07; 
	gs_strf_audio.nChannels = 0x01;
	gs_strf_audio.nSamplesPerSec = 8000;
	gs_strf_audio.wBitsPerSample = 8;
	gs_strf_audio.nBlockAlign = gs_strf_audio.nChannels * (gs_strf_audio.wBitsPerSample/8) ;
	gs_strf_audio.nAvgBytesPerSec = gs_strf_audio.nBlockAlign * gs_strf_audio.nSamplesPerSec; 
	gs_strf_audio.cbSize = 18;
	


	fwrite(&signlist[0], 4, 1, outAviFile);   
	fseek(outAviFile, 4, SEEK_CUR);				//暂时不写文件大小!!!!!!!!!!!!!!!!shmily_liu!!!!!!!!!!!!!!!!!!!!
	fwrite(&signlist[1], 4, 1, outAviFile);    
	fwrite(&signlist[2], 4, 1, outAviFile);    

	hdrl_size=8822;		
	fwrite(&hdrl_size, 4, 1, outAviFile);
	fwrite(&signlist[3], 4, 1, outAviFile);     //hdrl
	fwrite(&signlist[4], 4, 1, outAviFile);     //avih
	fwrite(&avihsize, 4, 1, outAviFile);        //avih的大小
	fwrite(&gs_avih, sizeof(gos_MainAVIHeader), 1, outAviFile);   
	fflush(outAviFile);


	fwrite(&signlist[2], 4, 1, outAviFile);     //LIST

	strl_size=4+4+4+sizeof(gos_AVIStreamHeader)+4+4+sizeof(gos_BITMAPINFOHEADER)+4124;   
	fwrite(&strl_size, 4, 1, outAviFile);

	fwrite(&signlist[5], 4, 1, outAviFile);     
	fwrite(&signlist[6], 4, 1, outAviFile);    
	fwrite(&avihsize, 4, 1, outAviFile);        
	fwrite(&gs_strh_video, sizeof(gos_AVIStreamHeader), 1, outAviFile);  

	fwrite(&signlist[7], 4, 1, outAviFile);  
	fwrite(&strfsize_video, 4, 1, outAviFile); 
	fwrite(&gs_strf_video, sizeof(gos_BITMAPINFOHEADER), 1, outAviFile); 
	fflush(outAviFile);

	fwrite(&junk, 4, 1, outAviFile);

	fwrite(&junk_size, 4, 1, outAviFile);
	fwrite(&junk4, 4, 1, outAviFile);
	fwrite(&zero, 4, 1, outAviFile);
	fwrite(&junk_dc, 4, 1, outAviFile);
	junk_locate=ftell(outAviFile);

	zerobuf=(char*)malloc(4336-junk_locate);             
	memset(zerobuf, 0, 4336-junk_locate);
	fwrite(zerobuf, 4336-junk_locate, 1, outAviFile);
	fflush(outAviFile);
	free(zerobuf);


	fwrite(&signlist[2], 4, 1, outAviFile);     //LIST

	strl_size=4230;
	fwrite(&strl_size, 4, 1, outAviFile);
	fwrite(&signlist[5], 4, 1, outAviFile);     //strl
	fwrite(&signlist[6], 4, 1, outAviFile);     //strh
	fwrite(&avihsize, 4, 1, outAviFile);        //strh的大小
	fwrite(&gs_strh_audio, sizeof(gos_AVIStreamHeader), 1, outAviFile);  //strh的内容

	//++shmily_liu: 可以暂时不写strl内容，等解码到第一个音频帧获得相关参数后再回头写，记下strl_local
	fwrite(&signlist[7], 4, 1, outAviFile);     //strf
	fwrite(&strfsize_audio, 4, 1, outAviFile);  //strf的大小
	fwrite(&gs_strf_audio, sizeof(gos_WAVEFORMATEX), 1, outAviFile);  //strf
	fseek(outAviFile, -2, SEEK_CUR);			

	fwrite(extrainfo, 12, 1, outAviFile);	
	fflush(outAviFile);

	junk_size=4116;
	fwrite(&junk, 4, 1, outAviFile);
	fwrite(&junk_size, 4, 1, outAviFile);
	fwrite(&junk4, 4, 1, outAviFile);
	fwrite(&zero, 4, 1, outAviFile);
	fwrite(&junk_wb, 4, 1, outAviFile);
	junk_locate=ftell(outAviFile);

	zerobuf=(char*)malloc(4104);		
	memset(zerobuf, 0, 4104);

	fwrite(zerobuf, 4104, 1, outAviFile);   //movi列表必须从0X26AA开始
	fflush(outAviFile);
	free(zerobuf);


	fwrite(&junk, 4, 1, outAviFile);
	junk_size=260;
	fwrite(&junk_size, 4, 1, outAviFile);
	fwrite(&myodml, 4, 1, outAviFile);	
	fwrite(&mydmlh, 4, 1, outAviFile);	
	fwrite(&f8, 4, 1, outAviFile);
	zerobuf=(char*)malloc(248);
	memset(zerobuf, 0, 248);
	fwrite(zerobuf, 248, 1, outAviFile);
	fflush(outAviFile);
	free(zerobuf);


	fwrite(&signlist[2], 4, 1, outAviFile);    //LIST
	fwrite(&info_size, 4, 1, outAviFile);
	fwrite(&myinfo, 4, 1, outAviFile);        //INFO
	fwrite(&myisft, 4, 1, outAviFile);        //ISFT
	fwrite(&isft_size, 4, 1, outAviFile);
	
	fwrite(infocontext, 12, 1, outAviFile);  
	fflush(outAviFile);

//*****************************JUNK 1016**************************************
	fwrite(&junk, 4, 1, outAviFile);
	junk_size=1016;
	fwrite(&junk_size, 4, 1, outAviFile);
	zerobuf=(char*)malloc(1016);
	memset(zerobuf, 0, 1016);
	fwrite(zerobuf, 1016, 1, outAviFile);
	fflush(outAviFile);
	free(zerobuf);


	fwrite(&signlist[2], 4, 1, outAviFile);		//list	
	movi_locate=ftell(outAviFile);
	fseek(outAviFile, 4, SEEK_CUR);				
	fwrite(&signlist[8], 4, 1, outAviFile);		//movi
    access_suc=gos_movi_write(outAviFile, video_head, audio_head, &movi_size, MicroSecPerFrame, pRecorder);
	if(access_suc==0){
		printf("write movi wrong\n");
		return 0;
	}
	movi_size+=4;								//"movi"	

TRACE("======== movi_locate = %d, movi_size = %d \n",movi_locate, movi_size);

#if 0
//****************************生成idx1列表************************************
	fwrite(&signlist[9], 4, 1, outAviFile);		//idx1
	idx1_locate=ftell(outAviFile);				//暂时不写idx1列表长度
	fseek(outAviFile, 4, SEEK_CUR);
    access_suc=gos_idx1_write(outAviFile, audio_head, video_head, &idx1_size);
	if(access_suc==0){
		printf("write idx1 wrong\n");
		return 0;
	}
printf("idx1_size = %d and addr = %d\n", idx1_size, idx1_locate);
#endif
//******************************回写部分**************************************

    fseek(outAviFile, movi_locate, SEEK_SET);
 	fwrite(&movi_size, 4, 1, outAviFile);
#if 0

	fseek(outAviFile, idx1_locate, SEEK_SET);
	fwrite(&idx1_size, 4, 1, outAviFile);
 	

	fseek(outAviFile, 4, SEEK_SET);
	file_size=hdrl_size+movi_size+idx1_size+8+8+4+8+1016+8+24+8;   //++hdrl; movi; avi; idx1; junk; junk; info; info
	fwrite(&file_size, 4, 1, outAviFile);	
	fflush(outAviFile);
#endif

//释放音频帧链表和视频帧链表的内存单元
	video_free=video_head;
	audio_free=audio_head;
	while(video_free){
		video_head=video_free->nextframe;
		free(video_free);
		video_free=video_head;
	}
	while(audio_free){
		audio_head=audio_free->nextframe;
		free(audio_free);
		audio_free=audio_head;
	}

	return status;
}

Uint8 *GOS_aviWriteCreate( )
{
	Uint8 *buf = NULL;

	buf = (Uint8 *)malloc(100*1024);          	
    if(buf == NULL) 
	{
        return NULL;
    }

	return buf;
}

int GOS_aviWriteDelete(Uint8 *av_tbuf)
{
    if(av_tbuf) 
	{
        free(av_tbuf);
    }
    
	return 1;
}

jbealoon Java_com_goscam_ulife_media_H264toAvi_initParam(JNIEnv* env, jobject thiz){
	//jmethodID mid;
	jclass clazz;
	jfieldID fid;
	
	pProcParm = (channel_ProcParm*)malloc(sizeof(channel_ProcParm))	
	
	if(pProcParm ==null){
		return false;
	}
	pProcParm->av_tbuf = GOS_aviWriteCreate();
	
	clazz = env->GetObjectClass(env,thiz);	
	fid = (*env)->GetFieldID(env, clazz, "gs_ServerIP", "Ljava/lang/String;");
	pProcParm->gs_ServerIP= (*env)->GetObjectField(env, this, fid);	
	
	fid = (*env)->GetFieldID(env, clazz, "gs_Username", "Ljava/lang/String;");
	pProcParm->gs_Username= (*env)->GetObjectField(env, this, fid);		
		
	fid = (*env)->GetFieldID(env, clazz, "gs_Password", "Ljava/lang/String;");
	pProcParm->gs_Password= (*env)->GetObjectField(env, this, fid);	
	
	fid = (*env)->GetFieldID(env, clazz, "gs_CamSerial", "Ljava/lang/String;");
	pProcParm->gs_CamSerial= (*env)->GetObjectField(env, this, fid);	
		
	fid = (*env)->GetFieldID(env, clazz, "gs_SockTrans", "I");
	pProcParm->gs_SockTrans = (*env)->GetIntField(env, this, fid);	
	
	fid = (*env)->GetFieldID(env, clazz, "gs_channel_proc_state", "I");
	pProcParm->gs_channel_proc_state = (*env)->GetIntField(env, this, fid);
	
	fid = (*env)->GetFieldID(env, clazz, "gs_channel_live_state", "I");
	pProcParm->gs_channel_live_state = (*env)->GetIntField(env, this, fid);	
	
	fid = (*env)->GetFieldID(env, clazz, "gs_Video_w", "I");
	pProcParm->gs_Video_w = (*env)->GetIntField(env, this, fid);
	
	fid = (*env)->GetFieldID(env, clazz, "gs_Video_h", "I");
	pProcParm->gs_Video_h = (*env)->GetIntField(env, this, fid);	
	
	fid = (*env)->GetFieldID(env, clazz, "gs_Audioflag", "I");
	pProcParm->gs_Audioflag = (*env)->GetIntField(env, this, fid);
	
	fid = (*env)->GetFieldID(env, clazz, "nBlockedFrame", "I");
	pProcParm->nBlockedFrame = (*env)->GetIntField(env, this, fid);	
	
	fid = (*env)->GetFieldID(env, clazz, "bWaitForIFrame", "I");
	pProcParm->bWaitForIFrame = (*env)->GetIntField(env, this, fid);

	fid = (*env)->GetFieldID(env, clazz, "avi_enable", "I");
	pProcParm->avi_enable = (*env)->GetIntField(env, this, fid);

	fid = (*env)->GetFieldID(env, clazz, "filename", "Ljava/lang/String;");
	pProcParm->filename = (*env)->GetObjectField(env, this, fid);

	fid = (*env)->GetFieldID(env, clazz, "bWaitForIFrame", "I");
	pProcParm->bWaitForIFrame = (*env)->GetIntField(env, this, fid);	
	
//	fid = (*env)->GetFieldID(env, clazz, "av_tbuf", "[byte");
//	pProcParm->av_tbuf = (*env)->GetObjectField(env, this, fid);	
	return  true;			 
}
jbealoon Java_com_goscam_ulife_media_H264toAvi_unInitParam(JNIEnv* env, jobject thiz){
	if (pProcParm != null){
		free(pProcParm); 
		pProcParm = null;		
	}	
}

void Java_com_goscam_ulife_media_H264toAvi_avi_write_process(JNIEnv* env, jobject thiz)
{
	int status = 0;
	FILE *fp = NULL;
	fp = fopen(pProcParm->filename, "wb");

	pProcParm->av_tbuf = GOS_aviWriteCreate();
	if(pProcParm->av_tbuf == NULL)
	{
		printf("GOS_aviWriteCreate error!\n");
		goto exit_lable;
	}

	status = GOS_aviWriteRun(fp, pProcParm);

exit_lable:
	status = GOS_aviWriteDelete(pProcParm->av_tbuf);	
	if(status != 0)
	{
		printf("GOS_aviWriteDelete error!\n");
	}
	fclose(fp);

	printf("\nGOS_aviWriteRun is over!!!\n");
	return ;
}



















