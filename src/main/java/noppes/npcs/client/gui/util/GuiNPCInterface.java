package noppes.npcs.client.gui.util;

import com.google.common.collect.Lists;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Mouse;

public abstract class GuiNPCInterface extends GuiScreen {
    public EntityPlayerSP player;
    public boolean drawDefaultBackground;
    public EntityNPCInterface npc;
    private Map<Integer, GuiNpcButton> buttons;
    private Map<Integer, GuiMenuTopButton> topbuttons;
    private Map<Integer, GuiMenuSideButton> sidebuttons;
    private Map<Integer, GuiNpcTextField> textfields;
    private Map<Integer, GuiNpcLabel> labels;
    private Map<Integer, GuiCustomScroll> scrolls;
    private Map<Integer, GuiNpcSlider> sliders;
    private Map<Integer, GuiScreen> extra;
    private List<IGui> components;
    public String title;
    public ResourceLocation background;
    public boolean closeOnEsc;
    public int guiLeft;
    public int guiTop;
    public int xSize;
    public int ySize;
    private SubGuiInterface subgui;
    public int mouseX;
    public int mouseY;
    public float bgScale;
    private GuiButton selectedButton;

    public GuiNPCInterface(EntityNPCInterface npc) {
        this.drawDefaultBackground = true;
        this.buttons = new ConcurrentHashMap();
        this.topbuttons = new ConcurrentHashMap();
        this.sidebuttons = new ConcurrentHashMap();
        this.textfields = new ConcurrentHashMap();
        this.labels = new ConcurrentHashMap();
        this.scrolls = new ConcurrentHashMap();
        this.sliders = new ConcurrentHashMap();
        this.extra = new ConcurrentHashMap();
        this.components = new ArrayList();
        this.background = null;
        this.closeOnEsc = false;
        this.bgScale = 1.0F;
        this.player = Minecraft.getMinecraft().player;
        this.npc = npc;
        this.title = "";
        this.xSize = 200;
        this.ySize = 222;
        this.mc = Minecraft.getMinecraft();
        this.itemRender = this.mc.getRenderItem();
        this.fontRenderer = this.mc.fontRenderer;
    }

    public GuiNPCInterface() {
        this((EntityNPCInterface) null);
    }

    public void setBackground(String texture) {
        this.background = new ResourceLocation("customnpcs", "textures/gui/" + texture);
    }

