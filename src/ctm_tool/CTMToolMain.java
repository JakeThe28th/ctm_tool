package ctm_tool;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import disaethia.io.nbt.NBTCompound;
import disaethia.io.nbt.NBTList;
import disaethia.io.nbt.NBTNamedTag;
import frost3d.Framebuffer;
import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.enums.IconType;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleTexture;
import frost3d.implementations.SimpleWindow;
import frost3d.utility.Log;
import frost3d.utility.Rectangle;
import snowui.GUIInstance;
import snowui.coss.ComposingStyleSheet;
import snowui.coss.enums.Color;
import snowui.coss.enums.PredicateKey;
import snowui.elements.abstracts.GUIElement;
import snowui.elements.base.GUIIcon;
import snowui.elements.base.GUIList;
import snowui.elements.base.GUIScrollable;
import snowui.elements.base.GUIText;
import snowui.elements.base.GUITextBox;
import snowui.elements.detail.GUIRemainderSplit;
import snowui.elements.docking.GUISplit;
import snowui.elements.frost3d.GUIF3DCanvas;

public class CTMToolMain {
	
	static boolean saved = true;
	
	// kinda jank but i'm too lazy to do a proper solution for this rn
	private static class GUIHitboxExtender extends GUIElement {
		{ identifier("hitbox_extender"); }
		GUIElement root;
		public GUIHitboxExtender(GUIElement root) {
			this.root = root;
			this.root.set(PredicateKey.DISABLED, true);
			this.registerSubElement(root);
		}
		@Override
		public void recalculateSize(GUIInstance gui) {
			this.unpadded_height = root.height();
			this.unpadded_width = root.width();
		}
		@Override
		public void updateDrawInfo(GUIInstance gui) {
			Rectangle b = this.aligned_limit_rectangle();
			this.hover_rectangle(b);
			root.limit_rectangle(b);
		}
	}
	
	private static void HELPERSetUniformMargins(ComposingStyleSheet sheet, String type, String margin) {
		sheet.setProperty(type, "left_margin", 		margin);
		sheet.setProperty(type, "right_margin", 	margin);
		sheet.setProperty(type, "top_margin", 		margin);
		sheet.setProperty(type, "bottom_margin",	margin);
	}
		
	public static GUIList 	layouts_gui	= new GUIList() {
		{ identifier("layout_list"); }
	};
	public static GUISplit 	main_split 	= new GUISplit();
	
	public static GUIRemainderSplit layouts_panel;
	
	static {
		
		GUIText new_layout_button = new GUIText("New Layout") {
			{ identifier("new_layout_button"); }
			@Override
			public void onSingleClick() {
				addLayout("layout-" + (int) (Math.random()*100000));
			}
		};
		
		layouts_panel = new GUIRemainderSplit(new_layout_button, layouts_gui);
	}

