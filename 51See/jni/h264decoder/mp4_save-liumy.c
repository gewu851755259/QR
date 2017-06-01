/*
 * Copyright (c) 2003 Fabrice Bellard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * @file
 * libavformat API example.
 *
 * Output a media file in any supported libavformat format.
 * The default codecs are used.
 */

#ifdef __cplusplus
extern "C"
{
#endif
	
#ifdef HAVE_AV_CONFIG_H
#undef HAVE_AV_CONFIG_H	
#endif
	
#include "libavcodec\avcodec.h"
#include "libavutil\avutil.h"
#include "libavformat\avformat.h"
	
#pragma comment(lib, "avcodec.lib")
#pragma comment(lib, "avutil.lib")
#pragma comment(lib, "avformat.lib")
#pragma comment(lib, "swscale.lib")
	
#ifdef __cplusplus
}
#endif

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

#include <libavutil/mathematics.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/avutil.h>

#undef exit

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#define GS_AV_NOPTS_VALUE 0x8000000000000000

/* 5 seconds stream duration */
#define STREAM_DURATION   200.0
#define STREAM_FRAME_RATE 25 /* 25 images/s */
#define STREAM_NB_FRAMES  ((int)(STREAM_DURATION * STREAM_FRAME_RATE))
#define STREAM_PIX_FMT    PIX_FMT_YUV420P /* default pix_fmt */

static int sws_flags = SWS_BICUBIC;

/**************************************************************/
/* audio output */

static float t, tincr, tincr2;
static int16_t *samples;
static int audio_input_frame_size;


typedef struct _byteStream_s
{
	FILE				  * fn;  								//	< Bitstream file pointer >	
	unsigned		char  * bitbuf;  							//	< Buffer for stream bits >					    	
	long			        bytesRead;							//	< The number of bytes read from the file >	    
	int  	bitbufLen;							//	< Size of the bit buffer in bytes >  		    
	int  	bitbufDataPos;
	int  	bitbufDataLen;						//	< Length of all the data in the bit buffer in bytes >  								    
	unsigned		char  * bitbufNalunit;						//	< Pointer to first NAL unit in the bitbuffer    																//		>										    
	int  	bitbufNalunitLen;
}	 byteStream_s;


//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
#define NALBUF_BLOCK_SIZE 4*1024

