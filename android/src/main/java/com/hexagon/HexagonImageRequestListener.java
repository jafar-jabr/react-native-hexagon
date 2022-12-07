package com.hexagon;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class HexagonImageRequestListener implements RequestListener<Drawable> {
    static final String REACT_ON_ERROR_EVENT = "onHexagonImageError";
    static final String REACT_ON_LOAD_EVENT = "onHexagonImageLoad";
    static final String REACT_ON_LOAD_END_EVENT = "onHexagonImageLoadEnd";
    private final String key;

    HexagonImageRequestListener(String key) {
        this.key = key;
    }

    private static WritableMap mapFromResource(Drawable resource) {
        WritableMap resourceData = new WritableNativeMap();
        resourceData.putInt("width", resource.getIntrinsicWidth());
        resourceData.putInt("height", resource.getIntrinsicHeight());
        return resourceData;
    }

    @Override
    public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
        HexagonImageOkHttpProgressGlideModule.forget(key);
        if (!(target instanceof ImageViewTarget)) {
            return false;
        }
        HexagonImageViewWithUrl view = (HexagonImageViewWithUrl) ((ImageViewTarget) target).getView();
        ThemedReactContext context = (ThemedReactContext) view.getContext();
        RCTEventEmitter eventEmitter = context.getJSModule(RCTEventEmitter.class);
        int viewId = view.getId();
        eventEmitter.receiveEvent(viewId, REACT_ON_ERROR_EVENT, new WritableNativeMap());
        eventEmitter.receiveEvent(viewId, REACT_ON_LOAD_END_EVENT, new WritableNativeMap());
        return false;
    }

    @Override
    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
        if (!(target instanceof ImageViewTarget)) {
            return false;
        }
        HexagonImageViewWithUrl view = (HexagonImageViewWithUrl) ((ImageViewTarget) target).getView();
        ThemedReactContext context = (ThemedReactContext) view.getContext();
        RCTEventEmitter eventEmitter = context.getJSModule(RCTEventEmitter.class);
        int viewId = view.getId();
        eventEmitter.receiveEvent(viewId, REACT_ON_LOAD_EVENT, mapFromResource(resource));
        eventEmitter.receiveEvent(viewId, REACT_ON_LOAD_END_EVENT, new WritableNativeMap());
        return false;
    }
}
