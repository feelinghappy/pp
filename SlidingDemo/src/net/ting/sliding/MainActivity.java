package net.ting.sliding;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.color;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{
	private SlideMenu slideMenu;
    ListView m_ListView;
    TextView m_TextView;
    TextView m_TextView1;
    TextView m_TextView2;
    TextView m_TextView3;
    Button m_dpButton;
    Button m_tpButton;
    Calendar c;
    TextView m_tvTime;
    String str;
    String str1;
    String str2;
    
    String result = null;
    InputStream is = null;
    StringBuilder sb = null;
    JSONArray jArray;
    

    float[] xv = new float[260];
    float[] yv = new float[260];
    
    private String title = "频谱图";
    private XYSeries series;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chart;
    private XYMultipleSeriesRenderer renderer;
    private Context context;
    private int addX = -1, addY;
    LinearLayout m_layout;
    private String[] menutext =  {"监测点1","监测点2","监测点3","监测点4","监测点5"};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		c=Calendar.getInstance();
		m_dpButton = (Button)findViewById(R.id.button1);
		m_tpButton = (Button)findViewById(R.id.button2);
		m_tvTime = (TextView)findViewById(R.id.tvTime);
		m_tvTime.setTextSize(20);
		m_tvTime.setTextColor(Color.WHITE);
/*
	    m_TextView1= (TextView)findViewById(R.id.textview1);
	    m_TextView2= (TextView)findViewById(R.id.textview2);
	    m_TextView3= (TextView)findViewById(R.id.textview3);
	    m_TextView1.setTextColor(color.white);
	    m_TextView2.setTextColor(color.white);
	    m_TextView3.setTextColor(color.white);
	    m_TextView1.setTextSize(20);
	    m_TextView2.setTextSize(20);
	    m_TextView3.setTextSize(20);*/
		
		
		m_dpButton.setOnClickListener(new  Button.OnClickListener()
		{

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new DatePickerDialog(MainActivity.this,new DatePickerDialog.OnDateSetListener() {
					
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						// TODO Auto-generated method stub
						  int  mYear = year;
			              int  mMonth = monthOfYear;
			              int  mDay = dayOfMonth;
			                //更新EditText控件日期 小于10加0
			              str1 =String.valueOf(mYear)+ "-" 
		                    +String.valueOf((mMonth + 1) < 10 ? 0 + (mMonth + 1) : (mMonth + 1))+"-"
		                    +String.valueOf(((mDay < 10) ? 0 + mDay : mDay));
			              m_tvTime.setText(str1);
					}
				},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
				
			}
				
		});
		
		
		
		m_tpButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new TimePickerDialog(MainActivity.this,new TimePickerDialog.OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						// TODO Auto-generated method stub
						int  mHour = hourOfDay;
		                int mMinute = minute;
		                    //更新EditText控件时间 小于10加0
		                str2=String.valueOf((mHour < 10 ? 0 + mHour : mHour))+":"
		                        +String.valueOf(mMinute < 10 ? 0 + mMinute : mMinute)+String.valueOf(":00") ; 
		                str = str1 + " " +str2;
		        		m_tvTime.setText(str);
					}
				},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show();
			}
			
		});
		
		m_TextView = (TextView)findViewById(R.id.textview1);
		m_TextView.setText("欢迎使用频谱感知系统V1.0");
		
		m_TextView.setTextColor(Color.WHITE);
		m_TextView.setTextSize(25);
		
		slideMenu = (SlideMenu) findViewById(R.id.slide_menu);
		ImageView menuImg = (ImageView) findViewById(R.id.title_bar_menu_btn);
		menuImg.setOnClickListener(this);
		
		m_ListView =(ListView)findViewById(R.id.listView1);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,menutext);
		m_ListView.setAdapter(adapter);
		
		m_ListView.setOnItemClickListener(new OnItemClickListener(){
        
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				slideMenu.closeMenu();
				str = str + String.valueOf(position);
//				m_TextView.setText(str);
				sendRequestWithHttpClient();
				
			}
			});
	}