    public ResourceLocation getResource(String texture) {
        return new ResourceLocation("customnpcs", "textures/gui/" + texture);
    }

    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        this.initPacket();
    }

    public void initPacket() {
    }

    public void initGui() {
        super.initGui();
        GuiNpcTextField.unfocus();
        if (this.subgui != null) {
            this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
            this.subgui.initGui();
        }

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        this.buttonList = Lists.newArrayList();
        this.buttons = new ConcurrentHashMap();
        this.topbuttons = new ConcurrentHashMap();
        this.sidebuttons = new ConcurrentHashMap();
        this.textfields = new ConcurrentHashMap();
        this.labels = new ConcurrentHashMap();
        this.scrolls = new ConcurrentHashMap();
        this.sliders = new ConcurrentHashMap();
        this.extra = new ConcurrentHashMap();
        this.components = new ArrayList();
    }

    public void updateScreen() {
        if (this.subgui != null) {
            this.subgui.updateScreen();
        } else {
            for (GuiNpcTextField tf : this.textfields.values()) {
                if (tf.enabled) {
                    tf.updateCursorCounter();
                }
            }

            for (IGui comp : this.components) {
                comp.updateScreen();
            }

            super.updateScreen();
        }

    }

    public void addExtra(GuiHoverText gui) {
        gui.setWorldAndResolution(this.mc, 350, 250);
        this.extra.put(Integer.valueOf(gui.id), gui);
    }

    public void mouseClicked(int i, int j, int k) {
        if (this.subgui != null) {
            this.subgui.mouseClicked(i, j, k);
        } else {
            for (GuiNpcTextField tf : this.textfields.values()) {
                if (tf.enabled) {
                    tf.mouseClicked(i, j, k);
                }
            }

            for (IGui comp : this.components) {
                if (comp instanceof IMouseListener) {
                    ((IMouseListener) comp).mouseClicked(i, j, k);
                }
            }

            this.mouseEvent(i, j, k);
            if (k == 0) {
                for (GuiCustomScroll scroll : this.scrolls.values()) {
                    scroll.mouseClicked(i, j, k);
                }

                for (GuiButton guibutton : this.buttonList) {
                    if (guibutton.mousePressed(this.mc, this.mouseX, this.mouseY)) {
                        Pre event = new Pre(this, guibutton, this.buttonList);
                        if (!MinecraftForge.EVENT_BUS.post(event)) {
                            guibutton = event.getButton();
                            this.selectedButton = guibutton;
                            guibutton.playPressSound(this.mc.getSoundHandler());
                            this.actionPerformed(guibutton);
                            if (this.equals(this.mc.currentScreen)) {
                                MinecraftForge.EVENT_BUS.post(new Post(this, event.getButton(), this.buttonList));
                            }
                        }
                        break;
                    }
                }
            }
        }

    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.selectedButton != null && state == 0) {
            this.selectedButton.mouseReleased(mouseX, mouseY);
            this.selectedButton = null;
        }

    }

    public void mouseEvent(int i, int j, int k) {
    }

    protected void actionPerformed(GuiButton guibutton) {
        if (this.subgui != null) {
            this.subgui.buttonEvent(guibutton);
        } else {
            this.buttonEvent(guibutton);
        }

    }

    public void buttonEvent(GuiButton guibutton) {
    }

    public void keyTyped(char c, int i) {
        if (this.subgui != null) {
            this.subgui.keyTyped(c, i);
        } else {
            boolean active = false;

            for (IGui gui : this.components) {
                if (gui.isActive()) {
                    active = true;
                    break;
                }
            }

            active = active || GuiNpcTextField.isActive();
            if (!this.closeOnEsc || i != 1 && (active || !this.isInventoryKey(i))) {
                for (GuiNpcTextField tf : this.textfields.values()) {
                    tf.textboxKeyTyped(c, i);
                }

                for (IGui comp : this.components) {
                    if (comp instanceof IKeyListener) {
                        ((IKeyListener) comp).keyTyped(c, i);
                    }
                }

            } else {
                this.close();
            }
        }
    }

    public void onGuiClosed() {
        GuiNpcTextField.unfocus();
    }

    public void close() {
        this.displayGuiScreen((GuiScreen) null);
        this.mc.setIngameFocus();
        this.save();
    }

    public void addButton(GuiNpcButton button) {
        this.buttons.put(Integer.valueOf(button.id), button);
        this.buttonList.add(button);
    }

    public void addTopButton(GuiMenuTopButton button) {
        this.topbuttons.put(Integer.valueOf(button.id), button);
        this.buttonList.add(button);
    }

    public void addSideButton(GuiMenuSideButton button) {
        this.sidebuttons.put(Integer.valueOf(button.id), button);
        this.buttonList.add(button);
    }

    public GuiNpcButton getButton(int i) {
        return (GuiNpcButton) this.buttons.get(Integer.valueOf(i));
    }

    public GuiMenuSideButton getSideButton(int i) {
        return (GuiMenuSideButton) this.sidebuttons.get(Integer.valueOf(i));
    }

    public GuiMenuTopButton getTopButton(int i) {
        return (GuiMenuTopButton) this.topbuttons.get(Integer.valueOf(i));
    }

    public void addTextField(GuiNpcTextField tf) {
        this.textfields.put(Integer.valueOf(tf.getId()), tf);
    }

    public GuiNpcTextField getTextField(int i) {
        return (GuiNpcTextField) this.textfields.get(Integer.valueOf(i));
    }

    public void add(IGui gui) {
        this.components.add(gui);
    }

    public IGui get(int id) {
        for (IGui comp : this.components) {
            if (comp.getID() == id) {
                return comp;
            }
        }

        return null;
    }

    public void addLabel(GuiNpcLabel label) {
        this.labels.put(Integer.valueOf(label.id), label);
    }

    public GuiNpcLabel getLabel(int i) {
        return (GuiNpcLabel) this.labels.get(Integer.valueOf(i));
    }

    public void addSlider(GuiNpcSlider slider) {
        this.sliders.put(Integer.valueOf(slider.id), slider);
        this.buttonList.add(slider);
    }

    public GuiNpcSlider getSlider(int i) {
        return (GuiNpcSlider) this.sliders.get(Integer.valueOf(i));
    }

    public void addScroll(GuiCustomScroll scroll) {
        scroll.setWorldAndResolution(this.mc, 350, 250);
        this.scrolls.put(Integer.valueOf(scroll.id), scroll);
    }

    public GuiCustomScroll getScroll(int id) {
        return (GuiCustomScroll) this.scrolls.get(Integer.valueOf(id));
    }

    public abstract void save();

    public void drawScreen(int i, int j, float f) {
        this.mouseX = i;
        this.mouseY = j;
        if (this.drawDefaultBackground && this.subgui == null) {
            this.drawDefaultBackground();
        }

        if (this.background != null && this.mc.renderEngine != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) this.guiLeft, (float) this.guiTop, 0.0F);
            GlStateManager.scale(this.bgScale, this.bgScale, this.bgScale);
            this.mc.renderEngine.bindTexture(this.background);
            if (this.xSize > 256) {
                this.drawTexturedModalRect(0, 0, 0, 0, 250, this.ySize);
                this.drawTexturedModalRect(250, 0, 256 - (this.xSize - 250), 0, this.xSize - 250, this.ySize);
            } else {
                this.drawTexturedModalRect(0, 0, 0, 0, this.xSize, this.ySize);
            }

            GlStateManager.popMatrix();
        }

        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 8, 16777215);

        for (GuiNpcLabel label : this.labels.values()) {
            label.drawLabel(this, this.fontRenderer);
        }

        for (GuiNpcTextField tf : this.textfields.values()) {
            tf.drawTextBox(i, j);
        }

        for (IGui comp : this.components) {
            comp.drawScreen(i, j);
        }

        for (GuiCustomScroll scroll : this.scrolls.values()) {
            scroll.drawScreen(i, j, f, !this.hasSubGui() && scroll.isMouseOver(i, j) ? Mouse.getDWheel() : 0);
        }

        for (GuiScreen gui : this.extra.values()) {
            gui.drawScreen(i, j, f);
        }

        super.drawScreen(i, j, f);
        if (this.subgui != null) {
            this.subgui.drawScreen(i, j, f);
        }

    }

    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }

    public void elementClicked() {
        if (this.subgui != null) {
            this.subgui.elementClicked();
        }

    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public void doubleClicked() {
    }

    public boolean isInventoryKey(int i) {
        return i == this.mc.gameSettings.keyBindInventory.getKeyCode();
    }

    public void drawDefaultBackground() {
        super.drawDefaultBackground();
    }

    public void displayGuiScreen(GuiScreen gui) {
        this.mc.displayGuiScreen(gui);
    }

    public void setSubGui(SubGuiInterface gui) {
        this.subgui = gui;
        this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
        this.subgui.parent = this;
        this.initGui();
    }

    public void closeSubGui(SubGuiInterface gui) {
        this.subgui = null;
    }

    public boolean hasSubGui() {
        return this.subgui != null;
    }

    public SubGuiInterface getSubGui() {
        return this.hasSubGui() && this.subgui.hasSubGui() ? this.subgui.getSubGui() : this.subgui;
    }

    public void drawNpc(int x, int y) {
        this.drawNpc(this.npc, x, y, 1.0F, 0);
    }

    public void drawNpc(EntityLivingBase entity, int x, int y, float zoomed, int rotation) {
        EntityNPCInterface npc = null;
        if (entity instanceof EntityNPCInterface) {
            npc = (EntityNPCInterface) entity;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (this.guiLeft + x), (float) (this.guiTop + y), 50.0F);
        float scale = 1.0F;
        if ((double) entity.height > 2.4D) {
            scale = 2.0F / entity.height;
        }

        GlStateManager.scale(-30.0F * scale * zoomed, 30.0F * scale * zoomed, 30.0F * scale * zoomed);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        RenderHelper.enableStandardItemLighting();
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f7 = entity.rotationYawHead;
        float f5 = (float) (this.guiLeft + x) - (float) this.mouseX;
        float f6 = (float) (this.guiTop + y) - 50.0F * scale * zoomed - (float) this.mouseY;
        int orientation = 0;
        if (npc != null) {
            orientation = npc.ais.orientation;
            npc.ais.orientation = rotation;
        }

        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan((double) (f6 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = (float) rotation;
        entity.rotationYaw = (float) Math.atan((double) (f5 / 80.0F)) * 40.0F + (float) rotation;
        entity.rotationPitch = -((float) Math.atan((double) (f6 / 40.0F))) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        this.mc.getRenderManager().playerViewY = 180.0F;
        this.mc.getRenderManager().renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        entity.prevRenderYawOffset = entity.renderYawOffset = f2;
        entity.prevRotationYaw = entity.rotationYaw = f3;
        entity.prevRotationPitch = entity.rotationPitch = f4;
        entity.prevRotationYawHead = entity.rotationYawHead = f7;
        if (npc != null) {
            npc.ais.orientation = orientation;
        }

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public void openLink(String link) {
        try {
            Class oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke((Object) null);
            oclass.getMethod("browse", URI.class).invoke(object, new URI(link));
        } catch (Throwable var4) {
            ;
        }

    }
}
