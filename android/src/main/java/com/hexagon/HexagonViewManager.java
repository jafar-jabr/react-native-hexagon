package com.hexagon;

import android.graphics.Color;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;

import java.net.URL;
public class HexagonViewManager extends SimpleViewManager<EffectiveShapeView> {
  public static final String REACT_CLASS = "HexagonView";
  private ImgStartListener imgStartListener;
  private  int cornerRadius=0;
  private String borderColor;
  private int borderWidth=0;
  private int width=0;
  private String src=null;
  private int height=0;
  private EffectiveShapeView reactImageView;
  private interface ImgStartListener {
    void startLoading();
  }

  @Override
  @NonNull
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  @NonNull
  public EffectiveShapeView createViewInstance(ThemedReactContext reactContext) {
    reactImageView = new EffectiveShapeView(reactContext);
    final Handler handler = new Handler();
    imgStartListener = new ImgStartListener() {
      @Override
      public void startLoading() {
        startDownloading( handler, reactImageView);

      }
    };

    imgStartListener.startLoading();
    return reactImageView;
  }
  private void startDownloading( final Handler handler, final EffectiveShapeView reactImageView) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          if(src!=null){
            URL url = new URL(src);
            final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            setImage(bmp, handler, reactImageView);
          }
        } catch (Exception e) {
          Log.e("ReactImageManager", "Error : " + e.getMessage());
        }
      }
    }).start();
  }
  private void setImage(final Bitmap bmp, Handler handler, final EffectiveShapeView reactImageView) {
    reactImageView.setImageBitmap(bmp);
    reactImageView.setBorderWidth(borderWidth);
    if(borderColor!=null){
      reactImageView.setBorderColor(Color.parseColor(borderColor));
    }
    reactImageView.drawShape(cornerRadius);
    if(reactImageView.getLayoutParams() != null) {
      android.view.ViewGroup.LayoutParams layoutParams = reactImageView.getLayoutParams();
      layoutParams.width = width;
      layoutParams.height = height;
      reactImageView.setLayoutParams(layoutParams);
    }
    reactImageView.invalidate();
  }

  @ReactProp(name = "src")
  public void setSrc(EffectiveShapeView view, String uri) {
    src=uri;
    imgStartListener.startLoading();
    view.invalidate();

  }

  @ReactProp(name = "cornerRadius")
  public void setCornerRadius(EffectiveShapeView view, int _cornerRadius) {
    cornerRadius=_cornerRadius;
    view.invalidate();

  }

  @ReactProp(name = "borderWidth")
  public void seBorderWidth(EffectiveShapeView view, int _borderWidth) {
    if(view !=null){
      view.setBorderWidth(borderWidth);
    }

    borderWidth= _borderWidth;
    view.invalidate();

  }

  @ReactProp(name = "borderColor")
  public void setBorderColor(EffectiveShapeView view, String _borderColor) {
    borderColor=_borderColor;
    view.invalidate();

  }

  @ReactProp(name = "width")
  public void setWidth(EffectiveShapeView view, int _width) {
    width=_width;
    view.invalidate();
  }

  @ReactProp(name = "height")
  public void setHeight(EffectiveShapeView view, int _height) {
    height=_height;
    view.invalidate();

    if(view.getLayoutParams() != null) {
      android.view.ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
      layoutParams.width = _height;
      layoutParams.height = _height;
      view.setLayoutParams(layoutParams);
    }
  }
}