///////////////////////////////////////////
			private void sendRequestWithHttpClient() {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
								HttpClient httpClient = new DefaultHttpClient();
								// 
								HttpGet httpGet = new HttpGet("http://192.168.8.102/2.json");
								Log.d("ppfxlMainActivity", "http://192.168.8.103/data.json");
								HttpResponse httpResponse = httpClient.execute(httpGet);
								int m = httpResponse.getStatusLine().getStatusCode();
								Log.d("ppfxlMainActivity", String.valueOf(m));
								Log.d("ppfxlMainActivity", String.valueOf(httpResponse.getStatusLine().getStatusCode()));
								if (httpResponse.getStatusLine().getStatusCode() == 200) 
								{
									HttpEntity entity = httpResponse.getEntity();
					                is = entity.getContent();
					                try 
					                	{
					                     	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
					                     	sb = new StringBuilder();
					                     	sb.append(reader.readLine() + "\n");
					                     	String line = "0";
					                     	while ((line = reader.readLine()) != null) {
					                     	sb.append(line + "\n");
					                     }
					                     is.close();
					                     result = sb.toString();
					                } catch (Exception e) {
					                Log.e("log_tag", "Error converting result " + e.toString());
					               }
		              
		                String stramp;
		                String strfrequency;
		                String strdate;
		                series = new XYSeries(title);
		                try {
		                     jArray = new JSONArray(result);
		                     JSONObject json_data = null;
		                     for (int i = 0; i < jArray.length()-1; i++) 
		                     {
		                        json_data = jArray.getJSONObject(i);
		                        strdate = json_data.getString("date");
		        				strfrequency = json_data.getString("frequency");
		                        stramp = json_data.getString("amp");
		        				strfrequency = json_data.getString("frequency");
		        				Log.d("MainActivity", "amp is " + stramp);
		        				Log.d("MainActivity", "frequency is " + strfrequency);
		        				xv[i] = Float.parseFloat(strfrequency);
		        				yv[i]= Float.parseFloat(stramp);
		        				series.add(Double.parseDouble(strfrequency), Double.parseDouble(stramp));
		        				Log.d("xv", "xv is " + String.valueOf(i));
		                     }
		                    Log.d("LinearLayout", "m_layout");
		                    Log.d("series.getItemCount()", String.valueOf(series.getItemCount()));
		                    DrawChart();
		                    
		                 } catch (JSONException e1) {

		                 } catch (ParseException e1) {

		                     e1.printStackTrace();
		                 }

					}
					else
					{
						Log.d("MainActivity", "123");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	void DrawChart()
	{
        //创建一个数据集的实例，这个数据集将被用来创建图表
        mDataset = new XYMultipleSeriesDataset();     
        //将点集添加到这个数据集中
        mDataset.addSeries(series);   		        
        //以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
        int color = Color.BLUE;
        PointStyle style = PointStyle.POINT;
        renderer = buildRenderer(color, style, true);
      //设置好图表的样式
        setChartSettings(renderer, "X", "Y", 880, 900, 0, 50, Color.WHITE, Color.WHITE);
        //生成图表      
        Intent intent = ChartFactory.getLineChartIntent(MainActivity.this, mDataset, renderer);
        startActivity(intent);
		
	}
    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle,
    	    double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor)  {
		// TODO Auto-generated method stub
		  //有关对图表的渲染可参看api文档
	     renderer.setChartTitle(title);
	     renderer.setXTitle(xTitle);
	     renderer.setYTitle(yTitle);
	     renderer.setXAxisMin(xMin);
	     renderer.setXAxisMax(xMax);
	     renderer.setYAxisMin(yMin);
	     renderer.setYAxisMax(yMax);
	     renderer.setAxesColor(axesColor);
	     renderer.setLabelsColor(labelsColor);
	     renderer.setShowGrid(false);
	     renderer.setGridColor(Color.GREEN);
	     renderer.setXLabels(5);
	     renderer.setYLabels(5);
	     renderer.setXTitle("频率(KHz)");
	     renderer.setYTitle("幅值(dBm)");
	     renderer.setLabelsTextSize(20);
	     renderer.setYLabelsAlign(Align.RIGHT);
	     renderer.setAxisTitleTextSize(30);
	     renderer.setPointSize((float) 20);
	     renderer.setShowLegend(false);
	     renderer.setChartTitleTextSize(30);
	     renderer.setLegendTextSize(20);
	  // 图表部分的背景颜色  
	     renderer.setBackgroundColor(Color.BLACK);  
	     renderer.setApplyBackgroundColor(true);
	     renderer.setExternalZoomEnabled(false);
	     renderer.setShowAxes(true);
	     
	 


		
	}
    ///////////////////////////////////
    private XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {
		// TODO Auto-generated method stub
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	     
	     //设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
	     XYSeriesRenderer r = new XYSeriesRenderer();
	     r.setColor(color);
	     r.setPointStyle(style);
	     r.setFillPoints(fill);
	     r.setLineWidth(8);
	     renderer.addSeriesRenderer(r);
	     
	     return renderer;
	
	}
	
    //////////////////////////////////////////////////////////

	@Override
	public void onClick(View v) {
		switch (v.getId()) 
		{
		case R.layout.layout_menu:
			Log.d("123", "123");
			break;
		case R.id.title_bar_menu_btn:
			if (slideMenu.isMainScreenShowing()) 
			{
				slideMenu.openMenu();
				
				
			} 
			else 
			{
				slideMenu.closeMenu();
			}
			break;
		}
		
	}

}
