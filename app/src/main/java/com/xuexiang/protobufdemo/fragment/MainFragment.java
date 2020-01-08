package com.xuexiang.protobufdemo.fragment;

import android.view.KeyEvent;

import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageContainerListFragment;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xpage.utils.TitleBar;
import com.xuexiang.xutil.common.ClickUtils;

/**
 * @author xuexiang
 * @since 2018/11/7 下午1:16
 */
@Page(name = "ProtoBuf-gRPC-Android", anim = CoreAnim.none)
public class MainFragment extends XPageContainerListFragment {

    @Override
    protected Class[] getPagesClasses() {
        return new Class[] {
                //此处填写fragment
                CommonChannelFragment.class,
                SSLChannelFragment.class,
                BRPCFragment.class,
                XHttpApiTestFragment.class
        };
    }

    @Override
    protected TitleBar initTitleBar() {
        return super.initTitleBar().setLeftClickListener(view -> ClickUtils.exitBy2Click());
    }


    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ClickUtils.exitBy2Click();
        }
        return true;
    }

}
