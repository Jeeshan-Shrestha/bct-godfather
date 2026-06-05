package com.bct.bct_godfather.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.springframework.stereotype.Service;

import com.bct.bct_godfather.manga.MangaDexClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

@Service
public class MangaDexService {

    private final MangaDexClient client;

    public MangaDexService(MangaDexClient client) {
        this.client = client;
    }

    public EmbedBuilder buildSearchEmbed(String title) {
        JsonNode results = client.searchManga(title);
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔍 MangaDex: " + title)
                .setColor(0xFF6740);

        if (results == null || results.isEmpty()) {
            return embed.setDescription("No results found.");
        }

        for (JsonNode manga : results) {
            String id = manga.get("id").asText();
            JsonNode attrs = manga.get("attributes");

            String name = getLocalized(attrs.get("title"));
            String desc = getLocalized(attrs.get("description"));
            String status = attrs.get("status").asText("unknown");
            String link = "https://mangadex.org/title/" + id;
            String shortDesc = desc.length() > 120 ? desc.substring(0, 120) + "…" : desc;

            embed.addField(
                name + " `[" + status + "]`",
                shortDesc + "\n🆔 `" + id + "`\n[Read on MangaDex](" + link + ")",
                false
            );
        }

        return embed;
    }

    public JsonNode getChapterInfo(String chapterId) {
        return client.getChapterInfo(chapterId);
    }

    public List<String> getPages(String chapterId) {
        return client.getChapterPages(chapterId);
    }

    public byte[] downloadPage(String url) {
        return client.downloadPage(url);
    }

    private String getLocalized(JsonNode node) {
        if (node == null || node.isEmpty()) return "Unknown";
        if (node.has("en")) return node.get("en").asText();
        return node.fields().next().getValue().asText();
    }

    private String extractGroupName(JsonNode chapter) {
        JsonNode relationships = chapter.get("relationships");
        if (relationships == null) return "Unknown group";

        for (JsonNode rel : relationships) {
            if ("scanlation_group".equals(rel.get("type").asText())) {
                JsonNode attrs = rel.get("attributes");
                if (attrs != null && attrs.has("name")) {
                    return attrs.get("name").asText();
                }
            }
        }
        return "Unknown group";
    }

    public EmbedBuilder buildChaptersEmbed(String mangaId) {
    JsonNode chapters = client.getChapters(mangaId, 5);
    EmbedBuilder embed = new EmbedBuilder()
            .setTitle("📖 Latest Chapters")
            .setColor(0x3498DB);

    if (chapters == null || chapters.isEmpty()) {
        return embed.setDescription("No English chapters found.");
    }

    for (JsonNode ch : chapters) {
        String chId = ch.get("id").asText();
        JsonNode attrs = ch.get("attributes");

        String chNum = attrs.get("chapter").isNull() ? "?" : attrs.get("chapter").asText();
        String chTitle = attrs.get("title").isNull() ? "No title" : attrs.get("title").asText();
        String link = "https://mangadex.org/chapter/" + chId;
        String group = extractGroupName(ch);

        // detect external chapters (MangaPlus, etc.)
        JsonNode externalUrl = attrs.get("externalUrl");
        boolean isExternal = externalUrl != null && !externalUrl.isNull();

        String readLink = isExternal
            ? "[Read on " + extractPlatformName(externalUrl.asText()) + "](" + externalUrl.asText() + ")"
            : "[Read here](" + link + ")";

        embed.addField(
            "Chapter " + chNum + " — " + chTitle,
            "👥 " + group + "\n" + readLink + "\n🆔 `" + chId + "`",
            false
        );
    }

    return embed;
    }

    private String extractPlatformName(String url) {
        if (url.contains("mangaplus")) return "MangaPlus";
        if (url.contains("azuki")) return "Azuki";
        if (url.contains("comikey")) return "Comikey";
        return "External Site";
    }

    public byte[] buildChapterPdf(String chapterId) throws Exception {
    List<String> pages = client.getChapterPagesSaver(chapterId);

    if (pages.isEmpty()) throw new RuntimeException("No pages found.");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PdfWriter writer = new PdfWriter(out);
    PdfDocument pdf = new PdfDocument(writer);
    Document document = new Document(pdf);
    document.setMargins(0, 0, 0, 0);

    for (String pageUrl : pages) {
        byte[] imageBytes = client.downloadPage(pageUrl);
        byte[] compressed = compressImage(imageBytes);

        ImageData imageData = ImageDataFactory.create(compressed);
        Image image = new Image(imageData);

        pdf.addNewPage(new PageSize(image.getImageScaledWidth(), image.getImageScaledHeight()));
        image.setFixedPosition(pdf.getNumberOfPages(), 0, 0);
        image.scaleToFit(image.getImageScaledWidth(), image.getImageScaledHeight());
        document.add(image);
    }

    document.close();
    return out.toByteArray();
}

    private byte[] compressImage(byte[] imageBytes) throws Exception {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (original == null) return imageBytes;

        // scale down if too wide
        int maxWidth = 800;
        BufferedImage resized = original;
        if (original.getWidth() > maxWidth) {
            int newHeight = (int) ((double) original.getHeight() / original.getWidth() * maxWidth);
            resized = new BufferedImage(maxWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, maxWidth, newHeight, null);
            g.dispose();
        }

        // write as compressed JPEG
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = jpegWriter.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.6f);

        jpegWriter.setOutput(ImageIO.createImageOutputStream(compressed));
        jpegWriter.write(null, new IIOImage(resized, null, null), param);
        jpegWriter.dispose();

        return compressed.toByteArray();
    }
}
