package com.ioigoume.eketamobilitytool.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

class GLCube {
	   private final IntBuffer mVertexBuffer;
	   
	   
	   private final IntBuffer mTextureBuffer;

	   
	   public GLCube() {
	      
	      int one = 65536;
	      int half = one / 2;
	      int vertices[] = { 
	            // FRONT
	            -half, -half, half, half, -half, half,
	            -half, half, half, half, half, half,
	            // BACK
	            -half, -half, -half, -half, half, -half,
	            half, -half, -half, half, half, -half,
	            // LEFT
	            -half, -half, half, -half, half, half,
	            -half, -half, -half, -half, half, -half,
	            // RIGHT
	            half, -half, -half, half, half, -half,
	            half, -half, half, half, half, half,
	            // TOP
	            -half, half, half, half, half, half,
	            -half, half, -half, half, half, -half,
	            // BOTTOM
	            -half, -half, half, -half, -half, -half,
	            half, -half, half, half, -half, -half, };

	      
	      
	      int texCoords[] = {
	            // FRONT
	            0, one, one, one, 0, 0, one, 0,
	            // BACK
	            one, one, one, 0, 0, one, 0, 0,
	            // LEFT
	            one, one, one, 0, 0, one, 0, 0,
	            // RIGHT
	            one, one, one, 0, 0, one, 0, 0,
	            // TOP
	            one, 0, 0, 0, one, one, 0, one,
	            // BOTTOM
	            0, 0, 0, one, one, 0, one, one, };
	      

	      
	      // Buffers to be passed to gl*Pointer() functions must be
	      // direct, i.e., they must be placed on the native heap
	      // where the garbage collector cannot move them.
	      //
	      // Buffers with multi-byte data types (e.g., short, int,
	      // float) must have their byte order set to native order
	      ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
	      vbb.order(ByteOrder.nativeOrder());
	      mVertexBuffer = vbb.asIntBuffer();
	      mVertexBuffer.put(vertices);
	      mVertexBuffer.position(0);
	      

	      
	      // ...
	      ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
	      tbb.order(ByteOrder.nativeOrder());
	      mTextureBuffer = tbb.asIntBuffer();
	      mTextureBuffer.put(texCoords);
	      mTextureBuffer.position(0);
	      
	   }
	   

	   public void draw(GL10 gl) { 
	      gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
	      
	      
	      gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, mTextureBuffer);
	      
	      gl.glColor4f(1, 1, 1, 1);
	      gl.glNormal3f(0, 0, 1);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	      gl.glNormal3f(0, 0, -1);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);

	      gl.glColor4f(1, 1, 1, 1);
	      gl.glNormal3f(-1, 0, 0);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4);
	      gl.glNormal3f(1, 0, 0);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4);

	      gl.glColor4f(1, 1, 1, 1);
	      gl.glNormal3f(0, 1, 0);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4);
	      gl.glNormal3f(0, -1, 0);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4);
	   }
	   
	   
	   static void loadTexture(GL10 gl, Context context, int resource) {
	      Bitmap bmp = BitmapFactory.decodeResource(
	            context.getResources(), resource);
	      GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
	      gl.glTexParameterx(GL10.GL_TEXTURE_2D,
	            GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	      gl.glTexParameterx(GL10.GL_TEXTURE_2D,
	            GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	      bmp.recycle();
	   }
	   
	   @SuppressWarnings("unused")
	private static ByteBuffer extract(Bitmap bmp){
		   ByteBuffer bb = ByteBuffer.allocateDirect(bmp.getHeight() * bmp.getWidth() * 4);
		   bb.order(ByteOrder.BIG_ENDIAN);
		   IntBuffer ib = bb.asIntBuffer();
		   
		   // Convert ARGB -> RGBA
		   for(int y = bmp.getHeight() - 1; y > -1; y--){
			   for(int x = 0; x < bmp.getWidth(); x++){
				   int pix = bmp.getPixel(x, bmp.getHeight() - y -1);
				   int red = ((pix >> 16))&0xFF;
				   int green = ((pix >> 8)&0xFF);
				   int blue = ((pix)&0xFF);
				   
				   // Make up alpha for intersesting effect
				   ib.put(red << 24 | green << 16 | blue << 8 | ((red + blue + green)/3));
			   }
		   }
		   bb.position(0);
		   return bb;
	   }
	   
	   @SuppressWarnings("unused")
	private static void Load(GL10 gl, ByteBuffer bb, int width, int height){
		   int[] tmp_tex = new int[1];
		   gl.glGenTextures(1, tmp_tex, 0);
		   int tex = tmp_tex[0];
		   
		   // Loat it up
		   gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
		   gl.glTexImage2D(	GL10.GL_TEXTURE_2D, 
				   			0, 
				   			GL10.GL_RGBA, 
				   			width, 
				   			height, 
				   			0, 
				   			GL10.GL_RGBA,
				   			GL10.GL_UNSIGNED_BYTE, 
				   			bb);
		   gl.glTexParameterx(	GL10.GL_TEXTURE_2D, 
				   				GL10.GL_TEXTURE_MIN_FILTER, 
				   				GL10.GL_LINEAR);
		   gl.glTexParameterx(	GL10.GL_TEXTURE_2D, 
	   							GL10.GL_TEXTURE_MAG_FILTER, 
	   							GL10.GL_LINEAR);
	   }
	   
	}
