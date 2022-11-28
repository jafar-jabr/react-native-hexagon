package com.hexagon;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.util.AttributeSet;


public class EffectiveShapeView extends androidx.appcompat.widget.AppCompatImageView {
  private final RectF tempCornerArcBounds = new RectF();
  private int mSideCount=0;
  private int mWidth;
  private int mHeight;

  float mCenterX, mCenterY=0;
    /**
     * Shape type
     */


    /**
     * direction of decorations
     */
    public interface Direction {
        int LEFT_TOP = 1;
        int LEFT_BOTTOM = 2;
        int RIGHT_TOP = 3;
        int RIGHT_BOTTOM = 4;
    }

    private int mDirection = Direction.RIGHT_BOTTOM;
    private int mBorderColor = Color.BLUE;
    private int mPolygonSides = 6;// defaut hexagon if mShapeType = Shape.POLYGON;
    private int mBorderWidth;
    private int mPadding;
    private int mResource;

    private float mRx = 24f;
    private float mRy = 24f;

    private boolean mInvalidated = true;
    private boolean mReBuildShader = true;

    private Drawable mDecorationsView;

    private final Paint mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mShaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mMaskPath = new Path();

    public EffectiveShapeView(Context context) {
        this(context, null);
    }

    public EffectiveShapeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EffectiveShapeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EffectiveShapeView);
            mDirection = a.getInt(R.styleable.EffectiveShapeView_decorations_direction, mDirection);
            mPolygonSides = a.getInt(R.styleable.EffectiveShapeView_sides, mPolygonSides);
            mRx = a.getFloat(R.styleable.EffectiveShapeView_radius_x, mRx);
            mRy = a.getFloat(R.styleable.EffectiveShapeView_radius_y, mRy);
            mDecorationsView = a.getDrawable(R.styleable.EffectiveShapeView_decorations_src);
            a.recycle();
        }
        mShaderPaint.setFilterBitmap(false);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (ScaleType.FIT_XY == scaleType) {
            scaleType = ScaleType.CENTER_CROP;
        }
        super.setScaleType(scaleType);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInEditMode()) {
            if (mInvalidated) {
                mInvalidated = false;
                mWidth=getMeasuredWidth();
                mHeight= getMeasuredHeight();
            }

            if (mReBuildShader) {
                mReBuildShader = false;
                createShader();
            }

            if (null != mShaderPaint.getShader()) {

                canvas.drawPath(mMaskPath, mShaderPaint);

                if (mBorderWidth > 0) {
                    mMaskPaint.setStyle(Paint.Style.STROKE);
                    mMaskPaint.setColor(mBorderColor);
                    mMaskPaint.setStrokeWidth(mBorderWidth);
                    canvas.drawPath(mMaskPath, mMaskPaint);

                }

                if (mDecorationsView != null) {
                  Bitmap bitmap = rotateDrawable(((BitmapDrawable) mDecorationsView).getBitmap());

                  int width = mDecorationsView.getIntrinsicWidth();
                    int height = mDecorationsView.getIntrinsicHeight();
                    canvas.rotate(30);
                  switch (mDirection) {
                        case Direction.LEFT_TOP:
                            canvas.drawBitmap(bitmap, mPadding, mPadding, mShaderPaint);
                            break;
                        case Direction.LEFT_BOTTOM:
                            canvas.drawBitmap(bitmap, mPadding, getHeight() - height - mPadding,
                                    mShaderPaint);
                            break;
                        case Direction.RIGHT_TOP:
                            canvas.drawBitmap(bitmap, getWidth() - width - mPadding, mPadding,
                                    mShaderPaint);
                            break;
                        case Direction.RIGHT_BOTTOM:
                            canvas.drawBitmap(bitmap, getWidth() - width - mPadding,
                                    getHeight() - height - mPadding,mShaderPaint);
                            break;
                    }



                }

            }
        } else {
            super.onDraw(canvas);
        }
    }
  private Bitmap rotateDrawable(Bitmap bmpOriginal) {
    Bitmap bmpResult = Bitmap.createBitmap(bmpOriginal.getHeight(), bmpOriginal.getWidth(), Bitmap.Config.ARGB_8888);
    Canvas tempCanvas = new Canvas(bmpResult);
    int pivot = bmpOriginal.getHeight() / 2;
    tempCanvas.rotate(90, pivot, pivot);
    tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);
    return bmpResult;
  }
    private void createShader() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            Bitmap bitmap = drawableToBitmap(drawable);
            BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP);
            shader.setLocalMatrix(getImageMatrix());
            mShaderPaint.setShader(shader);
        }
    }

    private void createMask(int width, int height,int cornerRadius) {
        mMaskPath.reset();
        mMaskPaint.setStyle(Paint.Style.FILL);
        createPolygonPath(width, height,cornerRadius);
    }

    private void createPolygonPath(int width, int height,int cornerRadius) {

       int sides=6;
        sides = Math.abs(sides);
        float radius, centerX, centerY;
        radius = centerX = centerY = Math.min(width, height) / 2;
        radius -= mBorderWidth / 2;// avoid stroke out of bounds
      mSideCount=sides;
      mCenterX=centerX;
      mCenterY=centerY;

      final double halfInteriorCornerAngle = 90 - (180.0 / mSideCount);
      final float halfCornerArcSweepAngle = (float) (90 - halfInteriorCornerAngle);
      final double distanceToCornerArcCenter = radius - cornerRadius / sin(toRadians(halfInteriorCornerAngle));

      for (int cornerNumber = 0; cornerNumber < mSideCount; cornerNumber++) {
        final double angleToCorner = cornerNumber * (360.0 / mSideCount);
        final float cornerCenterX = (float) (mCenterX + distanceToCornerArcCenter * cos(toRadians(angleToCorner)));
        final float cornerCenterY = (float) (mCenterY + distanceToCornerArcCenter * sin(toRadians(angleToCorner)));

        tempCornerArcBounds.set(
          cornerCenterX - cornerRadius,
          cornerCenterY - cornerRadius,
          cornerCenterX + cornerRadius,
          cornerCenterY + cornerRadius);


        mMaskPath.arcTo(
          tempCornerArcBounds,
          (float) (angleToCorner - halfCornerArcSweepAngle),
          2 * halfCornerArcSweepAngle);
      }

      Matrix matrix = new Matrix();
      mMaskPath.computeBounds(tempCornerArcBounds, true);
      matrix.postRotate(30, tempCornerArcBounds.centerX(), tempCornerArcBounds.centerY());
      mMaskPath.transform(matrix);
      mMaskPath.close();

    }

    /**
     * @param direction {@link Direction}
     */
    public void setDecorations(int direction, int padding, Drawable drawable) {
        mDirection = direction;
        mPadding = padding;
        mDecorationsView = fromDrawable(drawable);
        invalidate();
    }

    public void setBorderColor(int color) {
        mBorderColor = color;
        invalidate();
    }

    public void setBorderWidth(int width) {
        mBorderWidth = width;
        invalidate();
    }

    /**
     * @param rx The x-radius of the rounded corners on the round-rectangle
     * @param ry The y-radius of the rounded corners on the round-rectangle
     */
    public void setDegreeForRoundRectangle(int rx, int ry) {
        mRx = rx;
        mRy = ry;
    }


    public void drawShape(int cornerRadius) {
      createMask(mWidth,mHeight,cornerRadius*3);
      invalidate();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        mResource = 0;
        super.setImageDrawable(fromDrawable(drawable));
    }

    @Override
    public void setImageResource(int resId) {
        if (mResource != resId) {
            mResource = resId;
            setImageDrawable(resolveResource());
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setImageDrawable(getDrawable());
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        setImageDrawable(new BitmapDrawable(getResources(), bm));
    }

    private Drawable resolveResource() {
        Resources rsrc = getResources();
        if (rsrc == null) {
            return null;
        }

        Drawable d = null;
        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource);
            } catch (Exception e) {
                mResource = 0;
            }
        }
        return fromDrawable(d);
    }

    private Drawable fromDrawable(Drawable drawable) {
        mReBuildShader = true;
        if (drawable != null) {
            if (drawable instanceof BitmapDrawable) {
                return drawable;
            } else if (drawable instanceof LayerDrawable) {
                LayerDrawable ld = (LayerDrawable) drawable;
                drawable = ld.getDrawable(0);
            } else if (drawable instanceof StateListDrawable) {
                StateListDrawable stateListDrawable = (StateListDrawable) drawable;
                drawable = stateListDrawable.getCurrent();
            }

            if (!(drawable instanceof BitmapDrawable)) {
                Bitmap bm = drawableToBitmap(drawable);
                if (bm != null) {
                    drawable = new BitmapDrawable(getResources(), bm);
                }
            }
        }
        return drawable;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap;
        try {
            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
            } else {
                int width = Math.max(drawable.getIntrinsicWidth(), 2);
                int height = Math.max(drawable.getIntrinsicHeight(), 2);
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
            bitmap = null;
        }
        return bitmap;
    }
}
