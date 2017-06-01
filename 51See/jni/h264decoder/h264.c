#include <string.h>

#include <assert.h>
#include <jni.h>

#include <stdio.h>
#include <string.h>

#include "lib/libavformat/avformat.h"
#include "lib/libswscale/swscale.h"
#include "lib/libavcodec/avcodec.h"
#include "lib/libavutil/avutil.h"
#include "lib/libavutil/cpu.h"

#include "h264.h"

#define MY_YUV2RGB         1



static	AVCodec            *pCodec;
static	AVPacket           packet;
static	AVCodecContext     *pCodecCtx;
static  AVFrame            *pFrame; 
static	AVPicture          picture;
static	struct SwsContext  *img_convert_ctx;
static  char               alloc_picture_flag = 0;

//===========MY YUV to RGB======================
//////////////////////////////////////////////////////		
//int iWidth=240;
//int iHeight=320;

int lastWidth=320;
int lastHeight=240;
	
int *colortab=NULL;
int *u_b_tab=NULL;
int *u_g_tab=NULL;
int *v_g_tab=NULL;
int *v_r_tab=NULL;

short *tmp_pic=NULL;

unsigned int *rgb_2_pix=NULL;
unsigned int *r_2_pix=NULL;
unsigned int *g_2_pix=NULL;
unsigned int *b_2_pix=NULL;
		
void DeleteYUVTab()
{
	//av_free(tmp_pic);
	
	av_free(colortab);
	av_free(rgb_2_pix);
}

void CreateYUVTab_16()
{
	int i;
	int u, v;
	
	//tmp_pic = (short*)av_malloc(240*1280*2); // 最大320x240，16bits

	colortab = (int *)av_malloc(4*256*sizeof(int));
	u_b_tab = &colortab[0*256];
	u_g_tab = &colortab[1*256];
	v_g_tab = &colortab[2*256];
	v_r_tab = &colortab[3*256];

	for (i=0; i<256; i++)
	{
		u = v = (i-128);

		u_b_tab[i] = (int) ( 1.772 * u);
		u_g_tab[i] = (int) ( 0.34414 * u);
		v_g_tab[i] = (int) ( 0.71414 * v); 
		v_r_tab[i] = (int) ( 1.402 * v);
	}

	rgb_2_pix = (unsigned int *)av_malloc(3*768*sizeof(unsigned int));

	r_2_pix = &rgb_2_pix[0*768];
	g_2_pix = &rgb_2_pix[1*768];
	b_2_pix = &rgb_2_pix[2*768];

	for(i=0; i<256; i++)
	{
		r_2_pix[i] = 0;
		g_2_pix[i] = 0;
		b_2_pix[i] = 0;
	}

	for(i=0; i<256; i++)
	{
		r_2_pix[i+256] = (i & 0xF8) << 8;
		g_2_pix[i+256] = (i & 0xFC) << 3;
		b_2_pix[i+256] = (i ) >> 3;
	}

	for(i=0; i<256; i++)
	{
		r_2_pix[i+512] = 0xF8 << 8;
		g_2_pix[i+512] = 0xFC << 3;
		b_2_pix[i+512] = 0x1F;
	}

	r_2_pix += 256;
	g_2_pix += 256;
	b_2_pix += 256;
}

void DisplayYUV_16(unsigned int *pdst1, unsigned char *y, unsigned char *u, unsigned char *v, int width, int height, int src_ystride, int src_uvstride, int dst_ystride)
{
	int i, j;
	int r, g, b, rgb;

	int yy, ub, ug, vg, vr;

	unsigned char* yoff;
	unsigned char* uoff;
	unsigned char* voff;
	
	unsigned int* pdst=pdst1;

	int width2 = width/2;
	int height2 = height/2;
#if 0	
	if(width2>iWidth/2)
	{
		width2=iWidth/2;

		y+=(width-iWidth)/4*2;
		u+=(width-iWidth)/4;
		v+=(width-iWidth)/4;
	}

	if(height2>iHeight)
		height2=iHeight;
#endif

	for(j=0; j<height2; j++) // 一次2x2共四个像素
	{
		yoff = y + j * 2 * src_ystride;
		uoff = u + j * src_uvstride;
		voff = v + j * src_uvstride;

		for(i=0; i<width2; i++)
		{
			yy  = *(yoff+(i<<1));
			ub = u_b_tab[*(uoff+i)];
			ug = u_g_tab[*(uoff+i)];
			vg = v_g_tab[*(voff+i)];
			vr = v_r_tab[*(voff+i)];

			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			pdst[(j*dst_ystride+i)] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);

			yy = *(yoff+(i<<1)+src_ystride);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+src_ystride+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			pdst [((2*j+1)*dst_ystride+i*2)>>1] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);
		}
	}
}
//===========================================


