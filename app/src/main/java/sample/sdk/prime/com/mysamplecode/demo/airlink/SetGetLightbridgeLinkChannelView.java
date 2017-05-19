package sample.sdk.prime.com.mysamplecode.demo.airlink;

import android.content.Context;
import android.widget.ArrayAdapter;
import sample.sdk.prime.com.mysamplecode.R;
import sample.sdk.prime.com.mysamplecode.internal.controller.DJISampleApplication;
import sample.sdk.prime.com.mysamplecode.internal.utils.CallbackHandlers;
import sample.sdk.prime.com.mysamplecode.internal.utils.ModuleVerificationUtil;
import sample.sdk.prime.com.mysamplecode.internal.utils.ToastUtils;
import sample.sdk.prime.com.mysamplecode.internal.view.BaseSetGetView;
import dji.common.airlink.ChannelSelectionMode;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import java.util.ArrayList;

/**
 * Class for setting and getting channel in Lightbridge.
 */
public class SetGetLightbridgeLinkChannelView extends BaseSetGetView {
    public SetGetLightbridgeLinkChannelView(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (ModuleVerificationUtil.isLightbridgeLinkAvailable()) {
            DJISampleApplication.getProductInstance()
                                .getAirLink()
                                .getLightbridgeLink()
                                .setChannelSelectionMode(ChannelSelectionMode.MANUAL,
                                                         new CallbackHandlers.CallbackToastHandler());
        } else {
            ToastUtils.setResultToToast("Did not support.");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (ModuleVerificationUtil.isLightbridgeLinkAvailable()) {
            DJISampleApplication.getProductInstance()
                                .getAirLink()
                                .getLightbridgeLink()
                                .setChannelSelectionMode(ChannelSelectionMode.AUTO,
                                                         new CallbackHandlers.CallbackToastHandler());
        }
    }

    @Override
    protected void setMethod() {
        if (ModuleVerificationUtil.isLightbridgeLinkAvailable()) {
            DJISampleApplication.getProductInstance()
                                .getAirLink()
                                .getLightbridgeLink()
                                .setChannelNumber(mSpinnerSet.getSelectedItemPosition(),
                                                  new CallbackHandlers.CallbackToastHandler());
        }
    }

    @Override
    protected void getMethod() {
        if (ModuleVerificationUtil.isLightbridgeLinkAvailable()) {
            DJISampleApplication.getProductInstance()
                                .getAirLink()
                                .getLightbridgeLink()
                                .getChannelNumber(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                                    @Override
                                    public void onSuccess(Integer integer) {
                                        mGetTextString = integer.toString();
                                        mHandler.sendEmptyMessage(SET_GET_TEXTVIEW_WITH_RESULT);
                                    }

                                    @Override
                                    public void onFailure(DJIError djiError) {

                                    }
                                });
        }
    }

    @Override
    protected ArrayAdapter getArrayAdapter() {
        ArrayList<Integer> array = new ArrayList<>();
        for (int i = 13; i < 21; i++) {
            array.add(i);
        }
        return new ArrayAdapter(this.getContext(), R.layout.simple_list_item, array);
    }

    @Override
    public int getDescription() {
        return R.string.set_get_lb_airlink_channel_description;
    }
}
