package com.lostad.app.demo.view.my;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lostad.app.base.util.DownloadUtil;
import com.lostad.app.base.util.ImageChooserUtil;
import com.lostad.app.base.util.ImageTools;
import com.lostad.app.base.util.Validator;
import com.lostad.app.base.view.BaseActivity;
import com.lostad.app.base.view.component.BaseFormActivity;
import com.lostad.app.base.view.component.FormNumActivity;
import com.lostad.app.base.view.component.FormTextActivity;
import com.lostad.app.base.view.component.FormTextChinaeseActivity;
import com.lostad.app.demo.IConst;
import com.lostad.app.demo.R;
import com.lostad.app.demo.entity.UserInfo;
import com.lostad.applib.core.MyCallback;
import com.lostad.applib.util.DialogUtil;
import com.lostad.applib.util.FileDataUtil;

import org.xutils.ex.DbException;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;

public class FormMyInfoActivity extends BaseActivity {
    @ViewInject(R.id.tb_toolbar)
    private Toolbar tb_toolbar;

    @ViewInject(R.id.iv_head)
    private ImageView iv_head;

    @ViewInject(R.id.tv_nickname)
    private TextView tv_nickname;

    @ViewInject(R.id.tv_sex)
    private TextView tv_sex;

    @ViewInject(R.id.tv_weight)
    private TextView tv_weight;

    @ViewInject(R.id.tv_height)
    private TextView tv_height;

    @ViewInject(R.id.tv_age)
    private TextView tv_age;

    @ViewInject(R.id.tv_addr)
    private TextView tv_addr;

    private UserInfo mSysConfig;
    private File mFileHead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_myinfo);
        x.view().inject(this);
        super.initToolBarWithBack(tb_toolbar);
        setTitle("个人资料");
        try {
            mSysConfig = getMyApp().getDb().findById(UserInfo.class, getLoginConfig().getUserId());
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (mSysConfig != null) {
            initUI(mSysConfig);
        }
    }

    @Event(R.id.iv_head)
    private void onClickHead(View v) {
        ImageChooserUtil.showPicturePicker(this, true);
//      Intent i = new Intent(this,HeadGridActivity.class);
//      startActivityForResult(i, 100);
    }

    //	public void onClickNext(View v) {
