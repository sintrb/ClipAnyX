package com.sin.clipanyx;

import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.os.Bundle;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.listener.SaveListener;

import com.sin.android.sinlibs.activities.BaseActivity;
import com.sin.android.sinlibs.base.Callable;
import com.sin.android.sinlibs.utils.IntervalRunner;
import com.sin.clipanyx.model.TextData;
import com.sin.clipanyx.model.User;

public class MainActivity extends BaseActivity {
	private static User user = new User();

	private String lastText = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Bmob.initialize(this, "cf7c391e0adea90eb0855f516ee78590");

		user.setUsername("sintrb");
		user.setPassword("trb123");
		user.login(this, new SaveListener() {

			@Override
			public void onSuccess() {
				safeToast("onSuccess");
				IntervalRunner.run(new Callable() {
					@Override
					public void call(Object... args) {
						safeCall(new Callable() {
							@Override
							public void call(Object... args) {
								ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
								if (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
									ClipData cdText = clipboard.getPrimaryClip();
									Item item = cdText.getItemAt(0);
									if (item.getText() != null) {
										String newText = item.getText().toString();
										if (!newText.equals(lastText)) {
											lastText = newText;
											TextData t = new TextData(lastText);
											user.saveObj(MainActivity.this, t);

											t.setObjectId("last");
											t.save(MainActivity.this);
										}
									}
								}
							}
						});
					}
				}, 10000);

			}

			@Override
			public void onFailure(int arg0, String arg1) {
				safeToast("onFailure " + arg0 + " " + arg1);
			}
		});
	}
}
