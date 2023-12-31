package noppes.npcs.config;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.util.LRUHashMap;

public class TrueTypeFont {
   private static final int MaxWidth = 512;
   private static final List<Font> allFonts = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
   private List<Font> usedFonts = new ArrayList();
   private LinkedHashMap<String, TrueTypeFont$GlyphCache> textcache = new LRUHashMap<String, TrueTypeFont$GlyphCache>(100);
   private Map<Character, TrueTypeFont$Glyph> glyphcache = new HashMap();
   private List<TrueTypeFont$TextureCache> textures = new ArrayList();
   private Font font;
   private int lineHeight = 1;
   private Graphics2D globalG = (Graphics2D)(new BufferedImage(1, 1, 2)).getGraphics();
   public float scale = 1.0F;
   private int specialChar = 167;

   public TrueTypeFont(Font font, float scale) {
      this.font = font;
      this.scale = scale;
      this.globalG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      this.lineHeight = this.globalG.getFontMetrics(font).getHeight();
   }

   public TrueTypeFont(ResourceLocation resource, int fontSize, float scale) throws IOException, FontFormatException {
      InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream();
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      Font font = Font.createFont(0, stream);
      ge.registerFont(font);
      this.font = font.deriveFont(0, (float)fontSize);
      this.scale = scale;
      this.globalG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      this.lineHeight = this.globalG.getFontMetrics(font).getHeight();
   }

   public void setSpecial(char c) {
      this.specialChar = c;
   }

   public void draw(String text, float x, float y, int color) {
      TrueTypeFont$GlyphCache cache = this.getOrCreateCache(text);
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      GlStateManager.color(r, g, b, 1.0F);
      GlStateManager.enableBlend();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, 0.0F);
      GlStateManager.scale(this.scale, this.scale, 1.0F);
      float i = 0.0F;