//		next();
//	}
    @Event(R.id.ll_nickname)
    private void onClickName(View v) {
        try {
            Intent i = new Intent(FormMyInfoActivity.this, FormTextChinaeseActivity.class);
            i.putExtra("value", tv_nickname.getText());
            i.putExtra(FormTextActivity.KEY_MAX_LEN, 12);
            startActivityForResult(i, R.id.ll_nickname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Event(R.id.ll_sex)
    private void onClickSex(View v) {
        try {
            String[] itemList = {"男", "女", "取消"};
            DialogUtil.showAlertMenuCust(ctx, "选择性别", itemList, new MyCallback<Integer>() {
                @Override
                public void onCallback(Integer index) {
                    if (0 == index) {
                        tv_sex.setText("男");
                        mSysConfig.setSex("1");
                    } else if (1 == index) {
                        tv_sex.setText("女");
                        mSysConfig.setSex("2");
                    } else {
                        tv_sex.setText("");
                        mSysConfig.setSex("");
                    }
                    update(mSysConfig);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update(UserInfo u) {
        try {
            getMyApp().getDb().saveOrUpdate(u);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Event(R.id.ll_weight)
    private void onClickWeight(View v) {
        Intent i = new Intent(FormMyInfoActivity.this, FormNumActivity.class);
        i.putExtra(FormNumActivity.KEY_IS_INT, true);
        i.putExtra("value", tv_weight.getText());
        i.putExtra("desc", "填写体重，让系统对您的运动做出更合理的评估");
        i.putExtra(FormNumActivity.KEY_MIN_VALUE, 30);
        i.putExtra(FormNumActivity.KEY_MAX_VALUE, 150);
        i.putExtra(FormNumActivity.KEY_MAX_DESC, "体重不能大于150kg");
        i.putExtra(FormNumActivity.KEY_MIN_DESC, "体重不能小于30kg");

        i.putExtra(FormNumActivity.KEY_NULL_ABLE, false);
        startActivityForResult(i, R.id.ll_weight);
    }

    @Event(R.id.ll_height)
    private void onClickHeight(View v) {
        Intent i = new Intent(FormMyInfoActivity.this, FormNumActivity.class);
        i.putExtra("value", tv_height.getText());
        i.putExtra("desc", "填写身高，让系统对您的运动做出更合理的评估");
        i.putExtra(FormNumActivity.KEY_MAX_VALUE, 240.0);
        i.putExtra(FormNumActivity.KEY_MAX_DESC, "身高不能高于240cm");
        i.putExtra(FormNumActivity.KEY_MIN_VALUE, 80.0);
        i.putExtra(FormNumActivity.KEY_MIN_DESC, "身高不能低于80cm");
        i.putExtra(FormNumActivity.KEY_NULL_ABLE, false);
        startActivityForResult(i, R.id.ll_height);
    }

    @Event(R.id.ll_age)
    private void onClickBirt(View v) {
        Intent i = new Intent(FormMyInfoActivity.this, FormNumActivity.class);
        i.putExtra(FormNumActivity.KEY_MAX_LEN, 2);
        i.putExtra(FormNumActivity.KEY_IS_INT, true);
        i.putExtra(FormNumActivity.KEY_MAX_VALUE, 65);
        i.putExtra(FormNumActivity.KEY_MIN_VALUE, 14);
        i.putExtra("value", tv_age.getText());
        i.putExtra("desc", "填写年龄,让系统帮助您匹配更合适的健身伙伴");
        i.putExtra(FormNumActivity.KEY_NULL_ABLE, false);
        startActivityForResult(i, R.id.ll_age);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        String d;
        switch (requestCode) {


            case R.id.ll_nickname:
                d = data.getStringExtra(BaseFormActivity.KEY_VALUE);
                tv_nickname.setText(d);
                mSysConfig.setNickname(d);

                if (!Validator.isBlank(d)) {
                    update(mSysConfig);
                }
                break;

            case R.id.iv_head:

                ImageChooserUtil.onActivityResult(ctx, requestCode, resultCode, data, new ImageChooserUtil.PicCallback() {
                    @Override
                    public void onPicSelected(Bitmap bitmap) {
                        //System.out.println(bitmap);
                        iv_head.setImageBitmap(bitmap);
                        String fileName = FileDataUtil.createJpgFileName(getLoginConfig().getUserId() + "");
                        mFileHead = ImageTools.savePhotoToSDCard(bitmap, IConst.PATH_ROOT, fileName);

                        if (mFileHead != null && mFileHead.exists()) {
                            uploadHead(mFileHead);
                        } else {
                            DialogUtil.showAlert(ctx, "文件不存在！！！", null);
                        }
                    }
                });
                break;
            default:
                showToast("未处理。。。");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initUI(final UserInfo config) {

        if (config == null)
            return;

        if (Validator.isNotEmpty(config.headUrl)) {
            DownloadUtil.loadImage(this, iv_head, config.headUrl);
        }
        tv_nickname.setText(config.nickname);
        setSexValue(mSysConfig.sex);
    }


    public void setSexValue(String sex) {
        try {
            if (sex == null || "1".equals(sex)) {//默认是男
                tv_sex.setText("男");
            } else {
                tv_sex.setText("女");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//        ComponentName c = getCallingActivity();
//       
//    	if(!mSysConfig.isInfoReay()){
//	        getMenuInflater().inflate(R.menu.menu_pass, menu);
//		}
//
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		
//		if (item.getItemId() == R.id.action_next) {
//			next();
//		}
//
//		return super.onOptionsItemSelected(item);
//	}


    private void uploadHead(final File mFileSelected) {
        if (mFileSelected == null) {
            return;
        }
//		new Thread(){
//			public void run() {
//				final String key =IConst.ALIYUN_OSS_KEY_PREFIX_HEDAD+"/"+FileDataUtil.createJpgFileName(getLoginConfig().getId().toString());
//				FileManager.getInstance(ctx).resumableUpload(mFileSelected.getAbsolutePath(), key,new SaveCallback() {
//					@Override
//					public void onSuccess(final String objectKey) {
//
//						mSysConfig.setPicpath(objectKey);
//						LogMe.d(key+"<<<<<<<<<<<<<<<<"+objectKey);
//						runOnUiThread(new Runnable() {
//							@Override
//							public void run() {
//								DialogUtil.showToasMsg(ctx, "上传成功！");
//								mSysConfig.setPicpath(objectKey);
//								update(mSysConfig);
//								String url = getMyApplication().getPicSSO()+objectKey;
//								//DownloadUtil.loadImage(iv_head, url,R.drawable.head_default, R.drawable.head_default, R.drawable.head_default);
//							}
//						});
//					}
//
//					@Override
//					public void onProgress(String objectKey, int byteCount, int totalSize) {
//					}
//
//					@Override
//					public void onFailure(String objectKey, OSSException ossException) {
//						ossException.printStackTrace();
//						runOnUiThread(new Runnable() {
//							@Override
//							public void run() {
//								DialogUtil.showToasMsg(ctx, "上传失败！");
//								//getFinalBitmap().display(iv_head, mSysConfig.getPicpath());
//							}
//						});
//
//					}
//				});
//
//			};
//		}.start();

    }


}
