package wafflestomper.ghostwriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.java.games.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.SlotGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class GuiFileBrowser extends Screen{// implements YesNoCallback{
	
	//private GuiSaveAs.ScrollList scrollList;
	private FileSelectionList fileSelectionList;
	public int slotSelected = -1;
	private TextFieldWidget filenameField;
	private boolean directoryDirty = false;
	
	private Screen parentGui; // TODO: genericize somehow?
	private Clipboard tempClipboard = new Clipboard();
	
	private FileHandler fileHandler;
	private String displayPath = "";
	
	private static final int BUTTONWIDTH = 60;
	private static final int BUTTONHEIGHT = 20;
	
	private static final int BTN_SAVE = 0;
	private static final int BTN_CANCEL = 1;
	
	private Button btnSave;
	private Button btnCancel;
	
	private String bookTitle = "";
	private String bookAuthor = "";
	private List<String> bookPages;
	private static final Minecraft mc = Minecraft.getInstance();
	private boolean initialized = false;
	
	private static final int SLOT_HEIGHT = 12;
	

	public GuiFileBrowser(Screen _parentGui, String _title, String _author, List<String> _pages){
		super(new StringTextComponent("Title I think?")); // TODO
		this.parentGui = _parentGui;
		this.fileHandler = new FileHandler(this.tempClipboard);
		this.fileHandler.currentPath = this.fileHandler.getSavePath();
		this.displayPath = this.fileHandler.currentPath.getAbsolutePath();
		this.bookTitle = _title;
		this.bookAuthor = _author;
		this.bookPages = _pages;
	}
	
	
	private void driveButtonClicked(File root) {
		System.out.println("Drive button clicked " + root);
	}
	
	
	public void init(){
		super.init();
		this.minecraft.keyboardListener.enableRepeatEvents(true);
		
		if (this.initialized == false) {
			this.fileSelectionList = new FileSelectionList(this, this.mc, this.width, this.height, 32, this.height - 64, SLOT_HEIGHT);
			
			this.btnSave = this.addButton(new Button(this.width-(BUTTONWIDTH+5), this.height-50, BUTTONWIDTH, BUTTONHEIGHT, "Save", (pressedButton) ->{}));
			this.btnCancel = this.addButton(new Button(this.width-(BUTTONWIDTH+5), this.height-25, BUTTONWIDTH, BUTTONHEIGHT, "Cancel", (pressedButton) ->{}));
			
			//Add buttons for each non-empty drive letter
			int rootNum = 100;
			List<File> roots = this.fileHandler.getValidRoots();
			for (File root : roots){
				this.addButton(new Button(5, 35 + 21*(rootNum-100), 50, 20, root.getAbsolutePath(), (pressedButton)->{
					this.driveButtonClicked(root); // TODO: Test this to make sure it actually works
				}));
				rootNum++;
				//
			}
			populateFileList();
	//		this.scrollList.registerScrollButtons(4, 5);
			this.filenameField = new TextFieldWidget(this.mc.fontRenderer, this.width/2-125, this.height-32, 250, 20, "filename");
			this.filenameField.setMaxStringLength(100);
			String ftitle = this.bookTitle.trim().replaceAll(" ", ".").replaceAll("[^a-zA-Z0-9\\.]", "");
			String fauthor = this.bookAuthor.trim().replaceAll(" ", ".").replaceAll("[^a-zA-Z0-9\\.]", "");
			String defaultFilename = ftitle + "_" + fauthor + "_" + this.fileHandler.getUTC() + ".ghb";
			this.filenameField.setText(defaultFilename);
			
			this.initialized = true;
		}
		else {
			// TODO: Do resizing here
		}
		this.children.add(this.fileSelectionList);
		this.populateFileList();
	}
	
	
	private boolean isFilenameValid(){
		String fn = this.filenameField.getText();
		if (!fn.equals("")){
			return true;
		}
		return false;
	}
	
	/**
	 * replaces drawScreen?
	 */
	@Override
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) { 
		this.displayPath = this.fileHandler.currentPath.getAbsolutePath();
		this.btnSave.active = this.isFilenameValid();
		this.fileSelectionList.render(p_render_1_, p_render_2_, p_render_3_);
		super.render(p_render_1_, p_render_2_, p_render_3_);
		this.drawCenteredString(this.mc.fontRenderer, BookUtilities.truncateStringPixels(this.displayPath,"...", 200, true), this.width / 2, 20, 0xDDDDDD);
		this.filenameField.render(p_render_1_, p_render_2_, p_render_3_);
	}
	
	
	public void setSelectedSlot(FileSelectionList.Entry entry) {
		this.fileSelectionList.setSelected(entry);
		System.out.println("File selected! Do something here?");
	}
	
	@Override
	public void tick(){
		this.filenameField.tick();
		super.tick();
	}
	
	
	@Override
	public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
		this.filenameField.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
		return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
	}
	
	
	private void populateFileList(){
		this.fileSelectionList.updateFileList(this.fileHandler.listFiles(this.fileHandler.currentPath, this.directoryDirty));
		this.directoryDirty = false;
	}
	
	
	private void goBackToParentGui(){
		this.mc.displayGuiScreen(this.parentGui);
	}
	
	
	/**
	 * Handles special characters
	 */
	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
			return true;
		}
		// TODO: Escape should kick back to the parent GUI
		// TODO: Add ctrl+s to save
		return this.filenameField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}
	
	/**
	 * Handles ordinary characters
	 */
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		if (super.charTyped(p_charTyped_1_, p_charTyped_2_)) {
			return true;
		} 
		else {
			return this.filenameField.charTyped(p_charTyped_1_, p_charTyped_2_);
		}    	
	}
	
	
	public void confirmClicked(boolean result, int id)
	{
		if (id == 1000){
			if (result == true){
				saveBook();
			}
			else{
				this.mc.displayGuiScreen(this);
			}
		}
	}
	
	
	private File getSavePath(){
		return(new File(this.fileHandler.currentPath, this.filenameField.getText()));
	}
	
	
	private void saveBook(){
		if (this.fileHandler.saveBookToGHBFile(this.bookTitle, this.bookAuthor, this.bookPages, getSavePath())){
			this.mc.displayGuiScreen(GuiFileBrowser.this.parentGui);
		}
	}
	
	
