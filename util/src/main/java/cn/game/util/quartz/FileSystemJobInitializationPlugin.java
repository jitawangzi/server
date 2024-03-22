package cn.game.util.quartz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;

import org.quartz.SchedulerException;
import org.quartz.plugins.xml.JobInitializationPlugin;

public class FileSystemJobInitializationPlugin extends JobInitializationPlugin
{

	class JobFile {

		private String fileName;

		// These are set by initialize()
		private String filePath;
		private String fileBasename;
		private boolean fileFound;

		protected JobFile(String fileName) throws SchedulerException {
			this.fileName = fileName;
			initialize();
		}

		protected String getFileName() {
			return fileName;
		}

		protected boolean getFileFound() {
			return fileFound;
		}

		protected String getFilePath() {
			return filePath;
		}

		protected String getFileBasename() {
			return fileBasename;
		}

		private void initialize() throws SchedulerException {
			InputStream f = null;
			try {
				String furl = null;

				File file = new File(getFileName()); // files in filesystem
				if (!file.exists()) {
					URL url = classLoadHelper.getResource(getFileName());
					if (url != null) {
						// we need jdk 1.3 compatibility, so we abandon this
						// code...
						// try {
						// furl = URLDecoder.decode(url.getPath(), "UTF-8");
						// } catch (UnsupportedEncodingException e) {
						// furl = url.getPath();
						// }
						furl = URLDecoder.decode(url.getPath());
						file = new File(furl);
						try {
							f = url.openStream();
						} catch (IOException ignor) {
							// Swallow the exception
						}
					} else {
						try {
							f = new FileInputStream(getFileName());
						} catch (FileNotFoundException e) {

							e.printStackTrace();
						}
					}
				} else {
					try {
						f = new java.io.FileInputStream(file);
					} catch (FileNotFoundException e) {
						// ignore
					}
				}

				if (f == null) {
					if (isFailOnFileNotFound()) {
						throw new SchedulerException("File named '" + getFileName() + "' does not exist.");
					} else {
						getLog().warn("File named '" + getFileName() + "' does not exist.");
					}
				} else {
					fileFound = true;
					filePath = (furl != null) ? furl : file.getAbsolutePath();
					fileBasename = file.getName();
				}
			} finally {
				try {
					if (f != null) {
						f.close();
					}
				} catch (IOException ioe) {
					getLog().warn("Error closing jobs file " + getFileName(), ioe);
				}
			}
		}
	}
}
