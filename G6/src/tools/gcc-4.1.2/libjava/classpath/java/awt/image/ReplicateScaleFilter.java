/* ReplicateScaleFilter.java -- Java class for filtering images
   Copyright (C) 1999 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.awt.image;

import java.util.Hashtable;

/**
 * This filter should be used for fast scaling of images where the result
 * does not need to ensure straight lines are still straight, etc.  The
 * exact method is not defined by Sun but some sort of fast Box filter should
 * probably be correct.
 * <br>
 * Currently this filter does nothing and needs to be implemented.
 *
 * @author C. Brian Jones (cbj@gnu.org) 
 */
public class ReplicateScaleFilter extends ImageFilter
{
    public ReplicateScaleFilter(int width, int height) {
	destHeight = height;
	destWidth = width;
    }

    /**
     * The height of the destination image.
     */
    protected int destHeight;

    /**
     * The width of the destination image.
     */
    protected int destWidth;

    /**
     * The height of the source image.
     */
    protected int srcHeight;

    /**
     * The width of the source image.
     */
    protected int srcWidth;

    /**
     *
     */
    protected int srcrows[];

    /**
     *
     */
    protected int srccols[];

    /**
     *
     */
    protected Object outpixbuf;

    /**
     * An <code>ImageProducer</code> indicates the size of the image
     * being produced using this method.  A filter can override this 
     * method to intercept these calls from the producer in order to
     * change either the width or the height before in turn calling
     * the consumer's <code>setDimensions</code> method.
     * 
     * @param width the width of the image
     * @param height the height of the image 
     */
    public void setDimensions(int width, int height)
    {
	srcWidth = width;
	srcHeight = height;

	/* If either destHeight or destWidth is < 0, the image should
	   maintain its original aspect ratio.  When both are < 0,
	   just maintain the original width and height. */
	if (destWidth < 0 && destHeight < 0)
        {
	    destWidth = width;
	    destHeight = height;
	}
	else if (destWidth < 0)
	{
	    destWidth = (int) (width * ((double) destHeight / srcHeight));
	}
	else if (destHeight < 0)
	{
	    destHeight = (int) (height * ((double) destWidth / srcWidth));
	}

	if (consumer != null)
	  consumer.setDimensions(destWidth, destHeight);
    }

    /**
     * An <code>ImageProducer</code> can set a list of properties
     * associated with this image by using this method.
     *
     * @param props the list of properties associated with this image 
     */
    public void setProperties(Hashtable props)
    {
	props.put("filters", "ReplicateScaleFilter");
	if (consumer != null)
	  consumer.setProperties(props);
    }

    /**
     * This function delivers a rectangle of pixels where any
     * pixel(m,n) is stored in the array as a <code>byte</code> at
     * index (n * scansize + m + offset).  
     *
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     * @param model the <code>ColorModel</code> used to translate the pixels
     * @param pixels the array of pixel values
     * @param offset the index of the first pixels in the <code>pixels</code> array
     * @param scansize the width to use in extracting pixels from the <code>pixels</code> array
     */
    public void setPixels(int x, int y, int w, int h, 
	   ColorModel model, byte[] pixels, int offset, int scansize)
    {
	double rx = ((double) srcWidth) / destWidth;
	double ry = ((double) srcHeight) / destHeight;

	int destScansize = (int) Math.round(scansize / rx);

	byte[] destPixels = replicatePixels(x, y, w, h,
                                           model, pixels, offset, scansize,
	                                   rx, ry, destScansize);

	if (consumer != null)
	  consumer.setPixels((int) Math.floor(x/rx), (int) Math.floor(y/ry),
			     (int) Math.ceil(w/rx), (int) Math.ceil(h/ry),
			     model, destPixels, 0, destScansize);
    }

    /**
     * This function delivers a rectangle of pixels where any
     * pixel(m,n) is stored in the array as an <code>int</code> at
     * index (n * scansize + m + offset).  
     *
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     * @param model the <code>ColorModel</code> used to translate the pixels
     * @param pixels the array of pixel values
     * @param offset the index of the first pixels in the <code>pixels</code> array
     * @param scansize the width to use in extracting pixels from the <code>pixels</code> array
     */
    public void setPixels(int x, int y, int w, int h, 
           ColorModel model, int[] pixels, int offset, int scansize)
    {
	double rx = ((double) srcWidth) / destWidth;
	double ry = ((double) srcHeight) / destHeight;

	int destScansize = (int) Math.round(scansize / rx);

	int[] destPixels = replicatePixels(x, y, w, h,
                                           model, pixels, offset, scansize,
	                                   rx, ry, destScansize);

	if (consumer != null)
	  consumer.setPixels((int) Math.floor(x/rx), (int) Math.floor(y/ry),
			     (int) Math.ceil(w/rx), (int) Math.ceil(h/ry),
			     model, destPixels, 0, destScansize);
    }

    private byte[] replicatePixels(int srcx, int srcy, int srcw, int srch,
                                   ColorModel model, byte[] srcPixels,
                                   int srcOffset, int srcScansize,
                                   double rx, double ry, int destScansize)
    {
	byte[] destPixels =
	  new byte[(int) Math.ceil(srcw/rx) * (int) Math.ceil(srch/ry)];

	int a, b;
	for (int i = 0; i < destPixels.length; i++)
	{
	    a = (int) ((int) ( ((double) i) / destScansize) * ry) * srcScansize;
	    b = (int) ((i % destScansize) * rx);
	    if ((a + b + srcOffset) < srcPixels.length)
		destPixels[i] = srcPixels[a + b + srcOffset];
	}

	return destPixels;
    }

    private int[] replicatePixels(int srcx, int srcy, int srcw, int srch,
                                  ColorModel model, int[] srcPixels,
                                  int srcOffset, int srcScansize,
                                  double rx, double ry, int destScansize)
    {
	int[] destPixels =
	  new int[(int) Math.ceil(srcw/rx) * (int) Math.ceil(srch/ry)];

	int a, b;
	for (int i = 0; i < destPixels.length; i++)
	{
	    a = (int) ((int) ( ((double) i) / destScansize) * ry) * srcScansize;
	    b = (int) ((i % destScansize) * rx);
	    if ((a + b + srcOffset) < srcPixels.length)
		destPixels[i] = srcPixels[a + b + srcOffset];
	}

	return destPixels;
    }
}

