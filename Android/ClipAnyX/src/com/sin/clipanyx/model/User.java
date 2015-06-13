package com.sin.clipanyx.model;

import android.content.Context;
import cn.bmob.v3.BmobACL;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.SaveListener;

public class User extends BmobUser {
	private static final long serialVersionUID = 7153683316928611599L;

	public void saveObj(Context context, BmobObject obj) {
		BmobACL acl = new BmobACL();
		acl.setReadAccess(this, true);
		acl.setWriteAccess(this, true);
		obj.setACL(acl);

		obj.save(context);
	}

	public void saveObj(Context context, BmobObject obj, SaveListener insertListener) {
		BmobACL acl = new BmobACL();
		acl.setReadAccess(this, true);
		acl.setWriteAccess(this, true);
		obj.setACL(acl);

		obj.save(context, insertListener);
	}
}
