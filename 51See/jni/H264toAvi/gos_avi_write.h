#ifndef _GOS_AVIWRITE_H_
#define _GOS_AVIWRITE_H_

//#include <osa.h>

#define IMG_W	640
#define IMG_H	480
#define NALBUF_BLOCK_SIZE 512

typedef unsigned char Uint8;

typedef struct channel_ProcParm
{
        char    gs_ServerIP[16];        //??¨®|ipcamer¦Ì?IP¦Ì??¡¤
        char    gs_Username[16];        //¦Ì???ipcamer¦Ì?¨®??¡ì??
        char    gs_Password[16];        //¦Ì???ipcamer¦Ì??¨¹??
        char    gs_CamSerial[64];       //ipcamer¦Ì?D¨°¨¢Do?
        int             gs_SockTrans;   //¨ª¡§??socket
        int             gs_channel_proc_state; //¦Ì¡À?¡ã¦Ì?¨¢??¨®¡Á¡ä¨¬?
        int             gs_channel_live_state; //¦Ì¡À?¡ã¦Ì?¨¢??¨®¡Á¡ä¨¬?
        int             gs_Video_w; //ipcamer ¨º¨®?¦Ì¦Ì??¨ª?¨¨
        int             gs_Video_h; //ipcamer¨º¨®?¦Ì¦Ì????¨¨
        int             gs_Audioflag;

        sendList        *pVideo_list;
        unsigned int    nBlockedFrame;
        unsigned int    bWaitForIFrame;

        sendList        *pAvi_list;
        unsigned int    nAviBlockedFrame;
        unsigned int    bAviWaitForIFrame;

        sendList        *pAudio_list;
        //without drop packages action, 

        unsigned int    avi_enable;
        char            filename[256];
        unsigned char*  av_tbuf;

}channel_ProcParm;

typedef struct _byteStream_s
{
	FILE				  * fn;  				
	unsigned		char  * bitbuf; 				    	
	long			        bytesRead;		
	int  	bitbufLen;							
	int  	bitbufDataPos;
	int  	bitbufDataLen;					
	unsigned		char  * bitbufNalunit;
	int  	bitbufNalunitLen;
}	 byteStream_s;



//ÒôÆµÖ¡Á´±í
typedef struct aviaudioframe{
	unsigned int	version;		
	int				frame_size;			
	int				frame_locate;		

	unsigned int	Padding_bit; 
	float			frame_time;        

	float			duration_perframe; 
	unsigned int	Bitrate_index;    
	unsigned int	Sampling_index;  

	char			*buf;			

	struct aviaudioframe *nextframe;   
}audio_frame;

//ÊÓÆµÖ¡Á´±í
typedef struct avivideoframe{
	int		frame_locate;           
	int		frame_size;             

	int		keyframe;                
	double	frame_time;           
	
	char	*buf;                   

	struct avivideoframe *nextframe; 
}video_frame;


typedef struct aviwriteobj{
	Uint8 *av_tbuf;
	
}GOS_aviWObj;


void avi_write_process(CGOSCAMRecorder *pRecorder);


#endif
