//	protected void actionPerformed(GuiButton buttonPressed){
//		if (!buttonPressed.enabled){return;}
//		
//		switch (buttonPressed.id){
//			case BTN_SAVE:
//				if (getSavePath().exists()){
//					String ynmessage = "Are you sure you wish to overwrite";
//					this.mc.displayGuiScreen(new GuiYesNo(this, ynmessage, this.filenameField.getText() + "?", "Yes", "No", 1000));
//				}
//				else{
//					saveBook();
//				}
//				break;
//			case BTN_CANCEL:
//				Minecraft.getMinecraft().displayGuiScreen(GuiSaveAs.this.parentGui);
//				break;
//			default:
//				break;
//		}
//		
//		//Handle the drive letter buttons
//		if (buttonPressed.id >= 100){
//			this.fileHandler.currentPath = new File(buttonPressed.displayString);
//		}
//	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton){
		this.filenameField.mouseClicked(mouseX, mouseY, mouseButton);
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	
//	class ScrollList extends GuiSlot{
//		
//		private static final int SLOT_HEIGHT = 12;
//		
//		public ScrollList(){
//			super(GuiSaveAs.this.mc, GuiSaveAs.this.width, GuiSaveAs.this.height, 32, GuiSaveAs.this.height - 64, SLOT_HEIGHT);
//		}
//
//		
//		protected int getPaddedSize(){
//			int scrollHeight = GuiSaveAs.this.height - 96;
//			int minSlots = (int)Math.ceil(scrollHeight/SLOT_HEIGHT);
//			
//			if (GuiSaveAs.this.listItems.size() >= minSlots){
//				//The extra slot is for the parent directory item (..)
//				return GuiSaveAs.this.listItems.size() + 1;
//			}
//			else{
//				return minSlots;
//			}
//		}
//		
//		
//		protected int getSize(){
//			return getPaddedSize();
//		}
//		
//
//		/**
//		 * The element in the slot that was clicked, boolean for whether it was double clicked or not
//		 */
//		protected void elementClicked(int slotClicked, boolean doubleClicked, int clickXPos, int clickYPos){
//			this.setShowSelectionBox(true);
//			if (doubleClicked){
//				if (slotClicked == 0){
//					//Go up to the parent directory
//					GuiSaveAs.this.fileHandler.navigateUp();
//					GuiSaveAs.this.slotSelected = -1;
//					this.setShowSelectionBox(false);
//					return;
//				}
//				else if (slotClicked <= GuiSaveAs.this.listItems.size()){
//					File itemClicked = GuiSaveAs.this.listItems.get(slotClicked-1);
//					if (itemClicked.isDirectory()){
//						//Go into the clicked directory
//						GuiSaveAs.this.fileHandler.currentPath = itemClicked;
//						GuiSaveAs.this.slotSelected = -1;
//						this.setShowSelectionBox(false);
//						return;
//					}
//				}
//			}
//			else if (slotClicked > 0 && slotClicked <= GuiSaveAs.this.listItems.size()){
//				//A file or directory has been single-clicked
//				File selectedFile = GuiSaveAs.this.listItems.get(slotClicked-1);
//				if (selectedFile.isFile() && !isSelected(slotClicked)){
//					GuiSaveAs.this.filenameField.setText(selectedFile.getName());
//				}
//			}
//			else{
//				//This is called when an empty slot is selected
//				//It probably doesn't need to do anything
//			}
//			GuiSaveAs.this.slotSelected = slotClicked;
//		}
//		
//
//		/**
//		 * Returns true if the element passed in is currently selected
//		 */
//		protected boolean isSelected(int pos){
//			return pos == GuiSaveAs.this.slotSelected;
//		}
//		
//
//		/**
//		 * Return the height of the content being scrolled
//		 * This is used to determine the scrollable area. It doesn't affect the actual slot height
//		 */
//		protected int getContentHeight(){
//			return getPaddedSize() * SLOT_HEIGHT;
//		}
//
//		
//		protected void drawBackground(){
//			GuiSaveAs.this.drawDefaultBackground();
//		}
//
//		
//		protected void drawSlot(int slotNum, int slotX, int slotY, int four__UNKNOWN_USE__, int mouseX, int mouseY, float p_192637_7_){
//			List<File> list= GuiSaveAs.this.listItems;
//			//Empty padding slots at the bottom
//			if (slotNum > list.size()){return;}
//			
//			String s = "";
//			int color = 0xFFFFFF;
//			
//			if (slotNum == 0){
//				s = "..";
//				color = 0x00FF00;
//			}
//			else{
//				s = BookUtilities.truncateStringPixels(list.get(slotNum-1).getName(), "...", 200, false);
//				if (!list.get(slotNum-1).exists()){
//					color = 0x333333;
//					GuiSaveAs.this.directoryDirty = true;
//				}
//				else if (list.get(slotNum-1).isFile()){
//					color = 0xFF0000;
//				}
//				else if (list.get(slotNum-1).isDirectory()){
//					color = 0x00FF00;
//				}
//			}
//			GuiSaveAs.this.drawString(GuiSaveAs.this.fontRenderer, s, slotX + 2, slotY + 1, color);
//		}
//	}
}