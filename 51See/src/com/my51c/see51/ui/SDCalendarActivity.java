package com.my51c.see51.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.my51c.see51.data.CloudFileInfo;
import com.my51c.see51.data.CloudHandle;
import com.my51c.see51.data.Device;
import com.my51c.see51.listener.OnGetSDFileDirListener;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.media.cloudsdk;
import com.my51c.see51.service.AppData;
import com.my51c.see51.widget.DateWidgetDayCell;
import com.my51c.see51.widget.DateWidgetDayHeader;
import com.my51c.see51.widget.DayStyle;
import com.sdview.view.R;

public class SDCalendarActivity extends SherlockActivity implements OnClickListener {
	
	private final String TAG = "SDCalendarActivity";
	private LinearLayout layContent = null;
	private ArrayList<DateWidgetDayCell> days = new ArrayList<DateWidgetDayCell>();

	// ���ڱ���
	public static Calendar calStartDate = Calendar.getInstance();
	private Calendar calToday = Calendar.getInstance();
	private Calendar calCalendar = Calendar.getInstance();
	private Calendar calSelected = Calendar.getInstance();

	// ��ǰ��������
	private int iMonthViewCurrentMonth = 0;
	private int iMonthViewCurrentYear = 0;
	private int iFirstDayOfWeek = Calendar.MONDAY;

	private int Calendar_Width = 0;
	private int Cell_Width = 0;

	// ҳ��ؼ�
	TextView Top_Date = null;
	Button btn_pre_month = null;
	Button btn_next_month = null;
	TextView arrange_text = null;
	LinearLayout mainLayout = null;
	LinearLayout arrange_layout = null;

	// ���Դ
	ArrayList<String> Calendar_Source = null;
	Hashtable<Integer, Integer> calendar_Hashtable = new Hashtable<Integer, Integer>();
	Boolean[] flag = null;
	Calendar startDate = null;
	Calendar endDate = null;
	int dayvalue = -1;
	private Device mDevice = null;
	private AppData appData;
	private RemoteInteractionStreamer mediastream;
	static HashMap<String, String> dayEventCount = new HashMap<String, String>();
	
	public static int Calendar_WeekBgColor = 0;
	public static int Calendar_DayBgColor = 0;
	public static int isHoliday_BgColor = 0;
	public static int unPresentMonth_FontColor = 0;
	public static int isPresentMonth_FontColor = 0;
	public static int dayEventNumber_FontColor = 0;
	public static int isToday_BgColor = 0;
	public static int special_Reminder = 0;
	public static int common_Reminder = 0;
	public static int Calendar_WeekFontColor = 0;


	private final static int GET_DAY_EVENT = 1;
	private boolean bAllClick = false;
	private String deviceID = "";
	private String nvrDeviceId = "";
	String UserName = "";
	String month;
	cloudsdk csdk;
    CloudHandle ch;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SDRecordNVRActivity.isNVR = false;
		Bundle bundle = getIntent().getExtras();
		bAllClick = bundle.getBoolean("isAllClick");
		nvrDeviceId = bundle.getString("nvrDeviceId");
		appData = (AppData)getApplication();
		Log.i(TAG, "�豸ID��"+nvrDeviceId);
		if(bAllClick)
		{
			deviceID = bundle.getString("deviceID");
			mDevice = appData.getAccountInfo().getCurrentList().getDevice(deviceID);
			ch = new CloudHandle();
			String cloudurl = ParseCloudAddr(mDevice);
			csdk = new cloudsdk();
			ch.setStrADKPath("/temp/");
			csdk.Native_GHDSCClient_Init(ch);
			csdk.Native_GHDSCClient_Create(ch);
			
			String paramaddr = "AddrRemote[=]";
			paramaddr += cloudurl;
			paramaddr += "[|]TOConnect[=]60[|]TORW[=]60[|]";
			ch.setStrParam(paramaddr);
			ch.setStrUsername("guest");
			ch.setStrPassword("guest");
			ch.setStrSN(deviceID);
			csdk.Native_GHDSCClient_Connect(ch);
			String unixTime =String.valueOf(Calendar.getInstance().getTimeInMillis()/1000);
			ch.setStrQueryVideoMonthCountWhere(unixTime);
			ch.setStrQueryVideoMonthCountColumn("video_month_date,video_month_count");
			csdk.Native_GHDSCClient_Query_Month_Count_Video(ch);
			Log.i("nono:",deviceID+"--"+ byteToString(ch.getbQueryVideoMonthCountData()));
			parseVideoXml(ch.getbQueryVideoMonthCountData());
		}
		
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int screenWidth = display.getWidth();
		Calendar_Width = screenWidth;
		Cell_Width = Calendar_Width / 7 + 1;