	public static void main(String[] args) {
		
		Log.print_log = false;
		
		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(1024, 720, CTMToolMain.class.getCanonicalName());
		BuiltinShaders.init();
		
		GUIInstance gui = new GUIInstance(window, window.input());
					gui.root(main_split);
					gui.style().setProperty("layout_list", "background_color", "BLACK");
					gui.style().addContains("layout_list", "snowui-contained");
					gui.style().setProperty("trash_icon", "size", "12");
					gui.style().addContains("trash_icon", "icon");
					gui.style().addContains("trash_icon", "snowui-centered");
					gui.style().addContains("layout_label", "snowui-v_centered");
					gui.style().addContains("layout_label", "text");
					gui.style().addContains("layout_label", "snowui-w_contained");
					gui.style().setProperty("layout_label", "background_margin", "2");		
					gui.style().setProperty("trash_icon", "base_color", "#707070");					
					gui.style().setProperty("new_layout_button", "background_color", "#ff638a");
					gui.style().addContains("new_layout_button", "snowui-w_contained");
					gui.style().setPredicate("new_layout_button", "HOVERED=TRUE", "new_layout_hover");					
					gui.style().setProperty("new_layout_hover", "background_color", "#ff9cb6");					
					gui.style().setPredicate("new_layout_button", "DOWN=TRUE", "new_layout_down");					
					gui.style().setProperty("new_layout_down", "background_color", "#6e0c26");	
					gui.style().addContains("trash_icon_holder", "snowui-centered");
					gui.style().addContains("trash_icon_holder", "layout_label");

					gui.style().setProperty("snowui-hover", "background_color", "#2F2F2F");	
					gui.style().setProperty("f3d_canvas", "background_color", "#40806a");	
					gui.style().setProperty("f3d_canvas_lower", "background_color", "#478040");	
					gui.style().addContains("f3d_canvas_lower", "f3d_canvas");

					gui.style().setProperty("export_list_item", "background_color", "#326956");					
					gui.style().addContains("export_list_item", "snowui-w_contained");
					gui.style().addContains("export_list", "snowui-w_contained");
					gui.style().addContains("export_list_label", "snowui-v_centered");
					gui.style().setPredicate("export_list_item", "HOVERED=TRUE", "export_list_item_hover");			
					gui.style().setPredicate("export_list_item", "HOVERED=TRUE,DOWN=TRUE", "export_list_item_down");					
					gui.style().setProperty("export_list_item_hover", "background_color", "#47967c");					
					gui.style().setProperty("export_list_item_down", "background_color", "#093d35");					

					gui.style().setProperty("big_trash_icon", "size", "18");
					gui.style().setProperty("big_trash_icon",  "base_color", "#707070");
					gui.style().addContains("edits_list", "list");
					gui.style().addContains("big_trash_icon", "snowui-hoverable");
					
					gui.style().addContains("export_list_label", "textbox");
					gui.style().setProperty("textbox",  "min_height", "flex");
					gui.style().setProperty("textbox",  "max_height", "flex");

					gui.style().addContains("meta_textbox-text", "textbox-text");
					gui.style().setProperty("meta_textbox-text",  "size", "14");
					gui.style().setProperty("meta_text",  "size", "14");


					gui.fps.show_fps = false;

					HELPERSetUniformMargins(gui.style(), "edits_list", "2");	
					HELPERSetUniformMargins(gui.style(), "big_trash_icon", "8");	
					HELPERSetUniformMargins(gui.style(), "trash_icon", "4");	
					HELPERSetUniformMargins(gui.style(), "hitbox_extender", "0");	
					HELPERSetUniformMargins(gui.style(), "new_layout_button", "8");	
					
					HELPERSetUniformMargins(gui.style(), "meta_textbox", "0");	
					HELPERSetUniformMargins(gui.style(), "meta_textbox-text", "0");	
					HELPERSetUniformMargins(gui.style(), "meta_text", "0");	
					HELPERSetUniformMargins(gui.style(), "meta_split", "0");	
					
					gui.style().setProperty("meta_split",  "left_margin", "12");

		ArrayList<Layout> loaded_layouts = loadLayouts();
	
		for (Layout layout : loaded_layouts) {
			addLayout(layout);
		}
		
		if (loaded_layouts.isEmpty()) {
			addLayout("Default Layout");
		}

		main_split.first(layouts_panel);
		main_split.second(new GUIText("No layout selected") {
			{ identifier("snowui-centered"); }
		});
		
		main_split.split(0.15f);
		
		String title = "*";
		saved = true;
		
		long last_save_time = 0;
				
		while (!window.should_close()) {
			if (!saved && !title.endsWith("*")) {
				title = title + "*";
				window.title(title);
			} else if (saved && title.endsWith("*")) {
				title = CTMToolMain.class.getCanonicalName();
				window.title(title);
			}
			if (last_save_time + 5000 < System.currentTimeMillis()) {
				last_save_time = System.currentTimeMillis();
				saveAllLayouts("layouts");
				exportAllLayouts("layouts");
			}
			gui.size(window.width, window.height);
			gui.render();
			window.tick();
		}
		
		saveAllLayouts("layouts");
		exportAllLayouts("layouts");
	
		saveAllLayouts("layouts_backup");
		exportAllLayouts("layouts_backup");
	}
	
	// --------------------- //
	
