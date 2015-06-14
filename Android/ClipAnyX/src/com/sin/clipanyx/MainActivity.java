package com.sin.clipanyx;

import java.util.List;
import java.util.Locale;

import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import com.sin.android.sinlibs.activities.BaseActivity;
import com.sin.android.sinlibs.base.Callable;
import com.sin.clipanyx.model.TextData;

public class MainActivity extends BaseActivity {
	private String uid = "trb";

	private int gapsec = 10;

	private String lastText = null;

	private TextView tv_log = null;
	private int logid = 0;

	private boolean running = true;

	private ClipboardManager clipboard;

	public ClipboardManager getClipboard() {
		if (clipboard == null)
			clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		return clipboard;
	}

	private void addLog(final String log) {

		safeCall(new Callable() {

			@Override
			public void call(Object... args) {
				if (tv_log == null)
					tv_log = (TextView) findViewById(R.id.tv_log);

				tv_log.setText(String.format(Locale.getDefault(), "%04x>%s", logid++, log) + "\r\n" + tv_log.getText());
			}
		});

		if (((CheckBox) findViewById(R.id.cb_showtip)).isChecked()) {
			safeToast(log);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Bmob.initialize(this, Values.BMOB_APPKEY);

		// SMSSDK.initSDK(this, Values.MOB_APPKEY, Values.MOB_APPSEC);

		clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		((EditText) findViewById(R.id.et_gapsec)).setText("" + gapsec);
		((EditText) findViewById(R.id.et_token)).setText(uid);

		findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					uid = ((EditText) findViewById(R.id.et_token)).getText().toString();
					gapsec = Integer.parseInt(((EditText) findViewById(R.id.et_gapsec)).getText().toString());
					if (gapsec < 1)
						gapsec = 1;
				} catch (Exception e) {
					e.printStackTrace();
					addLog(e.getMessage());
				}
				((EditText) findViewById(R.id.et_gapsec)).setText("" + gapsec);
				((EditText) findViewById(R.id.et_token)).setText(uid);
			}
		});

		asynCall(new Callable() {

			@Override
			public void call(Object... args) {
				while (running) {
					BmobQuery<TextData> query = new BmobQuery<TextData>();
					query.order("-createdAt");
					query.addWhereEqualTo("uid", uid);
					query.setLimit(1);
					query.findObjects(MainActivity.this, new FindListener<TextData>() {

						@Override
						public void onSuccess(List<TextData> dats) {
							if (dats.size() > 0) {
								String newText = dats.get(0).text;
								if (!newText.equals(lastText) || lastText == null) {
									lastText = newText;

									addLog("Load:" + lastText);
									ClipData clipData = ClipData.newPlainText("tp", lastText);
									getClipboard().setPrimaryClip(clipData);
								}
							}
							if (lastText == null) {
								lastText = "";
							}
						}

						@Override
						public void onError(int arg0, String arg1) {
							addLog("onFailure " + arg0 + " " + arg1);
							lastText = "";
						}
					});

					try {
						Thread.sleep(gapsec * 1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		asynCall(new Callable() {

			@Override
			public void call(Object... args) {
				while (running) {
					if (getClipboard().getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
						ClipData cdText = clipboard.getPrimaryClip();
						Item item = cdText.getItemAt(0);
						if (item.getText() != null) {
							String newText = item.getText().toString();
							if (lastText != null && !newText.equals(lastText)) {
								lastText = newText;

								TextData t = new TextData(lastText);
								t.uid = uid;
								t.save(MainActivity.this, new SaveListener() {

									@Override
									public void onSuccess() {
										addLog("Save:" + lastText);
									}

									@Override
									public void onFailure(int arg0, String arg1) {
										addLog("onFailure " + arg0 + " " + arg1);
									}
								});
							}
						}
					}

					try {
						Thread.sleep(gapsec * 500);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		running = false;
		super.onDestroy();
	}
}
