package com.hexagon;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;


import androidx.annotation.NonNull;

import java.net.URL;
public class HexagonViewManager extends SimpleViewManager<EffectiveShapeView> {
  public static final String REACT_CLASS = "HexagonView";
  private ImgStartListener imgStartListener;
  private  int cornerRadius=0;
  private String borderColor;
  private int borderWidth=0;
  private interface ImgStartListener {
    void startLoading(String imgUrl);
  }

  @Override
  @NonNull
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  @NonNull
  public EffectiveShapeView createViewInstance(ThemedReactContext reactContext) {
    final EffectiveShapeView reactImageView = new EffectiveShapeView(reactContext);
    final Handler handler = new Handler();
    imgStartListener = new ImgStartListener() {
      @Override
      public void startLoading(final String imgUrl) {
        startDownloading(imgUrl, handler, reactImageView);

      }
    };

    return reactImageView;
  }
  private void startDownloading(final String imgUrl, final Handler handler, final EffectiveShapeView reactImageView) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          URL url = new URL(imgUrl);
          final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
          setImage(bmp, handler, reactImageView);
        } catch (Exception e) {
          Log.e("ReactImageManager", "Error : " + e.getMessage());
        }
      }
    }).start();
  }
  private void setImage(final Bitmap bmp, Handler handler, final EffectiveShapeView reactImageView) {
    handler.post(new Runnable() {
      @Override
      public void run() {

        reactImageView.setImageBitmap(bmp);
        reactImageView.setBorderWidth(borderWidth);
        reactImageView.setBorderColor(Color.parseColor(borderColor));
        reactImageView.drawShape(cornerRadius);
        if(reactImageView.getLayoutParams() != null) {
          reactImageView.getLayoutParams().height = 150;
          reactImageView.getLayoutParams().width = 150;
          reactImageView.setScaleType(ImageView.ScaleType.FIT_XY);
          reactImageView.requestLayout();
        }
      }
    });
  }


  public static Bitmap drawableToBitmap (Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable)drawable).getBitmap();
    }

    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
  }
  @ReactProp(name = "src")
  public void setSrc(EffectiveShapeView view, String uri) {
    imgStartListener.startLoading(uri);
  }


  @ReactProp(name = "cornerRadius")
  public void setCornerRadius(EffectiveShapeView view, int _cornerRadius) {
    cornerRadius=_cornerRadius;

  }

  @ReactProp(name = "borderWidth")
  public void seBorderWidth(EffectiveShapeView view, int _borderWidth) {
    if(view !=null){
      view.setBorderWidth(borderWidth);
    }

    borderWidth= _borderWidth;
  }
  @ReactProp(name = "borderColor")
  public void setBorderColor(EffectiveShapeView view, String _borderColor) {
    borderColor=_borderColor;
  }
}
