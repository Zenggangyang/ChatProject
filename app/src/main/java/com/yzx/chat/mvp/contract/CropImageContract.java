package com.yzx.chat.mvp.contract;

import android.graphics.Bitmap;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2018年03月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CropImageContract {

    public interface View extends BaseView<Presenter> {
        void goBack();

        void setEnableProgressDialog(boolean isEnable);

        void returnSaveResult(String imagePath);

        void showError(String error);
    }

    public interface Presenter extends BasePresenter<View> {
        void bitmapToFile(Bitmap bitmap);
    }

}
