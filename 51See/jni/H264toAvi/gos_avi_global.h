#ifndef GOS_AVI_GLOBAL_H_
#define GOS_AVI_GLOBAL_H_

#ifdef __cplusplus
extern "C"{
#endif

// 全局的几个宏定义
#define BUFFERSIZE 32768

#define GetIdValue(a,b,c,d)  (int)((a)|(b<<8)|(c<<16)|(d<<24))



#define  MOVI			GetIdValue('m','o','v','i')         
#define  LIST			GetIdValue('L','I','S','T')         
#define  STRFFORMAT		GetIdValue('s','t','r','f')  
#define  STRH			GetIdValue('s','t','r','h')  
#define  AVIHFORMAT		GetIdValue('a','v','i','h')   
#define  IDX1FORMAT		GetIdValue('i','d','x','1')  
#define  JUNKFORMAT		GetIdValue('J','U','N','K')  
#define  VPRPFORAMT		GetIdValue('v', 'p', 'r', 'p') 
#define  HDRL			GetIdValue('h', 'd', 'r', 'l')
#define  STRL			GetIdValue('s', 't', 'r', 'l')
#define  H264ID			GetIdValue('H', '2', '6', '4')
#define  VIDEOID		GetIdValue('v','i','d','s')
#define  AUDIOID		GetIdValue('a','u','d','s')
#define  MIDIID			GetIdValue('m','i','d','s')
#define  TXTID			GetIdValue('t','x','t','s')
#define  RIFFID			GetIdValue('R','I','F','F')
#define	 AVIID			GetIdValue('A','V','I',' ')
#define  DCID			GetIdValue('0','0','d','c')
#define  WBID			GetIdValue('0','1','w','b')
#define  ODMLID			GetIdValue('o','d','m','l')
#define  DMLHID			GetIdValue('d','m','l','h')
#define  INFOID			GetIdValue('I','N','F','O')
#define  ISFTID			GetIdValue('I','S','F','T')

#define  G711ID         GetIdValue('G','7','1','1')


/*typedef struct bs_str
{
	int  bufSize;
	int  bufPos;
	int  readLen;
	unsigned char *buf;
	FILE *inFile;
	FILE *outFile;
	FILE *newFile;

	int  startPos; //当前数据块的起始地址
	int  endPos;   //当前数据块的结束地址
}Stream;
*/



typedef struct
{
    int		dwMicroSecPerFrame;	
    int		dwMaxBytesPerSec;
    int		dwPaddingGranularity;	
	// size; normally 2K.
    int		dwFlags;		// the ever-present flags
    int		dwTotalFrames;	
    int		dwInitialFrames;
    int		dwStreams;
    int		dwSuggestedBufferSize;
    
    int		dwWidth;
    int		dwHeight;
    
    int		dwReserved[4];
} gos_MainAVIHeader;



typedef struct rectangle_t {
    short  left;
    short  top;
    short  right;
    short  bottom;
} rectangle_t;

typedef struct {
    int		fccType;
    int		fccHandler;
    int		dwFlags;
    short	wPriority;
    short	wLanguage;
    int		dwInitialFrames;
    int		dwScale;	
    int		dwRate;	
    int		dwStart;
    int		dwLength; 
    int		dwSuggestedBufferSize;
    int		dwQuality;
    int		dwSampleSize;
    rectangle_t	rcFrame;
} gos_AVIStreamHeader;



typedef struct gos_BITMAPINFOHEADER
{
    int        biSize; 
    int        biWidth;
    int        biHeight;
    short      biPlanes; 
    short      biBitCount;
    int        biCompression; 
    int        biSizeImage;  
                              
    int        biXPelsPerMeter; 
    int        biYPelsPerMeter; 
    int        biClrUsed;    
                              
    int        biClrImportant;
} gos_BITMAPINFOHEADER;

typedef struct
{
  short   wFormatTag; 
  short   nChannels;
  int  	  nSamplesPerSec;
  int     nAvgBytesPerSec;
  short   nBlockAlign; 
  short   wBitsPerSample;
  short   cbSize;   
} gos_WAVEFORMATEX;



typedef struct
{
	int ckid;
	int dwFlags;
	int dwChunkOffset;
	int dwChunkLength;
}gos_AVIINDEXENTRY;

typedef struct {
	unsigned int CompressedBMHeight;
	unsigned int CompressedBMWidth;
	unsigned int ValidBMHeight;
	unsigned int ValidBMWidth;
	unsigned int ValidBMXOffset;
	unsigned int ValidBMYOffset;
	unsigned int VideoXOffsetInT;
	unsigned int VideoYValidStartLine;
} VIDEO_FIELD_DESC;


typedef struct {
	unsigned int VideoFormatToken;
	unsigned int VideoStandard;
	unsigned int dwVerticalRefreshRate;
	unsigned int dwHTotalInT;
	unsigned int dwVTotalInLines;
	unsigned int dwFrameAspectRatio;
	unsigned int dwFrameWidthInPixels;
	unsigned int dwFrameHeightInLines;
	unsigned int nbFieldPerFrame;
	VIDEO_FIELD_DESC FieldInfo[2];
} gos_VideoPropHeader;


typedef struct AVIStream 
{
	gos_MainAVIHeader  avih;

	gos_AVIStreamHeader strh;

	gos_BITMAPINFOHEADER strfV;
	gos_WAVEFORMATEX     strfA;

	gos_VideoPropHeader vprp;

}gos_AVIFile;

#ifdef __cplusplus
}
#endif

#endif
