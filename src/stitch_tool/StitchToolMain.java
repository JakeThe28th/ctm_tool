package stitch_tool;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import frost3d.Framebuffer;
import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleTexture;
import frost3d.implementations.SimpleWindow;

public class StitchToolMain {
	
	record ExportSet(int index, HashMap<String, BufferedImage> set) {}
	
	public static void main(String[] args) throws IOException {

		ArrayList<ExportSet> sets = new ArrayList<>();
		
		for (File directory : new File("stitch/input").listFiles()) {
			HashMap<String, BufferedImage> set_content = new HashMap<>();
			
			for (File img : directory.listFiles()) {
				set_content.put(img.getName(), ImageIO.read(img));
			}
			
			ExportSet set = new ExportSet(Integer.parseInt(directory.getName()), set_content);
			sets.add(set);
		}
		
		// export =)
		
		File export_dir = new File("stitch/export");
		export_dir.mkdirs();
		
		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(1,1,"");
		BuiltinShaders.init();
		
		// (assuming all files in the first folder are present in the rest)
		for (String key : sets.getFirst().set().keySet()) {
			
			// for simplicity, assuming the textures are all 16x16
			int size = 16;
			
			int framecount = sets.size();
			
			SimpleCanvas canvas = new SimpleCanvas();
						 canvas.adopt(new Framebuffer(size, size * framecount));
						 
			canvas.color(new Vector4f(1,1,1,1));
				
			int draw_y = 0;
			for (ExportSet set : sets) {
				canvas.rect(0, draw_y, size, draw_y + size, 0, new SimpleTexture(set.set().get(key)));
				draw_y += size;
			}
			
			canvas.draw_frame();
			
			save_image(canvas, export_dir.toString() + "/" + key);
						
			Files.writeString(Paths.get(export_dir.toString() + "/" + key + ".mcmeta"), 
					"""
						{
						"animation": {
						    "frametime": 5
						  }
						}
					""");
			
		}
		
	}
	
	// (copied from another project cuz im lazy lol)
	private static void save_image(SimpleCanvas canvas, String filename) {
		canvas.framebuffer().bind();
		int[] pixels = new int[canvas.width()*canvas.height()];
		GL11.glReadPixels(0, 0, canvas.width(), canvas.height(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
//		ByteBuffer write = ByteBuffer.wrap(new byte[pixels.length*4]);
//		write.order(ByteOrder.LITTLE_ENDIAN);
//		for (int id : pixels) write.putInt(id);
		BufferedImage img = new BufferedImage(canvas.width(), canvas.height(), BufferedImage.TYPE_INT_ARGB);
		for (int ind = 0; ind < pixels.length; ind++) {
			int pxl = 0xFF000000;
			    pxl |= (pixels[ind] & 0x00FF0000) >> 16;
			    pxl |= (pixels[ind] & 0x0000FF00);
			    pxl |= (pixels[ind] & 0x000000FF) << 16;

			img.setRGB(ind % canvas.width(), canvas.height() - ((ind / canvas.width()) + 1), pxl);
		}
		try {
			ImageIO.write(img, "png", new File(filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
