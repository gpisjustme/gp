package com.netease.isport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.netease.util.GetIntentInstance;
import com.netease.util.NetWorkUtil;
import com.netease.util.PostandGetConnectionUtil;
import com.netease.util.RoundImageUtil;
import com.netease.util.SharedPreferenceUtil;
import com.netease.util.ToastUtil;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class UserProfileActivity extends Activity 
	implements OnViewChangeListener, OnClickListener{
	private ImageView photoCh;
	private ImageView mSexImage;
	private TextView  mUserName;
	private TextView  mUserLabel;
	private Bitmap    mDefaultBit;
	
	private static final int REQUEST_CODE = 1;//选择文件的返回码
	//private Intent fileChooserIntent;
	
	Intent intent        = GetIntentInstance.getIntent();
	ImageView preStep    = null;
	
	private LinearLayout[] mImageViews;
	private int mViewCount;
	private int mCurSel;
	private TextView mCompletedTextView;
	private TextView mUnCompletedTextView;
	private ListScrollLayout mScrollLayout;
	private ListView  mCompletedListView;
	private ListView  mUnCompletedListView;
	//private ImageView mEditUserProfile;
	private ListItemArrayAdapter mCompletedListAdapter;
	private ListItemArrayAdapter mUnCompletedListAdapter;
	private Boolean mBeMyself = true;
	private String  mOtherName;
	ArrayList<ListItem> mCompletedItemArray   = new ArrayList<ListItem>();
	ArrayList<ListItem> mUnCompletedItemArray = new ArrayList<ListItem>();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_user_profile);
		preStep=(ImageView)findViewById(R.id.title_bar_menu_btn);
		photoCh=(ImageView)findViewById(R.id.change_photo);
		mSexImage = (ImageView) findViewById(R.id.sex_profile);
		mUserName = (TextView) findViewById(R.id.user_name_profile);
		mUserLabel = (TextView) findViewById(R.id.label_profile);
		Intent intent = getIntent();
		String user_name = intent.getStringExtra("user");
		if (user_name.equals("other") )
			mBeMyself = false;
		if(! mBeMyself)
			mOtherName = intent.getStringExtra("name");
		//mEditUserProfile = (ImageView) findViewById(R.id.edit_user_profle);
		//mEditUserProfile.setOnClickListener(this);
		preStep.setOnClickListener(this);
		photoCh.setOnClickListener(this);
		//fileChooserIntent = new Intent(this,fileChooserActivity.class);
		mCompletedTextView   = (TextView) findViewById(R.id.compeleted_text);
		mUnCompletedTextView = (TextView) findViewById(R.id.uncompeleted_text);
		mScrollLayout = (ListScrollLayout) findViewById(R.id.listScrollLayout);
		mViewCount = mScrollLayout.getChildCount();
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.lllayout);
    	mImageViews = new LinearLayout[mViewCount];   	
    	for(int i = 0; i < mViewCount; i++)    	{
    		mImageViews[i] = (LinearLayout) linearLayout.getChildAt(i);
    		mImageViews[i].setEnabled(true);
    		mImageViews[i].setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int pos = (Integer)(v.getTag());
					setCurPoint(pos);
					mScrollLayout.snapToScreen(pos);
				}
    		});
    		mImageViews[i].setTag(i);
    	}
    	mCurSel = 0;
    	mImageViews[mCurSel].setEnabled(false);    	
    	mScrollLayout.SetOnViewChangeListener(this);
    	if(mBeMyself) {
	    	SharedPreferences sp = SharedPreferenceUtil.getSharedPreferences();
	    	String imageBase64 = sp.getString("imageBase64", "");
	    	String username = sp.getString("username", "");
	    	String sex = sp.getString("sex", "");
	    	String label = sp.getString("label", "");
	    	if(sex.equals("F")) {
	    		mSexImage.setImageResource(R.drawable.girl);
	    	} else {
	    		mSexImage.setImageResource(R.drawable.boy);
	    	}
	    	mUserName.setText(username);
	    	mUserLabel.setText(label);
			byte[] base64Bytes = Base64.decode(imageBase64.getBytes(), Base64.DEFAULT);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
			Bitmap bitmap = RoundImageUtil.toRoundCorner(BitmapFactory.decodeStream(bais));
			photoCh.setImageBitmap(bitmap);
			mDefaultBit = BitmapFactory.decodeResource(getResources(), R.drawable.user_photo);
    	} else {
    		try {
				setInfo_other();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		
		try {
			setJobs(PostandGetConnectionUtil.getCompletedUrl, mCompletedItemArray);
			setJobs(PostandGetConnectionUtil.getUnCompletedUrl, mUnCompletedItemArray);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCompletedListAdapter = new ListItemArrayAdapter(UserProfileActivity.this,
				R.layout.list_item, mCompletedItemArray);
		mUnCompletedListAdapter = new ListItemArrayAdapter(UserProfileActivity.this,
				R.layout.list_item, mUnCompletedItemArray);
		mCompletedListView   = (ListView) findViewById(R.id.completed_list);
		mUnCompletedListView = (ListView) findViewById(R.id.uncompleted_list);
		mCompletedListView.setItemsCanFocus(false);
		mUnCompletedListView.setItemsCanFocus(false);
		mCompletedListView.setAdapter(mCompletedListAdapter);
		mUnCompletedListView.setAdapter(mUnCompletedListAdapter);
		
		mUnCompletedListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("id", mUnCompletedItemArray.get((int) arg3).getmAcTId());
				intent.putExtra("name", mUnCompletedItemArray.get((int) arg3).getmUserName());
				intent.setClass(UserProfileActivity.this, InfoActivity.class);
				startActivity(intent);
			}
			
		});
		
		mCompletedListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("id", mCompletedItemArray.get((int) arg3).getmAcTId());
				intent.putExtra("name", mCompletedItemArray.get((int) arg3).getmUserName());
				intent.setClass(UserProfileActivity.this, InfoActivity.class);
				startActivity(intent);
			}
			
		});
	}
	
	void setInfo_other() throws URISyntaxException {
		if( !NetWorkUtil.isNetworkConnected(this.getApplicationContext()) ) {
			ToastUtil.show(getApplicationContext(), "网络服务不可用，请检查网络状态！");
			return;
		}
		List<NameValuePair> list=new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("name", mOtherName));
		list.add(new BasicNameValuePair("other", "y"));
    	HttpResponse res = PostandGetConnectionUtil.getConnect(PostandGetConnectionUtil.getinfoUrl, list);
		if (PostandGetConnectionUtil.responseCode(res) != 200)
			return;
		String json_str = PostandGetConnectionUtil.GetResponseMessage(res);
		if(json_str.length() != 0) {
			JsonInfoResult o = new DecodeJson().jsonInfo(json_str);
			if(o.getRet().equals("ok")) {
				mUserName.setText(o.getUsername());
				mUserLabel.setText(o.getLabel());
				if(o.getSex().equals("F")) {
		    		mSexImage.setImageResource(R.drawable.girl);
		    	} else {
		    		mSexImage.setImageResource(R.drawable.boy);
		    	}
				String image_location = PostandGetConnectionUtil.mediaUrlBase + o.getUserimage();
				try{
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					URL url_image = new URL(image_location);  
					InputStream is = url_image.openStream();  
					Bitmap bitmap  = BitmapFactory.decodeStream(is);
					photoCh.setImageBitmap(RoundImageUtil.toRoundCorner(bitmap));
					is.close();
				} catch(Exception e) {
		            e.printStackTrace();  
		        } 
			}
		}
	}
	
	void setJobs(String url, ArrayList<ListItem> itemArray) throws URISyntaxException {
		if( !NetWorkUtil.isNetworkConnected(this.getApplicationContext()) ) {
			ToastUtil.show(getApplicationContext(), "网络服务不可用，请检查网络状态！");
			return;
		}
		List<NameValuePair> list=new ArrayList<NameValuePair>();
		if (mBeMyself)
			list.add(new BasicNameValuePair("name", (String) mUserName.getText()));
		else
			list.add(new BasicNameValuePair("name", mOtherName));
    	HttpResponse res = PostandGetConnectionUtil.getConnect(url, list);
		if (PostandGetConnectionUtil.responseCode(res) != 200)
			return;
//		Toast.makeText(UserProfileActivity.this, 
//				 "setJobs", Toast.LENGTH_LONG).show();
		String json_str = PostandGetConnectionUtil.GetResponseMessage(res);
		if(json_str.length() != 0) {
			JsonPushRet o = new DecodeJson().jsonPush(json_str);
			itemArray.clear();
			if(o.getRet().equals("ok")) {
				int count = o.getCount();
				for(int i = 0; i < count; i++) {
					String theme = "主题：" + o.getList().get(i).getTheme();
					String details = "正文：" + o.getList().get(i).getDetails();
					String time = "时间：" + o.getList().get(i).getTime();
					String cnt = "人数："+ o.getList().get(i).getCount();
					String name = o.getList().get(i).getName();
					String img = o.getList().get(i).getImg();
					String id  = o.getList().get(i).getId();
					Bitmap bitmap = mDefaultBit;
					String image_location = PostandGetConnectionUtil.mediaUrlBase + img;
					// get the image from the url
					try{
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						URL url_image = new URL(image_location);  
						InputStream is = url_image.openStream();
						bitmap = RoundImageUtil.toRoundCorner(BitmapFactory.decodeStream(is));
						is.close();
					} catch(Exception e) {
			            e.printStackTrace();  
			        }
					itemArray.add(new ListItem(name, theme, time, cnt, details, id, bitmap));
				}
			}
		}
	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 switch(requestCode) {
		 case REQUEST_CODE:
			 if (resultCode == 0)  
				 return; 
			 Uri uri = data.getData();
			 Bitmap bmp = null;
			 ContentResolver cr = this.getContentResolver();   
		     try {
	            bmp = BitmapFactory.decodeStream(cr.openInputStream(uri));
		     } catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
		     }
		     if(bmp != null) {
			     Bitmap output = RoundImageUtil.toRoundCorner(RoundImageUtil.
			    		 resizeImage(bmp, 100, 100));
			     photoCh.setImageBitmap(output);
		     }
	         break;
		 }
	}
	
	private void setCurPoint(int index)
    {
    	if (index < 0 || index > mViewCount - 1 || mCurSel == index){
    		return ;
    	}    	
    	mImageViews[mCurSel].setEnabled(true);
    	mImageViews[index].setEnabled(false);    	
    	mCurSel = index;
    	
    	if(index == 0){
    		mUnCompletedTextView.setTextColor(0xff228B22);
    		mCompletedTextView.setTextColor(Color.BLACK);
    	} else {
    		mUnCompletedTextView.setTextColor(Color.BLACK);
    		mCompletedTextView.setTextColor(0xff228B22);
    	}
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.title_bar_menu_btn :{
				UserProfileActivity.this.finish();
				break;
			}
		}
		
	}
	private void toast(CharSequence hint){
	    Toast.makeText(this, hint , Toast.LENGTH_SHORT).show();
	}

	@Override
	public void OnViewChange(int view) {
		// TODO Auto-generated method stub
		setCurPoint(view);
	}
}