//======= FFMPEG YUV2RGB====================
//don't use this, need realse func
static void setupScaler() {
    
	// Release old picture and scaler
	//avpicture_free(&picture);
	sws_freeContext(img_convert_ctx);	
	
	// Allocate RGB picture
	//avpicture_alloc(&picture, PIX_FMT_RGB24,pCodecCtx->width,pCodecCtx->height);
	
	// Setup scaler
	static int sws_flags =  SWS_FAST_BILINEAR;
	img_convert_ctx = sws_getContext(pCodecCtx->width, 
									 pCodecCtx->height,
									 pCodecCtx->pix_fmt,
									 pCodecCtx->width, 
									 pCodecCtx->height,
									 PIX_FMT_RGB24,
									 sws_flags, NULL, NULL, NULL);
	
}

static void convertFrameToRGB() 
{
	setupScaler();
	sws_scale (img_convert_ctx,pFrame->data, pFrame->linesize,
			   0, pCodecCtx->height,
			   picture.data, picture.linesize);	
}

//++++++++++++ used in decoder+++++++++++
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

void initGlobal(){
	av_register_all();
}

int initDecoder(){
	
#ifdef 	MY_YUV2RGB
	CreateYUVTab_16();
#endif

	av_init_packet(&packet);
    // Find the decoder for the 264
    pCodec=avcodec_find_decoder(CODEC_ID_H264);
    if(pCodec==NULL)
        goto initError; // Codec not found
    
    pCodecCtx = avcodec_alloc_context();
    // Open codec
    if(avcodec_open(pCodecCtx, pCodec) < 0)
        goto initError; // Could not open codec
    // Allocate video frame
    
    pFrame=avcodec_alloc_frame();
    
    avpicture_alloc(&picture, PIX_FMT_RGB24,lastWidth,lastHeight);
    
    LOGV("InitDecoder success");
    return 0;
    
initError:
    //error action
    LOGV("InitDecoder failed");
	return -1;	
}

int decoderNal(byte* Buf, int nalLen, byte* Pixel, int* Res){
	
	packet.size = nalLen;
    packet.data = (unsigned char *)Buf;
    
    int got_picture_ptr=0;
    int nImageSize;
    nImageSize = avcodec_decode_video2(pCodecCtx,pFrame,&got_picture_ptr,&packet);

	if(nImageSize>0)
	{
		LOGV("nImageSize=%d",nImageSize);

		if (pFrame->data[0])
		{
			LOGV("pCodecCtx->width = %d ",pCodecCtx->width );
			LOGV("pCodecCtx->height  = %d ",pCodecCtx->height);
			// convertFrameToRGB();
			if (pCodecCtx->width != lastWidth ||pCodecCtx->height !=lastHeight)
			{	
				avpicture_free(&picture);
				avpicture_alloc(&picture, PIX_FMT_RGB24,pCodecCtx->width,pCodecCtx->height);	
				lastWidth=pCodecCtx->width ;
				lastHeight=pCodecCtx->height;
			}
			//LOGV("YUV2RGB...");
			//LOGV("Pixel = %p",Pixel);
			//LOGV("pFrame->data[0] = %p, %d-%d-%d-%d",pFrame->data[0],*(pFrame->data[0]),*(pFrame->data[0]+1),*(pFrame->data[0]+2),*(pFrame->data[0]+3));
			//LOGV("pFrame->data[1] = %p, %d-%d-%d-%d",pFrame->data[1],*(pFrame->data[1]),*(pFrame->data[1]+1),*(pFrame->data[1]+2),*(pFrame->data[1]+3));
			//LOGV("pFrame->data[2] = %p, %d-%d-%d-%d",pFrame->data[2],*(pFrame->data[2]),*(pFrame->data[2]+1),*(pFrame->data[2]+2),*(pFrame->data[2]+3));
			//LOGV("pCodecCtx->width = %d",pCodecCtx->width);
			//LOGV("pCodecCtx->height = %d",pCodecCtx->height);
			//LOGV("pFrame->linesize[0] = %d",pFrame->linesize[0]);
			//LOGV("pFrame->linesize[1] = %d",pFrame->linesize[1]);
			//LOGV("pCodecCtx->width = %d",pCodecCtx->width);
			#ifdef 	MY_YUV2RGB
			DisplayYUV_16((int*)Pixel, pFrame->data[0], pFrame->data[1], pFrame->data[2], pCodecCtx->width, pCodecCtx->height, pFrame->linesize[0], pFrame->linesize[1], pCodecCtx->width);		
			#else
			convertFrameToRGB();
			#endif	
			//LOGV("YUV2RGB copnlete!");	
			*Res = pCodecCtx->width;
			*(Res+1) = pCodecCtx->height;
		}	
	}
	
	return nImageSize;
}

void uninitDecoder(){
	
#ifdef MY_YUV2RGB
   DeleteYUVTab();
#else
	sws_freeContext(img_convert_ctx);	
#endif
	// Free RGB picture
	avpicture_free(&picture);
	alloc_picture_flag = 0;
	
    // Free the YUV frame
    av_free(pFrame);
	
    // Close the codec
    if (pCodecCtx) avcodec_close(pCodecCtx);	
}

void uninitGlobal(){
	;
}	

