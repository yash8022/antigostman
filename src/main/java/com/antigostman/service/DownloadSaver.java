package com.antigostman.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class DownloadSaver {

	// Comprehensive Office MIME -> extension map (includes legacy + OOXML +
	// macro-enabled + templates + others)
	private static final Map<String, String> MIME_TO_EXT;
	static {
		Map<String, String> m = new HashMap<>();

		// --- Word ---
		m.put("application/msword", ".doc");
		m.put("application/vnd.ms-word", ".doc");
		m.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
		m.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", ".dotx");
		m.put("application/vnd.ms-word.document.macroEnabled.12", ".docm");
		m.put("application/vnd.ms-word.template.macroEnabled.12", ".dotm");

		// --- Excel ---
		m.put("application/vnd.ms-excel", ".xls");
		m.put("application/vnd.ms-excel.sheet.macroEnabled.12", ".xlsm");
		m.put("application/vnd.ms-excel.template.macroEnabled.12", ".xltm");
		m.put("application/vnd.ms-excel.addin.macroEnabled.12", ".xlam");
		m.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
		m.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", ".xltx");
		m.put("text/csv", ".csv");

		// --- PowerPoint ---
		m.put("application/vnd.ms-powerpoint", ".ppt");
		m.put("application/vnd.ms-powerpoint.presentation.macroEnabled.12", ".pptm");
		m.put("application/vnd.ms-powerpoint.slideshow.macroEnabled.12", ".ppsm");
		m.put("application/vnd.ms-powerpoint.template.macroEnabled.12", ".potm");
		m.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx");
		m.put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", ".ppsx");
		m.put("application/vnd.openxmlformats-officedocument.presentationml.template", ".potx");

		// --- Visio ---
		m.put("application/vnd.visio", ".vsd"); // legacy
		m.put("application/vnd.ms-visio.drawing", ".vsd"); // sometimes used
		m.put("application/vnd.ms-visio.stencil", ".vss");
		m.put("application/vnd.ms-visio.template", ".vst");
		m.put("application/vnd.ms-visio.viewer", ".vdx"); // legacy XML
		m.put("application/vnd.ms-visio.drawing.main+xml", ".vsdx");
		m.put("application/vnd.ms-visio.stencil.main+xml", ".vssx");
		m.put("application/vnd.ms-visio.template.main+xml", ".vstx");
		m.put("application/vnd.ms-visio.drawing.macroEnabled.main+xml", ".vsdm");
		m.put("application/vnd.ms-visio.stencil.macroEnabled.main+xml", ".vssm");
		m.put("application/vnd.ms-visio.template.macroEnabled.main+xml", ".vstm");

		// --- Project ---
		m.put("application/vnd.ms-project", ".mpp");
		m.put("application/x-project", ".mpp");

		// --- OneNote ---
		m.put("application/onenote", ".one"); // may also cover .onepkg
		// Some detectors use:
		m.put("application/x-onenote", ".one");

		// --- Publisher ---
		m.put("application/x-mspublisher", ".pub");
		m.put("application/vnd.ms-publisher", ".pub");

		// --- Access ---
		m.put("application/vnd.ms-access", ".mdb");
		m.put("application/x-msaccess", ".mdb");
		// ACCDB is often reported as:
		m.put("application/vnd.ms-access.accdb.12", ".accdb");
		m.put("application/msaccess", ".mdb");

		// --- Outlook ---
		m.put("application/vnd.ms-outlook", ".msg");
		m.put("application/x-msg", ".msg");
		m.put("message/rfc822", ".eml");

		// --- Others commonly saved from Office ---
		m.put("application/vnd.openxmlformats-officedocument.theme+xml", ".thmx");
		m.put("application/vnd.openxmlformats-officedocument.presentationml.slide", ".sldx");
		m.put("application/vnd.ms-powerpoint.slide.macroEnabled.12", ".sldm");

		// --- Core doc types (if Tika returns these) ---
		m.put("application/pdf", ".pdf");
		m.put("application/json", ".json");
		m.put("application/xml", ".xml");
		m.put("text/xml", ".xml");
		m.put("text/html", ".html");
		m.put("text/plain", ".txt");
		m.put("image/png", ".png");
		m.put("image/jpeg", ".jpg");
		m.put("image/gif", ".gif");
		m.put("image/tiff", ".tif");

		MIME_TO_EXT = Collections.unmodifiableMap(m);
	}

	/**
	 * If Tika returns application/zip for an OOXML file (docx/xlsx/pptx/etc),
	 * inspect [Content_Types].xml to infer the right extension.
	 */
	private static String detectOOXMLExtensionFromZip(byte[] data) {
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data))) {
			ZipEntry entry;
			boolean hasWord = false, hasXl = false, hasPpt = false;
			boolean isMacroEnabled = false;
			boolean isTemplate = false;
			boolean isSlideshow = false;

			// First pass: quick folder hints
			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();
				if (name.startsWith("word/")) {
					hasWord = true;
				} else if (name.startsWith("xl/")) {
					hasXl = true;
				} else if (name.startsWith("ppt/")) {
					hasPpt = true;
				}
				zis.closeEntry();
				if (hasWord || hasXl || hasPpt) {
					break;
				}
			}

			// Second pass: read [Content_Types].xml for precise details
			try (ZipInputStream zis2 = new ZipInputStream(new ByteArrayInputStream(data))) {
				while ((entry = zis2.getNextEntry()) != null) {
					if ("[Content_Types].xml".equals(entry.getName())) {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						byte[] buf = new byte[4096];
						int r;
						while ((r = zis2.read(buf)) != -1) {
							bos.write(buf, 0, r);
						}
						String ct = bos.toString(StandardCharsets.UTF_8);
						// Look for macro-enabled and template parts
						if (ct.contains("word/document.macroEnabled")) {
							isMacroEnabled = true;
						}
						if (ct.contains("spreadsheetml.sheet.macroEnabled")) {
							isMacroEnabled = true;
						}
						if (ct.contains("presentationml.presentation.macroEnabled")) {
							isMacroEnabled = true;
						}

						if (ct.contains("word/template")) {
							isTemplate = true;
						}
						if (ct.contains("spreadsheetml.template")) {
							isTemplate = true;
						}
						if (ct.contains("presentationml.template")) {
							isTemplate = true;
						}

						if (ct.contains("presentationml.slideshow")) {
							isSlideshow = true;
						}
						break;
					}
					zis2.closeEntry();
				}
			}

			if (hasWord) {
				if (isMacroEnabled) {
					return ".docm";
				}
				if (isTemplate) {
					return ".dotx"; // .dotm is template + macroEnabled; above would catch macro first
				}
				return ".docx";
			}
			if (hasXl) {
				if (isMacroEnabled) {
					return ".xlsm";
				}
				if (isTemplate) {
					return ".xltx"; // .xltm if macro-enabled; macro flag takes precedence
				}
				return ".xlsx";
			}
			if (hasPpt) {
				if (isMacroEnabled) {
					return ".pptm";
				}
				if (isSlideshow) {
					return isMacroEnabled ? ".ppsm" : ".ppsx";
				}
				if (isTemplate) {
					return isMacroEnabled ? ".potm" : ".potx";
				}
				return ".pptx";
			}
		} catch (Exception ignore) {
			// fall through
		}
		return "";
	}

	private static String detectExtension(byte[] data) {
		String ext = "";
		String mimeType = "application/octet-stream";

		try {
			// Prefer Tika detection from bytes
			org.apache.tika.Tika tika = new org.apache.tika.Tika();
			mimeType = tika.detect(data);
		} catch (Exception ignored) {
		}

		// 1) Strong mapping if known
		if (mimeType != null) {
			String mapped = MIME_TO_EXT.get(mimeType);
			if (mapped != null && !mapped.isEmpty() && !mapped.equals(".bin")) {
				return mapped;
			}
		}

		// 2) Special case: OOXML sometimes returns application/zip
		if ("application/zip".equalsIgnoreCase(mimeType) || mimeType.contains("ooxml")) {
			String ooxmlExt = detectOOXMLExtensionFromZip(data);
			if (!ooxmlExt.isEmpty()) {
				return ooxmlExt;
			}
		}

		// 3) Heuristic fallbacks (yours, plus a few common ones)
		if (mimeType != null) {
			String mt = mimeType.toLowerCase(Locale.ROOT);
			if (mt.contains("pdf")) {
				return ".pdf";
			}
			if (mt.contains("json")) {
				return ".json";
			}
			if (mt.contains("ooxml")) {
				return ".xlsx";
			}
			if (mt.contains("xml")) {
				return ".xml";
			}
			if (mt.contains("html")) {
				return ".html";
			}
			if (mt.contains("text")) {
				return ".txt";
			}
			if (mt.contains("image/png")) {
				return ".png";
			}
			if (mt.contains("image/jpeg")) {
				return ".jpg";
			}
			if (mt.contains("image/gif")) {
				return ".gif";
			}
			if (mt.contains("image/tiff")) {
				return ".tif";
			}
			if (mt.contains("msword")) {
				return ".doc";
			}
			if (mt.contains("vnd.openxmlformats-officedocument.wordprocessingml.document")) {
				return ".docx";
			}
			if (mt.contains("vnd.ms-excel")) {
				return ".xls";
			}
			if (mt.contains("spreadsheetml.sheet")) {
				return ".xlsx";
			}
			if (mt.contains("ms-powerpoint")) {
				return ".ppt";
			}
			if (mt.contains("presentationml.presentation")) {
				return ".pptx";
			}
			if (mt.contains("message/rfc822")) {
				return ".eml";
			}
			if (mt.contains("vnd.ms-outlook")) {
				return ".msg";
			}
		}

		// 4) Last resort: empty (will trigger Open With flow)
		return "";
	}

	public static void saveAndOpen(byte[] body) {
		try {
			String ext = detectExtension(body);

			// Generate filename: DL-yyyyMMddHHmmss
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			String filename = "DL-" + timestamp + ext;

			File tempDir = new File(System.getProperty("java.io.tmpdir"));
			File tempFile = new File(tempDir, filename);

			Files.write(tempFile.toPath(), body);
			System.out.println("Saved download to: " + tempFile.getAbsolutePath());

			// Open file
			if (java.awt.Desktop.isDesktopSupported()) {
				SwingUtilities.invokeLater(() -> {
					try {
						if (ext.isEmpty()) {
							String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
							if (os.contains("linux")) {
								new ProcessBuilder("xdg-open", tempFile.getAbsolutePath()).start();
							} else if (os.contains("win")) {
								new ProcessBuilder("rundll32", "shell32.dll,OpenAs_RunDLL", tempFile.getAbsolutePath())
										.start();
							} else if (os.contains("mac")) {
								new ProcessBuilder("open", tempFile.getAbsolutePath()).start();
							} else {
								java.awt.Desktop.getDesktop().open(tempFile);
							}
						} else {
							java.awt.Desktop.getDesktop().open(tempFile);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						// Replace Antigostman.this with your owner frame if needed
						JOptionPane.showMessageDialog(null, "Failed to open file: " + ex.getMessage());
					}
				});
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Failed to save/open download: " + ex.getMessage());
		}
	}

}