		// �ƶ������ļ�������������
		mainLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.calendar_main, null);
		setContentView(mainLayout);
		
		Top_Date = (TextView) findViewById(R.id.Top_Date);
		btn_pre_month = (Button) findViewById(R.id.btn_pre_month);
		btn_next_month = (Button) findViewById(R.id.btn_next_month);
		LinearLayout backLayout = (LinearLayout)findViewById(R.id.calendar_back_layout);
		backLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				backMainActivity();
			}
		});
	
		btn_pre_month.setOnClickListener(new Pre_MonthOnClickListener());
		btn_next_month.setOnClickListener(new Next_MonthOnClickListener());
		
		// ���㱾�������еĵ�һ��(һ�������µ�ĳ��)������������
		calStartDate = getCalendarStartDate();
		mainLayout.addView(generateCalendarMain());
		DateWidgetDayCell daySelected = updateCalendar();

		if (daySelected != null)
			daySelected.requestFocus();

		LinearLayout.LayoutParams Param1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

		ScrollView view = new ScrollView(this);
		arrange_layout = createLayout(LinearLayout.VERTICAL);
		arrange_layout.setPadding(5, 2, 0, 0);
		arrange_text = new TextView(this);
		mainLayout.setBackgroundColor(Color.WHITE);
		arrange_text.setTextColor(Color.BLACK);
		arrange_text.setTextSize(18);
		arrange_layout.addView(arrange_text);

		startDate = GetStartDate();
		calToday = GetTodayDate();

		endDate = GetEndDate(startDate);
		view.addView(arrange_layout, Param1);
		mainLayout.addView(view);
		
		
		//appData.addUIActivity(new WeakReference<Activity>(this));
		// �½��߳�
		new Thread()
		{
			@Override
			public void run()
			{
				int day = GetNumFromDate(calToday, startDate);

				if (calendar_Hashtable != null && calendar_Hashtable.containsKey(day))
				{
					dayvalue = calendar_Hashtable.get(day);
				}
			}
		}.start();
		
		Calendar_WeekBgColor = this.getResources().getColor(R.color.Calendar_WeekBgColor);
		Calendar_DayBgColor = this.getResources().getColor(R.color.Calendar_DayBgColor);
		isHoliday_BgColor = this.getResources().getColor(R.color.isHoliday_BgColor);
		unPresentMonth_FontColor = this.getResources().getColor(R.color.unPresentMonth_FontColor);
		isPresentMonth_FontColor = this.getResources().getColor(R.color.isPresentMonth_FontColor);
		dayEventNumber_FontColor = this.getResources().getColor(R.color.red);
		isToday_BgColor = this.getResources().getColor(R.color.isToday_BgColor);
		special_Reminder = this.getResources().getColor(R.color.specialReminder);
		common_Reminder = this.getResources().getColor(R.color.commonReminder);
		Calendar_WeekFontColor = this.getResources().getColor(R.color.Calendar_WeekFontColor);

	}
	

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		
		if(bAllClick == false)
		{
			mediastream = appData.getRemoteInteractionStreamer();
			if(mediastream != null)
			{
			   mediastream.setOnGetSDFileDirListener(mOnGetSDFileDirListener);	
			   mediastream.getSDFileDir(nvrDeviceId);
			}
		}
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		if(bAllClick == false)
		{	
			if(mediastream != null)
			{
			   mediastream.setOnGetSDFileDirListener(null);
			}
		}
	}
	
	
	private String ParseCloudAddr(Device device)
	{
		String cloudUrl = null;
		if(device == null)
			return cloudUrl;
	
		String playurl = null;
		if(mDevice.getSee51Info() != null)
			playurl = mDevice.getSee51Info().getDataURL();

		if(playurl == null)
		    return cloudUrl;
		 
		String []urls = playurl.split(";");
		int i = 0;
		if(urls != null)
		{
			for(i=0;i<urls.length;i++)
			{
				if(urls[i].toLowerCase().startsWith("cloud://"))
				{
					cloudUrl = urls[i].substring(8);
					String strs[] = cloudUrl.split(":");
					String port = strs[1];
					Log.i("CloudRecordAcy", "--PORT:"+port);
					cloudUrl = strs[0]+":5557";
					break;
				}
			}
		}
		
		if(cloudUrl == null)
		{
			Log.i("CloudRecordAcy", "urlΪ�գ�����Ĭ�ϵ�ַ");
			cloudUrl = "221.214.50.79:5557";
		}
		return cloudUrl;
	}

	private MyHandler mHandler = new MyHandler(this);
	private static class MyHandler extends Handler
	{
		WeakReference<SDCalendarActivity> eActivity;
		
		public MyHandler(SDCalendarActivity outActivity)
		{
			// TODO Auto-generated constructor stub
			eActivity = new WeakReference<SDCalendarActivity>(outActivity);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			SDCalendarActivity theActivity = eActivity.get();
			if(theActivity != null)
				theActivity.handleMessage(msg);
		}
		
		private void release()
		{
			if(eActivity != null && eActivity.get() != null)
				eActivity.clear();
		}
	};
	
	private void handleMessage(Message msg)
	{
		switch (msg.what)
		{
		case GET_DAY_EVENT:
			{
				updateCalendar();
			}
			break;
		}
	}
	
	protected String byteToString(byte[] src)
	{
		int len = 0;
		for (; len < src.length; len++)
		{
			if (src[len] == 0)
			{
				break;
			}
		}
		return new String(src, 0, len);
	}
	
	private OnGetSDFileDirListener mOnGetSDFileDirListener = new OnGetSDFileDirListener()
	{
		@Override
		public void onGetFileDir(byte[] devbuf) {
			// TODO Auto-generated method stub
			clearDayEvent();
			String strFileList = byteToString(devbuf);
			Log.i("SDCalendarActivity", "SD-String"+strFileList);
			String []strItem = strFileList.split("\\|");
			
			for(int i=1; i<strItem.length; i++)
			{
				String []itemText = strItem[i].split(",");
				dayEventCount.put(itemText[0], itemText[0]);
				Log.i("SDClendarActivity", itemText[0]);
			}
			mHandler.sendEmptyMessage(GET_DAY_EVENT);
		}
	};
	
	/*add by xqe 2016.01.18*/
	void parseVideoXml(byte[] buf)
	{
		Log.i("CloudRecordActivity", byteToString(buf));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	    DocumentBuilder builder = null;
	    Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		String orgstring = null;
		try {
			orgstring = new String(buf,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String xmlstring = "<?xml version=\"1.0\" encoding=\"utf-8\"?><videos>";
		xmlstring += orgstring;
		xmlstring += "</videos>";
		
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(xmlstring.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}  
		
			try {
				doc = builder.parse(is);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		Element rootElement = null;
		if(doc != null)
		{  
			rootElement = doc.getDocumentElement();  
		}
		
		
	     NodeList items = null;
	     if(rootElement != null)
	     {	 
	    	 items =  rootElement.getElementsByTagName("video");  
	     }
	     
	     if(items == null)
	    	return;
	     
	     for (int i = 0; i < items.getLength(); i++) {  
	    	 
	            Node item = items.item(i);  
	            NodeList properties = item.getChildNodes();  
	            for (int j = 0; j < properties.getLength(); j++) {  
	                Node property = properties.item(j);  
	                String nodeName = property.getNodeName();  
	                if (nodeName.equals("video_month_date")) {  
	                	//������
	                	String dateStr = String.valueOf(property.getFirstChild().getNodeValue());
	                	String[] itemDate = dateStr.split("-");
	                	String date = itemDate[0]+itemDate[1]+itemDate[2];
	                	Log.i("SDClendarActivity", "--"+date);
	                	dayEventCount.put(date, date);
	                } else if (nodeName.equals("video_month_count")) { 
	                	//�ļ�����
	                	
	                } 
	            }  
	        }  
	}
	/*end by xqe 2016.01.18*/
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		dayEventCount.clear();
		super.onDestroy();
		mHandler.release();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		/*
		int id = v.getId();
		switch (id)
		{
		case R.id.btnReturn:
			this.finish();
			break;
		default:
			break;
		}
		*/
	}

	protected String GetDateShortString(Calendar date)
	{
		String returnString = date.get(Calendar.YEAR) + "/";
		returnString += date.get(Calendar.MONTH) + 1 + "/";
		returnString += date.get(Calendar.DAY_OF_MONTH);

		return returnString;
	}

	// �õ������������е����
	private int GetNumFromDate(Calendar now, Calendar returnDate)
	{
		Calendar cNow = (Calendar) now.clone();
		Calendar cReturnDate = (Calendar) returnDate.clone();
		setTimeToMidnight(cNow);
		setTimeToMidnight(cReturnDate);

		long todayMs = cNow.getTimeInMillis();
		long returnMs = cReturnDate.getTimeInMillis();
		long intervalMs = todayMs - returnMs;
		int index = millisecondsToDays(intervalMs);

		return index;
	}

	private int millisecondsToDays(long intervalMs)
	{
		return Math.round((intervalMs / (1000 * 86400)));
	}

	private void setTimeToMidnight(Calendar calendar)
	{
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}

	// ��ɲ���
	private LinearLayout createLayout(int iOrientation)
	{
		LinearLayout lay = new LinearLayout(this);
		lay.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		lay.setOrientation(iOrientation);

		return lay;
	}

	// �������ͷ��
	private View generateCalendarHeader()
	{
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		// layRow.setBackgroundColor(Color.argb(255, 207, 207, 205));

		for (int iDay = 0; iDay < 7; iDay++)
		{
			DateWidgetDayHeader day = new DateWidgetDayHeader(this, Cell_Width, 35);

			final int iWeekDay = DayStyle.getWeekDay(iDay, iFirstDayOfWeek);
			day.setData(iWeekDay);
			layRow.addView(day);
		}

		return layRow;
	}

	// �����������
	private View generateCalendarMain()
	{
		layContent = createLayout(LinearLayout.VERTICAL);
		// layContent.setPadding(1, 0, 1, 0);
		layContent.setBackgroundColor(Color.argb(255, 105, 105, 103));
		layContent.addView(generateCalendarHeader());
		days.clear();

		for (int iRow = 0; iRow < 6; iRow++)
		{
			layContent.addView(generateCalendarRow());
		}

		return layContent;
	}

	// ��������е�һ�У���������
	private View generateCalendarRow()
	{
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);

		for (int iDay = 0; iDay < 7; iDay++)
		{
			DateWidgetDayCell dayCell = new DateWidgetDayCell(this, Cell_Width, Cell_Width);
			dayCell.setItemClick(mOnDayCellClick);
			days.add(dayCell);
			layRow.addView(dayCell);
		}
		return layRow;
	}

	// ���õ������ںͱ�ѡ������
	private Calendar getCalendarStartDate()
	{
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);

		if (calSelected.getTimeInMillis() == 0)
		{
			calStartDate.setTimeInMillis(System.currentTimeMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		} else
		{
			calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		}

		UpdateStartDateForMonth();
		return calStartDate;
	}

	// ���ڱ������ϵ����ڶ��Ǵ���һ��ʼ�ģ��˷���������������ڱ�����������ʾ������
	private void UpdateStartDateForMonth()
	{
		iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
		iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.HOUR_OF_DAY, 0);
		calStartDate.set(Calendar.MINUTE, 0);
		calStartDate.set(Calendar.SECOND, 0);
		// update days for week
		UpdateCurrentMonthDisplay();
		int iDay = 0;
		int iStartDay = iFirstDayOfWeek;

		if (iStartDay == Calendar.MONDAY)
		{
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
			if (iDay < 0)
				iDay = 6;
		}

		if (iStartDay == Calendar.SUNDAY)
		{
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
			if (iDay < 0)
				iDay = 6;
		}

		calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
	}
	
	public static void clearDayEvent()
	{
		dayEventCount.clear();
	}

	// ��������
	private DateWidgetDayCell updateCalendar()
	{
		DateWidgetDayCell daySelected = null;
		boolean bSelected = false;
		final boolean bIsSelection = (calSelected.getTimeInMillis() != 0);
		final int iSelectedYear = calSelected.get(Calendar.YEAR);
		final int iSelectedMonth = calSelected.get(Calendar.MONTH);
		final int iSelectedDay = calSelected.get(Calendar.DAY_OF_MONTH);
		calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());

		for (int i = 0; i < days.size(); i++)
		{
			final int iYear = calCalendar.get(Calendar.YEAR);
			final int iMonth = calCalendar.get(Calendar.MONTH);
			final int iDay = calCalendar.get(Calendar.DAY_OF_MONTH);
			final int iDayOfWeek = calCalendar.get(Calendar.DAY_OF_WEEK);
			DateWidgetDayCell dayCell = days.get(i);
			dayCell.setbAllClick(bAllClick);
			
			if (dayEventCount != null)
			{
				final int mMonth = iMonth + 1;
				final int mDay = iDay ;
				String month_of_year;
				if (mMonth < 10)
					month_of_year = iYear + "0" + mMonth;
				else
					month_of_year = iYear + "" + mMonth;
				
				String tempday;
				if(mDay < 10)
				{
					tempday = "0" + mDay;
				}
				else
				{
					tempday = "" + mDay;
				}
				
				month_of_year += tempday; 
				
//				if(bAllClick == false)
				{
					if (dayEventCount.get(month_of_year) != null)
					{
						dayCell.setEventCount(1);//��ӱ�־
					} 
					else
					{
						dayCell.setEventCount(0);
					}
				}
			}

			// �ж��Ƿ���
			boolean bToday = false;

			if (calToday.get(Calendar.YEAR) == iYear)
			{
				if (calToday.get(Calendar.MONTH) == iMonth)
				{
					if (calToday.get(Calendar.DAY_OF_MONTH) == iDay)
					{
						bToday = true;
					}
				}
			}

			// check holiday
			boolean bHoliday = false;
			if ((iDayOfWeek == Calendar.SATURDAY) || (iDayOfWeek == Calendar.SUNDAY))
				bHoliday = true;
			if ((iMonth == Calendar.JANUARY) && (iDay == 1))
				bHoliday = true;

			// �Ƿ�ѡ��
			bSelected = false;

			if (bIsSelection)
				if ((iSelectedDay == iDay) && (iSelectedMonth == iMonth) && (iSelectedYear == iYear))
				{
					bSelected = true;
				}

			dayCell.setSelected(bSelected);

			// �Ƿ��м�¼
			boolean hasRecord = false;

			if (flag != null && flag[i] == true && calendar_Hashtable != null && calendar_Hashtable.containsKey(i))
			{
				// hasRecord = flag[i];
				hasRecord = Calendar_Source.get(calendar_Hashtable.get(i)).contains(UserName);
			}

			if (bSelected)
				daySelected = dayCell;

			dayCell.setData(iYear, iMonth, iDay, bToday, bHoliday, iMonthViewCurrentMonth, hasRecord);
			dayCell.invalidate();
			calCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		layContent.invalidate();

		return daySelected;
	}

	private String[] months = {"January ","February","March","April","May","June","July","August","September","October","November","December"};
	// ���������������ʾ������
	private void UpdateCurrentMonthDisplay()
	{
		String date = (months[calStartDate.get(Calendar.MONTH) ] + ","+calStartDate.get(Calendar.YEAR));
		String language = Locale.getDefault().getLanguage();
		if(language.equals("zh"))
		{
			date = calStartDate.get(Calendar.YEAR) +getString(R.string.year)+ (calStartDate.get(Calendar.MONTH) + 1) + getString(R.string.month);
		}
		else
		{
			date =   (months[calStartDate.get(Calendar.MONTH) ] + ","+calStartDate.get(Calendar.YEAR));
		}
		Top_Date.setText(date);
	}

	// ������°�ť�������¼�
	class Pre_MonthOnClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			arrange_text.setText("");
			calSelected.setTimeInMillis(0);
			iMonthViewCurrentMonth--;

			if (iMonthViewCurrentMonth == -1)
			{
				iMonthViewCurrentMonth = 11;
				iMonthViewCurrentYear--;
			}

			int m = iMonthViewCurrentMonth;
			m++;
			if (m < 10)
				month = iMonthViewCurrentYear + "0" + m;
			else
				month = iMonthViewCurrentYear + "" + m;
			mHandler.sendEmptyMessage(GET_DAY_EVENT);

			calStartDate.set(Calendar.DAY_OF_MONTH, 1);
			calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
			calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
			calStartDate.set(Calendar.HOUR_OF_DAY, 0);
			calStartDate.set(Calendar.MINUTE, 0);
			calStartDate.set(Calendar.SECOND, 0);
			calStartDate.set(Calendar.MILLISECOND, 0);
			UpdateStartDateForMonth();

			startDate = (Calendar) calStartDate.clone();
			endDate = GetEndDate(startDate);

			// �½��߳�
			new Thread()
			{
				@Override
				public void run()
				{

					int day = GetNumFromDate(calToday, startDate);

					if (calendar_Hashtable != null && calendar_Hashtable.containsKey(day))
					{
						dayvalue = calendar_Hashtable.get(day);
					}
				}
			}.start();

			updateCalendar();
		}

	}

	// ������°�ť�������¼�
	class Next_MonthOnClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			arrange_text.setText("");
			calSelected.setTimeInMillis(0);
			iMonthViewCurrentMonth++;

			if (iMonthViewCurrentMonth == 12)
			{
				iMonthViewCurrentMonth = 0;
				iMonthViewCurrentYear++;
			}

			calStartDate.set(Calendar.DAY_OF_MONTH, 1);
			calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
			calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
			UpdateStartDateForMonth();

			startDate = (Calendar) calStartDate.clone();
			endDate = GetEndDate(startDate);

			// �½��߳�
			new Thread()
			{
				@Override
				public void run()
				{
					int day = 5;

					if (calendar_Hashtable != null && calendar_Hashtable.containsKey(day))
					{
						dayvalue = calendar_Hashtable.get(day);
					}
				}
			}.start();

			updateCalendar();
		}
	}

	// ����������¼�
	private DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick()
	{
		public void OnClick(DateWidgetDayCell item)
		{
			calSelected.setTimeInMillis(item.getDate().getTimeInMillis());
			int day = calSelected.get(Calendar.DAY_OF_MONTH);
			if (calendar_Hashtable != null && calendar_Hashtable.containsKey(day))
			{
				arrange_text.setText(Calendar_Source.get(calendar_Hashtable.get(day)));
			} else
			{
				final int iYear = calSelected.get(Calendar.YEAR);
				int iMonth = calSelected.get(Calendar.MONTH);
				iMonth++;
				int iDay = calSelected.get(Calendar.DAY_OF_MONTH);
				String date;
				if (iMonth < 10)
					date = iYear + "0" + iMonth;
				else
					date = iYear + "" + iMonth;
				if (iDay < 10)
					date = date + "0" + iDay;
				else
					date = date + "" + iDay;
				if(bAllClick)
				{
					Calendar cal=Calendar.getInstance();
					cal.set(iYear, iMonth-1, iDay, 0, 0, 0);
					CloudRecordActivity.curStartTime = String.valueOf(cal.getTimeInMillis()/1000);
					
					cal.set(iYear, iMonth-1, iDay, 23, 59, 59);
					CloudRecordActivity.curEndTime = String.valueOf(cal.getTimeInMillis()/1000);
				}
				else
				{
					SDRecordFolderActivity.fileDate = date;
					System.out.println("������ڣ�"+date+"=="+SDRecordFolderActivity.fileDate);
				}
				finish();
			}
			item.setSelected(true);
			updateCalendar();
		}
	};

	public Calendar GetTodayDate()
	{
		Calendar cal_Today = Calendar.getInstance();
		cal_Today.set(Calendar.HOUR_OF_DAY, 0);
		cal_Today.set(Calendar.MINUTE, 0);
		cal_Today.set(Calendar.SECOND, 0);
		cal_Today.setFirstDayOfWeek(Calendar.MONDAY);

		return cal_Today;
	}

	// �õ���ǰ�����еĵ�һ��
	public Calendar GetStartDate()
	{
		int iDay = 0;
		Calendar cal_Now = Calendar.getInstance();
		cal_Now.set(Calendar.DAY_OF_MONTH, 1);
		cal_Now.set(Calendar.HOUR_OF_DAY, 0);
		cal_Now.set(Calendar.MINUTE, 0);
		cal_Now.set(Calendar.SECOND, 0);
		cal_Now.setFirstDayOfWeek(Calendar.MONDAY);

		iDay = cal_Now.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;

		if (iDay < 0)
		{
			iDay = 6;
		}

		cal_Now.add(Calendar.DAY_OF_WEEK, -iDay);

		return cal_Now;
	}

	public Calendar GetEndDate(Calendar startDate)
	{
		// Calendar end = GetStartDate(enddate);
		Calendar endDate = Calendar.getInstance();
		endDate = (Calendar) startDate.clone();
		endDate.add(Calendar.DAY_OF_MONTH, 41);
		return endDate;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// TODO Auto-generated method stub
		switch (item.getItemId())
		{
		case android.R.id.home:
			// Do whatever you want, e.g. finish()
			backMainActivity();
			break;
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public void backMainActivity()
	{
		finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
	
}
