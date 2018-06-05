package com.codefarm.spring.modules.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class NioFileUtil {

	 /** 
     * NIO读取文件 
     *  
     * @param filename 
     * @return 文件字节数组
     * @throws IOException 
     */  
    public static byte[] readFile(String filename) throws IOException { 
        FileChannel fc =null;
        RandomAccessFile file=null;
        try {
        	file = new RandomAccessFile(filename, "r");
            fc = file.getChannel();  
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,  
                    fc.size()).load();  
            System.out.println(byteBuffer.isLoaded());  
            byte[] result = new byte[(int) fc.size()];  
            if (byteBuffer.remaining() > 0) {  
                byteBuffer.get(result, 0, byteBuffer.remaining());  
            }  
            return result;
        } catch (IOException e) {  
            e.printStackTrace();  
            throw e;  
        } finally {  
            try {
                fc.close();
                file.close();
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }
    
    public static void writePcm(String direct,String fileName,byte[] b) {
    	try {
    		File f=new File(direct);
    		if(!f.exists()) {
    			f.mkdirs();
    		}
    		RandomAccessFile file = new RandomAccessFile(direct+fileName,"rw");
    		FileChannel fc = file.getChannel();
    		writePcm(fc,b);
    		fc.close();
    		file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    
    
	public static void writePcm(FileChannel fc,byte[] b) throws Exception {
		System.out.println("开始写文件");
		//先将上次文件删除
//		new File(file).delete();
//		RandomAccessFile raf1 = new RandomAccessFile(file,"rw");
//		FileChannel fc = raf1.getChannel();
//		ByteBuffer raf = MappedByteBuffer.allocate(mapsize);
		MappedByteBuffer raf = fc.map(FileChannel.MapMode.READ_WRITE, 0,  
                b.length).load();  
		raf.clear();
		raf.put(b);
		fc.write(raf);
		raf.flip();
		//在windows7 32bit 下, allocateDirect反而比allocate慢
		//ByteBuffer raf = ByteBuffer.allocateDirect(mapsize);
//		byte[] b1 = new byte[]{'a','b','c','d','e','f','g','h'};
//		byte[] utfstr = "this is a test".getBytes("UTF-8");
//		for(int i=0;i<loop;i++){
//			raf.put(b);
//			raf.putInt(i);
//			raf.putInt(i+1);
////			raf.put(utfstr);
////			raf.put((byte)'\n');
//			if(raf.remaining()<140){
//				raf.flip();
//				fc.write(raf);
//				raf.compact();
//			}
//		}
		//因为close方法可能将缓冲中最后剩余的flush到文件, 所以要纳入计时
//		fc.close();
	}
    
//    public static byte[] readFileByBytes(String fileName) {  
//    	  InputStream in = null;  
//    	  ByteArrayOutputStream out = new ByteArrayOutputStream();  
//    	  try {  
//    	   in = new FileInputStream(fileName);  
//    	   byte[] buf = new byte[1024];  
//    	   int length = 0;  
//    	   while ((length = in.read(buf)) != -1) {  
//    	    out.write(buf, 0, length);  
//    	   }  
//    	  } catch (Exception e1) {  
//    	   e1.printStackTrace();  
//    	  } finally {  
//    	   if (in != null) {  
//    	    try {  
//    	     in.close();  
//    	    } catch (IOException e1) {  
//    	    }  
//    	   }  
//    	  }  
//    	  return out.toByteArray();  
//    	 }  
}
