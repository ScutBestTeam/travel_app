package com.lostad.app.demo;

import android.app.Activity;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.avos.avoscloud.AVOSCloud;
import com.lostad.app.base.AppConfig;
import com.lostad.app.demo.entity.LoginConfig;
import com.lostad.app.base.util.PrefManager;
import com.lostad.app.demo.manager.SysManager;
import com.lostad.app.demo.view.chatkitapplication.CustomUserProvider;
import com.lostad.applib.BaseApplication;
import com.lostad.applib.entity.ILoginConfig;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.cookie.CookieJarImpl;
import com.zhy.http.okhttp.cookie.store.PersistentCookieStore;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.util.List;

import cn.leancloud.chatkit.LCChatKit;
import com.avos.avoscloud.im.v2.AVIMClient;

import de.greenrobot.event.EventBus;
import okhttp3.OkHttpClient;

import android.app.Application;
import android.content.Context;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.lostad.app.demo.event.SigninSuccessEvent;
import com.lostad.app.demo.imcloud.AVImClientManager;
import com.lostad.app.demo.imcloud.MessageHandler;
import com.lostad.app.demo.Model.UserInfo;
import com.lostad.app.demo.network.NetworkManager;

/**
 * 存放全局变量
 * 
 * @author sszvip
 * 
 */
public class MyApplication extends BaseApplication
		//implements AMapLocationListener {
{
	public Vibrator mVibrator;
	private LoginConfig mLoginConfig;
	private DbManager mDb;
	private DbManager.DaoConfig mDaoConfig;
	private static MyApplication instance;


	public static float sScale;
	public static int sHeightPix;
	private static Context context;
	private static  ILoginConfig  currUser;

	private final String APP_ID = "QCewswuadUQhNdoeL3E4WdxE-gzGzoHsz";
	private final String APP_KEY = "e1zHmsFoJd99N9PieDJagDAm";
    
    public static MyApplication getInstance() {  
        return instance;  
    }  

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		initFiles();
//		initLocation();
		initDb();
		mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
		//okhttp cookies设置
		CookieJarImpl cookieJar = new CookieJarImpl(new PersistentCookieStore(getApplicationContext()));
		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.cookieJar(cookieJar)
				//其他配置
				.build();
		OkHttpUtils.initClient(okHttpClient);
//////////////////////////////////////////////////
		context = getApplicationContext();
		NetworkManager.initialize(context);
//        Fresco.initialize(this);
		sScale = getResources().getDisplayMetrics().density;
		sHeightPix = getResources().getDisplayMetrics().heightPixels;
		LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
		AVOSCloud.setDebugLogEnabled(true);
		LCChatKit.getInstance().init(getApplicationContext(), APP_ID, APP_KEY);
		AVIMClient.setAutoOpen(false);
	}
	public static Context getAppContext(){
		return context;
	}

	public   static  ILoginConfig getCurrUser() {
		return currUser;
	}
	public   static  void setCurrUser(ILoginConfig iLoginConfig) {
		currUser=iLoginConfig;
	}

//	public static void setCurrUser(UserInfo currUser) {
//		MyApplication.currUser = currUser;
//
//		final String from = currUser.getUsername();
//		AVImClientManager.getInstance().open(from, new AVIMClientCallback() {
//			@Override
//			public void done(AVIMClient avimClient, AVIMException e) {
//				if(e == null){
//					EventBus.getDefault().post(new SigninSuccessEvent());
//				}
//			}
//		});
//	}




	private void initDb(){
		x.Ext.init(this);
		x.Ext.setDebug(AppConfig.isTestMode);
		mDaoConfig = new DbManager.DaoConfig()
				.setDbName(IConst.DB_NAME)
				.setDbDir(new File(IConst.PATH_ROOT))
				.setDbVersion(IConst.DB_VER_NUM)
				.setDbUpgradeListener(new DbManager.DbUpgradeListener() {
					@Override
					public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
						// TODO: ...
						// db.addColumn(...);
						// db.dropTable(...);
						try {
							db.dropTable(LoginConfig.class);
							db.dropTable(UserInfo.class);
						} catch (DbException e) {
							e.printStackTrace();
						}
					}
				});
	}
	private void initFiles() {
		File dir = new File(IConst.PATH_ROOT);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}


	public DbManager getDb() {
		if(mDb==null){
           mDb =  x.getDb(mDaoConfig);
		}
		return mDb;
	}

	@Override
	public ILoginConfig getLoginConfig() {
		if(mLoginConfig==null){
			try {//数据库只存储一个用户
				List<LoginConfig> list = getDb().findAll(LoginConfig.class);
				if(list!=null && list.size()>0){
					mLoginConfig = list.get(0);
				}
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
		return mLoginConfig;
	}

	@Override
	public void saveLoginConfig(ILoginConfig login) {
        if(login!=null){
			try {
				mLoginConfig = (LoginConfig)login;
				getDb().saveOrUpdate(mLoginConfig);
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 初始化定位
	 */
//	private void initLocation() {
//		// 初始化定位，只采用网络定位
//		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
//		mLocationManagerProxy.setGpsEnable(false);
//		// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
//		// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
//		// 在定位结束后，在合适的生命周期调用destroy()方法
//		// 其中如果间隔时间为-1，则定位只定一次,
//		// 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
//		mLocationManagerProxy.requestLocationData(
//				LocationProviderProxy.AMapNetwork, 60 * 1000, 15, this);
//
//	}

//	@Override
//	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
//	}
//
//	@Override
//	public void onProviderEnabled(String arg0) {
//	}
//
//	@Override
//	public void onProviderDisabled(String arg0) {
//	}
//
//	@Override
//	public void onLocationChanged(Location arg0) {
//		// TODO Auto-generated method stub
//	}

//	@Override
//	public void onLocationChanged(AMapLocation location) {
//
//		if (location != null && location.getAMapException().getErrorCode() == 0) {
//			// 定位成功回调信息，设置相关消息
//			String province = location.getProvince();
//			String city = location.getCity();
//			String district = location.getDistrict();// 区
//			Double lat = location.getLatitude();
//			Double log = location.getLongitude();
//
//			PrefManager.saveString(getApplicationContext(),
//					IConst.KEY_GIS_PROVINCE, province);
//			PrefManager.saveString(getApplicationContext(),
//					IConst.KEY_GIS_CITY, city);
//			PrefManager.saveString(getApplicationContext(),
//					IConst.KEY_GIS_DISTRICT, district);
//
//			PrefManager.saveFloat(getApplicationContext(),
//					IConst.KEY_GIS_LATITUDE, lat.floatValue());
//			PrefManager.saveFloat(getApplicationContext(),
//					IConst.KEY_GIS_LONGTITUDE, log.floatValue());
//		} else {
//			Log.e("AmapErr", "Location ERR:"
//					+ location.getAMapException().getErrorCode());
//		}
//
//	}

	private LocationManagerProxy mLocationManagerProxy;

	public  void dbquit() {
		try {
			getDb().delete(LoginConfig.class);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		@Override
		public void quit(boolean isClearData){
			if(isClearData){
			SysManager.getInstance().logout(this);
			}
		}



}