	static GUIText current_layout_label;
	
	static GUILayoutEditor current_layout_gui = null;
	static GUIExportListItem current_export_gui = null;

	public static void addLayout(String name) {
		addLayout(new Layout(name));
	}
	
	public static void addLayout(Layout layout) {
		saved = false;
		
		int index = layouts_gui.length();
		
		GUIElement icon = new GUIHitboxExtender(new GUIIcon(IconType.GENERIC_TRASH) {
			{ identifier("trash_icon"); }
		}) {
			{ identifier("trash_icon_holder"); }
			@Override
			public void onSingleClick() {
				removeLayout(index);
			}
		};
		
		GUIText label = new GUIText(layout.layout_name) {
			{ identifier("layout_label"); }
			@Override
			public void onSingleClick() {
				current_layout_label = this;
				selectLayout(index);
			}
		};
		
		layouts_gui.add(new GUIRemainderSplit(icon, label).horizontalify());
		
		addLayoutData(layout);
	}
	
	public static void removeLayout(int index) {
		saved = false;
		layouts_gui.remove(layouts_gui.get(index));
		removeLayoutData(index);
	}
		
	public static void selectLayout(int index) {
		if (current_layout_index == index) return;
		current_export_gui = null;
		current_layout_index = index;
		current_layout_gui = new GUILayoutEditor(layouts_data.get(index));
		
		main_split.second(current_layout_gui);
	}
	
	public static void selectExport(GUIExportListItem export) {
		if (current_export_gui == export) return;
		current_export_gui = export;
		Framebuffer frame = new Framebuffer(export.export.width, export.export.height);
		Log.send(frame.width(), frame.height());
		current_layout_gui.current_edit_canvas.adopt(frame);
		current_layout_gui.edit.force_update_canvas_rectangle();
		
		current_layout_gui.edit_list.clear();
		for (Edit edit : export.export.edits) {
			current_layout_gui.addEditTolist(edit);
		}
		
		current_layout_gui.current_edit_canvas.draw_frame();
	}
	
	public static void addExport(String file_name) {
		saved = false;
		current_layout_gui.addExportsGUI(addExportData(file_name));
	}
	
	public static void addEdit(Edit edit) {
		saved = false;
		current_export_gui.export.edits.add(edit);
		current_export_gui.update_preview();
		current_layout_gui.addEditTolist(edit);
	}
	
	public static void removeEdit(int index) {
		saved = false;
		current_export_gui.export.edits.remove(index);
		current_export_gui.update_preview();
		current_layout_gui.removeEditFromList(index);
	}
	
	// --------------------- //
	
	public static void addLayoutData(Layout layout) {
		layouts_data.add(layout);
	}
	
	public static void removeLayoutData(int index) {
		layouts_data.remove(index);
	}
	
	public static ExportedTexture addExportData(String name) {
		ExportedTexture export = new ExportedTexture();
		export.filename = name;
		layouts_data.get(current_layout_index).exports.add(export);
		return export;
	}
	
	public Layout getCurrentLayoutData() {
		return layouts_data.get(current_layout_index);
	}
	
	static int current_layout_index = -1;
	
	static ArrayList<Layout> layouts_data = new ArrayList<>();
	