      for(TrueTypeFont$Glyph gl : cache.glyphs) {
         if (gl.type != TrueTypeFont$GlyphType.NORMAL) {
            if (gl.type == TrueTypeFont$GlyphType.RESET) {
               GlStateManager.color(r, g, b, 1.0F);
            } else if (gl.type == TrueTypeFont$GlyphType.COLOR) {
               GlStateManager.color((float)(gl.color >> 16 & 255) / 255.0F, (float)(gl.color >> 8 & 255) / 255.0F, (float)(gl.color & 255) / 255.0F, 1.0F);
            }
         } else {
            GlStateManager.bindTexture(gl.texture);
            this.drawTexturedModalRect(i, 0.0F, (float)gl.x * this.textureScale(), (float)gl.y * this.textureScale(), (float)gl.width * this.textureScale(), (float)gl.height * this.textureScale());
            i += (float)gl.width * this.textureScale();
         }
      }

      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private TrueTypeFont$GlyphCache getOrCreateCache(String text) {
      TrueTypeFont$GlyphCache cache = (TrueTypeFont$GlyphCache)this.textcache.get(text);
      if (cache != null) {
         return cache;
      } else {
         cache = new TrueTypeFont$GlyphCache(this);

         for(int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == this.specialChar && i + 1 < text.length()) {
               char next = text.toLowerCase(Locale.ENGLISH).charAt(i + 1);
               int index = "0123456789abcdefklmnor".indexOf(next);
               if (index >= 0) {
                  TrueTypeFont$Glyph g = new TrueTypeFont$Glyph(this);
                  if (index < 16) {
                     g.type = TrueTypeFont$GlyphType.COLOR;
                     g.color = Minecraft.getMinecraft().fontRenderer.getColorCode(next);
                  } else if (index == 16) {
                     g.type = TrueTypeFont$GlyphType.RANDOM;
                  } else if (index == 17) {
                     g.type = TrueTypeFont$GlyphType.BOLD;
                  } else if (index == 18) {
                     g.type = TrueTypeFont$GlyphType.STRIKETHROUGH;
                  } else if (index == 19) {
                     g.type = TrueTypeFont$GlyphType.UNDERLINE;
                  } else if (index == 20) {
                     g.type = TrueTypeFont$GlyphType.ITALIC;
                  } else {
                     g.type = TrueTypeFont$GlyphType.RESET;
                  }

                  cache.glyphs.add(g);
                  ++i;
                  continue;
               }
            }

            TrueTypeFont$Glyph g = this.getOrCreateGlyph(c);
            cache.glyphs.add(g);
            cache.width += g.width;
            cache.height = Math.max(cache.height, g.height);
         }

         this.textcache.put(text, cache);
         return cache;
      }
   }

   private TrueTypeFont$Glyph getOrCreateGlyph(char c) {
      TrueTypeFont$Glyph g = (TrueTypeFont$Glyph)this.glyphcache.get(Character.valueOf(c));
      if (g != null) {
         return g;
      } else {
         TrueTypeFont$TextureCache cache = this.getCurrentTexture();
         Font font = this.getFontForChar(c);
         FontMetrics metrics = this.globalG.getFontMetrics(font);
         g = new TrueTypeFont$Glyph(this);
         g.width = Math.max(metrics.charWidth(c), 1);
         g.height = Math.max(metrics.getHeight(), 1);
         if (cache.x + g.width >= 512) {
            cache.x = 0;
            cache.y += this.lineHeight + 1;
            if (cache.y >= 512) {
               cache.full = true;
               cache = this.getCurrentTexture();
            }
         }

         g.x = cache.x;
         g.y = cache.y;
         cache.x += g.width + 3;
         this.lineHeight = Math.max(this.lineHeight, g.height);
         cache.g.setFont(font);
         cache.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         cache.g.drawString(c + "", g.x, g.y + metrics.getAscent());
         g.texture = cache.textureId;
         TextureUtil.uploadTextureImage(cache.textureId, cache.bufferedImage);
         this.glyphcache.put(Character.valueOf(c), g);
         return g;
      }
   }

   private TrueTypeFont$TextureCache getCurrentTexture() {
      TrueTypeFont$TextureCache cache = null;

      for(TrueTypeFont$TextureCache t : this.textures) {
         if (!t.full) {
            cache = t;
            break;
         }
      }

      if (cache == null) {
         this.textures.add(cache = new TrueTypeFont$TextureCache(this));
      }

      return cache;
   }

   public void drawCentered(String text, float x, float y, int color) {
      this.draw(text, x - (float)this.width(text) / 2.0F, y, color);
   }

   private Font getFontForChar(char c) {
      if (this.font.canDisplay(c)) {
         return this.font;
      } else {
         for(Font f : this.usedFonts) {
            if (f.canDisplay(c)) {
               return f;
            }
         }

         Font fa = new Font("Arial Unicode MS", 0, this.font.getSize());
         if (fa.canDisplay(c)) {
            return fa;
         } else {
            for(Font f : allFonts) {
               if (f.canDisplay(c)) {
                  Font var7;
                  this.usedFonts.add(var7 = f.deriveFont(0, (float)this.font.getSize()));
                  return var7;
               }
            }

            return this.font;
         }
      }
   }

   public void drawTexturedModalRect(float x, float y, float textureX, float textureY, float width, float height) {
      float f = 0.00390625F;
      float f1 = 0.00390625F;
      int zLevel = 0;
      BufferBuilder tessellator = Tessellator.getInstance().getBuffer();
      tessellator.begin(7, DefaultVertexFormats.POSITION_TEX);
      tessellator.noColor();
      tessellator.pos((double)x, (double)(y + height), (double)zLevel).tex((double)(textureX * f), (double)((textureY + height) * f1)).endVertex();
      tessellator.pos((double)(x + width), (double)(y + height), (double)zLevel).tex((double)((textureX + width) * f), (double)((textureY + height) * f1)).endVertex();
      tessellator.pos((double)(x + width), (double)y, (double)zLevel).tex((double)((textureX + width) * f), (double)(textureY * f1)).endVertex();
      tessellator.pos((double)x, (double)y, (double)zLevel).tex((double)(textureX * f), (double)(textureY * f1)).endVertex();
      Tessellator.getInstance().draw();
   }

   public int width(String text) {
      TrueTypeFont$GlyphCache cache = this.getOrCreateCache(text);
      return (int)((float)cache.width * this.scale * this.textureScale());
   }

   public int height(String text) {
      if (text != null && !text.trim().isEmpty()) {
         TrueTypeFont$GlyphCache cache = this.getOrCreateCache(text);
         return Math.max(1, (int)((float)cache.height * this.scale * this.textureScale()));
      } else {
         return (int)((float)this.lineHeight * this.scale * this.textureScale());
      }
   }

   private float textureScale() {
      return 0.5F;
   }

   public void dispose() {
      for(TrueTypeFont$TextureCache cache : this.textures) {
         GlStateManager.deleteTexture(cache.textureId);
      }

      this.textcache.clear();
   }

   public String getFontName() {
      return this.font.getFontName();
   }
}