static	int  readBytesFromFile( byteStream_s * str )
{
	int  n;
	
	if( str->bitbufLen - str->bitbufDataLen < NALBUF_BLOCK_SIZE )
	{	
		/* Buffer is too small -> allocate bigger buffer */
		str->bitbuf = realloc( str->bitbuf, str->bitbufLen + NALBUF_BLOCK_SIZE );
		
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
		{														//	  We are at the end of data -> read more from the bitstream file					    		
			int  n = readBytesFromFile( str );
			
			if( n == 0 )										//		End of bitstream -> stop search  		    
				break;
		}
		
		/* Find sequence of 0x00 ... 0x00 0x01 */		
		while( i < str->bitbufDataLen )
		{			
			currByte = str->bitbuf[ i ];
			i++;
			
			if( currByte > 1 )									//If current byte is > 1, it cannot be part of a start code  					    
				numZeros = 0;
			else if( currByte == 0 )  							//If current byte is 0, it could be part of a start code					    
				numZeros++;
			else
			{												//currByte == 1  						    
				if( numZeros > 1 )
				{											//currByte == 1. If numZeros > 1, we found a start code				    
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

  /*
   * Copy valid data to the beginning of the buffer
   */
	numRemainingBytes = str->bitbufDataLen - str->bitbufDataPos;

	if( numRemainingBytes > 0 )
	{
		memcpy( str->bitbuf, str->bitbuf + str->bitbufDataPos, numRemainingBytes );
	}

	/* Update bitbuffer variables */
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
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
#define AUDIO_BLOCK_SIZE 2048

static	int  readBytesFromAACFile( byteStream_s * str )
{
	int  n;
	
	if( str->bitbufLen - str->bitbufDataLen < AUDIO_BLOCK_SIZE )
	{	
		/* Buffer is too small -> allocate bigger buffer */
		str->bitbuf = realloc( str->bitbuf, str->bitbufLen + AUDIO_BLOCK_SIZE );
		
		if( str->bitbuf == NULL )
		{
			printf( "Cannot resize bitbuffer\n" );
			return -1;
		}
		str->bitbufLen += AUDIO_BLOCK_SIZE;
	}
	
	/* Read block of data */
	n = ( int ) fread( str->bitbuf + str->bitbufDataLen, 1, AUDIO_BLOCK_SIZE, str->fn );
	
	str->bytesRead	   += n;
	str->bitbufDataLen += n;
	
	return n;
}

static	int  findStartaudio( byteStream_s * str )
{
	int  numZeros;
	int  startCodeFound;
	int  i;
	int  currByte;

	int  alreadyFind = 0;
	
	numZeros	   = 0;
	startCodeFound = 0;
	
	i = str->bitbufDataPos;
	
	while( !startCodeFound )
	{		
		if( i == str->bitbufDataLen )
		{														//	  We are at the end of data -> read more from the bitstream file					    		
			int  n = readBytesFromAACFile( str );
			
			if( n == 0 )										//		End of bitstream -> stop search  		    
				break;
		}
		
		/* Find sequence of 0xFF 0xF1 0x6C 0x40 */		
		while( i < str->bitbufDataLen )
		{			
			currByte = str->bitbuf[ i ];
			i++;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			if( currByte == 0xFF )
			{
				alreadyFind = 1;
			}
			else if(currByte == 0xF1 && alreadyFind == 1)
			{
				alreadyFind = 2;
			}
			else if(currByte == 0x6C && alreadyFind == 2)
			{
				alreadyFind = 3;
			}
			else if(currByte == 0x40 && alreadyFind == 3)
			{
				startCodeFound = 1;
				break;
			}
			else
			{
				alreadyFind = 0;
			}
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

 		}

	}

	str->bitbufDataPos = i;
	
	if( startCodeFound )
		return ( alreadyFind + 1 );
	else
		return 0;
 }

static int  getNalunitBits_ByteStreamaudio( void * strIn )
{
	byteStream_s  * str;
	int  			numRemainingBytes;
	int  			startCodeLen;
	int  			nalUnitStartPos;

	str = ( byteStream_s * ) strIn;

  /*
   * Copy valid data to the beginning of the buffer
   */
	numRemainingBytes = str->bitbufDataLen - str->bitbufDataPos;

	if( numRemainingBytes > 0 )
	{
		memcpy( str->bitbuf, str->bitbuf + str->bitbufDataPos, numRemainingBytes );
	}

	/* Update bitbuffer variables */
	str->bitbufDataLen = numRemainingBytes;
	str->bitbufDataPos = 0;


	startCodeLen = findStartaudio( str );

	if( startCodeLen == 0 )
		return 0;

	nalUnitStartPos = str->bitbufDataPos - startCodeLen;

	startCodeLen = findStartaudio( str );

	if( startCodeLen != 0 )
		str->bitbufDataPos -= startCodeLen;

	str->bitbufNalunit	  = str->bitbuf + nalUnitStartPos;
	str->bitbufNalunitLen = str->bitbufDataPos;

	return 1;
}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/*
 * add an audio output stream
 */
static AVStream *add_audio_stream(AVFormatContext *oc, enum CodecID codec_id)
{
    AVCodecContext *c;
    AVStream *st;
    AVCodec *codec;

    /* find the audio encoder */
    codec = avcodec_find_encoder(codec_id);
    if (!codec) {
        fprintf(stderr, "codec not found\n");
        exit(1);
    }


    st = avformat_new_stream(oc, codec);

    if (!st) {
        fprintf(stderr, "Could not alloc stream\n");
        exit(1);
    }
    st->id = 1;

	c = st->codec;
    c->codec_id = codec_id;
    c->codec_type = AVMEDIA_TYPE_AUDIO;

    /* put sample parameters */
    c->sample_fmt  = AV_SAMPLE_FMT_S16;
    c->bit_rate    = 64000;
    c->sample_rate = 8000;
    c->channels    = 1;

    // some formats want stream headers to be separate
    if (oc->oformat->flags & AVFMT_GLOBALHEADER)
        c->flags |= CODEC_FLAG_GLOBAL_HEADER;

    return st;
}

static void open_audio(AVFormatContext *oc, AVStream *st)
{
    AVCodecContext *c;
	AVCodec *codec;
	int bytes = 0;

    c = st->codec;

	codec = avcodec_find_encoder(c->codec_id);
    if (!codec) {
        fprintf(stderr, "codec not found\n");
        exit(1);
    }

    /* open it */
    if (avcodec_open(c, codec) < 0) 
	{
        fprintf(stderr, "could not open audio codec\n");
        exit(1);
    }

    /* init signal generator */
    t     = 0;
    tincr = 2 * M_PI * 110.0 / c->sample_rate;
    /* increment frequency by 110 Hz per second */
    tincr2 = 2 * M_PI * 110.0 / c->sample_rate / c->sample_rate;


    if (c->codec->capabilities & CODEC_CAP_VARIABLE_FRAME_SIZE)
        audio_input_frame_size = 10000;

    else
        audio_input_frame_size = c->frame_size;

	bytes = av_get_bytes_per_sample(c->sample_fmt);
    samples = av_malloc(audio_input_frame_size * bytes * c->channels);  //++一帧pcm大小为2048 bytes
}

/* Prepare a 16 bit dummy audio frame of 'frame_size' samples and
 * 'nb_channels' channels. */
static void get_audio_frame(int16_t *samples, int frame_size, int nb_channels)
{
    int j, i, v;
    int16_t *q;

    q = samples;
    for (j = 0; j < frame_size; j++) {
        v = (int)(sin(t) * 10000);
        for (i = 0; i < nb_channels; i++)
            *q++ = v;
        t     += tincr;
        tincr += tincr2;
    }
}

static void write_audio_frame(AVFormatContext *oc, AVStream *st,unsigned char *buf, int framesize)
{
   AVCodecContext *c;
//    AVFrame *frame = avcodec_alloc_frame();
	int ret;
    c = st->codec;

/*
    get_audio_frame(samples, audio_input_frame_size, c->channels);
    frame->nb_samples = audio_input_frame_size;
    avcodec_fill_audio_frame(frame, c->channels, c->sample_fmt,
                             (uint8_t *)samples,
                             audio_input_frame_size *
                             av_get_bytes_per_sample(c->sample_fmt) *
                             c->channels, 1);

    avcodec_encode_audio2(c, &pkt, frame, &got_packet);
*/

    if (framesize > 0)
    {    
		AVPacket pkt = { 0 }; // data and size must be 0;
	    av_init_packet(&pkt);

		pkt.stream_index = st->index;
		pkt.size         = framesize;
		pkt.data         = buf;
		
		ret = av_interleaved_write_frame(oc, &pkt);
	}

    /* Write the compressed frame to the media file. */
    if (ret != 0) {
        fprintf(stderr, "Error while writing audio frame\n");
        exit(1);
    }
}


static void close_audio(AVFormatContext *oc, AVStream *st)
{
    avcodec_close(st->codec);

    av_free(samples);
}

/**************************************************************/
/* video output */

static AVFrame *picture, *tmp_picture;
static uint8_t *video_outbuf;
static int frame_count, video_outbuf_size;

/* Add a video output stream. */
static AVStream *add_video_stream(AVFormatContext *oc, enum CodecID codec_id)
{
    AVCodecContext *c;
    AVStream *st;
    AVCodec *codec;

	/* find the video encoder */
    codec = avcodec_find_encoder(codec_id);
    if (!codec) {
        fprintf(stderr, "codec not found\n");
        exit(1);
    }

    st = avformat_new_stream(oc, codec);
    if (!st) {
        fprintf(stderr, "Could not alloc stream\n");
        exit(1);
    }
	
	c=st->codec;
	c->codec_id = codec_id;  //++CODEC_ID_H264
    c->codec_type = AVMEDIA_TYPE_VIDEO;

    codec = avcodec_find_encoder(c->codec_id);
    if (!codec) {
        fprintf(stderr, "codec not found\n");
        exit(1);
    }


    avcodec_get_context_defaults3(c, codec);

    c->codec_id = codec_id;

    /* Put sample parameters. */
    c->bit_rate = 400000;
    /* Resolution must be a multiple of two. */
//    c->width    = 352;
//    c->height   = 288;
	c->width = 640;
	c->height = 480;
    /* timebase: This is the fundamental unit of time (in seconds) in terms
     * of which frame timestamps are represented. For fixed-fps content,
     * timebase should be 1/framerate and timestamp increments should be
     * identical to 1. */
    c->time_base.den = STREAM_FRAME_RATE;
    c->time_base.num = 1;
    c->gop_size      = 12; /* emit one intra frame every twelve frames at most */
    c->pix_fmt       = STREAM_PIX_FMT;
    if (c->codec_id == CODEC_ID_MPEG2VIDEO) {
        /* just for testing, we also add B frames */
        c->max_b_frames = 2;
    }
    if (c->codec_id == CODEC_ID_MPEG1VIDEO) {
        /* Needed to avoid using macroblocks in which some coeffs overflow.
         * This does not happen with normal video, it just happens here as
         * the motion of the chroma plane does not match the luma plane. */
        c->mb_decision = 2;
    }
    /* Some formats want stream headers to be separate. */
    if (oc->oformat->flags & AVFMT_GLOBALHEADER)
        c->flags |= CODEC_FLAG_GLOBAL_HEADER;

    return st;
}

static AVFrame *alloc_picture(enum PixelFormat pix_fmt, int width, int height)
{
    AVFrame *picture;
    uint8_t *picture_buf;
    int size;

    picture = avcodec_alloc_frame();
    if (!picture)
        return NULL;
    size        = avpicture_get_size(pix_fmt, width, height);
    picture_buf = av_malloc(size);
    if (!picture_buf) {
        av_free(picture);
        return NULL;
    }
    avpicture_fill((AVPicture *)picture, picture_buf,
                   pix_fmt, width, height);
    return picture;
}

static void open_video(AVFormatContext *oc, AVStream *st)
{
    AVCodecContext *c;
	AVCodec *codec;

    c = st->codec;

   codec = avcodec_find_encoder(c->codec_id);
   if (!codec) {
       fprintf(stderr, "codec not found\n");
       exit(1);
   }
   
   if(avcodec_open(c,codec)<0)
   {
       fprintf(stderr, "could not open codec\n");
       exit(1);
   }

    video_outbuf = NULL;
    if (!(oc->oformat->flags & AVFMT_RAWPICTURE)) {
        /* Allocate output buffer. */
        /* XXX: API change will be done. */
        /* Buffers passed into lav* can be allocated any way you prefer,
         * as long as they're aligned enough for the architecture, and
         * they're freed appropriately (such as using av_free for buffers
         * allocated with av_malloc). */
        video_outbuf_size = 200000;
        video_outbuf      = av_malloc(video_outbuf_size);
    }

    /* Allocate the encoded raw picture. */
    picture = alloc_picture(c->pix_fmt, c->width, c->height);
    if (!picture) {
        fprintf(stderr, "Could not allocate picture\n");
        exit(1);
    }

    /* If the output format is not YUV420P, then a temporary YUV420P
     * picture is needed too. It is then converted to the required
     * output format. */
    tmp_picture = NULL;
    if (c->pix_fmt != PIX_FMT_YUV420P) {
        tmp_picture = alloc_picture(PIX_FMT_YUV420P, c->width, c->height);
        if (!tmp_picture) {
            fprintf(stderr, "Could not allocate temporary picture\n");
            exit(1);
        }
    }
}

/* Prepare a dummy image. */
static void fill_yuv_image(AVFrame *pict, int frame_index,
                           int width, int height)
{
    int x, y, i;

    i = frame_index;

    /* Y */
    for (y = 0; y < height; y++)
        for (x = 0; x < width; x++)
            pict->data[0][y * pict->linesize[0] + x] = x + y + i * 3;

    /* Cb and Cr */
    for (y = 0; y < height / 2; y++) {
        for (x = 0; x < width / 2; x++) {
            pict->data[1][y * pict->linesize[1] + x] = 128 + y + i * 2;
            pict->data[2][y * pict->linesize[2] + x] = 64 + x + i * 5;
        }
    }
}

static void write_video_frame(AVFormatContext *oc, AVStream *st, unsigned char *decodedBuf, int frameSize)
{
    int  ret;
    AVCodecContext *c;
//    static struct SwsContext *img_convert_ctx;

    c = st->codec;

    /* If size is zero, it means the image was buffered. */
    if (frameSize > 0) 
	{
        AVPacket pkt;
        av_init_packet(&pkt);

		if (c->coded_frame->pts != GS_AV_NOPTS_VALUE)
			pkt.pts = av_rescale_q(c->coded_frame->pts, c->time_base, st->time_base);

        if (c->coded_frame->key_frame)
            pkt.flags |= AV_PKT_FLAG_KEY;

        pkt.stream_index = st->index;
        pkt.data         = decodedBuf;
        pkt.size         = frameSize;

		c->coded_frame->pts++;

        /* Write the compressed frame to the media file. */
        ret = av_interleaved_write_frame(oc, &pkt);

	}

    if (ret != 0) {
        fprintf(stderr, "Error while writing video frame\n");
        exit(1);
    }

    frame_count++;
}

static void close_video(AVFormatContext *oc, AVStream *st)
{
    avcodec_close(st->codec);
    av_free(picture->data[0]);
    av_free(picture);
    if (tmp_picture) {
        av_free(tmp_picture->data[0]);
        av_free(tmp_picture);
    }
    av_free(video_outbuf);
}

/**************************************************************/
/* media file output */

int main()
{
    const char *filename = "testmp4.mp4";
    AVOutputFormat *fmt;
    AVFormatContext *oc;
    AVStream *audio_st, *video_st;
    double audio_pts, video_pts;
    int i;
//++	FILE *f264 = NULL;
//++	FILE *fAac = NULL;

	byteStream_s	strByte;
	int				nalHeaderByte, nalType;

	byteStream_s	strByteAudio;
	int				AVFlag = 0;
	int				aframes = 0, vframes = 0;

//	int read_count,aframe_over,audioF_head;

    /* Initialize libavcodec, and register all codecs and formats. */
    av_register_all();

	
    /* allocate the output media context */
    avformat_alloc_output_context2(&oc, NULL, "mp4", filename);
    if (!oc) 
	{
        printf("Could not deduce output format from file extension: using MPEG.\n");
	    //++Allocate an AVFormatContext for an default output format.
		avformat_alloc_output_context2(&oc, NULL, "mpeg", filename);
    }
    if (!oc) 
	{
		printf("output file create failed!!!\n");
        return 1;
    }
    fmt = oc->oformat;

    /* Add the audio and video streams using the default format codecs
     * and initialize the codecs. */
    video_st = NULL;
    audio_st = NULL;
    if (fmt->video_codec != CODEC_ID_NONE) {
        video_st = add_video_stream(oc, fmt->video_codec);
    }
    if (fmt->audio_codec != CODEC_ID_NONE) {
        audio_st = add_audio_stream(oc, fmt->audio_codec);
    }

    /* Now that all the parameters are set, we can open the audio and
     * video codecs and allocate the necessary encode buffers. */
    if (video_st)
        open_video(oc, video_st);
    if (audio_st)
        open_audio(oc, audio_st);

    av_dump_format(oc, 0, filename, 1);

    /* open the output file, if needed */
    if (!(fmt->flags & AVFMT_NOFILE)) {
        if (avio_open(&oc->pb, filename, AVIO_FLAG_WRITE) < 0) {
            fprintf(stderr, "Could not open '%s'\n", filename);
            return 1;
        }
    }

//++初始化结束
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /* Write the stream header, if any. */
    avformat_write_header(oc, NULL);

	strByte.fn = fopen("F:\\Linux\\640x480_city.264","rb");
	if (!strByte.fn)
	{
		fprintf(stderr,"Could not open file!\n");
		exit(1);
	}

	strByteAudio.fn = fopen("F:\\audio\\woman-8k1ch.aac","rb");
	if (!strByteAudio.fn)
	{
		fprintf(stderr,"Can not open file!\n");
		exit(1);
	}
	
	if( ( strByte.bitbuf = malloc( 4 * 1024 ) ) == NULL )
	{
		printf( "Can not allocate memory for 264 stream!\n" );
		return -1;
	}
	strByte.bytesRead		  = 0;
	strByte.bitbufLen		  = 4 * 1024;
	strByte.bitbufDataLen	  = 0;
	strByte.bitbufDataPos	  = 0;
	strByte.bitbufNalunitLen  = 0;

	if( ( strByteAudio.bitbuf = malloc( 2048 ) ) == NULL )
	{
		printf( "Can not allocate memory for AAC stream!\n" );
		return -1;
	}
	strByteAudio.bytesRead		  = 0;
	strByteAudio.bitbufLen		  = 2048;
	strByteAudio.bitbufDataLen	  = 0;
	strByteAudio.bitbufDataPos	  = 0;
	strByteAudio.bitbufNalunitLen = 0;

    picture->pts = 0;
    while(!feof(strByte.fn) || !feof(strByteAudio.fn)) // 
//     while(!feof(strByte.fn)) // 
//   while(!feof(strByteAudio.fn)) // 
	{
        // Compute current audio and video time omit...... 
		
        /* write interleaved audio and video frames */
         if (AVFlag)
		 {
			 if( !getNalunitBits_ByteStreamaudio( &strByteAudio ) )
			 {
				 strByteAudio.bitbufNalunit	 = 0;
				 strByteAudio.bitbufNalunitLen = 0;
				 break;
			 }

             write_audio_frame(oc, audio_st, strByteAudio.bitbufNalunit, strByteAudio.bitbufNalunitLen);

			 AVFlag ^= 1;
printf("current %d aacframe\n", aframes++);
         } 
		 else 
		 {
			 if( !getNalunitBits_ByteStream( &strByte ) )
			 {
				 strByte.bitbufNalunit	 = 0;
				 strByte.bitbufNalunitLen = 0;
				 break;
			 }

           //++ write_video_frame(oc, video_st);
            write_video_frame(oc, video_st, strByte.bitbufNalunit, strByte.bitbufNalunitLen);
            picture->pts++;

//			AVFlag ^= 1;
printf("current %d 264frame\n", vframes++);
       }
    }

    /* Write the trailer, if any. The trailer must be written before you
     * close the CodecContexts open when you wrote the header; otherwise
     * av_write_trailer() may try to use memory that was freed on
     * av_codec_close(). */

    av_write_trailer(oc);

//++写mp4文件过程
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /* Close each codec. */
    if (video_st)
        close_video(oc, video_st);
    if (audio_st)
        close_audio(oc, audio_st);

    /* Free the streams. */
    for (i = 0; i < oc->nb_streams; i++) {
        av_freep(&oc->streams[i]->codec);
        av_freep(&oc->streams[i]);
    }

    if (!(fmt->flags & AVFMT_NOFILE))
        /* Close the output file. */
        avio_close(oc->pb);

    /* free the stream */
    av_free(oc);

	fclose(strByte.fn);
	fclose(strByteAudio.fn);

//++去初始化
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    return 0;
}