	public static void exportAllLayouts(String layout_dir) {
		for (Layout layout : layouts_data) {
			try {
				BufferedImage input = ImageIO.read(new File(layout.input_name));
				for (ExportedTexture export : layout.exports) {
					BufferedImage merge = export.merge(input);
					File output_dir = new File(layout_dir + "/" + layout.layout_name + "_export/");
						 output_dir.mkdirs();
					File output_path = new File(output_dir.getPath() + "/" + export.filename);
					ImageIO.write(merge, "png", output_path);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void saveAllLayouts(String layout_dir) {
		for (Layout layout : layouts_data) {
			NBTCompound file = new NBTCompound();
			
			file.put("name", layout.layout_name);
			file.put("input_filename", layout.input_name);

			NBTList ser_exports = new NBTList(file.TYPE);
			for (ExportedTexture export : layout.exports) {
				NBTCompound ser_export = new NBTCompound();
				
				ser_export.put("width", export.width);
				ser_export.put("height", export.height);
				ser_export.put("filename", export.filename);
				
				NBTList ser_edits = new NBTList(file.TYPE);
				for (Edit edit : export.edits) {
					NBTCompound ser_edit = new NBTCompound();
					ser_edit.put("source_left", edit.source_position().left());
					ser_edit.put("source_right", edit.source_position().right());
					ser_edit.put("source_top", edit.source_position().top());
					ser_edit.put("source_bottom", edit.source_position().bottom());
					ser_edit.put("dest_x", edit.dest_x);
					ser_edit.put("dest_y", edit.dest_y);
					ser_edits.add(ser_edit);
				}

				ser_export.put("edits", ser_edits);
				ser_exports.add(ser_export);
			}
			
			file.put("exports", ser_exports);
			
			String layout_name = layout.layout_name;
			
			try {
				String filename = layout_dir + "/"+layout_name+".nbt";
				Files.createDirectories(Paths.get(filename).getParent());
				new NBTNamedTag("", file).save(filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		saved = true;
	}
	
	public static ArrayList<Layout> loadLayouts() {
		ArrayList<Layout> layouts = new ArrayList<Layout>();
		
		File layouts_dir = new File("layouts");
		if (!layouts_dir.exists()) return layouts;
		
		for (File layout : layouts_dir.listFiles()) {
			if (layout.isDirectory()) continue;
			try {
				NBTNamedTag n_ser_layout = NBTNamedTag.read(layout.getPath());
				NBTCompound ser_layout = n_ser_layout.getCompound();
				Layout des_layout = new Layout(ser_layout.get("name").getString().get());
					   des_layout.input_name = ser_layout.get("input_filename").getString().get();
					   
				NBTList ser_exports = ser_layout.get("exports").getList();
				for (int i = 0; i < ser_exports.length.get(); i++) {
					NBTCompound ser_export = ser_exports.getCompound(i);
					
					ExportedTexture des_export = new ExportedTexture();
									des_export.filename 	= ser_export.get("filename")	.getString()	.get();
									des_export.width 		= ser_export.get("width")		.getInt()		.get();
									des_export.height 		= ser_export.get("height")		.getInt()		.get();
									
					NBTList ser_edits = ser_export.get("edits").getList();
					for (int j = 0; j < ser_edits.length.get(); j++) {
						NBTCompound ser_edit = ser_edits.getCompound(j);
						Rectangle source_pos = new Rectangle(
												ser_edit.get("source_left").getInt().get(),
												ser_edit.get("source_top").getInt().get(),
												ser_edit.get("source_right").getInt().get(),
												ser_edit.get("source_bottom").getInt().get()
												);
						des_export.edits.add(new Edit(
								source_pos, 
								ser_edit.get("dest_x").getInt().get(), 
								ser_edit.get("dest_y").getInt().get())
							);
					}
					
					des_layout.exports.add(des_export);
				}
				
				layouts.add(des_layout);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return layouts;
	}

	/** A section of the original texture, pasted onto the new one.*/
	public static record Edit(Rectangle source_position, int dest_x, int dest_y) {}
	
	/** A texture which will be exported after being generated from the array of Edits. */
	public static class ExportedTexture {
		String filename = "Unnamed_Export.png";
		ArrayList<Edit> edits = new ArrayList<>();
		int width = 16;
		int height = 16;
		public BufferedImage merge(BufferedImage source) {
			BufferedImage merged = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics g = merged.getGraphics();

			for (Edit edit : edits) {
				Rectangle o = edit.source_position();
				g.drawImage(source,
					edit.dest_x, edit.dest_y, edit.dest_x + o.width(), edit.dest_y  + o.height(), 
					o.left(), o.top(), o.right(), o.bottom(),
					null
				);
			}
			return merged;
		}
	}
	
	public static class Layout {
		public Layout(String name) { this.layout_name = name; }
		Rectangle selection;
		SimpleTexture temp_selection_texture;
		String layout_name = "Unnamed Layout";
		String input_name = "input.png";
		int snap_subdivision = -1;
		ArrayList<ExportedTexture> exports = new ArrayList<>();
	}
	
	// --------------------- //
	
	public static class GUIExportListItem extends GUIElement {
		
		{ identifier("export_list_item"); }
		
		ExportedTexture export;
		
		GUITextBox label;
		SimpleTexture preview;
		
		GUIList meta = new GUIList().horizontalify();
		
		public GUIExportListItem(ExportedTexture export) {
			this.export = export;
			label = new GUITextBox(export.filename) {
				@Override
				public void onFinishEditing(String old_text, String new_text) {
					export.filename = new_text;
				}
			};
			//label.set(PredicateKey.DISABLED, true);
			label.identifier("export_list_label");
			label.finish_on_enter(true);
			this.registerSubElement(label);
			this.update_preview();
			
			GUITextBox wtext = new GUITextBox(export.width + "") {
				@Override
				public void onFinishEditing(String old_text, String new_text) {
					try {
						export.width = Integer.parseInt(new_text);
						// refresh canvas size by reselecting
						GUIExportListItem last_exp = current_export_gui;
						current_export_gui = null;
						selectExport(last_exp);
					} catch (Exception e) {
						this.set_text(old_text);
						e.printStackTrace();
					}
				}
			};
			GUITextBox htext = new GUITextBox(export.height + "")  {
				@Override
				public void onFinishEditing(String old_text, String new_text) {
					try {
						export.height = Integer.parseInt(new_text);
						// refresh canvas size by reselecting
						GUIExportListItem last_exp = current_export_gui;
						current_export_gui = null;
						selectExport(last_exp);
					} catch (Exception e) {
						this.set_text(old_text);
						e.printStackTrace();
					}
				}
			};
			
			wtext.identifier("meta_textbox");
			htext.identifier("meta_textbox");
			wtext.text().identifier("meta_textbox-text");
			htext.text().identifier("meta_textbox-text");
			
			wtext.finish_on_enter(true);
			htext.finish_on_enter(true);
			
			GUIText wlabel = new GUIText("width: ");
			GUIText hlabel = new GUIText(" height: ");
			wlabel.identifier("meta_text");
			hlabel.identifier("meta_text");
			
			meta.identifier("meta_split");

			meta.add(new GUIRemainderSplit(wlabel, wtext).horizontalify());
			meta.add(new GUIRemainderSplit(hlabel, htext).horizontalify());
			this.registerSubElement(meta);
		}

		int texture_pad = 4;
		int tex_size = 64;
		
		@Override
		public void recalculateSize(GUIInstance gui) {
			this.unpadded_height = tex_size + (texture_pad*2);
			this.unpadded_width = tex_size + (texture_pad*2) + label.width();
		}

		@Override
		public void updateDrawInfo(GUIInstance gui) {
			Rectangle b = aligned_limit_rectangle();
			hover_rectangle(b);
			int preview_end = b.left() + 64 + (texture_pad*2);
			Rectangle label_limit = new Rectangle(preview_end, b.top(), b.right(), b.top()+label.height());
			label.wrap_width(label_limit.width(), gui);
			label.limit_rectangle(label_limit);
			
			int label_end = b.top()+label.height();
			int meta_height = gui.style().getProperty("meta_text", "size", null).pixels();
			meta.limit_rectangle(new Rectangle(preview_end, label_end, b.right(), label_end+meta_height));
		}
		
		@Override
		public void draw(GUIInstance gui, int depth) {
			if (preview == null) update_preview();
			Rectangle b = aligned_limit_rectangle();
			Rectangle preview_area = new Rectangle(
					b.left() + texture_pad, 
					b.top() + texture_pad, 
					b.left() + texture_pad + tex_size, 
					b.top() + texture_pad + tex_size);
			
			gui.canvas().color(Color.BLACK25.val());
			gui.canvas().rect(preview_area, 0);
			if (preview != null) {
				gui.canvas().color(Color.WHITE.val());
				gui.canvas().rect(preview_area, 0, preview);
			}
		}

		public void update_preview() {
			if (preview != null) preview.free();
			if (current_layout_gui != null) preview = new SimpleTexture(export.merge(current_layout_gui.input_image), true);
		}
		
	}

	public static class GUILayoutEditor extends GUIElement {
		
		Layout layout;
		
		SimpleCanvas current_input_canvas	= new SimpleCanvas();
		SimpleCanvas current_edit_canvas	= new SimpleCanvas();
		
		Rectangle current_input_hover_preview = null;
		
		GUIF3DCanvas	input	= new GUIF3DCanvas(current_input_canvas) {
			@Override
			public void onHover(GUIInstance gui) {
				boolean selection_changed = false;
				int mx = input.internal_mouse(gui).x;
				int my = input.internal_mouse(gui).y;
				if (layout.snap_subdivision <= 0) {
					// FREE FORM SELECTION
					if (gui.primary_click_pressed()) {
						if (layout.selection == null) {
							layout.selection = new Rectangle(mx, my, mx, my);
						} else {
							layout.selection = new Rectangle(mx, my, layout.selection.right(), layout.selection.bottom());
						}
					}
					if (gui.primary_click_down()) {
						if (layout.selection == null) {
							layout.selection = new Rectangle(mx, my, mx+1, my+1);
						} else {
							layout.selection = new Rectangle(layout.selection.left(), layout.selection.top(), mx+1, my+1);
						}
					}
					if (gui.primary_click_released()) {
						if (layout.selection.width() == 0 || layout.selection.height() == 0) {
							layout.selection = null;
							if (layout.temp_selection_texture != null) layout.temp_selection_texture.free();
							layout.temp_selection_texture = null;
						}
						selection_changed = true;
					}
					if (layout.selection != null) {
						Rectangle o = layout.selection;
						if (o.left() > o.right())  layout.selection = new Rectangle(o.right(), 	o.top(), 	o.left(), 	o.bottom());
						if (o.top()  > o.bottom()) layout.selection = new Rectangle(o.left(), 	o.bottom(), o.right(), 	o.top());
					}
				} else {
					// not free form selection...
					mx = (mx / layout.snap_subdivision) * layout.snap_subdivision;
					my = (my / layout.snap_subdivision) * layout.snap_subdivision;
					current_input_hover_preview = new Rectangle(mx, my, mx + layout.snap_subdivision, my + layout.snap_subdivision);
					if (gui.primary_click_down()) {
						layout.selection = current_input_hover_preview;
						selection_changed = true;
					}
				}
				
				if (selection_changed) {
					if (layout.selection != null) {
						Rectangle o = layout.selection;
						if (layout.temp_selection_texture != null) layout.temp_selection_texture.free();
						BufferedImage temp_selection_image = new BufferedImage(o.width(), o.height(), BufferedImage.TYPE_INT_ARGB);
						Graphics g = temp_selection_image.getGraphics();
						g.drawImage(input_image,
										0, 0, temp_selection_image.getWidth(), temp_selection_image.getHeight(), 
										o.left(), o.top(), o.right(), o.bottom(),
										null
									);
						layout.temp_selection_texture = new SimpleTexture(temp_selection_image);
					}
				}
			}
				
		};
		
		int hovered_edit_index = -1;
		Vector4f edit_hover_preview_color = new Vector4f(1, 0, 1, 0.5f);
		
		boolean clicked_edit_canvas = false;
		
		GUIF3DCanvas	edit	= new GUIF3DCanvas(current_edit_canvas) {
			{ identifier("f3d_canvas_lower"); }
			@Override
			public void onHover(GUIInstance gui) {
				if (current_export_gui != null) {
					// also scuffed janky cursed etc but i didn't
					// think very far ahead when making this and
					// i don't feel like restructuring anything lol
					current_edit_canvas.color(Color.WHITE.val());
					current_edit_canvas.rect(current_edit_canvas.size(), 0, current_export_gui.preview);
					
					if (hovered_edit_index >= 0 && hovered_edit_index < current_export_gui.export.edits.size()) {
						Edit edit = current_export_gui.export.edits.get(hovered_edit_index);
						current_edit_canvas.color(edit_hover_preview_color);
						current_edit_canvas.rect(
								edit.dest_x, 
								edit.dest_y, 
								edit.dest_x + edit.source_position.width(), 
								edit.dest_y + edit.source_position.height(),
								0);
						hovered_edit_index = -1;
					}
					
					if (gui.primary_click_pressed()) {
						clicked_edit_canvas = true;
					}
					
					int mx = edit.internal_mouse(gui).x;
					int my = edit.internal_mouse(gui).y;
					
					if (layout.snap_subdivision > 0) {
						mx = (mx / layout.snap_subdivision) * layout.snap_subdivision;
						my = (my / layout.snap_subdivision) * layout.snap_subdivision;
					}
					
					boolean true_hover = mx >= 0 && mx < current_edit_canvas.width() && my >= 0 && my < current_edit_canvas.height();
					
					if (layout.selection != null && true_hover) {
						Rectangle o = layout.selection;
						current_edit_canvas.color(Color.BLACK25.val());
						current_edit_canvas.rect(mx-1, my-1, mx+o.width()+1, my+o.height()+1, 0);
						current_edit_canvas.color(Color.WHITE.val());
						current_edit_canvas.rect(mx, my, mx + o.width(), my + o.height(), 0, layout.temp_selection_texture);
												
						if (gui.primary_click_released() && clicked_edit_canvas) {
							//layout.temp_selection_texture = null;
							Edit new_edit = new Edit(o, mx, my);
							addEdit(new_edit);
						}
					}
					
					current_edit_canvas.draw_frame();

				}
			}
		};
		
		@Override
		public void tickAnimation(GUIInstance gui) {
			if (gui.rawinput().keyPressed(GLFW.GLFW_KEY_ESCAPE)) {
				layout.selection = null;
			}
			if (gui.primary_click_released() && (edit.hover_rectangle() == null || !edit.hover_rectangle().contains(gui.mousepos()))) {
				clicked_edit_canvas = false;
			}
		}
		
		GUIList 		edit_list = new GUIList();
		GUIScrollable 	edit_scrollable = new GUIScrollable(edit_list);
		{ edit_list.identifier("edits_list"); }
		
		GUIRemainderSplit		edit_combined = new GUIRemainderSplit(edit_scrollable, edit).horizontalify();
		
		GUIList 		input_properties = new GUIList().horizontalify();
		
		GUIRemainderSplit input_split = new GUIRemainderSplit(input_properties, input);

		GUISplit		editor 	= new GUISplit(input_split, edit_combined).verticalify();
		GUIList 		exports = new GUIList();
		GUIScrollable   exports_scrollable = new GUIScrollable(exports);

		{
			exports.identifier("export_list");
		}
		
		GUIRemainderSplit exports_panel;
		
		{
			GUIText new_export_button = new GUIText("New export") {
				{ identifier("new_layout_button"); }
				@Override
				public void onSingleClick() {
					addExport("export-" + (int) (Math.random()*100000) + ".png");
				}
			};
			
			exports_panel = new GUIRemainderSplit(new_export_button, exports_scrollable);
		}
		
		GUISplit 		editor_edits_split = new GUISplit(editor, exports_panel);
		
		{
			this.registerSubElement(editor_edits_split);
		}

		public GUILayoutEditor(Layout layout) {
			this.layout = layout;
			
			current_input_canvas .adopt(new Framebuffer(16, 16, false));
			current_edit_canvas  .adopt(new Framebuffer(200, 30, false));
			
			for (ExportedTexture export : layout.exports) {
				Log.send(export.filename);
				addExportsGUI(export);
//				for (Edit edit : export.edits) {
//					addEditTolist(edit);
//				}
			}
			
			setInputFile(layout.input_name);
			
			GUITextBox name_textbox = new GUITextBox(layout.layout_name) {
				@Override
				public void onFinishEditing(String old_text, String new_text) {
					layout.layout_name = new_text;
					if (current_layout_label != null) {
						current_layout_label.text(new_text);
					}					
				}
			};
			name_textbox.finish_on_enter(true);
			
			GUITextBox input_name_textbox = new GUITextBox(layout.input_name) {
				@Override
				public void onFinishEditing(String old_text, String new_text) {
					layout.input_name = new_text;
					setInputFile(new_text);
				}
			};
			input_name_textbox.finish_on_enter(true);
			
			GUITextBox subdivision_setting_textbox = new GUITextBox(layout.snap_subdivision + "") {
				@Override
				public void onFinishEditing(String old_text, String new_text) {
					try {
						int setting = Integer.parseInt(new_text);
						layout.snap_subdivision = setting;
					} catch (Exception e) {
						e.printStackTrace();
						set_text(old_text);
					}
				}
			};
			subdivision_setting_textbox.finish_on_enter(true);
			
			input_properties.add(name_textbox);
			input_properties.add(input_name_textbox);
			input_properties.add(subdivision_setting_textbox);

		}
		
		private void setInputFile(String input_name) {
			try {
				BufferedImage current_input = ImageIO.read(new File(layout.input_name));
				setInputTexture(current_input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void addExportsGUI(ExportedTexture data) {
				GUIExportListItem export_gui = new GUIExportListItem(data) {
					@Override
					public void onSingleClick(GUIInstance gui) {
						selectExport(this);
						edit.onHover(gui);
					}
				};
				this.exports.add(export_gui);
		}

		public void addEditTolist(Edit edit) {
			
			GUIIcon delete_icon = new GUIIcon(IconType.GENERIC_TRASH) {
				{ identifier("big_trash_icon"); }
				@Override
				public void onHover(GUIInstance gui) {
					hovered_edit_index = edit_list.indexOf(this);
					GUILayoutEditor.this.edit.onHover(gui);
				}
				@Override
				public void onSingleClick(GUIInstance gui) {
					hovered_edit_index = edit_list.indexOf(this);
					removeEdit(hovered_edit_index);
					GUILayoutEditor.this.edit.onHover(gui);
				}
			};
			
			edit_list.add(delete_icon);
		}
		
		public void removeEditFromList(int index) {
			edit_list.remove(edit_list.get(index));
			hovered_edit_index = -1;
		}
		
		SimpleTexture input_texture;
		BufferedImage input_image;
		
		private void setInputTexture(BufferedImage image) {
			current_input_canvas.adopt(new Framebuffer(image.getWidth(), image.getHeight(), false));
			input_texture = new SimpleTexture(image);
			input_image = image;
		}

		@Override public void recalculateSize(GUIInstance gui) { /* N/A */}

		@Override
		public void updateDrawInfo(GUIInstance gui) {
			Rectangle b = this.limit_rectangle();
			this.hover_rectangle(b);
			editor_edits_split.limit_rectangle(b);
		}
		
		@Override
		public void draw(GUIInstance gui, int depth) {
			// Draw input texture
			current_input_canvas.color(Color.WHITE.val());
			current_input_canvas.rect(0, 0, current_input_canvas.width(), current_input_canvas.height(), depth, input_texture);
			// Draw input selection
			if (layout.selection != null) {
				float hue = (System.currentTimeMillis() % 10000) / 10000f;
				java.awt.Color color = new java.awt.Color(java.awt.Color.HSBtoRGB(hue, 1, 1));
				current_input_canvas.color(new Vector4f(
												color.getRed() / 255f, 
												color.getGreen() / 255f, 
												color.getBlue() / 255f, 
												0.25f
											));
				current_input_canvas.rect(layout.selection, depth);	
			}
			// + preview if subdivision is on
			if (current_input_hover_preview != null) {
				current_input_canvas.color(Color.WHITE25.val());
				current_input_canvas.rect(current_input_hover_preview, depth);	

			}
			// ...
			current_input_canvas.draw_frame();
			
			if (current_export_gui == null) {
				current_edit_canvas.textrenderer(gui.textrenderer());
				current_edit_canvas.color(Color.BLACK.val());
				current_edit_canvas.text(2, 2, depth, "No Export Selected");
				current_edit_canvas.draw_frame();
			}
		}
		
	}
	
}
