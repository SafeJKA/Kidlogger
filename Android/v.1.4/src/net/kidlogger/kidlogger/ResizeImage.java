package net.kidlogger.kidlogger;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ResizeImage {
	
	private KLService service;
	
	public ResizeImage(KLService service){
		this.service = service;
	}
	
	protected String resizeAndSaveImage(String imageFile){
		Bitmap bm = shrinkBitmap(imageFile);
		if(bm == null){
			service.app.logError("ResizeImage.resiszeAndSaveImage", "couldn't get bitmap. File: " +
					imageFile);
			return null;
		}
			
		
		// Save resized image 
		OutputStream os;
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() +	"/.callrecords" + 
			imageFile.substring(imageFile.lastIndexOf("/"), imageFile.length());
		File file = new File(path);
		try{
			os = new FileOutputStream(file);
			bm.compress(CompressFormat.JPEG, 80, os);
			os.flush();
			os.close();
		}catch(IOException e){
			service.app.logError("ResizeImage.resizeAndSaveImage", e.toString());
			return null;
		}
		
		return path;
	}	
	
	private Bitmap shrinkBitmap(String file){
		BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();        
        bmpFactoryOptions.inSampleSize = 8;
        bmpFactoryOptions.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        
        return bitmap;
	}
}
