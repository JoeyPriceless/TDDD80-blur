package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

import se.liu.ida.tddd80.blur.R;


// Source: https://gist.github.com/bkurzius/99c945bd1bdcf6af8f99
public class UserImageView extends NetworkImageView {
    private static final int defaultImageRes = R.mipmap.img_profile_default;
	Context mContext;
	private boolean blur;

	public void setBlur(boolean blur) {
		this.blur = blur;
	}

	public UserImageView(Context context) {
		super(context);
		mContext = context;
	}

	public UserImageView(Context context, Context mContext, boolean blur) {
		super(context);
		this.mContext = mContext;
		this.blur = blur;
	}

	public UserImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
	}

	public UserImageView(Context context, AttributeSet attrs,
                         int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

    // https://stackoverflow.com/a/31280690/4400799
	@Override
	public void setImageBitmap(Bitmap bm) {
		if(bm==null) return;
		if (blur)
    		bm = ViewUtil.blurBitmap(bm);
		setImageDrawable(new BitmapDrawable(mContext.getResources(), getCircularBitmap(bm)));
	}

    @Override
    public void setErrorImageResId(int errorImage) {
	    // Override error image with default profile picture.
        super.setErrorImageResId(defaultImageRes);
    }

	/**
	 * Creates a circular bitmap and uses whichever dimension is smaller to determine the width
	 * <br/>Also constrains the circle to the leftmost part of the image
	 *
	 * @param bitmap
	 * @return bitmap
	 */
	public Bitmap getCircularBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		int width = bitmap.getWidth();
		if(bitmap.getWidth()>bitmap.getHeight())
			width = bitmap.getHeight();
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, width, width);
		final RectF rectF = new RectF(rect);
		final float roundPx = width / 2;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

}