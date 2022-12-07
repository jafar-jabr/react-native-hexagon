package com.hexagon;

import static com.hexagon.HexagonImageRequestListener.REACT_ON_ERROR_EVENT;
import static com.hexagon.HexagonImageRequestListener.REACT_ON_LOAD_END_EVENT;
import static com.hexagon.HexagonImageRequestListener.REACT_ON_LOAD_EVENT;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

class HexagonImageViewManager extends SimpleViewManager<HexagonImageViewWithUrl> implements HexagonImageProgressListener {

    static final String REACT_CLASS = "HexagonImageView";
    static final String REACT_ON_LOAD_START_EVENT = "onHexagonImageLoadStart";
    static final String REACT_ON_PROGRESS_EVENT = "onHexagonImageProgress";
    private static final Map<String, List<HexagonImageViewWithUrl>> VIEWS_FOR_URLS = new WeakHashMap<>();

    @Nullable
    private RequestManager requestManager = null;

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected HexagonImageViewWithUrl createViewInstance(@NonNull ThemedReactContext reactContext) {
        if (isValidContextForGlide(reactContext)) {
            requestManager = Glide.with(reactContext);
        }

        return new HexagonImageViewWithUrl(reactContext);
    }

    @ReactProp(name = "source")
    public void setSource(HexagonImageViewWithUrl view, @Nullable ReadableMap source) {
            view.setSource(source);
            int cornerRadius= source.getInt("cornerRadius");
            int borderWidth= source.getInt("borderWidth");
            String  borderColor= source.getString("borderColor");
            view.setBorderWidth(borderWidth);
            view.setCornerRadius(cornerRadius);
            if(borderColor!=null){
                view.setBorderColor(Color.parseColor(borderColor));
            }
    }

    @ReactProp(name = "defaultSource")
    public void setDefaultSource(HexagonImageViewWithUrl view, @Nullable String source) {
        view.setDefaultSource(
                ResourceDrawableIdHelper.getInstance()
                        .getResourceDrawable(view.getContext(), source));
    }

    @ReactProp(name = "tintColor", customType = "Color")
    public void setTintColor(HexagonImageViewWithUrl view, @Nullable Integer color) {
        if (color == null) {
            view.clearColorFilter();
        } else {
            view.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    @ReactProp(name = "resizeMode")
    public void setResizeMode(HexagonImageViewWithUrl view, String resizeMode) {
        final HexagonImageViewWithUrl.ScaleType scaleType = HexagonImageViewConverter.getScaleType(resizeMode);
        view.setScaleType(scaleType);
    }

    @Override
    public void onDropViewInstance(@NonNull HexagonImageViewWithUrl view) {
        // This will cancel existing requests.
        view.clearView(requestManager);

        if (view.glideUrl != null) {
            final String key = view.glideUrl.toString();
            HexagonImageOkHttpProgressGlideModule.forget(key);
            List<HexagonImageViewWithUrl> viewsForKey = VIEWS_FOR_URLS.get(key);
            if (viewsForKey != null) {
                viewsForKey.remove(view);
                if (viewsForKey.size() == 0) VIEWS_FOR_URLS.remove(key);
            }
        }

        super.onDropViewInstance(view);
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put(REACT_ON_LOAD_START_EVENT, MapBuilder.of("registrationName", REACT_ON_LOAD_START_EVENT))
                .put(REACT_ON_PROGRESS_EVENT, MapBuilder.of("registrationName", REACT_ON_PROGRESS_EVENT))
                .put(REACT_ON_LOAD_EVENT, MapBuilder.of("registrationName", REACT_ON_LOAD_EVENT))
                .put(REACT_ON_ERROR_EVENT, MapBuilder.of("registrationName", REACT_ON_ERROR_EVENT))
                .put(REACT_ON_LOAD_END_EVENT, MapBuilder.of("registrationName", REACT_ON_LOAD_END_EVENT))
                .build();
    }

    @Override
    public void onProgress(String key, long bytesRead, long expectedLength) {
        List<HexagonImageViewWithUrl> viewsForKey = VIEWS_FOR_URLS.get(key);
        if (viewsForKey != null) {
            for (HexagonImageViewWithUrl view : viewsForKey) {
                WritableMap event = new WritableNativeMap();
                event.putInt("loaded", (int) bytesRead);
                event.putInt("total", (int) expectedLength);
                ThemedReactContext context = (ThemedReactContext) view.getContext();
                RCTEventEmitter eventEmitter = context.getJSModule(RCTEventEmitter.class);
                int viewId = view.getId();
                eventEmitter.receiveEvent(viewId, REACT_ON_PROGRESS_EVENT, event);
            }
        }
    }

    @Override
    public float getGranularityPercentage() {
        return 0.5f;
    }

    private static boolean isValidContextForGlide(final Context context) {
        Activity activity = getActivityFromContext(context);

        if (activity == null) {
            return false;
        }

        return !isActivityDestroyed(activity);
    }

    private static Activity getActivityFromContext(final Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }

        if (context instanceof ThemedReactContext) {
            final Context baseContext = ((ThemedReactContext) context).getBaseContext();
            if (baseContext instanceof Activity) {
                return (Activity) baseContext;
            }

            if (baseContext instanceof ContextWrapper) {
                final ContextWrapper contextWrapper = (ContextWrapper) baseContext;
                final Context wrapperBaseContext = contextWrapper.getBaseContext();
                if (wrapperBaseContext instanceof Activity) {
                    return (Activity) wrapperBaseContext;
                }
            }
        }

        return null;
    }

    private static boolean isActivityDestroyed(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return activity.isDestroyed() || activity.isFinishing();
        } else {
            return activity.isFinishing() || activity.isChangingConfigurations();
        }

    }

    @Override
    protected void onAfterUpdateTransaction(@NonNull HexagonImageViewWithUrl view) {
        super.onAfterUpdateTransaction(view);
        view.onAfterUpdate(this, requestManager, VIEWS_FOR_URLS);
    }

}
